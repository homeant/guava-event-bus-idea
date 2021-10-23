package io.github.homeant.guava.event.bus;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiEditorUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.awt.RelativePoint;
import io.github.homeant.guava.event.bus.action.ListenerFilter;
import io.github.homeant.guava.event.bus.action.ShowUsagesAction;
import io.github.homeant.guava.event.bus.config.EventBusSettings;
import io.github.homeant.guava.event.bus.constant.Constants;
import io.github.homeant.guava.event.bus.utils.PsiUtils;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;


@Log
public class EventBugLineMarkerProvider implements LineMarkerProvider {


    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements, @NotNull Collection<? super LineMarkerInfo<?>> result) {
        for (PsiElement element : elements) {
            Project project = element.getProject();
            EventBusSettings settings = EventBusSettings.getInstance(project);
            EventBusSettings.Setting setting = settings.getState();
            if (PsiUtils.isPublisher(element,setting.getPublisherList())) {
                result.add(new LineMarkerInfo<>(element,element.getTextRange(),Constants.PUBLISHER_ICON,null,this::publishHandle, GutterIconRenderer.Alignment.LEFT,()->Constants.PUBLISHER));
            }
            // @Subscribe
            if (PsiUtils.isListener(element,setting.getListenerList())) {
                PsiMethod method = (PsiMethod)element;
                result.add(createIconLineMarker(method.getIdentifyingElement(),Constants.LISTENER_ICON,this::listenHandle));
            }
        }
    }

    private LineMarkerInfo<PsiElement> createIconLineMarker(PsiElement element, Icon icon, GutterIconNavigationHandler<PsiElement> navHandler){
        return new LineMarkerInfo<>(element,
                element.getTextRange(),
                icon,
                null,
                navHandler,
                GutterIconRenderer.Alignment.LEFT,
                ()->"");
    }

    private NavigationGutterIconBuilder<PsiElement> gutterIconBuilder(PsiElement element, Icon icon){
        return NavigationGutterIconBuilder.create(icon)
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .setTarget(element);
    }

    /**
     * 发布者处理
     * @param event event
     * @param elt elt
     */
    private void publishHandle(MouseEvent event, PsiElement elt){
        if (elt instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression expression = (PsiMethodCallExpression) elt;
            try {
                PsiType[] expressionTypes = expression.getArgumentList().getExpressionTypes();
                if (expressionTypes.length > 0) {
                    PsiClass eventClass = PsiUtils.getClass(expressionTypes[0]);
                    if (eventClass != null) {
                        ShowUsagesAction action = new ShowUsagesAction(new ListenerFilter());
                        action.startFindUsages(eventClass, new RelativePoint(event), PsiEditorUtil.findEditor(elt), 100);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 监听者处理
     * @param event event
     * @param element element
     */
    private void listenHandle(MouseEvent event, PsiElement element){
        Project project = element.getProject();
        if(element instanceof PsiIdentifier){
            element = PsiTreeUtil.getParentOfType(element,PsiMethod.class);
        }
        if (element instanceof PsiMethod) {
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
            // com.google.common.eventbus.EventBus
            PsiClass eventBusClass = javaPsiFacade.findClass(Constants.EVENT_CLASS_ABS_NAME, GlobalSearchScope.allScope(project));
            if (eventBusClass == null) return;

            PsiMethod method = (PsiMethod) element;
            // 查找 eventBus.post
            PsiMethod postMethod = eventBusClass.findMethodsByName(Constants.PUBLISHER_FUNC_NAME, false)[0];
            if (null != postMethod) {
                PsiParameter parameter = method.getParameterList().getParameter(0);
                if (parameter != null) {
                    // 参数
                    PsiClass eventClass = ((PsiClassType) parameter.getType()).resolve();
                    if (eventClass != null) {
                        com.intellij.find.actions.ShowUsagesAction.startFindUsages(postMethod,new RelativePoint(event),PsiEditorUtil.findEditor(element));
//                        ShowUsagesAction action = new ShowUsagesAction(new PublisherFilter(eventClass));
//                        action.startFindUsages(postMethod, new RelativePoint(event), PsiEditorUtil.findEditor(element), 100);
                    }
                }
            }
        }
    }
}
