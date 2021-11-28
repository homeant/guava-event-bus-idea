package io.github.homeant.guava.event.bus.utils;

import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.impl.source.tree.java.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.homeant.guava.event.bus.constant.Constants;
import lombok.extern.java.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Log
public class PsiUtils {
    private PsiUtils() {

    }

    public static boolean methodParamEquals(PsiMethod source, PsiMethod target) {
        PsiParameterList sourceParameterList = source.getParameterList();
        PsiParameterList targetParameterList = target.getParameterList();
        if (sourceParameterList.getParametersCount() == targetParameterList.getParametersCount()) {
            for (int i = 0; i < sourceParameterList.getParametersCount(); i++) {
                PsiClassType sourceType = (PsiClassType) sourceParameterList.getParameter(i).getType();
                PsiClassType targetType = (PsiClassType) targetParameterList.getParameter(i).getType();
                String sourceName = sourceType.resolve().getQualifiedName();
                String targetName = targetType.resolve().getQualifiedName();
//                if(!"java.lang.Object".equals(targetName) && !"java.lang.Object".equals(sourceName)){
//                    if (!safeEquals(sourceName,targetName)) {
//                        return false;
//                    }
//                }
                if (!safeEquals(sourceName, targetName)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static PsiClass findClass(String name,Project project) {
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        return javaPsiFacade.findClass(name, GlobalSearchScope.allScope(project));
    }

    public static PsiMethod findMethod(PsiMethodCallExpression expression, Project project) {
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        PsiElement firstChild = expression.getFirstChild().getFirstChild();
        PsiType type = null;
        if (firstChild instanceof PsiNewExpression) {
            type = ((PsiNewExpression) firstChild).getType();
        } else if (firstChild instanceof PsiReferenceExpression) {
            type = ((PsiReferenceExpressionImpl) firstChild).getType();
        }
        if (type != null) {
            PsiClass eventBusClass = javaPsiFacade.findClass(type.getCanonicalText(), GlobalSearchScope.allScope(project));
            String methodName = expression.getFirstChild().getLastChild().getText();
            PsiMethod[] methods = eventBusClass.findMethodsByName(methodName, false);
            for (PsiMethod method : methods) {
                PsiParameterList sourceParameterList = method.getParameterList();
                PsiType[] targetParamTypeList = expression.getArgumentList().getExpressionTypes();
                if (sourceParameterList.getParametersCount() == targetParamTypeList.length) {
                    for (int i = 0; i < sourceParameterList.getParametersCount(); i++) {
                        PsiClassType sourceType = (PsiClassType) sourceParameterList.getParameter(i).getType();
                        if (!safeEquals(sourceType.resolve().getQualifiedName(), targetParamTypeList[i].getCanonicalText())) {
                            return method;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static PsiMethod[] findMethods(String qualifiedName, Project project) {
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        int index = qualifiedName.lastIndexOf(".");
        String className = qualifiedName.substring(0, index);
        String methodName = qualifiedName.substring(index + 1);
        PsiClass clazz = javaPsiFacade.findClass(className, GlobalSearchScope.allScope(project));
        if (clazz != null) {
            return clazz.findMethodsByName(methodName, false);
        }
        return new PsiMethod[]{};
    }

    public static PsiType[] getMethodParamType(PsiMethodCallExpression elt) {
        PsiExpressionList expressionList = (PsiExpressionList) elt.getLastChild();
        return expressionList.getExpressionTypes();
    }

    public static PsiClass[] getMethodParamClass(PsiElement element) {
        if (element instanceof PsiMethod) {
            PsiParameterList parameterList = ((PsiMethod) element).getParameterList();
            PsiClass[] psiClazz = new PsiClass[parameterList.getParametersCount()];
            if (!parameterList.isEmpty()) {
                for (int i = 0; i < parameterList.getParameters().length; i++) {
                    psiClazz[i] = ((PsiClassType) parameterList.getParameter(i).getType()).resolve();
                }
            }
            return psiClazz;
        } else if (element instanceof PsiMethodCallExpression) {
            PsiType[] typeArguments = ((PsiMethodCallExpression) element).getArgumentList().getExpressionTypes();
            PsiClass[] psiClazz = new PsiClass[typeArguments.length];
            for (int i = 0; i < typeArguments.length; i++) {
                psiClazz[i] = ((PsiClassType) typeArguments[i]).resolve();
            }
            return psiClazz;
        }
        throw new IllegalArgumentException(element.getText() + " not find Parameter");
    }

    public static boolean classEquals(PsiClass[] sources, PsiClass[] targets) {
        if (sources.length != targets.length) {
            return false;
        }
        for (int i = 0; i < sources.length; i++) {
            if (!Objects.equals(sources[i], targets[i])) {
                return false;
            }
        }
        return true;
    }


    public static boolean isJava(PsiElement element) {
        return element.getLanguage().is(Language.findLanguageByID("JAVA"));
    }


    public static boolean isPublisher(PsiElement element, List<String> publishList) {
        if (!isJava(element)) {
            return false;
        }
        if (element instanceof PsiMethodCallExpressionImpl && element.getFirstChild() != null && element.getFirstChild() instanceof PsiReferenceExpressionImpl) {
            PsiReferenceExpressionImpl all = (PsiReferenceExpressionImpl) element.getFirstChild();
            PsiElement firstChild = all.getFirstChild();
            if (firstChild instanceof PsiExpression) {
                PsiType type = ((PsiExpression) firstChild).getType();
                PsiIdentifierImpl methodIdentifier = (PsiIdentifierImpl) all.getLastChild();
                for (String pattern : publishList) {
                    int index = pattern.lastIndexOf(".");
                    String methodName = pattern.substring(index + 1);
                    String className = pattern.substring(0, index);
                    if (type != null) {
                        if (safeEquals(methodIdentifier.getText(), methodName) && safeEquals(className, type.getCanonicalText())) {
                            return true;
                        }
                        PsiType[] superTypes = type.getSuperTypes();
                        for (PsiType superType : superTypes) {
                            if (superType instanceof PsiClassReferenceType) {
                                PsiClass supperClass = ((PsiClassReferenceType) superType).resolve();
                                if (supperClass != null && safeEquals(className, supperClass.getQualifiedName())) {
                                    return true;
                                }
                            }
                        }
                    }
                    if (firstChild instanceof PsiReferenceExpression && safeEquals(methodIdentifier.getText(), methodName) && safeEquals(className, ((PsiReferenceExpressionImpl) firstChild).getCanonicalText())) {
                        return true;
                    }
                }

            }
        }
        return false;
    }

    public static boolean isListener(PsiElement element, List<String> listenList) {
        if (!isJava(element)) {
            return false;
        }
        if (element instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) element;
            PsiModifierList modifierList = method.getModifierList();
            for (String pattern : listenList) {
                if (pattern.startsWith("@")) {
                    String annotationName = pattern.substring(1);
                    for (PsiAnnotation psiAnnotation : modifierList.getAnnotations()) {
                        if (safeEquals(psiAnnotation.getQualifiedName(), annotationName)) {
                            return true;
                        }
                    }
                } else {
                    int index = pattern.lastIndexOf(".");
                    String className = pattern.substring(0, index);
                    String methodName = pattern.substring(index + 1);
                    Project project = element.getProject();
                    JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
                    PsiClass patternPsiClass = javaPsiFacade.findClass(className, GlobalSearchScope.allScope(project));
                    PsiClass psiClass = method.getContainingClass();
                    if (psiClass != null && patternPsiClass != null) {
                        if (patternPsiClass.isInterface()) {
                            PsiClass[] supers = psiClass.getSupers();
                            for (PsiClass supplierClass : supers) {
                                if (safeEquals(className, supplierClass.getQualifiedName()) && safeEquals(methodName, method.getName())) {
                                    return true;
                                }
                            }
                        } else {
                            if (safeEquals(className, psiClass.getQualifiedName()) && safeEquals(methodName, method.getName())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean safeEquals(String obj, String value) {
        return obj != null && obj.equals(value);
    }
}
