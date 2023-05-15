package io.github.deadducksoftware.marbloids;

import android.content.ContentValues;
import android.database.Cursor;

public class Game {
	
	public static final String TABLE_NAME = "games";
	public static final String _ID = "_id";	
	public static final String SCORE = "score";
	public static final String STATE = "state"; 
	public static final String BOARD = "board";
	public static final String PLAYER = "player";
	
	public static String getCreateStatement() {
		return String.format("create table %s (%s integer primary key autoincrement, " +
				"%s integer, %s integer, %s text, %s text);",
				TABLE_NAME, _ID, SCORE, STATE, BOARD, PLAYER);
	}
	
	private long mId;
	private long mScore;
	private int mState;
	private String mBoard;
	private String mPlayer;
	
	public Game() {
		int size = GameControl.GRID_SIZE * GameControl.GRID_SIZE;
		StringBuilder sb = new StringBuilder(size);
		for (int i = 0; i < size; i ++)
			sb.append(GameControl.CELL_EMPTY);
		setScore(0);
		setState(GameControl.STATE_PLAYER_PLAYING);
		setBoard(sb.toString());		
		setPlayer("");
	}
	
	public Game(Cursor cursor) {
		setId(cursor.getLong(cursor.getColumnIndex(_ID)));
		setScore(cursor.getLong(cursor.getColumnIndex(SCORE)));
		setState(cursor.getInt(cursor.getColumnIndex(STATE)));
		setBoard(cursor.getString(cursor.getColumnIndex(BOARD)));
		setPlayer(cursor.getString(cursor.getColumnIndex(PLAYER)));
	}
	
	public ContentValues getValues() {
		ContentValues values = new ContentValues();
		values.put(SCORE, getScore());
		values.put(STATE, getState());
		values.put(BOARD, getBoard());
		values.put(PLAYER, getPlayer());
		return values;
	}	
	
	public long getId() {
		return mId;
	}
	
	public void setId(long id) {
		mId = id;
	}

	public long getScore() {
		return mScore;
	}

	public void setScore(long score) {
		mScore = score;
	}
	
	public int getState() {
		return mState;
	}

	public void setState(int state) {
		mState = state;
	}
	
	public String getBoard() {
		return mBoard;
	}

	public void setBoard(String board) {
		mBoard = board;
	}

	public String getPlayer() {
		return mPlayer;
	}

	public void setPlayer(String player) {
		mPlayer = player;
	}
}
