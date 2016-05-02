package com.potatoandtomato.games.statics;

/**
 * Created by SiongLeng on 22/2/2016.
 */
public class Global {

    public static boolean DEBUG = false;         //default false
    public static boolean REVIEW_MODE = false;
    public static int ATTACK_TIME_PERCENT = 25;
    public static int ClOSE_DOOR_BUFFER_TIME = 5000;            //in milisec
    public static int BONUS_STAGE_NUMBER = 10;

    public static void setDEBUG(boolean isDebugging){
        DEBUG = isDebugging;
        if(DEBUG) ClOSE_DOOR_BUFFER_TIME = 1500;
    }

    public static void setDEBUG_BONUS(boolean isDebugging){
        setDEBUG(true);
        BONUS_STAGE_NUMBER = 2;
    }

}
