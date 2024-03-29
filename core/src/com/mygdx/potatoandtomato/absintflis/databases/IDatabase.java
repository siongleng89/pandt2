package com.mygdx.potatoandtomato.absintflis.databases;

import com.firebase.client.Firebase;
import com.firebase.client.annotations.Nullable;
import com.mygdx.potatoandtomato.enums.RoomUserState;
import com.mygdx.potatoandtomato.models.*;
import com.mygdx.potatoandtomato.services.FirebaseDB;
import com.potatoandtomato.common.models.LeaderboardRecord;
import com.potatoandtomato.common.models.Streak;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SiongLeng on 9/12/2015.
 */
public interface IDatabase {

     void saveLog(String msg);

     void authenticateUserByToken(String token, DatabaseListener<Profile> listener);

     void getProfileByGameNameLower(String gameName, DatabaseListener<Profile> listener);

     void monitorProfileByUserId(String userId, String classTag, DatabaseListener<Profile> listener);

     void getProfileByUserId(String userId, DatabaseListener<Profile> listener);

     void getUsernameByUserId(String userId, DatabaseListener<String> listener);

     void getUsernamesByUserIds(ArrayList<String> userIds, DatabaseListener<HashMap<String, String>> listener);

     void getUserCountryByUserId(String userId, DatabaseListener<String> listener);

     void getUserCountryByUserIds(ArrayList<String> userIds, DatabaseListener<HashMap<String, String>> listener);

     void getProfileByFacebookUserId(String facebookUserId, DatabaseListener<Profile> listener);

     void monitorUserCoinsCount(String userId, DatabaseListener<Integer> listener);

     void signCoinDecreaseAgreement(String userId, String transactionId, DatabaseListener listener);

     void updateProfile(Profile profile, DatabaseListener listener);

     void getAllGamesSimple(DatabaseListener<ArrayList<Game>> listener);

     ///////////////all about iab//////////////////////////
     void getAllProducts(DatabaseListener<ArrayList<CoinProduct>> listener);

     ///////////////all about rooms/////////////////////
     void updateRoomPlayingAndOpenState(Room room, Boolean isPlaying, Boolean isOpen, @Nullable DatabaseListener<String> listener);

     void saveRoom(Room room, boolean notify, @Nullable DatabaseListener<String> listener);    //except slot index

     void setOnDisconnectCloseRoom(Room room);

     void setInvitedUsers(ArrayList<String> invitedUserIds, Room room, DatabaseListener listener);

     void addUserToRoom(Room room, Profile user, int slotIndex, RoomUserState roomUserState, DatabaseListener<String> listener);

     void removeUserFromRoom(Room room, Profile user, DatabaseListener listener);

     void monitorRoomById(String id, String classTag, DatabaseListener<Room> listener);

     void setRoomUserState(Room room, String userId, RoomUserState roomUserState, DatabaseListener listener);

     void setRoomUserSlotIndex(Room room, String userId, int slotIndex, DatabaseListener listener);

     void getRoomById(String id, DatabaseListener<Room> listener);

     void monitorAllRooms(ArrayList<Room> rooms, String classTag, SpecialDatabaseListener<ArrayList<Room>, Room> listener);

     void monitorRoomInvitations(String roomId, String classTag, DatabaseListener listener);

     void checkRoomInvitationResponseExist(String roomId, String userId, DatabaseListener<Boolean> listener);

     void getPendingInvitationRoomIds(Profile profile, DatabaseListener<ArrayList<String>> listener);

     String notifyRoomChanged(Room room);

     ////////////////////////////////////

     void unauth();

     void offline();

     void online();

     void clearListenersByTag(String tag);

     void clearAllListeners();

     void clearAllOnDisconnectListenerModel();

     void savePlayedHistory(Profile profile, Room room, DatabaseListener<String> listener);

     void getPlayedHistories(Profile profile, DatabaseListener<ArrayList<GameHistory>> listener);

     void getGameByAbbr(String abbr, DatabaseListener<Game> listener);

     void getGameSimpleByAbbr(String abbr, DatabaseListener<Game> listener);

     Object getGameBelongDatabase(String abbr);

     void getTeamStreak(Game game, ArrayList<String> userIds, DatabaseListener<Streak> listener);

     void resetUserStreak(Game game, String userId, DatabaseListener listener);

     void getLeaderBoardAndStreak(Game game, int expectedCount, DatabaseListener<ArrayList<LeaderboardRecord>> listener);

     void getTeamHighestLeaderBoardRecordAndStreak(Game game, ArrayList<String> teamUserIds, DatabaseListener<LeaderboardRecord> listener);

     void getUserHighestLeaderBoardRecordAndStreak(Game game, String userId, DatabaseListener<LeaderboardRecord> listener);

     void getLeaderBoardRecordAndStreakById(Game game, String leaderboardId, DatabaseListener<LeaderboardRecord> listener);

     void deleteLeaderBoard(Game game, DatabaseListener listener);

     void checkScoreUpdated(Room room, DatabaseListener<Boolean> listener);

     void getAllInboxMessage(String userId, DatabaseListener<ArrayList<InboxMessage>> listener);

     void inboxMessageRead(String userId, String inboxMessageId, DatabaseListener listener);

     void checkFeedbackExist(String userId, DatabaseListener<Boolean> listener);

     void sendFeedback(String userId, RateAppsModel rateAppsModel, DatabaseListener listener);

     void getRewardVideoCoinCount(DatabaseListener<Integer> listener);

}
