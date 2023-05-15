package io.github.deadducksoftware.marbloids;

public enum GameControl {
	
	CURRENT;
	
	public static final int GRID_SIZE = 8;
	public static final int SCORE_INCREMENT = 100;
	public static final int SCORES_TOTAL = 20;
	public static final char CELL_EMPTY = 'E';
	public static final char CELL_PLAYER = 'P';
	public static final char CELL_ANDROID = 'A';
	public static final char CELL_CREATED = 'C';
	public static final char CELL_DESTROYED = 'D';
	public static final int STATE_GAME_OVER = 0;
	public static final int STATE_PLAYER_PLAYING = 1;
	public static final int STATE_PLAYER_CONNECT = 2;
	public static final int STATE_PLAYER_CONNECT_UPDATE = 3;
	public static final int STATE_PLAYER_ROWS = 4;
	public static final int STATE_PLAYER_ROWS_UPDATE = 5;
	public static final int STATE_PLAYER_CHAIN = 6;
	public static final int STATE_PLAYER_CHAIN_UPDATE = 7;
	public static final int STATE_PLAYER_FINISHED = 8;
	public static final int STATE_ANDROID_PLAYING = 9;
	public static final int STATE_ANDROID_PLAYING_UPDATE = 10;
	public static final int STATE_ANDROID_ROWS = 11;
	public static final int STATE_ANDROID_ROWS_UPDATE = 12;
	public static final int STATE_ANDROID_CHAIN = 13;
	public static final int STATE_ANDROID_CHAIN_UPDATE = 14;
	public static final int STATE_ANDROID_FINISHED = 15;
	
	private Game mGame;
	
	public Game getGame() {
		return mGame;
	}
	
	public void setGame(Game mGame) {
		this.mGame = mGame;
	}
	
	public long getId() {
		return mGame.getId();
	}
	
	public char [][] getBoard() {
		String board = mGame.getBoard();
		char [][] boardModel = new char[GRID_SIZE][GRID_SIZE];
		int i = 0;
		for (int c = 0; c < GRID_SIZE; c++)
			for (int r = 0; r < GRID_SIZE; r++)
				boardModel[c][r] = board.charAt(i++);
		return boardModel;
	}
	
	public void setBoard(char [][] boardModel) {
		StringBuilder sb = new StringBuilder(GRID_SIZE * GRID_SIZE);
		for (int c = 0; c < GRID_SIZE; c++)
			for (int r = 0; r < GRID_SIZE; r++)
				sb.append(boardModel[c][r]);
		mGame.setBoard(sb.toString());
	}
	
	public int getState() {
		return mGame.getState();
	}
	
	public void setState(int state) {
		mGame.setState(state);
	}
	
	public long getScore() {
		return mGame.getScore();
	}
	
	public void setScore(long score) {
		mGame.setScore(score);
	}
	
	public void incrementScore() {
		mGame.setScore(mGame.getScore() + SCORE_INCREMENT);
	}
	
	public String getPlayer() {
		return mGame.getPlayer();
	}
	
	public void setPlayer(String player) {
		mGame.setPlayer(player);
	}
}
