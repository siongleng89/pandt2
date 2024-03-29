package com.potatoandtomato.games.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.potatoandtomato.games.CoveredChessGame;
import com.potatoandtomato.games.statics.Global;

public class DesktopLauncher {
	public static void main (String[] arg) {
		//Entrance.setGameLibCoordinator(new com.potatoandtomato.common.GameLibCoordinator("", "", "", null, ));
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 360;
		config.height = 640;

		TexturePacker.Settings settings = new TexturePacker.Settings();
		settings.maxWidth = 2048;
		settings.maxHeight = 2048;
		settings.filterMag = Texture.TextureFilter.Linear;
		settings.filterMin = Texture.TextureFilter.Linear;
		if(arg.length > 0 && arg[0].equals("pack")) TexturePacker.process(settings, "../../images", "../../android/assets", "pack");

		boolean isContinue = false;
		if(arg.length > 0 && arg[0].equals("continue")) isContinue = true;

		CoveredChessGame coveredChessGame = new CoveredChessGame("covered_chess", isContinue);


		if(arg.length > 0 && (arg[0].equals("debug"))) Global.DEBUG = true;

		if(arg.length > 1 && (arg[1].equals("noentrance"))) Global.NO_ENTRANCE = true;

		if((arg.length > 2 && (arg[2].equals("bot")))
				|| (arg.length > 1 && (arg[1].equals("bot")))) Global.BOT_MATCH = true;

		if(arg.length > 0 && arg[0].equals("pack")) return;

		new LwjglApplication(coveredChessGame, config);




	}
}
