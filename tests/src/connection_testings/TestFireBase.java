package connection_testings;

import abstracts.TestAbstract;
import com.firebase.client.Firebase;
import com.mygdx.potatoandtomato.absintflis.databases.DatabaseListener;
import com.mygdx.potatoandtomato.absintflis.databases.IDatabase;
import com.mygdx.potatoandtomato.absintflis.databases.SpecialDatabaseListener;
import com.mygdx.potatoandtomato.helpers.services.FirebaseDB;
import com.mygdx.potatoandtomato.helpers.utils.Threadings;
import com.mygdx.potatoandtomato.models.Profile;
import com.mygdx.potatoandtomato.models.Game;
import com.mygdx.potatoandtomato.models.Room;
import com.mygdx.potatoandtomato.models.RoomUser;
import helpers.MockModel;
import helpers.T_Threadings;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by SiongLeng on 9/12/2015.
 */
public class TestFireBase extends TestAbstract {

    private String _unitTestUrl = "https://forunittest.firebaseio.com";

    @Test
    public void testConnectFirebase(){
        final boolean[] waiting = {true};
        IDatabase databases = new FirebaseDB(_unitTestUrl);
        databases.getTestTableCount(new DatabaseListener<Integer>() {
            @Override
            public void onCallback(Integer obj, Status st) {
                waiting[0] = false;
                Assert.assertEquals(Status.SUCCESS, st);
                Assert.assertEquals(true, obj > 0);
            }
        });

        while(waiting[0]){
            T_Threadings.sleep(100);
        }
    }

    @Test
    public void testCreateUser(){
        final boolean[] waiting = {true};
        final IDatabase databases = new FirebaseDB(_unitTestUrl);
        databases.loginAnonymous(new DatabaseListener<Profile>() {
            @Override
            public void onCallback(Profile obj, Status st) {
                Assert.assertEquals(false, st == Status.FAILED);
                Assert.assertEquals(false, obj == null);
                databases.createUserByUserId(obj.getUserId(), new DatabaseListener<Profile>() {
                    @Override
                    public void onCallback(Profile obj, Status st) {
                        Assert.assertEquals(false, st == Status.FAILED);
                        Assert.assertEquals(false, obj == null);
                        databases.getProfileByUserId(obj.getUserId(), new DatabaseListener<Profile>(Profile.class) {
                            @Override
                            public void onCallback(Profile obj, Status st) {
                                Assert.assertEquals(false, st == Status.FAILED);
                                Assert.assertEquals(false, obj == null);
                                Assert.assertEquals(false, obj.getUserId() == null);
                                waiting[0] = false;
                            }
                        });
                    }
                });

            }
        });

        while(waiting[0]){
            T_Threadings.sleep(100);
        }
    }

    @Test
    public void testGetAllGames(){
        final boolean[] waiting = {true};
        final IDatabase databases = new FirebaseDB(_unitTestUrl);
        databases.getAllGames(new DatabaseListener<ArrayList<Game>>(Game.class) {
            @Override
            public void onCallback(ArrayList<Game> obj, Status st) {
                Assert.assertEquals(false, st == Status.FAILED);
                Assert.assertEquals(false, obj == null);
                waiting[0] = false;
            }
        });

        while(waiting[0]){
            T_Threadings.sleep(100);
        }
    }


    @Test
    public void testSave_MonitorSingleRoom_OnDisconnectRoom(){

        final int[] monitorCount = {0};
        final IDatabase databases = new FirebaseDB(_unitTestUrl);
        final boolean[] waiting = {true};

        //create room
        final Room r = MockModel.mockRoom(null);

        Assert.assertEquals(true, r.getId() == null);


        databases.saveRoom(r, new DatabaseListener<String>() {
            @Override
            public void onCallback(String obj, Status st) {
                Assert.assertEquals(true, st == Status.SUCCESS);
                waiting[0] = false;
            }
        });

        Assert.assertEquals(false, r.getId() == null);

        while(waiting[0]){
            T_Threadings.sleep(100);
        }

        //create end

        waiting[0] = true;

        databases.monitorRoomById(r.getId(), new DatabaseListener<Room>(Room.class) {
            @Override
            public void onCallback(Room obj, Status st) {
                Assert.assertEquals(true, st == Status.SUCCESS);
                Assert.assertEquals(r.getId(), obj.getId());

                Assert.assertTrue(EqualsBuilder.reflectionEquals(obj.getGame(), r.getGame()));
                Assert.assertTrue(EqualsBuilder.reflectionEquals(obj.getHost(), r.getHost()));
                Assert.assertEquals(obj.getRoomUsers().size(), r.getRoomUsers().size());
                for (Map.Entry<String, RoomUser> entry : obj.getRoomUsers().entrySet()) {
                    String key = entry.getKey();
                    RoomUser user1 = obj.getRoomUsers().get(key);
                    RoomUser user2 = r.getRoomUsers().get(key);

                    Assert.assertTrue(EqualsBuilder.reflectionEquals(user1.getProfile(), user2.getProfile()));
                    Assert.assertEquals(user1.getSlotIndex(), user2.getSlotIndex());
                }
                Assert.assertEquals(r.getRoomId(), obj.getRoomId());
                Assert.assertEquals(r.getRoundCounter(), obj.getRoundCounter());
                Assert.assertEquals(r.isOpen(), obj.isOpen());
                Assert.assertEquals(r.isPlaying(), obj.isPlaying());
                monitorCount[0]++;
                waiting[0] = false;
            }
        });

        while(waiting[0]){
            T_Threadings.sleep(100);
        }


        //update room
        waiting[0] = true;
        r.setRoomId("999");
        databases.saveRoom(r, new DatabaseListener<String>() {
            @Override
            public void onCallback(String obj, Status st) {
                Assert.assertEquals(true, st == Status.SUCCESS);
                waiting[0] = false;
            }
        });
        while(waiting[0]){
            T_Threadings.sleep(100);
        }
        //update end

        Assert.assertEquals(2, monitorCount[0]);

        T_Threadings.sleep(500);
        //disconnected
        waiting[0] = true;
        databases.removeUserFromRoomOnDisconnect(r, r.getHost(), new DatabaseListener<String>() {
            @Override
            public void onCallback(String obj, Status st) {
                Assert.assertEquals(st, Status.SUCCESS);
                databases.offline();
                r.getRoomUsers().remove(r.getHost().getUserId());
                r.setOpen(false);
                databases.online();
            }
        });

        while(waiting[0]){
            T_Threadings.sleep(100);
        }

        Assert.assertEquals(3, monitorCount[0]);

        waiting[0] = true;

        r.getRoomUsers().get("another").setSlotIndex(20);
        databases.saveRoom(r, new DatabaseListener<String>() {
            @Override
            public void onCallback(String obj, Status st) {

            }
        });

        while(waiting[0]){
            T_Threadings.sleep(100);
        }
        Assert.assertEquals(4, monitorCount[0]);
    }


    @Test
    public void TestMonitorAllRoom(){
        final int[] monitorCount = {0};
        final IDatabase databases = new FirebaseDB(_unitTestUrl);
        final boolean[] waiting = {true};
        ArrayList<Room> rooms = new ArrayList<>();

        final Room r = MockModel.mockRoom(null);
        r.setOpen(true);

        databases.saveRoom(r, new DatabaseListener<String>() {
            @Override
            public void onCallback(String obj, Status st) {
                waiting[0] = false;
            }
        });

        while(waiting[0]){
            T_Threadings.sleep(100);
        }


        waiting[0] = true;
        databases.monitorAllRooms(rooms, new SpecialDatabaseListener<ArrayList<Room>, Room>() {
            @Override
            public void onCallbackTypeOne(ArrayList<Room> obj, Status st) {
                Assert.assertEquals(st , Status.SUCCESS);
                Assert.assertEquals(true , obj.size()>0);
                waiting[0] = false;
            }

            @Override
            public void onCallbackTypeTwo(Room obj, Status st) {
                Assert.assertEquals(st , Status.SUCCESS);
                monitorCount[0]++;
                waiting[0] = false;
            }
        });

        while(waiting[0]){
            T_Threadings.sleep(100);
        }

        Assert.assertEquals(true , rooms.size()>0);

        waiting[0] =true;
        r.setOpen(false);

        databases.saveRoom(r, new DatabaseListener<String>() {
            @Override
            public void onCallback(String obj, Status st) {

            }
        });

        while(waiting[0]){
            T_Threadings.sleep(100);
        }

        Assert.assertEquals(1, monitorCount[0]);

        for(Room r1 : rooms){
            if(r1.getId().equals(r.getId())){
                Assert.assertEquals(r1.isOpen(), r.isOpen());
            }
        }
    }

}
