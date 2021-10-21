package io.github.homeant.guava.event.bus.config;

import com.intellij.execution.console.ConsoleFoldingSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@State(name = "GuavaEventBusSettings", storages = @Storage("guavaEventBus.xml"))
public class EventBusSettings implements PersistentStateComponent<EventBusSettings.Setting> {

    private final List<String> listenerList = new ArrayList<>();
    private final List<String> publisherList = new ArrayList<>();

    @Nullable
    @Override
    public EventBusSettings.Setting getState() {
        return new Setting();
    }

    @Override
    public void loadState(@NotNull EventBusSettings.Setting state) {
        listenerList.addAll(filterEmptyStringsFromCollection(state.getListenerList()));
        publisherList.addAll(filterEmptyStringsFromCollection(state.getPublisherList()));
    }

    public static EventBusSettings getSettings() {
        return ApplicationManager.getApplication().getService(EventBusSettings.class);
    }

    private static Collection<String> filterEmptyStringsFromCollection(Collection<String> collection) {
        return ContainerUtil.filter(collection, input -> !StringUtil.isEmpty(input));
    }

    public List<String> getListenerList() {
        return listenerList;
    }

    public List<String> getPublisherList() {
        return publisherList;
    }

    @Data
    public static class Setting{
        private List<String> listenerList;
        private List<String> publisherList;
    }
}
