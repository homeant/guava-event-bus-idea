package io.github.homeant.guava.event.bus.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.XCollection;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@State(name = "GuavaEventBusSettings", storages = @Storage("guavaEventBus.xml"))
public class EventBusSettings implements PersistentStateComponent<EventBusSettings.Setting> {

    private final Setting setting = new Setting();

    @Override
    public void loadState(@NotNull Setting state) {
        Set<String> listenerList = new HashSet<>(state.getListenerList());
        Set<String> publisherList = new HashSet<>(state.getPublisherList());
        setting.setPublisherList(new ArrayList<>(publisherList));
        setting.setListenerList(new ArrayList<>(listenerList));
    }

    @Override
    public @Nullable EventBusSettings.Setting getState() {
        return setting;
    }

    public static EventBusSettings getInstance(Project project){
        return project.getService(EventBusSettings.class);
    }

    @Data
    public static class Setting{
        @Tag("listener")
        @XCollection
        private List<String> listenerList = new ArrayList<>(Arrays.asList("@com.google.common.eventbus.Subscribe"));
        @Tag("publisher")
        @XCollection
        private List<String> publisherList = new ArrayList<>(Arrays.asList("com.google.common.eventbus.EventBus.post", "com.google.common.eventbus.AsyncEventBus.post"));
    }
}
