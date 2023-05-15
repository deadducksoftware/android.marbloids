package io.github.deadducksoftware.marbloids;

import java.lang.ref.WeakReference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
	
	private Context mContext;
	private DataStore mDataStore;
	private SurfaceHolder mHolder;
	private GameThread mThread;
	private TextView mScoreView;
	private TextView mMessageView;
	private String mPlayerName;
	
	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
        mContext = context;
		mHolder = getHolder();
        mHolder.addCallback(this);
        SharedPreferences prefs = PreferenceManager.
        		getDefaultSharedPreferences(context);
        mPlayerName = prefs.getString(
        		PrefsActivity.PLAYER_NAME, 
        		PrefsActivity.PLAYER_NAME_DEFAULT).trim();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN)
			mThread.setPendingEvent(GameThread.EVENT_PRESS,
					event.getX(), event.getY());
		return super.onTouchEvent(event);
	}	
	
	public void setDataStore(DataStore dataStore) {
		mDataStore = dataStore;
	}
	
	public void setScoreView(TextView view) {
		mScoreView = view;
	}
	
	public void setMessageView(TextView view) {
		mMessageView = view;
	}	
	
	public void updateScore() {
		mScoreView.setText(String.valueOf(GameControl.CURRENT.getScore()));
	}
	
	public void updatePlayerTurn(boolean player) {
		if (player)
			mMessageView.setVisibility(View.VISIBLE);
		else
			mMessageView.setVisibility(View.INVISIBLE);
	}
	
	private void requestPlayerName() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		final EditText input = new EditText(mContext);
		input.setSingleLine();
		input.setText(mPlayerName);
		FrameLayout layout = new FrameLayout(mContext);
		layout.setPadding(10, 5, 10, 5);
		layout.addView(input);
		builder.setView(layout);
		builder.setTitle(R.string.gameOverTitle);
		builder.setMessage(R.string.gameOverScoreMessage);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setCancelable(false);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String player = input.getText().toString().trim();
				GameControl.CURRENT.setPlayer(player);
				if (!mDataStore.saveGame(GameControl.CURRENT.getGame()))
					Toast.makeText(mContext, R.string.gameSaveError, Toast.LENGTH_LONG).show();
				requestNewGame();
			}
        });
		final AlertDialog alert = builder.show();
		final Button okButton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
		if (mPlayerName.length() == 0)
			okButton.setEnabled(false);
		input.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) { }
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			@Override
			public void afterTextChanged(Editable s) {
				String newName = s.toString().trim();
				okButton.setEnabled(newName.length() > 0);
			}
		});		
	}
	
	private void requestNewGame() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setCancelable(false);
		builder.setTitle(R.string.gameOverTitle);
		builder.setMessage(R.string.gameOverMessage);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setPositiveButton(R.string.newGame, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) { newGame(); }
		});
		builder.setNegativeButton(R.string.mainMenu, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) { quitGame(); }
		});
		builder.create().show();		
	}

	private void newGame() {
		Game game = mDataStore.createGame();
		if (game != null) {
			GameControl.CURRENT.setGame(game);
			mThread.startNewGame();
		}
	}
	
	public void endGame() {
		updatePlayerTurn(false);
		long lowestScore = 0;
		long scoreCount = 0; 
		mDataStore.deleteLowScoringGames(GameControl.SCORES_TOTAL);
		lowestScore = mDataStore.getLowestScore();
		scoreCount = mDataStore.getScoreCount();
		mDataStore.saveGame(GameControl.CURRENT.getGame());
		// Get name if high score then ask for replay
		if (scoreCount <= GameControl.SCORES_TOTAL) // Includes current
			requestPlayerName();
		else if (GameControl.CURRENT.getScore() >= lowestScore)
			requestPlayerName();
		else
			requestNewGame();		
	}	
	
	private void quitGame() {
		((GameActivity) mContext).quit();
	}
	
	public void startGameThread() {
        mThread = new GameThread(mHolder, mContext, new ThreadHandler(this));        
		mThread.setThreadRunning(true);
        mThread.start();		
	}
	
	public void stopGameThread() {
		boolean retry = true;
        mThread.setThreadRunning(false);
        while (retry) {
            try {
                mThread.join();
                retry = false;
            }
            catch (InterruptedException e) {
            	// Do nothing
            }
        }
	}
	
	private static class ThreadHandler extends Handler {
		private final WeakReference<GameView> mGameView;
		
		public ThreadHandler(GameView gameView) {
			mGameView = new WeakReference<GameView>(gameView);
		}
		
        @Override
        public void handleMessage(Message m) {
        	GameView view = mGameView.get();
        	view.updateScore();
        	switch (m.what) {
        		case GameControl.STATE_PLAYER_PLAYING:
        			view.updatePlayerTurn(true);
        			break;
        		case GameControl.STATE_PLAYER_CONNECT:
        			view.updatePlayerTurn(false);
        			break;
        		case GameControl.STATE_GAME_OVER:
        			view.endGame();
        			break;
        	}
        }		
	}
}
