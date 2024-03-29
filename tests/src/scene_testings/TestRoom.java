package scene_testings;

import abstracts.MockDB;
import abstracts.MockGamingKit;
import abstracts.TestAbstract;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.firebase.client.annotations.Nullable;
import com.mygdx.potatoandtomato.PTScreen;
import com.mygdx.potatoandtomato.absintflis.databases.DatabaseListener;
import com.mygdx.potatoandtomato.absintflis.databases.IDatabase;
import com.potatoandtomato.common.absints.DownloaderListener;
import com.potatoandtomato.common.absints.IDownloader;
import com.mygdx.potatoandtomato.absintflis.gamingkit.GamingKit;
import com.mygdx.potatoandtomato.enums.UpdateRoomMatesCode;
import com.mygdx.potatoandtomato.enums.SceneEnum;
import com.mygdx.potatoandtomato.services.GCMSender;
import com.potatoandtomato.common.utils.SafeThread;
import com.potatoandtomato.common.utils.Threadings;
import com.mygdx.potatoandtomato.models.*;
import com.mygdx.potatoandtomato.scenes.room_scene.RoomLogic;
import com.mygdx.potatoandtomato.scenes.room_scene.RoomScene;
import com.potatoandtomato.common.enums.Status;
import com.mygdx.potatoandtomato.absintflis.mocks.MockModel;
import helpers.Mockings;
import helpers.T_Services;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.atLeast;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * Created by SiongLeng on 17/12/2015.
 */
public class TestRoom extends TestAbstract {

//    private Services _services;
//    private Room _room;
//
//    @Before
//    public void setUp() throws Exception {
//        _services = T_Services.mockServices();
//        _room = MockModel.mockRoom("1");
//        _services.getPreferences().delete(_room.getGame().getAbbr());
//        _services.setProfile(MockModel.mockProfile());
//        _services.setDatabase(new MockDB(){
//            @Override
//            public void getGameByAbbr(String abbr, DatabaseListener<Game> listener) {
//                listener.onCallback(_room.getGame(), Status.SUCCESS);
//            }
//        });
//    }
//
//    @Test
//    public void testRoomLogicScene(){
//        setPreferenceHasGame();
//        RoomLogic logic = new RoomLogic(Mockings.mockPTScreen(), _services, _room, false);
//        logic.onShow();
//        RoomScene scene = (RoomScene) logic.getScene();
//        Assert.assertEquals(true, ((Table) scene.getRoot()).hasChildren());
//    }
//
//    @Test
//    public void testDownloadGame(){
//        RoomLogic logic = Mockito.spy(new RoomLogic(Mockings.mockPTScreen(), _services, _room, false));
//        _services.setDownloader(new IDownloader() {
//            @Override
//            public SafeThread downloadFileToPath(String urlString, File targetFile, DownloaderListener listener) {
//                int percent = 0;
//                while (percent < 100){
//                    percent+=25;
//                    listener.onStep(percent);
//                }
//                listener.onCallback(null, Status.SUCCESS);
//                return new SafeThread();
//            }
//
//            @Override
//            public void downloadData(String url, DownloaderListener listener) {
//
//            }
//        });
//        logic.onInit();
//        Threadings.sleep(4000);
//        verify(logic, atLeast(2)).sendUpdateRoomMates(eq(UpdateRoomMatesCode.UPDATE_DOWNLOAD), anyString());
//        verify(logic, times(1)).sendUpdateRoomMates(eq(UpdateRoomMatesCode.UPDATE_DOWNLOAD), eq(String.valueOf(100)));
//
//
//
//    }
//
//    @Test
//    public void testStartGameCheck(){
//        RoomLogic logic = Mockito.spy(new RoomLogic(Mockings.mockPTScreen(), _services, _room, false));
//        logic.onInit();
//
//        Assert.assertEquals(1, logic.startGameCheck(true));
//
//        Profile user2 = MockModel.mockProfile("99");
//        _room.addRoomUser(user2, true);
//        _room.changeTeam(1, user2);
//        _room.getGame().setTeamMinPlayers("1");
//        _room.getGame().setTeamCount("2");
//
//        _services.getGamingKit().onUpdateRoomMatesReceived(UpdateRoomMatesCode.UPDATE_DOWNLOAD, "50", _services.getProfile().getUserId());
//        Threadings.sleep(500);
//        verify(logic, times(1)).receivedUpdateRoomMates(eq(UpdateRoomMatesCode.UPDATE_DOWNLOAD), eq("50"), eq(_services.getProfile().getUserId()));
//        Assert.assertEquals(2, logic.startGameCheck(true));
//        _services.getGamingKit().onUpdateRoomMatesReceived(UpdateRoomMatesCode.UPDATE_DOWNLOAD, "100", _services.getProfile().getUserId());
//        Threadings.sleep(500);
//        verify(logic, times(1)).receivedUpdateRoomMates(eq(UpdateRoomMatesCode.UPDATE_DOWNLOAD), eq("100"), eq(_services.getProfile().getUserId()));
//        Assert.assertEquals(0, logic.startGameCheck(true));
//    }
//
//    @Test
//    public void testHostLeft(){
//
//        _services.setDatabase(new MockDB(){
//            @Override
//            public void monitorRoomById(String id, String classTag, DatabaseListener<Room> listener) {
//                super.monitorRoomById(id, classTag, listener);
//                _room.getRoomUsersMap().remove(_room.getHost().getUserId());
//                listener.onCallback(_room, Status.SUCCESS);
//            }
//        });
//
//        RoomLogic logic = Mockito.spy(new RoomLogic(Mockings.mockPTScreen(), _services, _room, false));
//        logic.onInit();
//        verify(logic, times(1)).checkHostInRoom();
//        Assert.assertEquals(false, logic.checkHostInRoom());
//    }
//
//    @Test
//    public void testJoinRoom(){
//        _room.getRoomUsersMap().clear();
//
//        _room.addRoomUser(MockModel.mockProfile("1"), true);      //0
//        _room.addRoomUser(MockModel.mockProfile("2"), true);      //1
//        _room.addRoomUser(MockModel.mockProfile("3"), 3, true);       //3
//        _room.addRoomUser(MockModel.mockProfile("4"), true);      //2
//
//        Assert.assertEquals(4, _room.getRoomUsersCount());
//        Assert.assertEquals(0, _room.getSlotIndexByUserId(MockModel.mockProfile("1").getUserId()));
//        Assert.assertEquals(1, _room.getSlotIndexByUserId(MockModel.mockProfile("2").getUserId()));
//        Assert.assertEquals(2, _room.getSlotIndexByUserId(MockModel.mockProfile("4").getUserId()));
//        Assert.assertEquals(3, _room.getSlotIndexByUserId(MockModel.mockProfile("3").getUserId()));
//
//    }
//
//    @Test
//    public void testGameStartCancel(){
//
//        MockGamingKit gamingKit = mock(MockGamingKit.class);
//        _services.setGamingKit(gamingKit);
//        _room.getGame().setTeamMinPlayers("1");
//        _room.getGame().setMinPlayers("2");
//        _room.getGame().setTeamCount("2");
//        _room.getGame().setTeamMaxPlayers("1");
//        _room.getRoomUsersMap().get("another").setSlotIndex(1);
//        _room.convertRoomUsersToTeams();
//        Assert.assertEquals(2, _room.getTeams().size());
//        Assert.assertEquals(1, _room.getTeams().get(0).getPlayers().size());
//        Assert.assertEquals(1, _room.getTeams().get(1).getPlayers().size());
//        RoomLogic logic = Mockito.spy(new RoomLogic(Mockings.mockPTScreen(), _services, _room, false));
//        logic.onInit();
//        logic.hostSendStartGame();
//
//        verify(gamingKit, times(1)).updateRoomMates(eq(UpdateRoomMatesCode.START_GAME), anyString());
//
//        logic.receivedUpdateRoomMates(UpdateRoomMatesCode.START_GAME, "", "");
//        logic.stopGameStartCountDown(_room.getRoomUsersMap().get("another").getProfile());
//        Threadings.sleep(1600);
//        verify(logic, times(0)).gameStarted();
//
//        logic.receivedUpdateRoomMates(UpdateRoomMatesCode.START_GAME, "", "");
//        Threadings.sleep(5000);
//        verify(logic, times(0)).gameStarted();
//        verify(logic, times(1)).sendUpdateRoomMates(eq(UpdateRoomMatesCode.GAME_STARTED), anyString());
//
//        logic.receivedUpdateRoomMates(UpdateRoomMatesCode.GAME_STARTED, "", "");
//        Threadings.sleep(1000);
//        verify(logic, times(1)).gameStarted();
//
//    }
//
//    @Test
//    public void testGameStartOrEndParameters(){
//        IDatabase database = Mockito.spy(new MockDB(){
//            @Override
//            public void monitorRoomById(String id, String classTag, DatabaseListener<Room> listener) {
//                super.monitorRoomById(id, classTag, listener);
//                listener.onCallback(_room, Status.SUCCESS);
//            }
//        });
//
//        _services.setDatabase(database);
//        RoomLogic logic = Mockito.spy(new RoomLogic(Mockings.mockPTScreen(), _services, _room, false));
//        logic.onInit();
//
//        Assert.assertEquals(0, _room.getRoundCounter());
//
//        Profile user2 = MockModel.mockProfile("another");
//        _room.changeTeam(1, user2);
//        _room.getGame().setTeamMinPlayers("1");
//        _room.getGame().setTeamCount("2");
//
//        logic.startGameCountDown();
//        Threadings.sleep(7100);
//
//        Assert.assertEquals(true, _room.isPlaying());
//        Assert.assertEquals(false, _room.isOpen());
//        Assert.assertEquals(1, _room.getRoundCounter());
//        verify(logic, times(1)).gameStarted();
//        verify(database, Mockito.times(2)).saveRoom(any(Room.class), eq(true), any(DatabaseListener.class));
//        verify(database, times(1)).savePlayedHistory(any(Profile.class), any(Room.class), any(DatabaseListener.class));
//
//        logic.onShow();
//        Assert.assertEquals(false, _room.isPlaying());
//        Assert.assertEquals(true, _room.isOpen());
//        Assert.assertEquals(1, _room.getRoundCounter());
//        verify(database, times(1)).saveRoom(any(Room.class), eq(false), any(DatabaseListener.class));
//        verify(database, times(3)).saveRoom(any(Room.class), eq(true), any(DatabaseListener.class));
//    }
//
//    @Test
//    public void testPushNotificationSent(){
//        GCMSender gcmSender = mock(GCMSender.class);
//        _services.setGcmSender(gcmSender);
//        RoomLogic logic = Mockito.spy(new RoomLogic(Mockings.mockPTScreen(), _services, _room, false));
//        _room.getRoomUsersMap().remove("another");
//        _room.addRoomUser(MockModel.mockProfile("another"), 1, true);
//
//        _services.setProfile(_room.getProfileByUserId("123"));
//
//        logic.selfUpdateRoomStatePush();
//        verify(gcmSender, times(0)).send(eq(_room.getProfileByUserId("another")), any(PushNotification.class));
//        verify(gcmSender, times(1)).send(eq(_room.getProfileByUserId("123")), any(PushNotification.class));
//
//        gcmSender = mock(GCMSender.class);
//        _services.setGcmSender(gcmSender);
//        logic.gameStarted();
//        verify(gcmSender, times(1)).send(eq(_room.getProfileByUserId("another")), any(PushNotification.class));
//        verify(gcmSender, times(1)).send(eq(_room.getProfileByUserId("123")), any(PushNotification.class));
//    }
//
//    @Test
//    public void testContinueGame(){
//        GamingKit mockKit = Mockito.spy(new MockGamingKit());
//        _services.setGamingKit(mockKit);
//        PTScreen screen = Mockings.mockPTScreen();
//
//        _room.setOpen(false);
//        _room.setPlaying(true);
//
//        RoomLogic logic = Mockito.spy(new RoomLogic(screen, _services, _room, true));
//        logic.onInit();
//        logic.onShow();
//        verify(logic, times(0)).checkHostInRoom();
//        verify(logic, times(1)).continueGame();
//        verify(logic, times(0)).gameStarted();
//
//        Threadings.sleep(1000);
//
//        verify(screen, times(1)).toScene(eq(SceneEnum.GAME_SANDBOX), any(Room.class), eq(true));
//        Assert.assertEquals(false, _room.isOpen());
//
//    }
//
//
//
//    @Test
//    public void testHostLeaveRoomParameters(){
//
//        GamingKit mockKit = Mockito.spy(new MockGamingKit());
//        _services.setGamingKit(mockKit);
//        RoomLogic logic = Mockito.spy(new RoomLogic(Mockings.mockPTScreen(), _services, _room, false));
//        logic.onShow();
//
//        logic.leaveRoom();
//
//        verify(mockKit, times(1)).leaveRoom();
//        Assert.assertEquals(false, _room.isOpen());
//        Assert.assertEquals(-1, _room.getSlotIndexByUserId(_services.getProfile().getUserId()));
//
//    }
//
//
//    @Test
//    public void testUserLeaveRoomParameters(){
//
//        GamingKit mockKit = Mockito.spy(new MockGamingKit());
//        _room.setOpen(true);
//        _services.setGamingKit(mockKit);
//        _services.setProfile(MockModel.mockProfile("another"));
//        RoomLogic logic = Mockito.spy(new RoomLogic(Mockings.mockPTScreen(), _services, _room, false));
//        logic.onShow();
//
//        logic.leaveRoom();
//
//        verify(mockKit, times(1)).leaveRoom();
//        verify(mockKit, times(1)).updateRoomMates(eq(UpdateRoomMatesCode.LEFT_ROOM), eq(""));
//        Assert.assertEquals(true, _room.isOpen());
//    }
//
//
//    private void setPreferenceHasGame(){
//        _services.getPreferences().put(_room.getGame().getAbbr(), _room.getGame().getVersion());
//
//    }
//
//    @Test
//    public void testUserReady(){
//        final Profile profile = MockModel.mockProfile();
//        final boolean[] waiting = {true};
//        GamingKit mockKit = Mockito.spy(new MockGamingKit());
//        _room.setOpen(true);
//        _services.setGamingKit(mockKit);
//        _services.setProfile(MockModel.mockProfile());
//        _room.getRoomUserByUserId(profile.getUserId()).setReady(false);
//        Assert.assertEquals(false, _room.getRoomUserByUserId(profile.getUserId()).getReady());
//
//        RoomLogic logic = Mockito.spy(new RoomLogic(Mockings.mockPTScreen(), _services, _room, false));
//        logic.onInit();
//        logic.onShow();
//
//        MockDB mockDB = new MockDB(){
//            @Override
//            public void saveRoom(Room room, boolean notify, @Nullable DatabaseListener<String> listener) {
//                waiting[0] = false;
//                Assert.assertEquals(true, room.getRoomUserByUserId(profile.getUserId()).getReady());
//            }
//        };
//        _services.setDatabase(mockDB);
//        logic.onShow();
//
//        while (waiting[0]){
//            Threadings.sleep(100);
//        }
//
//        waiting[0] = true;
//        _room.getRoomUserByUserId("another").setReady(true);
//        Assert.assertEquals(true, _room.getRoomUserByUserId(profile.getUserId()).getReady());
//
//        MockDB mockDB2 = new MockDB(){
//            @Override
//            public void saveRoom(Room room, boolean notify, @Nullable DatabaseListener<String> listener) {
//                waiting[0] = false;
//                Assert.assertEquals(false, room.getRoomUserByUserId(profile.getUserId()).getReady());
//            }
//        };
//        _services.setDatabase(mockDB2);
//        logic.onHide();
//
//        while (waiting[0]){
//            Threadings.sleep(100);
//        }
//
//    }



}
