package com.xmx.mygallery.Tools;

/**
 * Created by The_onE on 2016/8/6.
 */
public abstract class NewThread {

    public abstract void process();

    public void start() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                process();
            }
        };
        thread.start();
    }
}
