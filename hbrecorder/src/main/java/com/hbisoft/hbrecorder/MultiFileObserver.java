package com.hbisoft.hbrecorder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;


public class MultiFileObserver extends FileObserver {

    private List<SingleFileObserver> mObservers;
    private final String mPath;
    private final int mMask;
    private final MyListener mListener;

    public MultiFileObserver(String path, MyListener listener) {
        super(path, ALL_EVENTS);
        mPath = path;
        mMask = ALL_EVENTS;
        mListener = listener;
    }


    @Override
    public void startWatching() {
        if (mObservers != null) return;

        mObservers = new ArrayList<>();
        Stack<String> stack = new Stack<>();
        stack.push(mPath);

        while (!stack.isEmpty()) {
            String parent = stack.pop();
            mObservers.add(new SingleFileObserver(parent, mMask));
            File path = new File(parent);
            File[] files = path.listFiles();
            if (files == null) continue;

            for (File f : files) {
                if (f.isDirectory() && !f.getName().equals(".") && !f.getName().equals("..")) {
                    stack.push(f.getPath());
                }
            }
        }

        for (SingleFileObserver sfo : mObservers) {
            sfo.startWatching();
        }
    }

    @Override
    public void stopWatching() {
        if (mObservers == null) return;

        for (SingleFileObserver sfo : mObservers) {
            sfo.stopWatching();
        }
        mObservers.clear();
        mObservers = null;
    }

    @Override
    public void onEvent(int event, final String path) {
        if (event == android.os.FileObserver.CLOSE_WRITE) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mListener.onCompleteCallback();
                }
            });
        }
    }

    class SingleFileObserver extends FileObserver {
        final String mPath;


        SingleFileObserver(String path, int mask) {
            super(path, mask);
            mPath = path;
        }

        @Override
        public void onEvent(int event, String path) {
            String newPath = mPath + "/" + path;
            MultiFileObserver.this.onEvent(event, newPath);
        }
    }
}
