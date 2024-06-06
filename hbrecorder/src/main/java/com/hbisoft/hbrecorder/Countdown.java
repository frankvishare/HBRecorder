package com.hbisoft.hbrecorder;

import java.util.Timer;
import java.util.TimerTask;

public abstract class Countdown {
    private final Timer timer;
    private long totalTime;
    private long interval;

    private long delay;
    private TimerTask task;
    private long startTime = -1;

    public Countdown(long totalTime, long interval) {
        this(totalTime, interval, 0);
    }

    public Countdown(long totalTime, long interval, long delay) {
        this.totalTime = totalTime;
        this.interval = interval;
        this.delay = delay;
        this.timer = new Timer("PreciseCountdown", true);
        this.task = createTask();
    }

    public void start() {
        timer.scheduleAtFixedRate(task, delay, interval);
    }

    public void stop() {
        onStopCalled();
        task.cancel();
        dispose();
    }

    public void restart() {
        stop();
        this.task = createTask();
        start();
    }

    // Call this when there's no further use for this timer
    public void dispose(){
        timer.cancel();
        timer.purge();
    }

    private TimerTask createTask() {
        return new TimerTask() {
            @Override
            public void run() {
                long timeLeft = calculateTimeLeft();
                if (timeLeft <= 0) {
                    cancel();
                    onFinished();
                } else {
                    onTick(timeLeft);
                }
            }
        };
    }

    private long calculateTimeLeft() {
        long currentTime = task.scheduledExecutionTime();
        if (startTime < 0) {
            startTime = currentTime;
            return totalTime;
        }

        long elapsedTime = currentTime - startTime;
        long timeLeft = totalTime - elapsedTime;

        return Math.max(timeLeft, 0); // Ensure timeLeft is non-negative
    }

    public abstract void onTick(long timeLeft);
    public abstract void onFinished();
    public abstract void onStopCalled();
}
