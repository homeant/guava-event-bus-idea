package io.github.homeant.guava.event.bus.config;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xml.Convert;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@State(name = "GuavaEventBusSettings", storages = @Storage("guavaEventBus.xml"))
public class EventBusSettings implements PersistentStateComponent<EventBusSettings.Setting> {

    private Setting setting = new Setting();

    public EventBusSettings() {
        setting.setPublisherList(new ArrayList<>(Arrays.asList("com.google.common.eventbus.EventBus.post", "com.google.common.eventbus.AsyncEventBus.post")));
    }

    @Override
    public void loadState(@NotNull Setting state) {
        this.setting = state;
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
        private List<String> listenerList = new ArrayList<>();
        private List<String> publisherList = new ArrayList<>();
    }
}
