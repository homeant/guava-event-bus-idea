package io.github.homeant.guava.event.bus.utils;

import com.intellij.lang.Language;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import io.github.homeant.guava.event.bus.constant.Constants;

public class PsiUtils {
    private PsiUtils(){

    }

    public static boolean isJava(PsiElement element){
        return element.getLanguage().is(Language.findLanguageByID("JAVA"));
    }


    public static boolean isEventBusPostMethod(PsiElement element){
        if(isJava(element)){
            if (element instanceof PsiMethodCallExpressionImpl && element.getFirstChild() != null && element.getFirstChild() instanceof PsiReferenceExpressionImpl) {
                PsiReferenceExpressionImpl all = (PsiReferenceExpressionImpl) element.getFirstChild();
                if (all.getFirstChild() instanceof PsiReferenceExpression) {
                    PsiReferenceExpression start = (PsiReferenceExpression) all.getFirstChild();
                    PsiIdentifierImpl post = (PsiIdentifierImpl) all.getLastChild();
                    if (safeEquals(post.getText(), Constants.PUBLISHER_FUNC_NAME) && start.getType()!=null && safeEquals(Constants.EVENT_CLASS_ABS_NAME, start.getType().getCanonicalText())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isEventBusHandlerMethod(PsiElement element) {
        if(isJava(element)){
            if(element instanceof PsiMethod){
                PsiMethod method = (PsiMethod) element;
                PsiModifierList modifierList = method.getModifierList();
                for (PsiAnnotation psiAnnotation : modifierList.getAnnotations()) {
                    if (safeEquals(psiAnnotation.getQualifiedName(), Constants.LISTENER_ANNOTATION_NAME)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isEventBusClass(PsiClass psiClass) {
        return safeEquals(psiClass.getName(), Constants.EVENT_CLASS_NAME);
    }

    private static boolean isSuperClassEventBus(PsiClass psiClass) {
        PsiClass[] supers = psiClass.getSupers();
        if (supers.length == 0) {
            return false;
        }
        for (PsiClass superClass : supers) {
            if (safeEquals(superClass.getName(), Constants.EVENT_CLASS_NAME)) {
                return true;
            }
        }
        return false;
    }

    private static boolean safeEquals(String obj, String value) {
        return obj != null && obj.equals(value);
    }

}
