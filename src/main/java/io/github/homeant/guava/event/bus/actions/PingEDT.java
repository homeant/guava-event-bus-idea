package io.github.homeant.guava.event.bus.actions;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

public class PingEDT {
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    private final String myName;
    private final Runnable pingAction;
    private volatile boolean stopped;
    private volatile boolean pinged;
    private final @NotNull BooleanSupplier myShutUpCondition;
    private final int myMaxUnitOfWorkThresholdMs; //-1 means indefinite

    private final AtomicBoolean invokeLaterScheduled = new AtomicBoolean();
    private final Runnable myUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            boolean b = invokeLaterScheduled.compareAndSet(true, false);
            assert b;
            if (stopped || myShutUpCondition.getAsBoolean()) {
                stop();
                return;
            }
            long start = System.currentTimeMillis();
            int processed = 0;
            while (true) {
                if (processNext()) {
                    processed++;
                }
                else {
                    break;
                }
                long finish = System.currentTimeMillis();
                if (myMaxUnitOfWorkThresholdMs != -1 && finish - start > myMaxUnitOfWorkThresholdMs) break;
            }
            if (!isEmpty()) {
                scheduleUpdate();
            }
        }
    };

    public PingEDT(@NotNull @NonNls String name,
            @NotNull BooleanSupplier shutUpCondition,
            int maxUnitOfWorkThresholdMs,
            @NotNull Runnable pingAction) {
        myName = name;
        myShutUpCondition = shutUpCondition;
        myMaxUnitOfWorkThresholdMs = maxUnitOfWorkThresholdMs;
        this.pingAction = pingAction;
    }

    private boolean isEmpty() {
        return !pinged;
    }

    private boolean processNext() {
        pinged = false;
        pingAction.run();
        return pinged;
    }

    // returns true if invokeLater was called
    public boolean ping() {
        pinged = true;
        return scheduleUpdate();
    }

    // returns true if invokeLater was called
    private boolean scheduleUpdate() {
        if (!stopped && invokeLaterScheduled.compareAndSet(false, true)) {
            SwingUtilities.invokeLater(myUpdateRunnable);
            return true;
        }
        return false;
    }

    public void stop() {
        stopped = true;
    }
}
