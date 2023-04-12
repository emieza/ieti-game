package cat.ieti.ietigame;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.github.czyzby.websocket.CommonWebSockets;

import cat.ieti.ietigame.IetiGame;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		CommonWebSockets.initiate();
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("The IETI Game");
		config.setWindowedMode(800,480);
		new Lwjgl3Application(new IetiGame(), config);
	}
}
