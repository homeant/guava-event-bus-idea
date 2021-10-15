package io.github.homeant.guava.event.bus.utils;

import com.intellij.lang.Language;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import io.github.homeant.guava.event.bus.constant.Constants;

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
