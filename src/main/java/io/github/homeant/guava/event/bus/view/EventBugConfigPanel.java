package io.github.homeant.guava.event.bus.view;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.AddEditDeleteListPanel;
import com.intellij.ui.ListSpeedSearch;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EventBugConfigPanel extends JPanel {

    private ListPanel publishListPanel;

    private ListPanel listenListPanel;

    public EventBugConfigPanel() {
        super(new BorderLayout());
        Splitter splitter = new Splitter(true);
        add(splitter, BorderLayout.CENTER);

        publishListPanel = new ListPanel("Publisher Filter:","Enter a substring of a publisher you'd like to see editor:");
        listenListPanel = new ListPanel("Listener Filter:","Enter a substring of a listener you'd like to see editor:");
        splitter.setFirstComponent(publishListPanel);
        splitter.setSecondComponent(listenListPanel);


    }


    private static class ListPanel extends AddEditDeleteListPanel<String> {

        private final @NlsContexts.DialogMessage String message;

        public ListPanel(@NlsContexts.Label String title,@NlsContexts.DialogMessage String message) {
            super(title, new ArrayList<>());
            this.message = message;
            new ListSpeedSearch(myList);
        }

        @Override
        protected @Nullable String findItemToAdd() {
            return showEditDialog("");
        }

        private @Nullable String showEditDialog(final String initialValue) {
            return Messages.showInputDialog(this, message, "eventBus pattern", Messages.getQuestionIcon(), initialValue, new InputValidatorEx() {
                @Override
                public boolean checkInput(String inputString) {
                    return !StringUtil.isEmpty(inputString);
                }

                @Override
                public boolean canClose(String inputString) {
                    return !StringUtil.isEmpty(inputString);
                }

                @Override
                public @NlsContexts.DetailedDescription @Nullable String getErrorText(String inputString) {
                    if (!checkInput(inputString)) {
                        return "eventBus rule string cannot be empty";
                    }
                    return null;
                }
            });
        }

        void resetFrom(List<String> patterns) {
            myListModel.clear();
            patterns.stream().sorted(String.CASE_INSENSITIVE_ORDER).forEach(myListModel::addElement);
        }

        void applyTo(List<? super String> patterns) {
            patterns.clear();
            for (Object o : getListItems()) {
                patterns.add((String)o);
            }
        }

        public void addRule(String rule) {
            addElement(rule);
        }

        @Override
        protected String editSelectedItem(String item) {
            return showEditDialog(item);
        }
    }
}
