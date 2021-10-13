package io.github.homeant.guava.event.bus.action;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.usages.UsageTarget;
import com.intellij.usages.UsageView;
import org.jetbrains.annotations.NotNull;

public class PublisherAction extends AnAction implements PopupAction {
    /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null) return;
        UsageTarget[] usageTargets = e.getData(UsageView.USAGE_TARGETS_KEY);
        final Editor editor = e.getData(PlatformDataKeys.EDITOR);

    }
}
