package com.marcosoft.storageSoftware.infrastructure.util;

import javafx.application.Platform;

import java.util.Timer;
import java.util.TimerTask;

// Agregar clase interna Debouncer
public class Debouncer {
    private Timer timer = new Timer(true);

    public void debounce(Runnable task, int delayMillis) {
        timer.cancel();
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(task);
            }
        }, delayMillis);
    }
}