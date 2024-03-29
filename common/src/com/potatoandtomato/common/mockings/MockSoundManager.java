package com.potatoandtomato.common.mockings;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;
import com.potatoandtomato.common.absints.ISoundsPlayer;
import com.potatoandtomato.common.utils.Threadings;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SiongLeng on 20/1/2016.
 */
public class MockSoundManager implements ISoundsPlayer {

    private float _volume;

    private HashMap<Sound, Long> _externalSoundIdsMap;
    private ArrayList<Music> _externalMusics;

    public MockSoundManager() {
        _externalSoundIdsMap = new HashMap();
        _externalMusics = new ArrayList();
        setVolume(1f);
    }

    @Override
    public void disposeAllExternalSounds() {

    }

    @Override
    public void playMusic(final Music music) {
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                music.setVolume(_volume);
                music.setLooping(true);
                music.play();
                _externalMusics.add(music);
            }
        });
    }

    @Override
    public void playMusicNoLoop(final Music music) {
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                music.setVolume(_volume);
                music.setLooping(false);
                music.play();
                _externalMusics.add(music);
            }
        });
    }

    @Override
    public void stopMusic(final Music music) {
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                music.stop();
                _externalMusics.remove(music);
            }
        });
    }

    @Override
    public void stopAllMusics() {
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                for(Music music : _externalMusics){
                    music.stop();
                }
                _externalMusics.clear();
            }
        });
    }

    @Override
    public void playSound(final Sound sound) {
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                sound.play(_volume);
            }
        });
    }

    @Override
    public void playSoundLoop(final Sound sound) {
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                long id = sound.play(_volume);
                sound.setLooping(id, true);
                _externalSoundIdsMap.put(sound, id);
            }
        });
    }

    @Override
    public void stopSoundLoop(final Sound sound) {
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                if(_externalSoundIdsMap.containsKey(sound)){
                    sound.setLooping(_externalSoundIdsMap.get(sound), false);
                    _externalSoundIdsMap.remove(sound);
                }
            }
        });
    }

    @Override
    public void setVolume(float _volume) {
        this._volume = _volume;

        if(_volume > 0.1f) _volume = 0.1f;
    }
}
