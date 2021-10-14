package io.github.homeant.guava.event.bus.utils;

import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.search.GlobalSearchScope;
import io.github.homeant.guava.event.bus.constant.Constants;

import java.util.*;

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

    public static boolean hasAnnotation(PsiElement element, String annotationName) {
        return findAnnotation(element, annotationName) != null;
    }

    static PsiAnnotation findAnnotation(PsiElement element, String annotationName) {
        if (element instanceof PsiModifierListOwner) {
            PsiModifierListOwner listOwner = (PsiModifierListOwner) element;
            PsiModifierList modifierList = listOwner.getModifierList();

            if (modifierList != null) {
                for (PsiAnnotation psiAnnotation : modifierList.getAnnotations()) {
                    if (annotationName.equals(psiAnnotation.getQualifiedName())) {
                        return psiAnnotation;
                    }
                }
            }
        }
        return null;
    }


    public static PsiMethod findMethod(PsiElement element) {
        if (element == null) {
            return null;
        } else if (element instanceof PsiMethod) {
            return (PsiMethod) element;
        } else {
            return findMethod(element.getParent());
        }
    }

    public static PsiClass getClass(PsiElement psiElement) {
        if (psiElement instanceof PsiVariable) {
            PsiVariable variable = (PsiVariable) psiElement;
            return getClass(variable.getType());
        } else if (psiElement instanceof PsiMethod) {
            return ((PsiMethod) psiElement).getContainingClass();
        }

        return null;
    }

    public static PsiClass getClass(PsiType psiType) {
        if (psiType instanceof PsiClassType) {
            return ((PsiClassType) psiType).resolve();
        }
        return null;
    }

    public static PsiField findField(PsiElement element) {
        if (element == null) {
            return null;
        } else if (element instanceof PsiField) {
            return (PsiField) element;
        } else {
            return findField(element.getParent());
        }
    }

    private static PsiClassType getPsiClassType(PsiElement psiElement) {
        if (psiElement instanceof PsiVariable) {
            return (PsiClassType) ((PsiVariable) psiElement).getType();
        } else if (psiElement instanceof PsiMethod) {
            return (PsiClassType) ((PsiMethod) psiElement).getReturnType();
        }
        return null;
    }
    private static PsiClassType extractFirstTypeParameter(PsiClassType psiClassType) {
        return (PsiClassType) psiClassType.resolveGenerics().getSubstitutor()
                .getSubstitutionMap().values().iterator().next();
    }
}
