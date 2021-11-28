package io.github.homeant.guava.event.bus;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class EventBusBundle extends DynamicBundle {

    private static final EventBusBundle ourInstance = new EventBusBundle();

    public EventBusBundle() {
        super("messages.EventBusBundle");
    }

    public static String message(@NotNull @PropertyKey(resourceBundle = "messages.EventBusBundle") String key) {
        if (key == null) {
            return null;
        }
        return message(key, new Object[0]);
    }

    public static String message(@NotNull @PropertyKey(resourceBundle = "messages.EventBusBundle") String key, Object... params) {
        if (key == null) {
            return null;
        }
        if (params == null) {
            return null;
        }
        return ourInstance.getMessage(key, params);
    }

}
