package io.github.homeant.guava.event.bus.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@State(name = "GuavaEventBusSettings", storages = @Storage("guavaEventBus.xml"))
public class EventBusSettings implements PersistentStateComponent<EventBusSettings.Setting> {

    private final EventBusSettings.Setting setting = new Setting();

    public Setting getSetting() {
        return setting;
    }

    @Nullable
    @Override
    public EventBusSettings.Setting getState() {
        //setting.setPublisherList(List.of("com.google.common.eventbus.EventBus.post","com.google.common.eventbus.AsyncEventBus.post"));
        return setting;
    }

    @Override
    public void loadState(@NotNull EventBusSettings.Setting state) {
        Set<String> listener = new HashSet<>(state.getListenerList());
        Set<String> publisher = new HashSet<>(state.getPublisherList());
        setting.setListenerList(new ArrayList<>(listener));
        setting.setPublisherList(new ArrayList<>(publisher));
    }

    public static EventBusSettings getSettings(Project project) {
        return project.getService(EventBusSettings.class);
    }

    @Data
    public static class Setting{
        private List<String> listenerList = new ArrayList<>();
        private List<String> publisherList = new ArrayList<>();
    }
}
