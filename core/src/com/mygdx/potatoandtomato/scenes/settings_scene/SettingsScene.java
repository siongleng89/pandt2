package com.mygdx.potatoandtomato.scenes.settings_scene;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.mygdx.potatoandtomato.PTScreen;
import com.mygdx.potatoandtomato.absintflis.scenes.SceneAbstract;
import com.mygdx.potatoandtomato.assets.Fonts;
import com.mygdx.potatoandtomato.assets.Textures;
import com.mygdx.potatoandtomato.controls.*;
import com.mygdx.potatoandtomato.models.Services;
import com.mygdx.potatoandtomato.statics.Global;
import com.potatoandtomato.common.utils.RunnableArgs;
import com.potatoandtomato.common.utils.Threadings;

/**
 * Created by SiongLeng on 19/12/2015.
 */
public class SettingsScene extends SceneAbstract {

    TextField _displayNameTextField;
    BtnColor _facebookBtn, _saveBtn, creditBtn;
    ToggleButton soundToggleButton, autoPlayAudioToggleButton;

    public SettingsScene(Services services, PTScreen screen) {
        super(services, screen);
    }

    @Override
    public void populateRoot() {
        topBar = new TopBar(_root, _texts.settingsSceneTitle(), false, _assets, _screen, _services.getCoins());

        Label.LabelStyle labelTitleStyle = new Label.LabelStyle();
        labelTitleStyle.fontColor = Color.valueOf("fff6d8");
        labelTitleStyle.font = _assets.getFonts().get(Fonts.FontId.PIZZA_XXL_REGULAR_S_a05e00_1_1);

        Table settingsTable = new Table();
        settingsTable.setBackground(new TextureRegionDrawable(_assets.getTextures().get(Textures.Name.WOOD_BG_NORMAL)));

        /////////////////////////
        //Sounds
        /////////////////////////
        Label soundsLabel = new Label(_texts.soundsTitle(), labelTitleStyle);
        soundToggleButton = new ToggleButton(
                new TextureRegionDrawable(_assets.getTextures().get(Textures.Name.SELECT_BOX)),
                new TextureRegionDrawable(_assets.getTextures().get(Textures.Name.UNSELECT_BOX)));
        soundToggleButton.setToggleOn(Global.ENABLE_SOUND);

        /////////////////////////
        //Auto play audio label
        /////////////////////////
        Label autoPlayAudioLabel = new Label(_texts.autoPlayAudioMsgTitle(), labelTitleStyle);
        autoPlayAudioLabel.setWrap(true);
        autoPlayAudioToggleButton = new ToggleButton(
                new TextureRegionDrawable(_assets.getTextures().get(Textures.Name.SELECT_BOX)),
                new TextureRegionDrawable(_assets.getTextures().get(Textures.Name.UNSELECT_BOX)));
        autoPlayAudioToggleButton.setToggleOn(Global.AUTO_PLAY_AUDIO_MSG);


        //////////////////////////
        //Separator
        //////////////////////////
        Image separatorImage = new Image(_assets.getTextures().get(Textures.Name.WOOD_SEPARATOR_HORIZONTAL));

        ///////////////////
        //Display name
        ////////////////////
        Label displayNameLabel = new Label(_texts.displayNameTitle(), labelTitleStyle);

        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = _assets.getFonts().get(Fonts.FontId.MYRIAD_M_REGULAR);
        textFieldStyle.fontColor = Color.BLACK;
        textFieldStyle.cursor = new TextureRegionDrawable(_assets.getTextures().get(Textures.Name.CURSOR_BLACK));
        _displayNameTextField = new PTTextField(_assets, true);
        _displayNameTextField.setText(_services.getProfile().getDisplayName(99));

        //////////////////////////
        //Save Button
        //////////////////////////
        _saveBtn = new BtnColor(BtnColor.ColorChoice.RED, _assets);
        _saveBtn.setText(_texts.btnTextUpdateName());


        //////////////////////////
        //Separator
        //////////////////////////
        Image separatorImage2 = new Image(_assets.getTextures().get(Textures.Name.WOOD_SEPARATOR_HORIZONTAL));

        ///////////////////////////
        //Facebook status
        //////////////////////////
        Label socialLabel = new Label(_texts.facebookTitle(), labelTitleStyle);

        _facebookBtn = new BtnColor(BtnColor.ColorChoice.BLUE, _assets);
        _facebookBtn.setText(_services.getSocials().isFacebookLogon() ? _texts.logout() : _texts.login());

        //////////////////////////
        //Separator
        //////////////////////////
        Image separatorImage3 = new Image(_assets.getTextures().get(Textures.Name.WOOD_SEPARATOR_HORIZONTAL));

        ///////////////////////////
        //Credit
        //////////////////////////
        Label creditLabel = new Label(_texts.creditsTitle(), labelTitleStyle);

        creditBtn = new BtnColor(BtnColor.ColorChoice.GREEN, _assets);
        creditBtn.setText(_texts.btnTextShowCredits());


        ////////////////////////
        //populations
        /////////////////////////
        settingsTable.align(Align.top);
        settingsTable.padLeft(25).padRight(25).padTop(30).padBottom(30);
        settingsTable.add(soundsLabel).left();
        settingsTable.add(soundToggleButton).right();
        settingsTable.row().padTop(5);
        settingsTable.add(autoPlayAudioLabel).left();
        settingsTable.add(autoPlayAudioToggleButton).right();
        settingsTable.row();
        settingsTable.add(separatorImage).expandX().fillX().colspan(2).padTop(10).padBottom(10);
        settingsTable.row();
        settingsTable.add(displayNameLabel).left();
        settingsTable.add(_displayNameTextField).right();
        settingsTable.row();
        settingsTable.add(_saveBtn).colspan(2).right().padTop(15).width(170);
        settingsTable.row();
        settingsTable.add(separatorImage2).expandX().fillX().colspan(2).padTop(10).padBottom(10);
        settingsTable.row();
        settingsTable.add(socialLabel).left();
        settingsTable.add(_facebookBtn).right();
        settingsTable.row();
        settingsTable.add(separatorImage3).expandX().fillX().colspan(2).padTop(10).padBottom(10);
        settingsTable.row();
        settingsTable.add(creditLabel).left();
        settingsTable.add(creditBtn).right();


        _root.add(settingsTable).width(350);
    }

    public void getCreditsDesign(final RunnableArgs<Actor> runnableArgs){
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                Table table = new Table();
                Label.LabelStyle labelStyle = new Label.LabelStyle(_assets.getFonts().get(Fonts.FontId.MYRIAD_S_BOLD), Color.BLACK);

                Label label = new Label(_texts.allCredits(), labelStyle);
                label.setWrap(true);
                table.add(label).expand().fill();

                runnableArgs.run(table);
            }
        });
    }

    public ToggleButton getAutoPlayAudioToggleButton() {
        return autoPlayAudioToggleButton;
    }

    public ToggleButton getSoundToggleButton() {
        return soundToggleButton;
    }

    public TextField getDisplayNameTextField() {
        return _displayNameTextField;
    }

    public BtnColor getFacebookBtn() {
        return _facebookBtn;
    }

    public BtnColor getSaveBtn() {
        return _saveBtn;
    }

    public BtnColor getCreditBtn() {
        return creditBtn;
    }




}
