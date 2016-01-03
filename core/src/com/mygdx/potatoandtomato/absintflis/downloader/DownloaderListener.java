package com.mygdx.potatoandtomato.absintflis.downloader;

import com.badlogic.gdx.Net;

/**
 * Created by SiongLeng on 13/12/2015.
 */
public abstract class DownloaderListener {

    public enum Status{
        SUCCESS, FAILED
    }

    public abstract void onCallback(byte[] bytes, Status st);

    public void onStep(double percentage){

    }

}