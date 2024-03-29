package com.potatoandtomato.games.screens;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.potatoandtomato.common.assets.Assets;
import com.potatoandtomato.common.utils.Threadings;
import com.potatoandtomato.games.assets.Fonts;
import com.potatoandtomato.games.assets.Textures;
import com.potatoandtomato.games.controls.DummyButton;
import com.potatoandtomato.games.enums.Direction;
import com.potatoandtomato.games.helpers.Positions;
import com.potatoandtomato.games.services.SoundsWrapper;
import com.potatoandtomato.games.models.TerrainModel;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateBy;

/**
 * Created by SiongLeng on 30/12/2015.
 */
public class TerrainActor extends Table {

    private TerrainActor _this;
    private Assets _assets;
    private Image _glowingTile;
    private Image _backgroundImage;
    private boolean _selected;
    private Table _chessTable;
    private Table _greenTile, _redTile;
    private Label _percentLabel;
    private boolean _initialized;
    private Image _arrowLeft, _arrowRight, _arrowUp, _arrowDown, _arrowTopLeft,
            _arrowTopRight, _arrowBottomLeft, _arrowBottomRight, _crackImage;
    private Table _battleTable;
    private SoundsWrapper _soundsWrapper;
    private ChessActor _chessActor;
    private Table root;

    public TerrainActor(Table root, Assets _assets, ChessActor chessActor, SoundsWrapper soundsWrapper) {
        this.root = root;
        _this = this;
        this._soundsWrapper = soundsWrapper;
        this._assets = _assets;

        populate(chessActor);
    }

    public void populate(final ChessActor chessActor){
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                new DummyButton(_this, _assets);

                _backgroundImage = new Image(_assets.getTextures().get(Textures.Name.TRANS_BLACK_BG));
                _backgroundImage.setFillParent(true);
                _this.addActor(_backgroundImage);

                _crackImage = new Image(_assets.getTextures().get(Textures.Name.CRACK));
                _crackImage.setTouchable(Touchable.disabled);
                _crackImage.setPosition(-10, -10);
                _crackImage.setVisible(false);
                _this.addActor(_crackImage);

                _glowingTile = new Image(_assets.getTextures().get(Textures.Name.GLOWING_TILE));
                _glowingTile.setVisible(false);
                _glowingTile.setTouchable(Touchable.disabled);
                _this.addActor(_glowingTile);

                _chessTable = new Table();
                _this.add(_chessTable).expand().fill();
                setChessActor(chessActor);

                _greenTile = new Table();
                _greenTile.setBackground(new TextureRegionDrawable(_assets.getTextures().get(Textures.Name.GREEN_TILE)));
                _greenTile.setFillParent(true);
                _greenTile.getColor().a = 0;
                _this.addActor(_greenTile);

                _redTile = new Table();
                _redTile.setBackground(new TextureRegionDrawable(_assets.getTextures().get(Textures.Name.RED_TILE)));
                _redTile.setFillParent(true);
                _redTile.setVisible(false);
                _this.addActor(_redTile);

                Label.LabelStyle labelStyle = new Label.LabelStyle();
                labelStyle.font = _assets.getFonts().get(Fonts.FontId.MYRIAD_M_REGULAR_B_000000_588e54_1);
                _percentLabel = new Label("", labelStyle);
                _percentLabel.setTouchable(Touchable.disabled);
                _greenTile.add(_percentLabel);

                _battleTable = new Table();
                _battleTable.setFillParent(true);
                _this.addActor(_battleTable);
            }
        });
    }

    public void setChessActor(final ChessActor chessActor){
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                _chessTable.clear();
                _chessTable.add(chessActor).size(55);
                _chessActor = chessActor;
            }
        });
    }

    public void setSelected(final boolean selected){
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                if(_selected != selected){
                    _selected = selected;
                    if(_selected){
                        _glowingTile.setSize(130, 135);
                        _glowingTile.setPosition(
                                Positions.centerX(_this.getWidth(), _glowingTile.getWidth()) + 2,
                                Positions.centerY(_this.getHeight(), _glowingTile.getHeight()) + 1);
                        _glowingTile.addAction(sequence(fadeOut(0f), fadeIn(0.25f)));
                        _glowingTile.setVisible(true);
                    }
                    else{
                        _glowingTile.addAction(sequence(fadeOut(0.25f), new RunnableAction() {
                            @Override
                            public void run() {
                                _glowingTile.setSize(0, 0);
                            }
                        }));
                    }
                }
            }
        });
    }

    public void showPercent(final int percent, final Direction direction){
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                if(percent != -1){
                    _percentLabel.setText(percent + "%");
                }
                else{
                    _percentLabel.setText("");
                }

                _greenTile.clearActions();
                _greenTile.addAction(fadeIn(0.3f));
                hideAllArrows();
                if(direction == Direction.LEFT) {
                    _arrowLeft.setVisible(true);
                }
                else if(direction == Direction.TOP){
                    _arrowUp.setVisible(true);
                }
                else if(direction == Direction.RIGHT){
                    _arrowRight.setVisible(true);
                }
                else if(direction == Direction.BOTTOM){
                    _arrowDown.setVisible(true);
                }
                else if(direction == Direction.BOTTOM_LEFT){
                    _arrowBottomLeft.setVisible(true);
                }
                else if(direction == Direction.BOTTOM_RIGHT){
                    _arrowBottomRight.setVisible(true);
                }
                else if(direction == Direction.TOP_LEFT){
                    _arrowTopLeft.setVisible(true);
                }
                else if(direction == Direction.TOP_RIGHT){
                    _arrowTopRight.setVisible(true);
                }
            }
        });
    }

    public void hidePercent(){
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                _greenTile.clearActions();
                _greenTile.addAction(fadeOut(0.1f));
                hideAllArrows();
            }
        });
    }

    private void hideAllArrows(){
        if(_arrowLeft != null) _arrowLeft.setVisible(false);
        if(_arrowRight != null) _arrowRight.setVisible(false);
        if(_arrowUp != null) _arrowUp.setVisible(false);
        if(_arrowDown != null) _arrowDown.setVisible(false);
        if(_arrowBottomLeft != null) _arrowBottomLeft.setVisible(false);
        if(_arrowBottomRight != null) _arrowBottomRight.setVisible(false);
        if(_arrowTopLeft != null) _arrowTopLeft.setVisible(false);
        if(_arrowTopRight != null) _arrowTopRight.setVisible(false);
    }

    public void showCanMoveTo(){
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                _redTile.setVisible(true);
            }
        });
    }

    public void hideCanMoveTo(){
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                _redTile.setVisible(false);
            }
        });
    }

    public void showBattle(){
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                Image battleCloud = new Image(_assets.getTextures().get(Textures.Name.BATTLE_CLOUD));
                _battleTable.add(battleCloud).size(80, 80);
                battleCloud.setOrigin(Align.center);
                battleCloud.addAction(forever(sequence(Actions.rotateBy(-5, 0.1f), Actions.rotateBy(5, 0.1f))));

                Image battleEffect = new Image(_assets.getTextures().get(Textures.Name.BATTLE_EFFECT));
                battleEffect.setFillParent(true);
                battleEffect.setOrigin(Align.center);
                _battleTable.addActor(battleEffect);
                battleEffect.addAction(forever(sequence(Actions.scaleTo(1.1f, 1.1f, 0.3f), Actions.scaleTo(1f, 1f, 0.3f))));
            }
        });
    }

    public void hideBattle(){
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                _battleTable.clear();
            }
        });
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if(!_initialized){
            _initialized = true;
            initializeArrows();
            initializeCrack();
        }

    }

    private void initializeArrows(){
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                float width = 17;
                float height = 20;

                Vector2 coords = Positions.actorLocalToStageCoord(_this);
                _arrowLeft = new Image(_assets.getTextures().get(Textures.Name.ARROW_RIGHT));
                _arrowLeft.setSize(width, height);
                _arrowLeft.setTouchable(Touchable.disabled);
                _arrowLeft.setPosition(coords.x + Positions.centerX(_this.getWidth(), _arrowLeft.getWidth()) - _this.getWidth()/2 + _arrowLeft.getWidth()/ 2 ,
                        coords.y + Positions.centerY(_this.getHeight(), _arrowLeft.getHeight()));
                _arrowLeft.setVisible(false);
                _arrowLeft.addAction(forever(sequence(Actions.moveBy(3, 0, 0.3f), Actions.moveBy(-3, 0, 0.3f))));
                root.addActor(_arrowLeft);

                _arrowRight = new Image(_assets.getTextures().get(Textures.Name.ARROW_LEFT));
                _arrowRight.setSize(width, height);
                _arrowRight.setTouchable(Touchable.disabled);
                _arrowRight.setPosition(coords.x + Positions.centerX(_this.getWidth(), _arrowRight.getWidth()) + _this.getWidth()/2 - _arrowRight.getWidth()/ 2 ,
                        coords.y + Positions.centerY(_this.getHeight(), _arrowRight.getHeight()));
                _arrowRight.setVisible(false);
                _arrowRight.addAction(forever(sequence(Actions.moveBy(-3, 0, 0.3f), Actions.moveBy(3, 0, 0.3f))));
                root.addActor(_arrowRight);

                _arrowUp = new Image(_assets.getTextures().get(Textures.Name.ARROW_DOWN));
                _arrowUp.setSize(width, height);
                _arrowUp.setTouchable(Touchable.disabled);
                _arrowUp.setPosition(coords.x + Positions.centerX(_this.getWidth(), _arrowUp.getWidth()) ,
                        coords.y + Positions.centerY(_this.getHeight(), _arrowUp.getHeight()) + _this.getHeight()/2 - _arrowUp.getHeight()/ 2);
                _arrowUp.setVisible(false);
                _arrowUp.addAction(forever(sequence(Actions.moveBy(0, -3, 0.3f), Actions.moveBy(0, 3, 0.3f))));
                root.addActor(_arrowUp);

                _arrowDown = new Image(_assets.getTextures().get(Textures.Name.ARROW_UP));
                _arrowDown.setSize(width, height);
                _arrowDown.setTouchable(Touchable.disabled);
                _arrowDown.setPosition(coords.x + Positions.centerX(_this.getWidth(), _arrowDown.getWidth()) ,
                        coords.y + Positions.centerY(_this.getHeight(), _arrowDown.getHeight()) - _this.getHeight()/2 + _arrowDown.getHeight() / 2);
                _arrowDown.setVisible(false);
                _arrowDown.addAction(forever(sequence(Actions.moveBy(0, 3, 0.3f), Actions.moveBy(0, -3, 0.3f))));
                root.addActor(_arrowDown);

                _arrowBottomLeft = new Image(_assets.getTextures().get(Textures.Name.ARROW_TOP_RIGHT));
                _arrowBottomLeft.setSize(20, 20);
                _arrowBottomLeft.setTouchable(Touchable.disabled);
                _arrowBottomLeft.setPosition(coords.x ,
                        coords.y);
                _arrowBottomLeft.setVisible(false);
                _arrowBottomLeft.addAction(forever(sequence(Actions.moveBy(3, 3, 0.3f), Actions.moveBy(-3, -3, 0.3f))));
                root.addActor(_arrowBottomLeft);

                _arrowBottomRight = new Image(_assets.getTextures().get(Textures.Name.ARROW_TOP_LEFT));
                _arrowBottomRight.setSize(20, 20);
                _arrowBottomRight.setTouchable(Touchable.disabled);
                _arrowBottomRight.setPosition(coords.x + _this.getWidth() - _arrowBottomRight.getWidth(),
                        coords.y);
                _arrowBottomRight.setVisible(false);
                _arrowBottomRight.addAction(forever(sequence(Actions.moveBy(-3, 3, 0.3f), Actions.moveBy(3, -3, 0.3f))));
                root.addActor(_arrowBottomRight);

                _arrowTopRight = new Image(_assets.getTextures().get(Textures.Name.ARROW_BOTTOM_LEFT));
                _arrowTopRight.setSize(20, 20);
                _arrowTopRight.setTouchable(Touchable.disabled);
                _arrowTopRight.setPosition(coords.x + _this.getWidth() - _arrowTopRight.getWidth(),
                        coords.y + _this.getHeight() - _arrowTopRight.getHeight());
                _arrowTopRight.setVisible(false);
                _arrowTopRight.addAction(forever(sequence(Actions.moveBy(-3, -3, 0.3f), Actions.moveBy(3, 3, 0.3f))));
                root.addActor(_arrowTopRight);

                _arrowTopLeft = new Image(_assets.getTextures().get(Textures.Name.ARROW_BOTTOM_RIGHT));
                _arrowTopLeft.setSize(20, 20);
                _arrowTopLeft.setTouchable(Touchable.disabled);
                _arrowTopLeft.setPosition(coords.x,
                        coords.y + _this.getHeight() - _arrowTopLeft.getHeight());
                _arrowTopLeft.setVisible(false);
                _arrowTopLeft.addAction(forever(sequence(Actions.moveBy(3, -3, 0.3f), Actions.moveBy(-3, 3, 0.3f))));
                root.addActor(_arrowTopLeft);
            }
        });
    }

    private void initializeCrack(){
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                _crackImage.setSize(_this.getWidth() + 20, _this.getHeight() + 10);
            }
        });
    }

    public void animateBroken(){
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                _crackImage.addAction(fadeOut(0.3f));
                _backgroundImage.addAction(sequence(fadeOut(0.3f), new RunnableAction(){
                    @Override
                    public void run() {
                        _chessActor.addAction(sequence(delay(0.5f), parallel(forever(Actions.rotateBy(360, 3f)), Actions.scaleBy(-1, -1, 3f))));
                    }
                }));
            }
        });
    }

    public void invalidate(final TerrainModel model){
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                if(model.isBroken()){
                    _backgroundImage.setVisible(false);
                    _chessTable.setVisible(false);
                    _crackImage.setVisible(false);
                }
                else if(model.isBreaking()){
                    _crackImage.setVisible(true);
                }
            }
        });
    }


}
