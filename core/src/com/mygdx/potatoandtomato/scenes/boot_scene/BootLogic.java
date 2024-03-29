package com.mygdx.potatoandtomato.scenes.boot_scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mygdx.potatoandtomato.PTScreen;
import com.mygdx.potatoandtomato.absintflis.databases.DatabaseListener;
import com.mygdx.potatoandtomato.absintflis.gamingkit.ConnectionChangedListener;
import com.mygdx.potatoandtomato.absintflis.scenes.LogicAbstract;
import com.mygdx.potatoandtomato.absintflis.scenes.SceneAbstract;
import com.mygdx.potatoandtomato.absintflis.services.RestfulApiListener;
import com.mygdx.potatoandtomato.absintflis.socials.FacebookListener;
import com.mygdx.potatoandtomato.assets.Sounds;
import com.mygdx.potatoandtomato.enums.AnalyticEvent;
import com.mygdx.potatoandtomato.enums.ClientConnectionStatus;
import com.mygdx.potatoandtomato.enums.ConfirmIdentifier;
import com.mygdx.potatoandtomato.enums.SceneEnum;
import com.mygdx.potatoandtomato.helpers.Analytics;
import com.mygdx.potatoandtomato.models.*;
import com.mygdx.potatoandtomato.services.Confirm;
import com.mygdx.potatoandtomato.statics.Terms;
import com.potatoandtomato.common.statics.Vars;
import com.mygdx.potatoandtomato.utils.Logs;
import com.potatoandtomato.common.broadcaster.BroadcastEvent;
import com.potatoandtomato.common.broadcaster.BroadcastListener;
import com.potatoandtomato.common.enums.Status;
import com.potatoandtomato.common.utils.Strings;
import com.shaded.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by SiongLeng on 2/12/2015.
 */
public class BootLogic extends LogicAbstract {

    private BootScene _bootScene;
    private boolean _fbStepPast;
    private boolean _logined;
    private LoginReturnData loginReturnData;

    public BootLogic(PTScreen screen, Services services, Object... objs) {
        super(screen, services, objs);

        _bootScene = new BootScene(_services, _screen);

    }

    @Override
    public void onShow() {
        super.onShow();

        _bootScene.reset();
        _services.getDatabase().clearAllListeners();
        _services.getChat().hideChat();
        _services.getCoins().dispose();
        _services.getDataCaches().dispose();
        _services.getProfile().reset();
        _services.getTutorials().hide();
        _services.getSoundsPlayer().playMusic(Sounds.Name.THEME_MUSIC);
        _screen.showRotateSunrise();
        publishBroadcast(BroadcastEvent.DESTROY_ROOM);

        _services.getDatabase().offline();
        _services.getGamingKit().disconnect();
        _fbStepPast = false;
        _logined = false;
        _services.getDatabase().online();
        _services.getDatabase().unauth();
        checkCrashedBefore();
    }

    @Override
    public void onShown() {
        super.onShown();

        if(_services.getAutoJoiner().isAutoJoining()){
            _bootScene.showSocialLogin();
            if(_services.getSocials().isFacebookLogon()){    //user already logged in facebook before, log in again now
                loginFacebook();
            }
            else{
                checkContainsSecondaryUserId();
            }
        }
    }

    public void showLoginBox(){
        _bootScene.showSocialLogin();
        if(_services.getSocials().isFacebookLogon()){    //user already logged in facebook before, log in again now
            loginFacebook();
        }
    }

    public void loginFacebook() {
        Analytics.log(AnalyticEvent.LoginSocial);
        _bootScene.showSocialLoggingIn();

        _services.getSocials().loginFacebook(new FacebookListener() {
            @Override
            public void onLoginComplete(Result result) {
                if(result == Result.SUCCESS){
                    checkContainsSecondaryUserId();
                }
                else{
                    loginFailed(LoginFailedReason.SocialLoginFailed);
                }
            }
        });
    }

    public void checkContainsSecondaryUserId(){
        String userId = _services.getPreferences().get(Terms.USERID);
        if(Strings.isEmpty(userId)){
            String userId2 = _services.getPreferences().get(Terms.USERID_2);
            if(!Strings.isEmpty(userId2)){
                _services.getPreferences().put(Terms.USERID, _services.getPreferences().get(Terms.USERID_2));
                _services.getPreferences().put(Terms.USER_SECRET, _services.getPreferences().get(Terms.USER_SECRET_2));
            }
        }
        afterFacebookPhase();
    }

    public void afterFacebookPhase(){
        _fbStepPast = true;
        _bootScene.showPTLoggingIn();
        String userId = _services.getPreferences().get(Terms.USERID);

        if(!Strings.isEmpty(userId)){
            retrieveUserToken();
        }
        else{
            createNewUser();
        }
    }

    public void retrieveUserToken(){
        final String userId = _services.getPreferences().get(Terms.USERID);
        final String userSecret = _services.getPreferences().get(Terms.USER_SECRET);

        _services.getRestfulApi().loginUser(userId, userSecret, _services.getSocials().getFacebookProfile(), new RestfulApiListener<String>() {
            @Override
            public void onCallback(String result, Status st) {
                if(st == Status.FAILED && result.equals("USER_NOT_FOUND")){
                    _services.getPreferences().delete(Terms.USERID);
                    _services.getPreferences().delete(Terms.USER_SECRET);
                    createNewUser();
                }
                else if(st == Status.FAILED && result.equals("FAIL_CONNECT")){
                    loginFailed(LoginFailedReason.PTRestfulFailed);
                }
                else if(st == Status.FAILED){
                    loginFailed(LoginFailedReason.GeneralFailure);
                }
                else{
                    try {
                        ObjectMapper objectMapper = Vars.getObjectMapper();
                        loginReturnData = objectMapper.readValue(result, LoginReturnData.class);
                        if(!loginReturnData.getUserId().equals(userId)){
                            _services.getPreferences().put(Terms.USERID_2, userId);
                            _services.getPreferences().put(Terms.USER_SECRET_2, userSecret);
                        }

                        _services.getPreferences().put(Terms.USERID, loginReturnData.getUserId());
                        _services.getPreferences().put(Terms.USER_SECRET, loginReturnData.getSecret());
                        loginPTWithToken(loginReturnData.getToken());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    public void createNewUser(){
        _bootScene.showPTCreatingUser();
        if(_services.getSocials().isFacebookLogon()){
            _services.getRestfulApi().createNewUserWithFacebookProfile(_services.getSocials().getFacebookProfile(), new RestfulApiListener<UserIdSecretModel>() {
                @Override
                public void onCallback(UserIdSecretModel obj, Status st) {
                    if(st == Status.FAILED){
                        loginFailed(LoginFailedReason.GeneralFailure);
                    }
                    else{
                        _services.getPreferences().put(Terms.USERID, obj.getUserId());
                        _services.getPreferences().put(Terms.USER_SECRET, obj.getSecret());
                        checkContainsSecondaryUserId();
                    }
                }
            });
        }
        else{
            _services.getRestfulApi().createNewUser(new RestfulApiListener<UserIdSecretModel>() {
                @Override
                public void onCallback(UserIdSecretModel obj, Status st) {
                    if (st == Status.FAILED) {
                        loginFailed(LoginFailedReason.GeneralFailure);
                    } else {
                        _services.getPreferences().put(Terms.USERID, obj.getUserId());
                        _services.getPreferences().put(Terms.USER_SECRET, obj.getSecret());
                        retrieveUserToken();
                    }
                }
            });
        }
    }

    public void loginPTWithToken(final String token){
        _services.getDatabase().authenticateUserByToken(token, new DatabaseListener<Profile>(Profile.class) {
            @Override
            public void onCallback(Profile obj, Status st) {
                if (st == Status.FAILED || obj == null){
                    loginFailed(LoginFailedReason.GeneralFailure);
                }
                else {
                    obj.setToken(token);
                    _services.getProfile().copyToThis(obj);
                    loginGCM();
                }
            }
        });
    }

    public void loginFailed(LoginFailedReason loginFailedReason){
        _services.getAutoJoiner().stopAutoJoinRoom();

        switch (loginFailedReason){
            case SocialLoginFailed:
                _bootScene.showSocialLoginFailed();
                break;
            case PTRestfulFailed:
                _bootScene.showPTDown();
                break;
            case GeneralFailure:
                Analytics.log(AnalyticEvent.LoginFailed);
                _bootScene.showPTLogInFailed();
                break;
        }
    }


    public void loginGCM(){
        subscribeBroadcastOnceWithTimeout(BroadcastEvent.LOGIN_GCM_CALLBACK, 5000, new BroadcastListener<String>() {
            @Override
            public void onCallback(String obj, Status st) {
                if (st == Status.SUCCESS) {
                    _services.getProfile().setGcmId(obj);
                    loginPTSuccess();
                } else {
                    loginPTSuccess();       //allow login without gcm
                    //retrieveUserFailed();
                }
            }
        });
        publishBroadcast(BroadcastEvent.LOGIN_GCM_REQUEST);
    }


    public void loginPTSuccess(){
        _services.getProfile().setCountry(loginReturnData.getCountry());
        _services.getDatabase().updateProfile(_services.getProfile(), null);
        _services.getCoins().profileReady();
        _services.getDataCaches().startCaches();
        _services.getGamingKit().connect(_services.getProfile());
        _services.getBroadcaster().broadcast(BroadcastEvent.USER_READY, _services.getProfile());
    }

    public void loginProcessCompleteSucceed(){
        Analytics.log(AnalyticEvent.LoginSuccess);

        _screen.hideRotateSunrise();
        if(Strings.isEmpty(_services.getProfile().getGameName())){
            _screen.toScene(SceneEnum.INPUT_NAME);
        }
        else{
            _screen.toScene(SceneEnum.GAME_LIST);
        }
        _logined = true;
    }

    private void checkCrashedBefore(){
        String msg = Logs.getAndDeleteLogMsg();
        if(!Strings.isEmpty(msg)){
            _services.getDatabase().saveLog(msg);
            _services.getConfirm().show(ConfirmIdentifier.CrashedReportSent, _texts.confirmAppsCrashed(), Confirm.Type.YES, null);
            Analytics.log(AnalyticEvent.SentCrashMsg);
        }
    }

    @Override
    public void setListeners() {
        _bootScene.getPlayButton().addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                _services.getSoundsPlayer().playSoundEffect(Sounds.Name.TOGETHER_CHEERS);
                showLoginBox();
            }
        });

        _bootScene.getTickIcon().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if(!_fbStepPast) loginFacebook();
                else checkContainsSecondaryUserId();
            }
        });

        _bootScene.getCrossIcon().addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if(!_fbStepPast) checkContainsSecondaryUserId();
                else Gdx.app.exit();
            }
        });

//        _bootScene.getVersionLabel().addListener(new ClickListener(){
//            @Override
//            public void clicked(InputEvent event, float x, float y) {
//                super.clicked(event, x, y);
//                Logs.getLoggings().setEnabled(true);
//            }
//        });

        _services.getGamingKit().addListener(getClassTag(), new ConnectionChangedListener() {
            @Override
            public void onChanged(String userId, ClientConnectionStatus st) {
                if(_services.getProfile() != null && userId != null && userId.equals(_services.getProfile().getUserId())){
                    if(!_logined){
                        if(st == ClientConnectionStatus.CONNECTED){
                            loginProcessCompleteSucceed();
                        }
                        else{
                            loginFailed(LoginFailedReason.GeneralFailure);
                        }
                    }
                }
            }
        });
    }

    @Override
    public SceneAbstract getScene() {
        return _bootScene;
    }

    private enum LoginFailedReason{
        SocialLoginFailed, PTRestfulFailed, GeneralFailure
    }


}
