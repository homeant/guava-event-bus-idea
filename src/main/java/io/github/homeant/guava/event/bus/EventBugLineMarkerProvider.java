package io.github.homeant.guava.event.bus;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import io.github.homeant.guava.event.bus.config.EventBusSettings;
import io.github.homeant.guava.event.bus.constant.Constants;
import io.github.homeant.guava.event.bus.handler.EventBusNavigationHandler;
import io.github.homeant.guava.event.bus.handler.GoItemProviderHandler;
import io.github.homeant.guava.event.bus.handler.ListenerProviderHandler;
import io.github.homeant.guava.event.bus.handler.PublisherProviderHandler;
import io.github.homeant.guava.event.bus.utils.PsiUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;


public class EventBugLineMarkerProvider implements LineMarkerProvider {


    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        Project project = element.getProject();
        EventBusSettings settings = EventBusSettings.getInstance(project);
        EventBusSettings.Setting setting = settings.getState();
        if (setting != null) {
            // publisher
            if (PsiUtils.isPublisher(element, setting.getPublisherList())) {
                return createLineMarkerInfo(element, Constants.PUBLISHER_ICON, EventBusBundle.message("guava.event.bus.publisher.title"), new ListenerProviderHandler(setting, element));
            }
            // listener
            if (PsiUtils.isListener(element, setting.getListenerList())) {
                PsiMethod method = (PsiMethod) element;
                return createLineMarkerInfo(method.getIdentifyingElement(), Constants.LISTENER_ICON, EventBusBundle.message("guava.event.bus.listener.title"), new PublisherProviderHandler(setting, element));
            }
        }
        return null;
    }


    private LineMarkerInfo<PsiElement> createLineMarkerInfo(PsiElement element, Icon icon, String title, GoItemProviderHandler goItemProviderHandler) {
        return new LineMarkerInfo<>(element,
                element.getTextRange(),
                icon,
                e -> title,
                new EventBusNavigationHandler(goItemProviderHandler),
                GutterIconRenderer.Alignment.LEFT);
    }
}
