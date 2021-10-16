package io.github.homeant.guava.event.bus;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiEditorUtil;
import com.intellij.ui.awt.RelativePoint;
import io.github.homeant.guava.event.bus.action.ListenFilter;
import io.github.homeant.guava.event.bus.action.PublishFilter;
import io.github.homeant.guava.event.bus.action.ShowUsagesAction;
import io.github.homeant.guava.event.bus.constant.Constants;
import io.github.homeant.guava.event.bus.utils.PsiUtils;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseEvent;


@Log
public class EventBugLineMarkerProvider implements LineMarkerProvider {

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        // post
        if (PsiUtils.isEventBusPostMethod(element)) {
            // 201
            //return new LineMarkerInfo<>(element,element.getTextRange(),Constants.PUBLISHER_ICON,el -> Constants.PUBLISHER,this::publishHandle, GutterIconRenderer.Alignment.LEFT);
            return new LineMarkerInfo<>(element,element.getTextRange(),Constants.PUBLISHER_ICON,el -> Constants.PUBLISHER,this::publishHandle, GutterIconRenderer.Alignment.LEFT,()->Constants.PUBLISHER);
//            return gutterIconBuilder(element,Constants.PUBLISHER_ICON)
//                    .setPopupTitle(Constants.PUBLISHER)
//                    .setTooltipText(Constants.PUBLISHER)
//                    .createLineMarkerInfo(element,this::publishHandle);
        }
        // @Subscribe
        if (PsiUtils.isEventBusHandlerMethod(element)) {
            // 201
            // return new LineMarkerInfo<>(element,element.getTextRange(),Constants.LISTENER_ICON,el -> Constants.LISTENER,this::listenHandle, GutterIconRenderer.Alignment.LEFT);
            return new LineMarkerInfo<>(element,element.getTextRange(),Constants.LISTENER_ICON,el -> Constants.LISTENER,this::listenHandle, GutterIconRenderer.Alignment.LEFT,()->Constants.PUBLISHER);
//            return gutterIconBuilder(element,Constants.LISTENER_ICON)
//                    .setPopupTitle(Constants.LISTENER)
//                    .setTooltipText(Constants.LISTENER)
//                    .createLineMarkerInfo(element, this::listenHandle);
        }
        return null;
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
                        ShowUsagesAction action = new ShowUsagesAction(new PublishFilter());
                        action.startFindUsages(eventClass, new RelativePoint(event), PsiEditorUtil.findEditor(elt), 100);
                    }
                }
            } catch (Exception ee) {
                ee.fillInStackTrace();
            }

        }
    }

    /**
     * 监听者处理
     * @param event event
     * @param elt elt
     */
    private void listenHandle(MouseEvent event, PsiElement elt){
        if (elt instanceof PsiMethod) {
            Project project = elt.getProject();
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
            // com.google.common.eventbus.EventBus
            PsiClass eventBusClass = javaPsiFacade.findClass(Constants.EVENT_CLASS_ABS_NAME, GlobalSearchScope.allScope(project));
            if (eventBusClass == null) return;

            PsiMethod method = (PsiMethod) elt;
            // 查找 eventBus.post
            PsiMethod postMethod = eventBusClass.findMethodsByName(Constants.PUBLISHER_FUNC_NAME, false)[0];
            if (null != postMethod) {
                PsiParameter parameter = method.getParameterList().getParameter(0);
                if (parameter != null) {
                    // 参数
                    PsiClass eventClass = ((PsiClassType) parameter.getType()).resolve();
                    if (eventClass != null) {
                        ShowUsagesAction action = new ShowUsagesAction(new ListenFilter(eventClass));
                        action.startFindUsages(postMethod, new RelativePoint(event), PsiEditorUtil.findEditor(elt), 100);
                    }
                }
            }
        }
    }
}
