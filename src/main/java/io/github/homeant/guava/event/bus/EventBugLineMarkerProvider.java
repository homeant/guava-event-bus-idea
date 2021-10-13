package io.github.homeant.guava.event.bus;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiEditorUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.ui.awt.RelativePoint;
import io.github.homeant.guava.event.bus.constant.Constants;
import io.github.homeant.guava.event.bus.utils.PsiUtils;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;


@Log
public class EventBugLineMarkerProvider implements LineMarkerProvider {

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (PsiUtils.isEventBusPostMethod(element)) {
            return NavigationGutterIconBuilder.create(Constants.PUBLISHER_ICON)
                    .setAlignment(GutterIconRenderer.Alignment.LEFT)
                    .setTarget(element)
                    .setPopupTitle("publisher")
                    .setTooltipText("publisher")
                    .createLineMarkerInfo(element);
        }
        if (PsiUtils.isEventBusHandlerMethod(element)) {
            return NavigationGutterIconBuilder.create(Constants.LISTENER_ICON)
                    .setAlignment(GutterIconRenderer.Alignment.LEFT)
                    .setTarget(element)
                    .setPopupTitle("listener")
                    .setTooltipText("listener")
                    .createLineMarkerInfo(element, (e, elt) -> {
                        if (elt instanceof PsiMethod) {
                            Project project = elt.getProject();
                            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
                            PsiClass eventBusClass = javaPsiFacade.findClass(Constants.EVENT_CLASS_ABS_NAME, GlobalSearchScope.allScope(project));
                            if (eventBusClass == null) return;

                            PsiMethod method = (PsiMethod) elt;
                            // 查找 eventBus.post
                            PsiMethod postMethod = eventBusClass.findMethodsByName(Constants.PUBLISHER_FUNC_NAME, false)[0];
                            if (null != postMethod) {
                                PsiParameter parameter = method.getParameterList().getParameter(0);
                                if (parameter != null) {
                                    PsiClass eventClass = ((PsiClassType) parameter.getType()).resolve();
                                    if (eventClass != null) {
                                        Editor editor = PsiEditorUtil.findEditor(elt);
                                    }
                                }
                            }
                        }
                    });
        }
        return null;
    }
}
