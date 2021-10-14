package io.github.homeant.guava.event.bus;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.find.FindManager;
import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesManager;
import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.find.findUsages.PsiElement2UsageTargetAdapter;
import com.intellij.find.impl.FindManagerImpl;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiEditorUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.ui.ScrollingUtil;
import com.intellij.ui.SpeedSearchBase;
import com.intellij.ui.SpeedSearchComparator;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.popup.AbstractPopup;
import com.intellij.usageView.UsageViewBundle;
import com.intellij.usages.*;
import com.intellij.usages.impl.UsageNode;
import com.intellij.usages.impl.UsageViewImpl;
import com.intellij.usages.rules.UsageFilteringRuleProvider;
import com.intellij.util.Alarm;
import com.intellij.util.Processor;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.AsyncProcessIcon;
import io.github.homeant.guava.event.bus.constant.Constants;
import io.github.homeant.guava.event.bus.utils.PsiUtils;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


@Log
public class EventBugLineMarkerProvider implements LineMarkerProvider {

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (PsiUtils.isEventBusPostMethod(element)) {
            return gutterIconBuilder(element,Constants.PUBLISHER_ICON)
                    .setPopupTitle(Constants.PUBLISHER)
                    .setTooltipText(Constants.PUBLISHER)
                    .createLineMarkerInfo(element,this::publishHandle);
        }
        if (PsiUtils.isEventBusHandlerMethod(element)) {
            return gutterIconBuilder(element,Constants.LISTENER_ICON)
                    .setPopupTitle(Constants.LISTENER)
                    .setTooltipText(Constants.LISTENER)
                    .createLineMarkerInfo(element, this::listenHandle);
        }
        return null;
    }

    private NavigationGutterIconBuilder<PsiElement> gutterIconBuilder(PsiElement element, Icon icon){
        return NavigationGutterIconBuilder.create(icon)
                .setAlignment(GutterIconRenderer.Alignment.LEFT)
                .setTarget(element);
    }

    private void publishHandle(MouseEvent e, PsiElement elt){

    }

    private void listenHandle(MouseEvent e, PsiElement elt){
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
                    // 参数
                    PsiClass eventClass = ((PsiClassType) parameter.getType()).resolve();
                    if (eventClass != null) {
                        log.info("event class "+ eventClass);
                        // 查找发布者
                        Editor editor = PsiEditorUtil.findEditor(elt);
                        FindUsagesManager findUsagesManager = ((FindManagerImpl) FindManager.getInstance(project)).getFindUsagesManager();
                        FindUsagesHandler handler = findUsagesManager.getNewFindUsagesHandler(elt, false);
                        if(handler==null) return;
                        //showElementUsages(handler,editor,new RelativePoint(e), 100, getDefaultOptions(handler));
                        ProgressIndicator progressIndicator = FindUsagesManager.startProcessUsages(handler, handler.getPrimaryElements(), handler.getSecondaryElements(), usage -> {
                            log.info("usage:" + usage);
                            return false;
                        }, getDefaultOptions(handler), () -> {

                        });
                        progressIndicator.start();
                    }
                }
            }
        }
    }

    private static FindUsagesOptions getDefaultOptions(@NotNull FindUsagesHandler handler) {
        FindUsagesOptions options = handler.getFindUsagesOptions();
        // by default, scope in FindUsagesOptions is copied from the FindSettings, but we need a default one
        options.searchScope = FindUsagesManager.getMaximalScope(handler);
        return options;
    }

}
