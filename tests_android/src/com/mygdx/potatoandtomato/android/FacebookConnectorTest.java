package com.mygdx.potatoandtomato.android;

import android.test.ActivityInstrumentationTestCase2;
import com.potatoandtomato.common.broadcaster.BroadcastEvent;
import com.potatoandtomato.common.broadcaster.BroadcastListener;
import com.potatoandtomato.common.enums.Status;

/**
 * Created by SiongLeng on 9/12/2015.
 */
public class FacebookConnectorTest extends ActivityInstrumentationTestCase2<AndroidLauncher> {

    AndroidLauncher _activity;

    public FacebookConnectorTest() {
        super(AndroidLauncher.class);
    }

    @Override
    protected void setUp() throws Exception {
        _activity = getActivity();
    }

    public void testLoginTimeout() {
        final boolean[] waiting = {true};

        _activity.getBroadcaster().subscribeOnceWithTimeout(BroadcastEvent.LOGIN_FACEBOOK_CALLBACK, 500, new BroadcastListener() {
            @Override
            public void onCallback(Object obj, Status st) {
                waiting[0] = false;
                assertEquals(Status.FAILED, st);
                assertEquals(true, obj == null);
            }
        });

        assertEquals(true, _activity.getBroadcaster().hasEventCallback(BroadcastEvent.LOGIN_FACEBOOK_REQUEST));
        _activity.getBroadcaster().broadcast(BroadcastEvent.LOGIN_FACEBOOK_REQUEST);     //just to test facebook activity can be launched

        while (waiting[0]) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
