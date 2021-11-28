package io.github.homeant.guava.event.bus.handler;

import com.intellij.find.FindManager;
import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesHandlerBase;
import com.intellij.find.findUsages.FindUsagesManager;
import com.intellij.find.findUsages.JavaFindUsagesHandler;
import com.intellij.find.impl.FindManagerImpl;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import io.github.homeant.guava.event.bus.config.EventBusSettings;
import io.github.homeant.guava.event.bus.utils.PsiUtils;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class PublisherProviderHandler implements GoItemProviderHandler {

    private static final Logger LOG = Logger.getInstance(PublisherProviderHandler.class);

    private final EventBusSettings.Setting setting;

    private final PsiElement psiElement;

    public PublisherProviderHandler(EventBusSettings.Setting setting, PsiElement psiElement) {
        this.setting = setting;
        this.psiElement = psiElement;
    }


    @Override
    public String getTitle() {
        return "Publisher list";
    }

    @Override
    public PsiElement getPsiElement() {
        return psiElement;
    }

    @Override
    public PsiElement[] getPrimaryElements() {
        List<PsiElement> list = new ArrayList<>();
        if (psiElement instanceof PsiMethod) {
            Project project = psiElement.getProject();
            for (String publish : setting.getPublisherList()) {
                for (PsiMethod psiMethod : PsiUtils.findMethods(publish, project)) {
                    list.add(psiMethod);
                }
            }
        }
        return list.toArray(new PsiElement[]{});
    }

    @Override
    public List<PsiElement> findElementList(PsiElement elt) {
        List<PsiElement> list = new ArrayList<>();
        if (elt instanceof PsiIdentifier) {
            elt = PsiTreeUtil.getParentOfType(elt, PsiMethod.class);
        }
        if (elt instanceof PsiMethod) {
            Project project = elt.getProject();
            for (String publish : setting.getPublisherList()) {
                for (PsiMethod psiMethod : PsiUtils.findMethods(publish, project)) {
                    list.add(psiMethod);
                }
            }
        }
        return list;
    }

    @Override
    public List<GotoRelatedItem> conversion(List<Usage> usageList) {
        List<GotoRelatedItem> gotoList = new ArrayList<>();
        if (psiElement instanceof PsiMethod) {
            for (Usage usage : usageList) {
                PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();
                if (element instanceof PsiJavaCodeReferenceElement) {
                    PsiMethodCallExpression methodCallExpression = PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class);
                    if (methodCallExpression != null) {
                        PsiClass[] targetParamClass = PsiUtils.getMethodParamClass(methodCallExpression);
                        PsiClass[] sourceParamClass = PsiUtils.getMethodParamClass(psiElement);
                        if (PsiUtils.classEquals(targetParamClass, sourceParamClass)) {
                            gotoList.add(new GotoRelatedItem(element));
                        }
                    }
                }
            }
        }
        return gotoList;
    }


}
