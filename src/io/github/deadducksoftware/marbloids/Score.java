package io.github.deadducksoftware.marbloids;

import android.database.Cursor;

public class Score {
	
	private long mScore;
	private String mPlayer;
	
	public Score(long score, String player) {
		mScore = score;
		mPlayer = player;
	}
	
	public Score(Cursor cursor) {
		mScore = cursor.getLong(cursor.getColumnIndex(Game.SCORE));
		mPlayer = cursor.getString(cursor.getColumnIndex(Game.PLAYER));
	}
	
	public String getScore() {
		return String.valueOf(mScore);
	}

	public String getPlayer() {
		return mPlayer;
	}
}
