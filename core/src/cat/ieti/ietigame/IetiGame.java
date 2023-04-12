package cat.ieti.ietigame;


import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class IetiGame extends Game {
	public SpriteBatch batch;
	public BitmapFont font;
	public OrthographicCamera camera;

	public int SCR_HEIGHT = 480;
	public int SCR_WIDTH = 800;

	public void create() {
		batch = new SpriteBatch();
		font = new BitmapFont();
		camera = new OrthographicCamera();
		camera.setToOrtho(false,SCR_WIDTH,SCR_HEIGHT);
		this.setScreen(new MainMenuScreen(this));
	}

	public void render() {
		super.render();
	}

	public void dispose() {
		batch.dispose();
		font.dispose();
	}
}
