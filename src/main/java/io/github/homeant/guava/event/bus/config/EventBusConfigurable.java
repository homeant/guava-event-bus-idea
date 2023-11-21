package io.github.homeant.guava.event.bus.config;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.AddEditDeleteListPanel;
import com.intellij.ui.ListSpeedSearch;
import com.intellij.ui.TreeUIHelper;
import lombok.extern.java.Log;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventBusConfigurable implements Configurable {
    private static final Logger LOG = Logger.getInstance(EventBusConfigurable.class);

    private JPanel rootPanel;
    private ListPanel listenListPanel;
    private ListPanel publishListPanel;

    private final EventBusSettings eventBusSettings;

    public EventBusConfigurable(Project project){
        eventBusSettings = EventBusSettings.getInstance(project);
    }

    /**
     * Returns the visible name of the configurable component.
     * Note, that this method must return the display name
     * that is equal to the display name declared in XML
     * to avoid unexpected errors.
     *
     * @return the visible name of the configurable component
     */
    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Event Bus";
    }

    /**
     * Creates new Swing form that enables user to configure the settings.
     * Usually this method is called on the EDT, so it should not take a long time.
     * <p>
     * Also this place is designed to allocate resources (subscriptions/listeners etc.)
     *
     * @return new Swing form to show, or {@code null} if it cannot be created
     * @see #disposeUIResources
     */
    @Override
    public @Nullable JComponent createComponent() {
        if(rootPanel==null){
            LOG.info("created EventBus config");
            rootPanel = new JPanel(new BorderLayout());
            Splitter splitter = new Splitter(true);
            rootPanel.add(splitter, BorderLayout.CENTER);
            publishListPanel = new ListPanel("Publisher filter:","Enter a substring of a publisher you'd like to see editor:");
            listenListPanel = new ListPanel("Listener filter:","Enter a substring of a listener you'd like to see editor:");
            splitter.setFirstComponent(publishListPanel);
            splitter.setSecondComponent(listenListPanel);
        }
        return rootPanel;
    }

    /**
     * Indicates whether the Swing form was modified or not.
     * This method is called very often, so it should not take a long time.
     *
     * @return {@code true} if the settings were modified, {@code false} otherwise
     */
    @Override
    public boolean isModified() {
        return true;
    }

    /**
     * Stores the settings from the Swing form to the configurable component.
     * This method is called on EDT upon user's request.
     *
     */
    @Override
    public void apply() {
        EventBusSettings.Setting setting = eventBusSettings.getState();
        if(setting!=null) {
            listenListPanel.applyTo(setting.getListenerList());
            publishListPanel.applyTo(setting.getPublisherList());
        }
    }

    @Override
    public void reset() {
        if(rootPanel!=null) {
            EventBusSettings.Setting setting = eventBusSettings.getState();
            if(setting!=null) {
                listenListPanel.resetFrom(setting.getListenerList());
                publishListPanel.resetFrom(setting.getPublisherList());
            }
        }
    }

    private static class ListPanel extends AddEditDeleteListPanel<String> {

        private final @NlsContexts.DialogMessage String message;

        public ListPanel(@NlsContexts.Label String title,@NlsContexts.DialogMessage String message) {
            super(title, new ArrayList<>());
            this.message = message;
            new ListSpeedSearch<>(myList);
            TreeUIHelper.getInstance().installListSpeedSearch(myList);
        }

        @Override
        protected @Nullable String findItemToAdd() {
            return showEditDialog("");
        }

        private @Nullable String showEditDialog(final String initialValue) {
            return Messages.showInputDialog(this, message, "EventBus Pattern", Messages.getQuestionIcon(), initialValue, new InputValidatorEx() {
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
                        return "EventBus rule string cannot be empty";
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

        @Override
        protected String editSelectedItem(String item) {
            return showEditDialog(item);
        }
    }
}
