package com.mygdx.potatoandtomato.helpers.controls;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mygdx.potatoandtomato.PTScreen;
import com.mygdx.potatoandtomato.helpers.services.Assets;
import com.mygdx.potatoandtomato.helpers.utils.Positions;
import com.mygdx.potatoandtomato.helpers.utils.Sizes;

/**
 * Created by SiongLeng on 10/12/2015.
 */
public class TopBar {

    Table _root;
    String _title;
    boolean _noPreviousScene;
    Assets _assets;
    Table _topBarTable;
    float _barHeight = 70;
    Image _iconImg;
    Label _titleLabel;
    PTScreen _screen;

    public TopBar(Table _root, String _title, boolean _noPreviousScene,
                        Assets _assets, PTScreen _screen) {
        this._root = _root;
        this._title = _title;
        this._noPreviousScene = _noPreviousScene;
        this._assets = _assets;
        this._screen = _screen;
        setTopBar();
        setIconListener();
    }

    public void setTopBar(){

        _topBarTable = new Table();
        _topBarTable.setWidth(Positions.getWidth());
        _topBarTable.setHeight(_barHeight);
        _topBarTable.setBackground(new TextureRegionDrawable(_assets.getTopBarBg()));
        _topBarTable.setPosition(0, Positions.getHeight() - _barHeight);

        TextureRegion iconRegion = _noPreviousScene ? _assets.getQuitIcon() : _assets.getBackIcon();
        Vector2 iconSize = Sizes.resize(45, iconRegion);
        _iconImg = new Image(iconRegion);
        _iconImg.setSize(iconSize.x, iconSize.y);
        _iconImg.setPosition(76f/2 - iconSize.x/2, _barHeight/2 - iconSize.y/2);

        Label.LabelStyle titleLabelStyle = new Label.LabelStyle();
        titleLabelStyle.font = _assets.getTopBarFont();
        _titleLabel = new Label(_title, titleLabelStyle);

        _topBarTable.addActor(_iconImg);
        _topBarTable.add(_titleLabel).expand().fill().padLeft(90);
        _root.padTop(_barHeight);
        _root.addActor(_topBarTable);

        _iconImg.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                _screen.back();
            }
        });

    }

    public void setIconListener(){
        _iconImg.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
            }
        });
    }

}
