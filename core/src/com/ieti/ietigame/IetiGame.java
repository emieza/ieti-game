package com.ieti.ietigame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class IetiGame extends ApplicationAdapter {
	SpriteBatch batch;
	Texture ietisheet;
	Animation<TextureRegion> ietiWalk;
	float stateTime;

	int FRAME_ROWS = 5;
	int FRAME_COLS = 6;

	@Override
	public void create () {
		batch = new SpriteBatch();

		ietisheet = new Texture("ieti.png");
		TextureRegion[][] tmp = TextureRegion.split( ietisheet,
					ietisheet.getWidth() / FRAME_COLS,
					ietisheet.getHeight() / FRAME_ROWS );

		TextureRegion[] walkFrames = new TextureRegion[FRAME_COLS];
		int index = 0;
		for( int j=0; j<FRAME_COLS; j++ ) {
			// a la fila 1 hi ha el sprite caminant
			walkFrames[index++] = tmp[1][j];
		}
		ietiWalk = new Animation<TextureRegion>(0.25f, walkFrames );
		stateTime = 0f;
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.3f, 0.5f, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stateTime += Gdx.graphics.getDeltaTime();
		TextureRegion ietiFrame = ietiWalk.getKeyFrame( stateTime, true );
		batch.begin();
		//batch.draw(img, 0, 0);
		batch.draw( ietiFrame, 50, 50);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		ietisheet.dispose();
	}
}
