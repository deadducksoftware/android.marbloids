package io.github.deadducksoftware.marbloids;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private static final int MENU_COLOR = 0xffffffff;
	private static final int MENU_SELECTED_COLOR = 0xffffff00;
	
	private Context mContext;
	private DataStore mDataStore;
	private TextView mResumeGame;
	private TextView mNewGame;
	private TextView mScores;
	private TextView mHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mContext = this;
        mDataStore = new DataStore(this);
        mResumeGame = (TextView) findViewById(R.id.mainResumeGame);
        mNewGame = (TextView) findViewById(R.id.mainNewGame);
        mScores = (TextView) findViewById(R.id.mainScores);
        mHelp = (TextView) findViewById(R.id.mainHelp);
        mResumeGame.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(mContext, GameActivity.class));
			}
		});
        mNewGame.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (GameControl.CURRENT.getGame() == null) {
					startGame(false);
				}
				else {
					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setTitle(R.string.mainNewGameTitle);
					builder.setMessage(R.string.mainAbandonGameMessage);
					builder.setIcon(android.R.drawable.ic_dialog_alert);
					builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							startGame(true);
						}
			        });
					builder.setNegativeButton(R.string.cancel, null);
					builder.create().show();						
				}
			}
		});
        mScores.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(mContext, ScoresActivity.class));
			}
		});
        mHelp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(mContext, HelpActivity.class));
			}
		});
        OnTouchListener menuTouchListener = new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				TextView textView = (TextView) view;
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					textView.setTextColor(MENU_SELECTED_COLOR);
				if (event.getAction() == MotionEvent.ACTION_UP)
					textView.setTextColor(MENU_COLOR);
				return false;
			}
		};
        mResumeGame.setOnTouchListener(menuTouchListener);
        mNewGame.setOnTouchListener(menuTouchListener);
        mScores.setOnTouchListener(menuTouchListener);
        mHelp.setOnTouchListener(menuTouchListener);   
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		Game game = mDataStore.getCurrentGame();
        GameControl.CURRENT.setGame(game);
        if (game == null)
        	mResumeGame.setVisibility(View.INVISIBLE);
        else
        	mResumeGame.setVisibility(View.VISIBLE);        	
        mResumeGame.setTextColor(MENU_COLOR);
        mNewGame.setTextColor(MENU_COLOR);
        mScores.setTextColor(MENU_COLOR);
        mHelp.setTextColor(MENU_COLOR);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDataStore != null)
			mDataStore.close();
	}    
	
	private void startGame(boolean deleteOld) {
		if (deleteOld)
			mDataStore.deleteGame(GameControl.CURRENT.getId());
		Game newGame = mDataStore.createGame();
		if (newGame != null) {
			GameControl.CURRENT.setGame(newGame);
			startActivity(new Intent(this, GameActivity.class));
		}
	}
}
