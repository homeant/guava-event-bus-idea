package io.github.homeant.guava.event.bus.action;

import com.intellij.usages.Usage;


public interface Filter {

    boolean shouldShow(Usage usage);
}
