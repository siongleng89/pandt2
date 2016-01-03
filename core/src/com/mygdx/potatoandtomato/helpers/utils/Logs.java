package com.mygdx.potatoandtomato.helpers.utils;

import com.badlogic.gdx.graphics.FPSLogger;

/**
 * Created by SiongLeng on 4/12/2015.
 */
public class Logs {

    private static long _startTime;
    private static FPSLogger _fps;
    private static SafeThread _fpsThread;

    public static void show(String msg){
        System.out.println(msg);
    }

    public static void show(float msg){
        System.out.println(msg);
    }

    public static void startLogFps(){
        if(_fps == null){
            _fps = new FPSLogger();
            _fpsThread = new SafeThread();
            Threadings.runInBackground(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        if(_fpsThread.isKilled()) return;
                        _fps.log();
                        Threadings.sleep(1000);
                    }
                }
            });

        }
    }

    public static void stopLogFps(){
        _fpsThread.kill();
        _fps = null;
    }

    public static String getCallerClassName() {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        String callerClassName = null;
        for (int i=1; i<stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(Logs.class.getName())&& ste.getClassName().indexOf("java.lang.Thread")!=0) {
                if (callerClassName==null) {
                    callerClassName = ste.getClassName();
                } else if (!callerClassName.equals(ste.getClassName())) {
                    return ste.getClassName();
                }
            }
        }
        return null;
    }

    public static void startMeasure(){
        _startTime = System.nanoTime();
    }

    public static long endMeasure(){
        return System.nanoTime() - _startTime;
    }


}