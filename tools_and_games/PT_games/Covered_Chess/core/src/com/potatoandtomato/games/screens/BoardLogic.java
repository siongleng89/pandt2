package com.potatoandtomato.games.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.potatoandtomato.common.*;
import com.potatoandtomato.common.Threadings;
import com.potatoandtomato.games.absint.ActionListener;
import com.potatoandtomato.games.absint.DatabaseListener;
import com.potatoandtomato.games.assets.Sounds;
import com.potatoandtomato.games.enums.ActionType;
import com.potatoandtomato.games.enums.ChessColor;
import com.potatoandtomato.games.enums.ChessType;
import com.potatoandtomato.games.helpers.*;
import com.potatoandtomato.games.models.*;
import com.potatoandtomato.games.references.StatusRef;
import com.potatoandtomato.games.references.BattleRef;
import com.potatoandtomato.games.references.MovementRef;
import com.shaded.fasterxml.jackson.core.JsonParseException;
import com.shaded.fasterxml.jackson.databind.JsonMappingException;
import com.shaded.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy;

/**
 * Created by SiongLeng on 29/12/2015.
 */
public class BoardLogic {

    Services _services;
    GameCoordinator _coordinator;
    ArrayList<TerrainLogic> _terrains;
    GraveyardLogic _graveyard;
    BoardModel _boardModel;
    BattleRef _battleRef;
    MovementRef _movementRef;
    StatusRef _statusRef;

    BoardScreen _screen;
    GameDataController _gameDataController;
    TerrainLogic _lastActiveTerrainLogic;
    SplashLogic _splashLogic;
    RoomMsgHandler _roomMsgHandler;
    SafeThread _getGameDataSafeThread;

    public BoardLogic(Services services, GameCoordinator coordinator) {
        this._services = services;
        this._coordinator = coordinator;
        _battleRef = new BattleRef();
        _movementRef = new MovementRef();
        _statusRef = new StatusRef(services.getSoundsWrapper());

        _boardModel = new BoardModel(-1);
        _terrains = new ArrayList<TerrainLogic>();
        _gameDataController = new GameDataController(coordinator);
        _graveyard = new GraveyardLogic(new GraveModel(),
                coordinator, services.getTexts(), services.getAssets(), services.getSoundsWrapper());
        _roomMsgHandler = new RoomMsgHandler(this, _coordinator);
        _splashLogic = new SplashLogic(coordinator, _services);

        _screen = new BoardScreen(coordinator, services, _splashLogic.getSplashActor(), _graveyard.getGraveyardActor());

        setListeners();
    }

    //call when new game
    public void init(){
        if(_coordinator.meIsDecisionMaker()){
            _boardModel.setCurrentTurnIndex(_gameDataController.getFirstTurnIndex());
            gameDataReady(_boardModel, _gameDataController.getGameData(), _graveyard.getGraveModel());
            saveGameDataToDB();
        }
        else{
           getGameDataFromDB();
        }
    }

    public void continueGame(){
        getGameDataFromDB();
    }

    private void saveGameDataToDB(){
        JSONObject jsonObject = new JSONObject();
        try {
            int i = 0;
            for(TerrainLogic logic : _terrains){
                jsonObject.put(String.valueOf(i), logic.getChessLogic().getChessModel().toJson());
                i++;
            }
            jsonObject.put("graveModel", _graveyard.getGraveModel().toJson());
            jsonObject.put("boardModel", _boardModel.toJson());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        _services.getDatabase().saveGameData(jsonObject.toString());

    }

    private void getGameDataFromDB(){
        _getGameDataSafeThread = new SafeThread();
        Threadings.delay(2000, new Runnable() {
            @Override
            public void run() {
                Threadings.runInBackground(new Runnable() {
                    @Override
                    public void run() {
                        final String[] json = new String[1];
                        while (!_getGameDataSafeThread.isKilled()){
                            _services.getDatabase().getGameData(new DatabaseListener<String>(String.class) {
                                @Override
                                public void onCallback(String result, Status st) {
                                    if(st == Status.SUCCESS && result != null && !result.equals("")){
                                        json[0] = result;
                                        _getGameDataSafeThread.kill();
                                    }
                                }
                            });
                            int i = 0;
                            while (!_getGameDataSafeThread.isKilled() && i < 5){
                                Threadings.sleep(500);
                                i++;
                            }
                        }
                        try {
                            ArrayList<ChessModel> chessModels = new ArrayList<ChessModel>();
                            JSONObject jsonObject = new JSONObject(json[0]);
                            ObjectMapper mapper1 = new ObjectMapper();
                            for(int i = 0; i < 32; i++){
                                chessModels.add(mapper1.readValue(jsonObject.getString(String.valueOf(i)), ChessModel.class));
                            }
                            GraveModel graveModel = mapper1.readValue(jsonObject.getString("graveModel"), GraveModel.class);
                            BoardModel boardModel = mapper1.readValue(jsonObject.getString("boardModel"), BoardModel.class);

                            gameDataReady(boardModel, chessModels, graveModel);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (JsonMappingException e) {
                            e.printStackTrace();
                        } catch (JsonParseException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });

    }


    private void gameDataReady(final BoardModel boardModel, ArrayList<ChessModel> chessModels, GraveModel graveModel){

        _boardModel = boardModel;
        invalidate();
        _graveyard.setGraveModel(graveModel);

        int i = 0;
        for(int row = 0; row < 8 ; row++){
            for(int col = 0; col < 4; col++){
                final ChessModel chessModel = chessModels.get(i);
                TerrainLogic terrainLogic = new TerrainLogic(new TerrainModel(col, row),
                        _services.getAssets(), _coordinator, chessModel,
                        _services.getSoundsWrapper(), _gameDataController, _battleRef);
                terrainLogic.setActionListener(new ActionListener() {
                    @Override
                    public void onSelected() {
                        if(!this.getTerrainLogic().isSelected()){
                            terrainSelected(this.getTerrainLogic().getTerrainModel().getCol(),
                                    this.getTerrainLogic().getTerrainModel().getRow());
                            _roomMsgHandler.sendTerrainSelected(this.getTerrainLogic().getTerrainModel().getCol(),
                                    this.getTerrainLogic().getTerrainModel().getRow());
                        }
                    }

                    @Override
                    public void onOpened() {
                        openChess(this.getTerrainLogic().getTerrainModel().getCol(),
                                this.getTerrainLogic().getTerrainModel().getRow());
                        _roomMsgHandler.sendChessOpenFull(this.getTerrainLogic().getTerrainModel().getCol(),
                                this.getTerrainLogic().getTerrainModel().getRow());
                    }

                    @Override
                    public void onMoved(int fromCol, int fromRow, int toCol, int toRow, boolean isFromWon) {
                        boolean random = (new Random()).nextBoolean();
                        chessMoved(fromCol, fromRow, toCol, toRow, isFromWon, false, random);
                        _roomMsgHandler.sendMoveChess(fromCol, fromRow, toCol, toRow, isFromWon, random);
                    }

                    @Override
                    public void changeTurnReady(ActionType actionType, ChessType winnerChessType, ChessType loserChessType, boolean random) {
                        preTurnSwitched(actionType, this.getTerrainLogic(), winnerChessType, loserChessType, random);
                    }

                    @Override
                    public void onChessKilled(ChessType chessType) {
                        chessKilled(chessType);
                    }
                });
                _terrains.add(terrainLogic);
                i++;
            }
        }

        _screen.populateTerrains(_terrains);
        setTurnTouchable();
        _roomMsgHandler.onGameReady();

    }

    private void chessKilled(ChessType chessType){
        _graveyard.addChessToGrave(chessType);
        if(_graveyard.getGraveModel().getLeftChessCountByColor(ChessColor.RED) == 0){
            endGame(ChessColor.YELLOW);
        }
        else if(_graveyard.getGraveModel().getLeftChessCountByColor(ChessColor.YELLOW) == 0){
            endGame(ChessColor.RED);
        }
    }

    public void openChess(int col, int row){
        TerrainLogic openLogic =  Terrains.getTerrainLogicByPosition(_terrains, col, row);
        _lastActiveTerrainLogic = openLogic;
        openLogic.openTerrainChess();
    }

    //random variable is for decision making(eg, injured/king)
    public void chessMoved(int fromCol, int fromRow, int toCol, int toRow, boolean isFromWon, boolean showMovement, boolean random){
        TerrainLogic fromLogic = Terrains.getTerrainLogicByPosition(_terrains, fromCol, fromRow);
        TerrainLogic toLogic = Terrains.getTerrainLogicByPosition(_terrains, toCol, toRow);
        _lastActiveTerrainLogic = toLogic;
        toLogic.moveChessToThis(fromLogic, showMovement, isFromWon, random);
        hideAllTerrainPercentTile();
    }

    private void preTurnSwitched(ActionType actionType, TerrainLogic terrainLogic,
                                       ChessType winnerChessType, ChessType loserChessType, boolean random){
        disableTouchable();

        if(_boardModel.getAccTurnCount() >= 20){
            _boardModel.setSuddenDeath(true);
        }

        if(actionType == ActionType.OPEN){
            _statusRef.chessOpened(_terrains, terrainLogic, isMyTurn(), new Runnable() {
                @Override
                public void run() {
                    switchTurn();
                }
            });
        }
        else if(actionType == ActionType.MOVE){
            _statusRef.chessMoved(_terrains, terrainLogic, winnerChessType, loserChessType, random, new Runnable() {
                @Override
                public void run() {
                    switchTurn();
                }
            });
        }


    }

    private void switchTurn(){
        if(_lastActiveTerrainLogic != null){
            clearAllTerrainsHighlights();
            _lastActiveTerrainLogic.getChessLogic().getChessModel().setFocusing(true);
            _lastActiveTerrainLogic.getChessLogic().invalidate();
        }

        _boardModel.switchTurnIndex();
        _statusRef.turnOver(_terrains);
        invalidate();
    }

    private void invalidate(){
        _graveyard.onBoardModelChanged(_boardModel);
        setTurnTouchable();
    }

    private void disableTouchable(){
        _screen.setCanTouchChessTable(false);
    }

    private void setTurnTouchable(){
        _screen.setCanTouchChessTable(isMyTurn());
    }

    private boolean isMyTurn(){
        return _boardModel.getCurrentTurnIndex() == _coordinator.getMyUniqueIndex();
    }

    public void terrainSelected(int col, int row){
        TerrainLogic clickedLogic = Terrains.getTerrainLogicByPosition(_terrains, col, row);
        if(!clickedLogic.isSelected()){
            clearAllTerrainsHighlights();

            clickedLogic.setSelected(true);
            if(clickedLogic.isOpened()){
                showPossibleMoves(clickedLogic);
            }
        }
    }

    private void hideAllTerrainPercentTile(){
        for(TerrainLogic terrainLogic : _terrains){
            terrainLogic.hidePercentTile();
        }
    }

    private void clearAllTerrainsHighlights(){
        for(TerrainLogic terrainLogic : _terrains){
            terrainLogic.getChessLogic().getChessModel().setFocusing(false);
            terrainLogic.setSelected(false);
            terrainLogic.hidePercentTile();
        }
    }

    public void showPossibleMoves(TerrainLogic logic){
        ArrayList<TerrainLogic> possibleMoveLogics = _movementRef.getPossibleValidMoves(_terrains, logic);
        for(final TerrainLogic terrainLogic : possibleMoveLogics){
            terrainLogic.showPercentTile(logic);
        }
        logic.setDragAndDrop(possibleMoveLogics);
    }

    private void endGame(final boolean won){
        _screen.populateEndGameTable();
        Threadings.delay(1000, new Runnable() {
            @Override
            public void run() {
                _screen.showEndGameTable(won);
                _services.getSoundsWrapper().stopTheme();
                _services.getSoundsWrapper().playSounds(won ? Sounds.Name.WIN : Sounds.Name.LOSE);
                Threadings.delay(2000, new Runnable() {
                    @Override
                    public void run() {
                        _screen.getEndGameRootTable().addListener(new ClickListener() {
                            @Override
                            public void clicked(InputEvent event, float x, float y) {
                                super.clicked(event, x, y);
                                _coordinator.endGame();
                            }
                        });
                    }
                });

            }
        });
    }

    private void endGame(ChessColor wonChessColor) {
        endGame(_gameDataController.getMyChessColor() == wonChessColor);
    }

    public BoardScreen getScreen() {
        return _screen;
    }

    private void setListeners(){
        _coordinator.setUserStateListener(new UserStateListener() {
            @Override
            public void userAbandoned(String s) {
                if(!s.equals(_coordinator.getUserId())){
                    endGame(true);
                }
            }

            @Override
            public void userConnected(String s) {
                _screen.setPaused(false, _boardModel.getCurrentTurnIndex() == _coordinator.getMyUniqueIndex());
            }

            @Override
            public void userDisconnected(String s) {
                clearAllTerrainsHighlights();
                _screen.setPaused(true, _boardModel.getCurrentTurnIndex() == _coordinator.getMyUniqueIndex());
                saveGameDataToDB();
            }
        });

    }

}