package io.github.homeant.guava.event.bus.utils;

import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.search.GlobalSearchScope;
import io.github.homeant.guava.event.bus.constant.Constants;

import java.util.List;

public class PsiUtils {
    private PsiUtils() {

    }

    public static boolean isJava(PsiElement element) {
        return element.getLanguage().is(Language.findLanguageByID("JAVA"));
    }

    public static boolean isEventBusPostMethod(PsiElement element) {
        if (isJava(element) && element instanceof PsiMethodCallExpressionImpl && element.getFirstChild() != null && element.getFirstChild() instanceof PsiReferenceExpressionImpl) {
            PsiReferenceExpressionImpl all = (PsiReferenceExpressionImpl) element.getFirstChild();
            if (all.getFirstChild() instanceof PsiReferenceExpression) {
                PsiReferenceExpression start = (PsiReferenceExpression) all.getFirstChild();
                PsiIdentifierImpl post = (PsiIdentifierImpl) all.getLastChild();
                boolean isEventBusClass = start.getType() != null && safeEquals(Constants.EVENT_CLASS_ABS_NAME, start.getType().getCanonicalText());
                boolean isAsyncEventBusClass = start.getType() != null && safeEquals(Constants.EVENT_ASYNC_CLASS_ABS_NAME, start.getType().getCanonicalText());
                if (safeEquals(post.getText(), Constants.PUBLISHER_FUNC_NAME) && (isEventBusClass || isAsyncEventBusClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isPublisher(PsiElement element, List<String> publishList) {
        if (isJava(element) && element instanceof PsiMethodCallExpressionImpl && element.getFirstChild() != null && element.getFirstChild() instanceof PsiReferenceExpressionImpl) {
            PsiReferenceExpressionImpl all = (PsiReferenceExpressionImpl) element.getFirstChild();
            if (all.getFirstChild() instanceof PsiReferenceExpression) {
                PsiReferenceExpression start = (PsiReferenceExpression) all.getFirstChild();
                PsiIdentifierImpl post = (PsiIdentifierImpl) all.getLastChild();
                for (String pattern : publishList) {
                    int index = pattern.lastIndexOf(".");
                    String method = pattern.substring(index+1);
                    String className = pattern.substring(0, index);
                    if (safeEquals(post.getText(), method) && start.getType() != null && safeEquals(className, start.getType().getCanonicalText())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isListener(PsiElement element,List<String> listenList) {
        if (isJava(element) && element instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) element;
            PsiModifierList modifierList = method.getModifierList();
            for (String pattern : listenList) {
                if(pattern.startsWith("@")){
                    for (PsiAnnotation psiAnnotation : modifierList.getAnnotations()) {
                        if (safeEquals(psiAnnotation.getQualifiedName(),pattern.substring(1))) {
                            return true;
                        }
                    }
                }else{
                    int index = pattern.lastIndexOf(".");
                    String className = pattern.substring(0,index);
                    String methodName = pattern.substring(index+1);
                    Project project = element.getProject();
                    JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
                    PsiClass patternPsiClass = javaPsiFacade.findClass(className, GlobalSearchScope.allScope(project));
                    if(patternPsiClass.isInterface()){
                        PsiReferenceList implementsList = patternPsiClass.getImplementsList();

                    }else{
                        PsiClass psiClass = method.getContainingClass();
                        if(safeEquals(className,psiClass.getQualifiedName()) && safeEquals(methodName,method.getName())){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean isEventBusHandlerMethod(PsiElement element) {
        if (isJava(element) && element instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) element;
            PsiModifierList modifierList = method.getModifierList();
            for (PsiAnnotation psiAnnotation : modifierList.getAnnotations()) {
                if (safeEquals(psiAnnotation.getQualifiedName(), Constants.LISTENER_ANNOTATION_NAME)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean safeEquals(String obj, String value) {
        return obj != null && obj.equals(value);
    }

    public static PsiClass getClass(PsiType psiType) {
        if (psiType instanceof PsiClassType) {
            return ((PsiClassType) psiType).resolve();
        }
        return null;
    }

}
