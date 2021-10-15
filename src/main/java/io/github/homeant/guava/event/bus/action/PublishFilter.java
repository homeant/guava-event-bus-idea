package io.github.homeant.guava.event.bus.action;

import com.intellij.psi.*;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import io.github.homeant.guava.event.bus.utils.PsiUtils;


public class ListenFilterJava implements Filter {
    @Override
    public boolean shouldShow(Usage usage) {
        PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();
        if (element instanceof PsiJavaCodeReferenceElement) {
            if ((element = element.getParent()) instanceof PsiTypeElement) {
                if ((element = element.getParent()) instanceof PsiParameter) {
                    if ((element = element.getParent()) instanceof PsiParameterList) {
                        if ((element = element.getParent()) instanceof PsiMethod) {
                            PsiMethod method = (PsiMethod) element;
                            if (PsiUtils.isEventBusHandlerMethod(method)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
