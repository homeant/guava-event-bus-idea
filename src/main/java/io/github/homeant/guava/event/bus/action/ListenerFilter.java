package io.github.homeant.guava.event.bus.action;

import com.intellij.psi.*;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import io.github.homeant.guava.event.bus.utils.PsiUtils;

import java.util.List;


public class ListenerFilter implements Filter {

    private List<String> publishList;


    @Override
    public boolean shouldShow(Usage usage) {
        PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();
        if (element instanceof PsiJavaCodeReferenceElement) {
            element = element.getParent();
            if (element instanceof PsiTypeElement) {
                element = element.getParent();
                if (element instanceof PsiParameter) {
                    element = element.getParent();
                    if (element instanceof PsiParameterList) {
                        element = element.getParent();
                        if (element instanceof PsiMethod) {
                            PsiMethod method = (PsiMethod) element;
                            return PsiUtils.isEventBusHandlerMethod(method);
                        }
                    }
                }
            }
        }
        return false;
    }
}
