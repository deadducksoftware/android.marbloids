package io.github.deadducksoftware.marbloids;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends Activity {
	
	private DataStore mDataStore;
	private GameView mGameView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);
		mDataStore = new DataStore(this);
		TextView scoreView = (TextView) findViewById(R.id.scoreView);
		TextView messageView = (TextView) findViewById(R.id.messageView);
		mGameView = (GameView) findViewById(R.id.gameView);
		mGameView.setDataStore(mDataStore);
		mGameView.setScoreView(scoreView);
		mGameView.setMessageView(messageView);
		mGameView.setZOrderOnTop(true);
		mGameView.getHolder().setFormat(PixelFormat.TRANSPARENT);
		scoreView.setText(String.valueOf(GameControl.CURRENT.getScore()));
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mGameView.startGameThread();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mGameView.stopGameThread();
		if (!mDataStore.saveGame(GameControl.CURRENT.getGame()))
			Toast.makeText(this, R.string.gameSaveError, Toast.LENGTH_LONG).show();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDataStore != null)
			mDataStore.close();
	}
	
	public void quit() {
		finish();
	}
}
