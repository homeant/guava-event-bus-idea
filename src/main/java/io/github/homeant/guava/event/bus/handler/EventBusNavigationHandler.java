package io.github.homeant.guava.event.bus.handler;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.find.FindManager;
import com.intellij.find.findUsages.*;
import com.intellij.find.impl.FindManagerImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.intellij.usages.UsageViewManager;
import io.github.homeant.guava.event.bus.actions.PingEDT;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventBusNavigationHandler implements GutterIconNavigationHandler {

    private static final Logger LOG = Logger.getInstance(EventBusNavigationHandler.class);

    private final GoItemProviderHandler handler;

    public EventBusNavigationHandler(GoItemProviderHandler handler) {
        this.handler = handler;
    }

    @Override
    public void navigate(MouseEvent e, PsiElement elt) {
        Project project = elt.getProject();
        PsiElement psiElement = handler.getPsiElement();
        if(psiElement==null){
            return;
        }
        PsiElement[] primaryElements = handler.getPrimaryElements();
        final List<Usage> usages = new ArrayList<>();
        // 查找关系
        FindManagerImpl findManager = (FindManagerImpl) FindManager.getInstance(project);
        FindUsagesManager findUsagesManager = findManager.getFindUsagesManager();
        JavaFindUsagesHandler usagesHandler = (JavaFindUsagesHandler)findUsagesManager.getFindUsagesHandler(psiElement, true);
        if (usagesHandler == null) {
            LOG.error(psiElement + " usagesHandler is null");
            return;
        }
        PsiElement2UsageTargetAdapter[] selfUsageTargets = {new PsiElement2UsageTargetAdapter(psiElement, true)};
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        PingEDT pingEDT = new PingEDT("", () -> atomicBoolean.get(), 100, () -> {
            if (!atomicBoolean.get()) {
                return;
            }

        });
        // search
        FindUsagesManager.startProcessUsages(usagesHandler,primaryElements, usagesHandler.getSecondaryElements(), usage -> {
            System.out.println(usage);
            synchronized (usages) {
                if (UsageViewManager.isSelfUsage(usage, selfUsageTargets)) {
                    return false;
                }
                if (!this.handler.filter(usage)) {
                    //return false;
                }
                usages.add(usage);
                pingEDT.ping();
                return true;
            }
        }, getDefaultOptions(usagesHandler), () -> ApplicationManager.getApplication().invokeLater(() -> {
            pingEDT.ping(); // repaint status
            synchronized (usages) {
                atomicBoolean.set(true);
                this.handler.navigate(this.handler.conversion(usages), new RelativePoint(e));
            }
        }, project.getDisposed()));
    }

    private static FindUsagesOptions getDefaultOptions(@NotNull FindUsagesHandler handler) {
        FindUsagesOptions options = handler.getFindUsagesOptions();
        // by default, scope in FindUsagesOptions is copied from the FindSettings, but we need a default one
        options.searchScope = FindUsagesManager.getMaximalScope(handler);
        return options;
    }
}
