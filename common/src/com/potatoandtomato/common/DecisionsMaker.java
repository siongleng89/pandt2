package com.potatoandtomato.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by SiongLeng on 2/2/2016.
 */
public class DecisionsMaker {

    private ArrayList<String> _userIDs;

    public DecisionsMaker(ArrayList<Team> teams) {
        _userIDs = new ArrayList<String>();
        teamsChanged(teams);
    }

    public void teamsChanged(ArrayList<Team> teams){
        _userIDs.clear();
        for(Team team : teams){
            for(Player player : team.getPlayers()){
                if(player.getIsConnected()){
                    _userIDs.add(player.getUserId());
                }
            }
        }
        Collections.sort(_userIDs);
    }

    public boolean checkIsDecisionMaker(String userId){
        if(_userIDs.size() > 0){
            return _userIDs.get(0).equals(userId);
        }
        return false;
    }

}
