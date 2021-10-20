package io.github.homeant.guava.event.bus.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "GuavaEventBusSettings", storages = @Storage("guavaEventBus.xml"))
public class EventBusSettings implements PersistentStateComponent<EventBusSettings.Setting> {

    @Nullable
    @Override
    public EventBusSettings.Setting getState() {
        return new Setting();
    }

    @Override
    public void loadState(@NotNull EventBusSettings.Setting state) {

    }

    public static class Setting{

    }
}
