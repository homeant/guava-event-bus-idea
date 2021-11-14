package io.github.homeant.guava.event.bus.handler;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.find.actions.ShowUsagesAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiEditorUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.usages.UsageView;
import com.intellij.usages.impl.UsageViewImpl;
import io.github.homeant.guava.event.bus.constant.Constants;
import io.github.homeant.guava.event.bus.utils.PsiUtils;

import java.awt.event.MouseEvent;

public class ShowListenerHandler implements GutterIconNavigationHandler {
    @Override
    public void navigate(MouseEvent event, PsiElement elt) {
        if (elt instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression expression = (PsiMethodCallExpression) elt;
            try {
                PsiType[] expressionTypes = expression.getArgumentList().getExpressionTypes();
                if (expressionTypes.length > 0) {
                    PsiClass eventClass = PsiUtils.getClass(expressionTypes[0]);
                    if (eventClass != null) {
                        ((com.intellij.find.actions.ShowUsagesAction) ActionManager.getInstance().getAction("ShowUsages"))
                                .startFindUsages(eventClass,
                                        new RelativePoint(event),
                                        PsiEditorUtil.findEditor(elt));
//                        ShowUsagesAction action = new ShowUsagesAction(new ListenerFilter(elt.getProject()));
//                        action.startFindUsages(eventClass, new RelativePoint(event), PsiEditorUtil.findEditor(elt), 100);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }



    }
}
