package io.github.deadducksoftware.marbloids;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DataStore  extends SQLiteOpenHelper {

	public static final String DB_NAME = "marbloids";
	public static final int DB_VERSION = 1;
	
	public Context mContext;
	
	public DataStore(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		mContext = context;
	}	
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(Game.getCreateStatement());
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
	}
	
	public long create(String tableName, ContentValues values) {
		SQLiteDatabase db = getWritableDatabase();
		return db.insert(tableName, null, values);
	}
	
	public int update(String tableName, ContentValues values, String where, String [] whereValues) {
		SQLiteDatabase db = getWritableDatabase();
		return db.update(tableName, values, where, whereValues);
	}
	
	public int delete(String tableName, String where, String [] whereValues) {
		SQLiteDatabase db = getWritableDatabase();
		return db.delete(tableName, where, whereValues);
	}
	
	public Game createGame() {
		try {
			Game game = new Game();
			long id = create(Game.TABLE_NAME, game.getValues());
			game.setId(id);
			return game;
		}
		catch (SQLException ex) {
			return null;
		}
	}	
	
	public boolean saveGame(Game game) {
		try {
			update(Game.TABLE_NAME, game.getValues(), Game._ID + " = ?",
					new String [] { String.valueOf(game.getId()) });
			return true;
		}
		catch (SQLiteException ex) {
			return false;
		}
	}
	
	public boolean deleteGame(long id) {
		try {
			delete(Game.TABLE_NAME, Game._ID + " = ?",
					new String [] { String.valueOf(id) });
			return true;
		}
		catch (SQLiteException ex) {
			return false;
		}
	}
	
	public boolean deleteOldGames() {
		try {
			delete(Game.TABLE_NAME, Game.STATE + " = 0", new String [] {});
			return true;
		}
		catch (SQLException ex) {
			return false;
		}
	}
	
	public Game getCurrentGame() {
		String sql = String.format("select * from %s where %s != 0",
				Game.TABLE_NAME, Game.STATE);
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.rawQuery(sql, new String [] { });
			if (cursor.moveToFirst())
				return new Game(cursor);
			else
				return null;
		}
		catch (SQLiteException ex) {
			return null;
		}
		finally {
			if (cursor != null)
				cursor.close();
		}		
	}
	
	public boolean deleteLowScoringGames(int total) {
		String sql = String.format(
				"select %s from %s where %s = 0 order by %s desc",
				Game._ID, Game.TABLE_NAME, Game.STATE, Game.SCORE);
		ArrayList<Long> oldRows = new ArrayList<Long>();
		SQLiteDatabase db = getReadableDatabase();
		long count = 0;
		Cursor cursor = null;
		try {
			cursor = db.rawQuery(sql, new String [] { });
			while (cursor.moveToNext())
				if (++count > total)
					oldRows.add(cursor.getLong(0));
		}
		catch (SQLException ex) {
			return false;
		}
		finally {
			if (cursor != null)
				cursor.close();
		}
		try {
			for (Long row: oldRows) {
				db.delete(Game.TABLE_NAME, Game._ID + " = ?",
						new String [] { String.valueOf(row) });
			}
		}
		catch (SQLiteException ex) {
			return false;
		}
		return true;
	} 
	
	public ArrayList<Score> getScores() {
		String select = String.format(
				"select %s, %s from %s where %s = 0 order by %s desc",
				Game.SCORE, Game.PLAYER, Game.TABLE_NAME, Game.STATE, Game.SCORE);
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = null;
		ArrayList<Score> list = new ArrayList<Score>();
		try {
			cursor = db.rawQuery(select, new String [] { });
			while (cursor.moveToNext())
				list.add(new Score(cursor));
			return list;		
		}
		catch (SQLException ex) {
			return null;
		}
		finally {
			if (cursor != null)
				cursor.close();
		}
	}	
	
	public long getLowestScore() {
		String sql = String.format("select %s from %s where %s = 0 order by %s",
				Game.SCORE, Game.TABLE_NAME, Game.STATE, Game.SCORE);
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.rawQuery(sql, new String [] { });
			if (cursor.moveToFirst())
				return cursor.getLong(0);
			else
				return 0;
		}
		catch (SQLiteException ex) {
			return 0;
		}
		finally {
			if (cursor != null)
				cursor.close();
		}		
	}	
	
	public long getScoreCount() {
		String sql = String.format("select count(*) from %s", Game.TABLE_NAME);
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.rawQuery(sql, new String [] { });
			if (cursor.moveToFirst())
				return cursor.getLong(0);
			else
				return 0;
		}
		catch (SQLException ex) {
			return 0;
		}
		finally {
			if (cursor != null)
				cursor.close();
		}		
	}	
}
