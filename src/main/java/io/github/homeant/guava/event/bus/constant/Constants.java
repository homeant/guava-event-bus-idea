package io.github.homeant.guava.event.bus.constant;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class Constants {

    private Constants(){

    }

    public static final String PUBLISHER = "publisher";
    public static final String LISTENER = "listener";

    public static final String PUBLISHER_ICON_NAME = "/icons/publisher.png";
    public static final String LISTENER_ICON_NAME = "/icons/listener.png";
    public static final Icon PUBLISHER_ICON = IconLoader.getIcon(Constants.PUBLISHER_ICON_NAME,Constants.class);
    public static final Icon LISTENER_ICON = IconLoader.getIcon(Constants.LISTENER_ICON_NAME,Constants.class);
    public static final String PACKAGE_NAME = "com.google.common.eventbus";

    public static final String EVENT_CLASS_NAME = "EventBus";

    public static final String EVENT_ASYNC_CLASS_NAME = "AsyncEventBus";


    public static final String EVENT_CLASS_ABS_NAME =PACKAGE_NAME+"."+EVENT_CLASS_NAME;
    public static final String EVENT_ASYNC_CLASS_ABS_NAME =PACKAGE_NAME+"."+EVENT_ASYNC_CLASS_NAME;

    public static final String PUBLISHER_FUNC_NAME = "post";

    public static final String LISTENER_ANNOTATION_NAME = PACKAGE_NAME + ".Subscribe";
}
