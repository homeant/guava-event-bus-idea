package io.github.homeant.guava.event.bus;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.find.findUsages.FindUsagesManager;
import com.intellij.find.findUsages.FindUsagesUtil;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.search.DelegatingGlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.FindClassUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.awt.RelativePoint;
import io.github.homeant.guava.event.bus.config.EventBusSettings;
import io.github.homeant.guava.event.bus.constant.Constants;
import io.github.homeant.guava.event.bus.utils.PsiUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


public class EventBugLineMarkerProvider extends RelatedItemLineMarkerProvider {

    private static final Logger LOG = Logger.getInstance(EventBugLineMarkerProvider.class);


    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        Project project = element.getProject();
        EventBusSettings settings = EventBusSettings.getInstance(project);
        EventBusSettings.Setting setting = settings.getState();
        // publisher
        if (PsiUtils.isPublisher(element, setting.getPublisherList())) {
            RelatedItemLineMarkerInfo lineMarkerInfo = createLineMarkerInfo(element, Constants.PUBLISHER_ICON, EventBusBundle.message("guava.event.bus.publisher.title"), (elt) -> getListenerList(elt, setting.getListenerList()));
            result.add((RelatedItemLineMarkerInfo<PsiElement>) lineMarkerInfo);
        }
        // listener
        if (PsiUtils.isListener(element, setting.getListenerList())) {
            PsiMethod method = (PsiMethod) element;
            result.add(createLineMarkerInfo(method.getIdentifyingElement(), Constants.LISTENER_ICON, EventBusBundle.message("guava.event.bus.listener.title"), (elt) -> getPublisherList(elt, setting.getPublisherList())));
        }
    }

    private RelatedItemLineMarkerInfo<PsiElement> createLineMarkerInfo(PsiElement element, Icon icon, String title, Function<PsiElement, List<GotoRelatedItem>> function) {
        List<GotoRelatedItem> gotoList = function.apply(element);
        return new RelatedItemLineMarkerInfo<>(element,
                element.getTextRange(),
                icon,
                e -> title,
                (e, elt) -> {
                    if (gotoList.size() == 1) {
                        gotoList.get(0).navigate();
                    } else if(gotoList.size()>1){
                        NavigationUtil.getRelatedItemsPopup(gotoList, "Go to Related Files").show(new RelativePoint(e));
                    }
                },
                GutterIconRenderer.Alignment.LEFT, () -> gotoList);
    }

    public List<GotoRelatedItem> getListenerList(PsiElement elt, List<String> listenerList) {
        List<GotoRelatedItem> gotoList = new ArrayList<>();
        Project project = elt.getProject();
        if (elt instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression expression = (PsiMethodCallExpression) elt;
            PsiMethod sourceMethod = PsiUtils.findMethod(expression, project);
            if(sourceMethod!=null){
                for (String pattern : listenerList) {
                    if(pattern.startsWith("@")){
                        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
                        PsiClass aClass = javaPsiFacade.findClass(pattern.substring(1), GlobalSearchScope.allScope(project));
                        LOG.debug("class:",aClass);
                    }else{
                        PsiMethod[] methods = PsiUtils.findMethods(pattern, project);
                        for (PsiMethod method : methods) {
                            if(PsiUtils.methodParamEquals(method,sourceMethod)){
                                gotoList.add(new GotoRelatedItem(method,"JAVA"));
                            }
                        }
                    }
                }
            }
        }
        return gotoList;
    }

    public List<GotoRelatedItem> getPublisherList(PsiElement elt, List<String> publisherList) {
        List<GotoRelatedItem> gotoList = new ArrayList<>();
        Project project = elt.getProject();
        if (elt instanceof PsiIdentifier) {
            elt = PsiTreeUtil.getParentOfType(elt, PsiMethod.class);
        }
        if (elt instanceof PsiMethod) {
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
            // com.google.common.eventbus.EventBus
            PsiMethod method = (PsiMethod) elt;
            PsiClass[] paramClass = PsiUtils.getMethodParamClass(method);
            if (paramClass.length > 0) {
                PsiClass eventBusClass = javaPsiFacade.findClass(paramClass[0].getQualifiedName(), GlobalSearchScope.allScope(project));
                if (eventBusClass != null) {
                    for (String pattern : publisherList) {
                        if (!pattern.contains("(")) {
                            PsiMethod[] methods = PsiUtils.findMethods(pattern, project);
                            if (methods.length > 0) {
                                for (PsiMethod psiMethod : methods) {
                                    if (PsiUtils.methodParamEquals(method, psiMethod)) {
                                        gotoList.add(new GotoRelatedItem(psiMethod, "JAVA"));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return gotoList;
    }

}
