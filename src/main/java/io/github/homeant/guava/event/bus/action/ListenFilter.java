package io.github.homeant.guava.event.bus.action;

import com.intellij.psi.*;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import io.github.homeant.guava.event.bus.utils.PsiUtils;
import lombok.extern.java.Log;

import java.util.Objects;

@Log
public class ListenFilter implements Filter{

    private final PsiClass eventClass;

    public ListenFilter(PsiClass eventClass) {
        this.eventClass = eventClass;
    }

    @Override
    public boolean shouldShow(Usage usage) {
        PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();
        if (element instanceof PsiReferenceExpression) {
            element = element.getParent();
            if (element instanceof PsiMethodCallExpression) {
                PsiMethodCallExpression callExpression = (PsiMethodCallExpression) element;
                PsiType[] types = callExpression.getArgumentList().getExpressionTypes();
                for (PsiType type : types) {
                    //  eventBug.post(new Event());
                    if (Objects.equals(PsiUtils.getClass(type).getQualifiedName(), eventClass.getQualifiedName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
