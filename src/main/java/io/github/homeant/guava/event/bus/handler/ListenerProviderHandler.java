package io.github.homeant.guava.event.bus.handler;

import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import io.github.homeant.guava.event.bus.config.EventBusSettings;
import io.github.homeant.guava.event.bus.utils.PsiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListenerProviderHandler implements GoItemProviderHandler {

    private static final Logger LOG = Logger.getInstance(ListenerProviderHandler.class);

    private final EventBusSettings.Setting setting;

    private final PsiElement psiElement;


    public ListenerProviderHandler(EventBusSettings.Setting setting, PsiElement psiElement) {
        this.setting = setting;
        this.psiElement = psiElement;
    }

    @Override
    public String getTitle() {
        return "Listener list";
    }

    @Override
    public PsiElement getPsiElement() {
        if (psiElement instanceof PsiMethodCallExpression) {
            return PsiUtils.findMethodParamClass(psiElement)[0];
        }
        return null;
    }

    @Override
    public PsiElement[] getPrimaryElements() {
        if (psiElement instanceof PsiMethodCallExpression) {
            return PsiUtils.findMethodParamClass(psiElement);
        }
        return PsiElement.EMPTY_ARRAY;
    }

    @Override
    public List<PsiElement> findElementList(PsiElement elt) {
        List<PsiElement> eltList = new ArrayList<>();
        Project project = elt.getProject();
        if (elt instanceof PsiMethodCallExpression) {
            for (String listener : setting.getListenerList()) {
                if (listener.startsWith("@")) {
                    PsiClass annotationClass = PsiUtils.findClass(listener.substring(1), project);
                    if (annotationClass != null) {
                        eltList.add(annotationClass);
                    }
                } else {
                    PsiMethod[] methods = PsiUtils.findMethods(listener, project);
                    for (PsiMethod method : methods) {
                        if (!eltList.contains(method)) {
                            eltList.add(method);
                        }
                    }
                }
            }
        }
        return eltList;
    }

    @Override
    public boolean filter(Usage usage) {
        PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();
        Project project = element.getProject();
        if (element instanceof PsiJavaCodeReferenceElement) {
            PsiMethod psiMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
            if (psiMethod != null) {
                PsiClass[] sourceMethodParamClass = PsiUtils.findMethodParamClass(psiMethod);
                for (String listener : setting.getListenerList()) {
                    if (listener.startsWith("@")) {
                        PsiAnnotation annotation = psiMethod.getAnnotation(listener.substring(1));
                        if (annotation != null) {
                            return true;
                        }
                    } else {
                        PsiMethod[] methods = PsiUtils.findMethods(listener, project);
                        for (PsiMethod method : methods) {
                            if (PsiUtils.safeEquals(method.getName(), psiMethod.getName())) {
                                PsiClass[] targetMethodParamClass = PsiUtils.findMethodParamClass(method);
                                if (PsiUtils.classEquals(sourceMethodParamClass, targetMethodParamClass)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public List<GotoRelatedItem> conversion(List<Usage> usageList) {
        return usageList.stream().map(usage -> {
            PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();
            PsiMethod psiMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
            return new GotoRelatedItem(psiMethod);
        }).collect(Collectors.toList());
    }
}
