package cat.ieti.ietigame;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.github.czyzby.websocket.CommonWebSockets;

import cat.ieti.ietigame.IetiGame;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CommonWebSockets.initiate();
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new IetiGame(), config);
	}
}
