package com.potatoandtomato.games.services;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by SiongLeng on 30/12/2015.
 */
public class Texts {

    public String yourTurn() {
        return "Your Turn";
    }

    public String enemyTurn() {
        return "Enemy Turn";
    }

    public String graveYard() {
        return "Graveyard";
    }

    public String tutorial() {
        return "Tutorial";
    }

    public String gameStart() {
        return "Game Start";
    }

    public String youWin() {
        return "You Win";
    }

    public String youLose() {
        return "You Lose";
    }

    public String you() {
        return "You";
    }

    public String enemy() {
        return "Enemy";
    }

    public String vs() {
        return "vs";
    }

    public String timeToCatchUp() {
        return "Time to catch up";
    }

    public String xWinStreak() {
        return "%s steps towards jungle throne";
    }

    public String breakEnemyXWinStreak() {
        return "Stop opponent %s steps towards jungle throne";
    }

    public String easyWin() {
        return "Walk in the park";
    }

    //public String hardFoughtWin() { return "Almost lose"; }
    public String normalWin() {
        return "So much win!";
    }

    public String firstTimeWinPlayer() {
        return "First win against this opponent, Roar!";
    }

    public String pawnLeaderboardPlayer() {
        return "Bring down Jungle King contender";
    }

    public String beatBot() {
        return "Bot killer";
    }

    ////////////////////////////////////////////////
    //tutorial texts
    ////////////////////////////////////////////////

    public String tutorialAboutFoodChain() {
        return "Hello, you jungle king, thiz the guide to eatzing. Circle indicates which animal can eat other animals morez easily. ";
    }

    public String tutorialAboutSwipeOpen() {
        return "You open, you swipez";
    }

    public String tutorialAboutDrag() {
        return "You attacksss, dragz towards the chess, % tellsz you what is yourz chance of winning the battlesss";
    }

    public String tutorialAboutPoison() {
        return "Poisoned. Attack reducedz";
    }

    public String tutorialAboutAttackUp() {
        return "Attack upssss";
    }

    public String tutorialAboutAttackDown() {
        return "Attack downssss";
    }

    public String tutorialAboutParalyzed() {
        return "Paralyzed. No move";
    }

    public String tutorialAboutKing() {
        return "King of the species. Rare. Move 2 squareszz attack up up up";
    }

    public String tutorialAboutAggressive() {
        return "Aggressive. Appearz after Turn 70. End the game alreadz or forest smaller. All animals move 2 squares and attacker��s attack go up";
    }

    public String tutorialEnd(){
        return "The End";
    }


    ////////////////////////////////
    //about on screen tutorials
    ///////////////////////////////
    public String onScreenTutorialDragToMove(){
        return "Drag to move and attack other animals";
    }

    public String onScreenTutorialSeeMore(){
        return "See here for more";
    }

    public String onScreenTutorialSwipeToOpen(){
        return "Swipe to open";
    }

    public String tutorialAboutGameMsg(){
        return "Welcome to Behind You a Jungle! The objective of this game is to flip chess pieces, drag them to move and battle, and be the last one standing!";
    }

    //////////////////////////////////////////
    //bot name
    //////////////////////////////////////////
    public String getRandomBotName(){
        ArrayList<String> nameList = new ArrayList();

        nameList.add("Ali Bot");

        Collections.shuffle(nameList);
        return nameList.get(0);
    }

}