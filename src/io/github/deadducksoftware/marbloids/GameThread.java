package io.github.deadducksoftware.marbloids;

import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.SurfaceHolder;

public class GameThread extends Thread {
	
	public static final int PLAYER_COLOR = 0xffff0000;
	public static final int ANDROID_COLOR = 0xffa0a0a0;
	public static final int CREATED_COLOR = 0xffffffff;
	public static final int DESTROYED_COLOR = 0xff00ff00;	
	
	public static final int EVENT_NONE = 0;
	public static final int EVENT_PRESS = 1;
	public static final int PAUSE_FOR_EFFECT = 200;
	
	private SurfaceHolder mSurfaceHolder;
	private Handler mHandler;
	private boolean mRunLoop;
	private boolean mProcessStates;
	private int mLastStateMesage;
	private int mNewPieceCol;
	private int mNewPieceRow;
	private int mEventPending;
	private int mEventX;
	private int mEventY;
	private char [][] mBoardModel;
	private int mGridSize;
	private int mCellSize;
	private int mBoardSize;
	private int mBoardLeft;
	private int mBoardTop;
	private Paint mPaintBoard;
	private Paint mPaintGrid;
	private Paint mPaintBorder;
	private Paint mPaintPlayer;
	private Paint mPaintAndroid;
	private Paint mPaintCreated;
	private Paint mPaintDestroyed;
	
	public GameThread(SurfaceHolder surfaceHolder, Context context, Handler handler) {
		mSurfaceHolder = surfaceHolder;
		mHandler = handler;
		mRunLoop = false;
		mProcessStates = true;
		mLastStateMesage = -1;
		mGridSize = GameControl.GRID_SIZE;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		int playerColor = prefs.getInt(PrefsActivity.PLAYER_COLOR,
				PrefsActivity.PLAYER_COLOR_DEFAULT);		
		int androidColor = prefs.getInt(PrefsActivity.ANDROID_COLOR,
				PrefsActivity.ANDROID_COLOR_DEFAULT);		
		int createdColor = prefs.getInt(PrefsActivity.CREATED_COLOR,
				PrefsActivity.CREATED_COLOR_DEFAULT);		
		int destroyedColor = prefs.getInt(PrefsActivity.DESTROYED_COLOR,
				PrefsActivity.DESTROYED_COLOR_DEFAULT);			
    	mPaintBoard = new Paint();
    	mPaintBoard.setStyle(Style.FILL);
    	mPaintBoard.setColor(Color.BLACK);
    	mPaintGrid = new Paint();
    	mPaintGrid.setStrokeWidth(1);
    	mPaintGrid.setStyle(Style.STROKE);
    	mPaintGrid.setColor(0xff181818);
    	mPaintBorder = new Paint();
    	mPaintBorder.setStyle(Style.STROKE);
    	mPaintBorder.setColor(Color.GRAY);      	
		mPaintPlayer = new Paint();
    	mPaintPlayer.setAntiAlias(true);
    	mPaintPlayer.setStyle(Style.FILL);
    	mPaintPlayer.setColor(playerColor);
    	mPaintAndroid = new Paint();
    	mPaintAndroid.setAntiAlias(true);
    	mPaintAndroid.setStyle(Style.FILL);
    	mPaintAndroid.setColor(androidColor);
    	mPaintCreated = new Paint();
    	mPaintCreated.setAntiAlias(true);
    	mPaintCreated.setStyle(Style.FILL);
    	mPaintCreated.setColor(createdColor);
    	mPaintDestroyed = new Paint();
    	mPaintDestroyed.setAntiAlias(true);
    	mPaintDestroyed.setStyle(Style.FILL);
    	mPaintDestroyed.setColor(destroyedColor);
		startNewGame();
		resetPendingEvent();
	}
	
    /*
     * Game loop
     */

	public void setThreadRunning(boolean running) {
		mRunLoop = running;
	}
	
	public void setPendingEvent(int type, float x, float y) {
		mEventPending = type;
		mEventX = (int) x;
		mEventY = (int) y;
	}
	
	private void resetPendingEvent() {
		mEventPending = EVENT_NONE;
		mEventX = 0;
		mEventY = 0;
	}
	
	private void sendStateMessage() {
		int state = getGameState();
		if (mLastStateMesage != state) {
			mLastStateMesage = state;
			Message message = new Message();
			message.what = state;
			mHandler.sendMessage(message);
		}
	}
	
	private int getGameState() {
		return GameControl.CURRENT.getState();
	}
	
	private void setGameState(int state) {
		GameControl.CURRENT.setState(state);
	}
	
	public void startNewGame() {
		mBoardModel = GameControl.CURRENT.getBoard();
		mProcessStates = true;
	}
	
	/*
	 * connects = groups of 3 pieces created when Player adds a new piece
	 * 
	 * chains = additional connects created when the board rearranges after removing
	 *          connects ('chain reactions')
	 *          
	 * rows = full rows of Android pieces
	 */
	
	@Override
	public void run() {

		// If we exited before created pieces were updated
		// the color might get stuck.
		unstickStuckPieces();
		
		while (mRunLoop) {
			
			if (mProcessStates && mSurfaceHolder.getSurface().isValid()) {
				Canvas canvas = null;
				try {
					canvas = mSurfaceHolder.lockCanvas(null);
					synchronized (mSurfaceHolder) {
						draw(canvas);
					}
				}
				finally {
					if (canvas != null)
						mSurfaceHolder.unlockCanvasAndPost(canvas);
				}
				switch (getGameState()) {
					case GameControl.STATE_PLAYER_PLAYING:
						loopPlayerPlaying();
						break;
					case GameControl.STATE_PLAYER_CONNECT:
						loopPlayerConnect();
						break;
					case GameControl.STATE_PLAYER_CONNECT_UPDATE:
						loopPlayerConnectUpdate();
						break;
					case GameControl.STATE_PLAYER_ROWS:
						loopPlayerRows();
						break;
					case GameControl.STATE_PLAYER_ROWS_UPDATE:
						loopPlayerRowsUpdate();
						break;					
					case GameControl.STATE_PLAYER_CHAIN:
						loopPlayerChain();
						break;
					case GameControl.STATE_PLAYER_CHAIN_UPDATE:
						loopPlayerChainUpdate();
						break;					
					case GameControl.STATE_PLAYER_FINISHED:
						loopPlayerFinished();
						break;
					case GameControl.STATE_ANDROID_PLAYING:
						loopAndroidPlaying();
						break;
					case GameControl.STATE_ANDROID_PLAYING_UPDATE:
						loopAndroidPlayingUpdate();
						break;
					case GameControl.STATE_ANDROID_ROWS:
						loopAndroidRows();
						break;
					case GameControl.STATE_ANDROID_ROWS_UPDATE:
						loopAndroidRowsUpdate();
						break;
					case GameControl.STATE_ANDROID_CHAIN:
						loopAndroidChain();
						break;
					case GameControl.STATE_ANDROID_CHAIN_UPDATE:
						loopAndroidChainUpdate();
						break;						
					case GameControl.STATE_ANDROID_FINISHED:
						loopAndroidFinished();
						break;
					case GameControl.STATE_GAME_OVER:
						mProcessStates = false;
						loopGameOver();
						break;
				}
				sendStateMessage();
			}
			pauseLoop(50);			
		}
	}
	
	private void pauseLoop(int millis) {
		try {
			sleep(millis);
		}
		catch (InterruptedException e) {
		}		
	}
	
	private void pauseForEffect() {
		pauseLoop(PAUSE_FOR_EFFECT);
	}
	
	private void loopPlayerPlaying() { 	// Intial State
		if (mEventPending == EVENT_PRESS && addPiecePlayer()) {
			setGameState(GameControl.STATE_PLAYER_CONNECT);
			resetPendingEvent();
		}		
	}
	
	private void loopPlayerConnect() {
		pauseForEffect();
		if (eliminateConnects(mNewPieceCol, mNewPieceRow)) {
			// Pieces removed - save model here because
			// we don't do it in eliminateConnects.
			GameControl.CURRENT.setBoard(mBoardModel);
			GameControl.CURRENT.incrementScore();
			setGameState(GameControl.STATE_PLAYER_CONNECT_UPDATE);
		}
		else {
			setGameState(GameControl.STATE_PLAYER_FINISHED);
		}
	}
	
	private void loopPlayerConnectUpdate() {
		pauseForEffect();
		clearExplodedPieces();
		collapseColumns();
		setGameState(GameControl.STATE_PLAYER_ROWS);
	}
	
	private void loopPlayerRows() {
		if (eliminateFullRows())
			setGameState(GameControl.STATE_PLAYER_ROWS_UPDATE);
		else
			setGameState(GameControl.STATE_PLAYER_CHAIN);
	}
	
	private void loopPlayerRowsUpdate() {
		pauseForEffect();
		clearExplodedPieces();
		collapseColumns();
		setGameState(GameControl.STATE_PLAYER_CHAIN);
	}
	
	private void loopPlayerChain() {
		if (eliminateNextChain()) {
			// Pieces removed - save model here because
			// we don't do it in eliminateNextChain.
			GameControl.CURRENT.setBoard(mBoardModel);
			GameControl.CURRENT.incrementScore();
			setGameState(GameControl.STATE_PLAYER_CHAIN_UPDATE);
		}
		else {
			setGameState(GameControl.STATE_PLAYER_FINISHED);
		}
	}
	
	private void loopPlayerChainUpdate() {
		pauseForEffect();
		clearExplodedPieces();
		collapseColumns();
		setGameState(GameControl.STATE_PLAYER_ROWS);
	}	
	
	private void loopPlayerFinished() {
		if (isGameOver())
			setGameState(GameControl.STATE_GAME_OVER);
		else
			setGameState(GameControl.STATE_ANDROID_PLAYING);
	}
	
	private void loopAndroidPlaying() {
		pauseForEffect();
		Random random = new Random();
		int retry = 0;
		int retryLimit = 100;
		// Try to add a piece to a random column
		while (retry < retryLimit) {
			int col = random.nextInt(mGridSize);
			if (addPieceToColumn(col, GameControl.CELL_CREATED)) {
				setGameState(GameControl.STATE_ANDROID_PLAYING_UPDATE);
				resetPendingEvent();
				break;
			}
			retry++;	
		}
		// If retries over limit pick first available row (shouldn't happen)
		if (retry >= retryLimit) {
			for (int col = 0; col < mGridSize; col++) {
				if (mBoardModel[col][mGridSize - 1] == GameControl.CELL_EMPTY) {
					addPieceToColumn(col, GameControl.CELL_CREATED);
					setGameState(GameControl.STATE_ANDROID_PLAYING_UPDATE);
					resetPendingEvent();
					break;					
				}
			}
		}
	}
	
	private void loopAndroidPlayingUpdate() {
		pauseForEffect();
		mBoardModel[mNewPieceCol][mNewPieceRow] = GameControl.CELL_ANDROID;
		GameControl.CURRENT.setBoard(mBoardModel);
		setGameState(GameControl.STATE_ANDROID_ROWS);
	}	
	
	private void loopAndroidRows() {
		if (eliminateFullRows())
			setGameState(GameControl.STATE_ANDROID_ROWS_UPDATE);
		else
			setGameState(GameControl.STATE_ANDROID_CHAIN);		
	}
	
	private void loopAndroidRowsUpdate() {
		pauseForEffect();
		clearExplodedPieces();
		collapseColumns();
		setGameState(GameControl.STATE_ANDROID_CHAIN);
	}
	
	private void loopAndroidChain() {
		if (eliminateNextChain()) {
			// Pieces removed - save model here because
			// we don't do it in eliminateNextChain.
			GameControl.CURRENT.setBoard(mBoardModel);
			GameControl.CURRENT.incrementScore();	
			setGameState(GameControl.STATE_ANDROID_CHAIN_UPDATE);
		}
		else {
			setGameState(GameControl.STATE_ANDROID_FINISHED);
		}
	}
	
	private void loopAndroidChainUpdate() {
		pauseForEffect();
		clearExplodedPieces();
		collapseColumns();
		setGameState(GameControl.STATE_ANDROID_ROWS);
	}		
	
	private void loopAndroidFinished() {
		if (isGameOver())
			setGameState(GameControl.STATE_GAME_OVER);
		else
			setGameState(GameControl.STATE_PLAYER_PLAYING);
	}
	
	private void loopGameOver() {
	}	
	
    /*
     * Game logic
     */
    
    private boolean addPiecePlayer() {
    	// Out of bounds - illegal move
    	if (mEventX < mBoardLeft) return false;
    	if (mEventX > mBoardLeft + mBoardSize - 2) return false;
    	if (mEventY < mBoardTop) return false;
    	if (mEventY > mBoardTop + mBoardSize - 2) return false;
    	// Column pressed
    	int col = (mEventX - mBoardLeft + 1) / mCellSize;
    	return addPieceToColumn(col, GameControl.CELL_PLAYER);
    }

    private boolean addPieceToColumn(int col, char cell) {
    	int max = mGridSize - 1;
    	if (col > max) col = max;
    	if (col < 0) col = 0;
    	int row = max;
    	// Column is full - illegal move
    	if (mBoardModel[col][row] != GameControl.CELL_EMPTY)
    		return false;
    	// Populate next available row in chosen column
    	while (row >= 0 && mBoardModel[col][row] == GameControl.CELL_EMPTY)
    		row--;
    	mBoardModel[col][row + 1] = cell;
    	mNewPieceCol = col;
    	mNewPieceRow = row + 1;
    	// Save board model
    	GameControl.CURRENT.setBoard(mBoardModel);
    	return true;
    }
    
    private void collapseColumns() {
    	for (int col = 0; col < mGridSize; col++) {
	    	StringBuilder sb = new StringBuilder(mGridSize);
	    	for (int row = 0; row < mGridSize; row++) {
	    		if (mBoardModel[col][row] != GameControl.CELL_EMPTY)
	    			sb.append(mBoardModel[col][row]);
	    		mBoardModel[col][row] = GameControl.CELL_EMPTY;
	    	}
	    	String s = sb.toString();
	    	for (int i = 0, l = s.length(); i < l; i++) {
	    		mBoardModel[col][i] = s.charAt(i);
	    	}
    	}
    	// Save the model
    	GameControl.CURRENT.setBoard(mBoardModel);
    }
    
    private boolean isGameOver() {
    	for (int col = 0; col < mGridSize; col++)
    		if (mBoardModel[col][mGridSize - 1] == GameControl.CELL_EMPTY)
    			return false;
    	return true;
    }
    
    private boolean eliminateConnects(int col, int row) {
    	char empty = GameControl.CELL_EMPTY;
    	char destroyed = GameControl.CELL_DESTROYED; 
    	char player = GameControl.CELL_PLAYER;
    	char area[][] = {
    			{empty, empty, empty, empty, empty},
    			{empty, empty, empty, empty, empty},
    			{empty, empty, empty, empty, empty},
    			{empty, empty, empty, empty, empty},
    			{empty, empty, empty, empty, empty}
    	};
    	// Populate the detection grid
    	// e.g.
    	// area[0][4] = mBoardModel[col - 2][row + 2];
    	// area[0][3] = mBoardModel[col - 2][row + 1];
    	// area[0][2] = mBoardModel[col - 2][row];
    	// area[0][1] = mBoardModel[col - 2][row - 1];
    	// area[0][0] = mBoardModel[col - 2][row - 2];
    	// ...
    	// area[4][4] = mBoardModel[col + 2][row + 2];
    	// area[4][3] = mBoardModel[col + 2][row + 1];
    	// area[4][2] = mBoardModel[col + 2][row];
    	// area[4][1] = mBoardModel[col + 2][row - 1];
    	// area[4][0] = mBoardModel[col + 2][row - 2];
    	//
    	for (int c = 0; c < 5; c++)
    		for (int r = 0; r < 5; r++)
    			area[c][4 - r] = getCellContent(col - 2 + c, row + 2 - r); 
    	//
    	// ---------------------
    	// |0,4|1,4|2,4|3,4|4,4|
    	// ---------------------
    	// |0,3|1,3|2,3|3,3|4,3|
    	// ---------------------
    	// |0,2|1,2|2,2|3,2|4,2|   col,row => 2,2
    	// ---------------------
    	// |0,1|1,1|2,1|3,1|4,1|
    	// ---------------------
    	// |0,0|1,0|2,0|3,0|4,0|
    	// ---------------------
    	//
    	// Find the connects (centred then extended)
    	//
    	//                       7     5 
    	//     \ /                \   /
    	//   1--O--   then    1 --  O  -- 2 
    	//     / \                / | \
    	//    2   3              4  3  6
    	//
    	if (area[1][2] == player && area[3][2] == player) { // Horizontal
    		mBoardModel[col - 1][row] = destroyed;
    		mBoardModel[col][row] = destroyed;
    		mBoardModel[col + 1][row] = destroyed;
    		return true;
    	}
    	if (area[1][1] == player && area[3][3] == player) { // Slash
    		mBoardModel[col - 1][row - 1] = destroyed;
    		mBoardModel[col][row] = destroyed;
    		mBoardModel[col + 1][row + 1] = destroyed;
    		return true;
    	}
    	if (area[1][3] == player && area[3][1] == player) { // Backslash
    		mBoardModel[col - 1][row + 1] = destroyed;
    		mBoardModel[col][row] = destroyed;
    		mBoardModel[col + 1][row - 1] = destroyed;
    		return true;
    	}
    	if (area[0][2] == player && area[1][2] == player) { // 9 o'clock
    		mBoardModel[col - 2][row] = destroyed;
    		mBoardModel[col - 1][row] = destroyed;
    		mBoardModel[col][row] = destroyed;
    		return true;
    	}
    	if (area[3][2] == player && area[4][2] == player) { // 3 o'clock
    		mBoardModel[col][row] = destroyed;
    		mBoardModel[col + 1][row] = destroyed;
    		mBoardModel[col + 2][row] = destroyed;
    		return true;
    	}
    	if (area[2][1] == player && area[2][0] == player) { // 6 o'clock
    		mBoardModel[col][row - 2] = destroyed;
    		mBoardModel[col][row - 1] = destroyed;
    		mBoardModel[col][row] = destroyed;
    		return true;
    	}
    	if (area[0][0] == player && area[1][1] == player) { // 7:30
    		mBoardModel[col - 2][row - 2] = destroyed;
    		mBoardModel[col - 1][row - 1] = destroyed;
    		mBoardModel[col][row] = destroyed;
    		return true;
    	}   
    	if (area[3][3] == player && area[4][4] == player) { // 1:30
    		mBoardModel[col][row] = destroyed;
    		mBoardModel[col + 1][row + 1] = destroyed;
    		mBoardModel[col + 2][row + 2] = destroyed;
    		return true;
    	}    	
    	if (area[3][1] == player && area[4][0] == player) { // 4:30
    		mBoardModel[col][row] = destroyed;
    		mBoardModel[col + 1][row - 1] = destroyed;
    		mBoardModel[col + 2][row - 2] = destroyed;
    		return true;
    	}     	
    	if (area[0][4] == player && area[1][3] == player) { // 10:30
    		mBoardModel[col - 2][row + 2] = destroyed;
    		mBoardModel[col - 1][row + 1] = destroyed;
    		mBoardModel[col][row] = destroyed;
    		return true;
    	}
    	return false;
    }
    
    private boolean eliminateNextChain() {
		for (int col = 0; col < mGridSize; col++)
			for (int row = 0; row < mGridSize; row++)
				if (eliminateChain(col, row))
					return true; // repeat
		return false;
    }
    
    /*
     * Note, the matching order can differ between a connect search and a chain search.
     * Connect search uses a 7x7 grid while a chain search uses a 3x3.
     * The logical way to run the searches is to always use the chain search, this way
     * the order in which pieces are matched to groups of three will be consistent.
     * However, a basic playability principle requires that adding a piece between two
     * others would remove that group of three.
     * 
     *   O                O  
     *   |                |
     *   v                v
     * OO O  = OXXX and  O OO  = XXXO
     * 
     * Using the chain search only would always remove from the left:
     * 
     *   O                O  
     *   |                |
     *   v                v
     * OO O  = XXXO and  O OO  = XXXO
     * 
     * This is logically consistent but counter-intuitive, so we allow an inconsistency
     * between the searches when removing a freshly added piece and an already placed piece.
     */
    private boolean eliminateChain(int col, int row) {
    	if (mBoardModel[col][row] != GameControl.CELL_PLAYER)
    		return false;
    	char empty = GameControl.CELL_EMPTY;
    	char destroyed = GameControl.CELL_DESTROYED; 
    	char player = GameControl.CELL_PLAYER;
    	char area[][] = {
    			{empty, empty, empty},
    			{empty, empty, empty},
    			{empty, empty, empty}
    	};
    	//
    	// Populate the detection grid
    	//
    	// area[0][2] = mBoardModel[col - 1][row + 1];
    	// area[0][1] = mBoardModel[col - 1][row];
    	// area[0][0] = mBoardModel[col - 1][row - 1]; 
    	// ...
    	// area[2][2] = mBoardModel[col + 1][row + 1];
    	// area[2][1] = mBoardModel[col + 1][row];
    	// area[2][0] = mBoardModel[col + 1][row - 1];    	
    	//
    	for (int c = 0; c < 3; c++)
    		for (int r = 0; r < 3; r++)
    			area[c][2 - r] = getCellContent(col - 1 + c, row + 1 - r);
    	//
    	// -------------
    	// |0,2|1,2|2,2|
    	// -------------
    	// |0,1|1,1|2,1|   col,row => 1,1
    	// -------------
    	// |0,0|1,0|2,0|
    	// -------------
    	//
    	// Find the chains (cascade connects)
    	if (area[0][1] == player && area[2][1] == player) { // Horizontal
    		mBoardModel[col - 1][row] = destroyed;
    		mBoardModel[col][row] = destroyed;
    		mBoardModel[col + 1][row] = destroyed;
    		return true;
    	}
    	if (area[0][0] == player && area[2][2] == player) { // Slash
    		mBoardModel[col - 1][row - 1] = destroyed;
    		mBoardModel[col][row] = destroyed;
    		mBoardModel[col + 1][row + 1] = destroyed;
    		return true;
    	}
    	if (area[1][2] == player && area[1][0] == player) { // Vertical
    		mBoardModel[col][row + 1] = destroyed;
    		mBoardModel[col][row] = destroyed;
    		mBoardModel[col][row - 1] = destroyed;
    		return true;
    	}
    	if (area[0][2] == player && area[2][0] == player) { // Backslash
    		mBoardModel[col - 1][row + 1] = destroyed;
    		mBoardModel[col][row] = destroyed;
    		mBoardModel[col + 1][row - 1] = destroyed;
    		return true;
    	}    	
    	return false;
    }
    
    private char getCellContent(int col, int row) {
    	if (col >= 0 && col < mGridSize && row >= 0 && row < mGridSize)
    		return mBoardModel[col][row];
    	else
    		return GameControl.CELL_EMPTY;
    }
    
    private boolean eliminateFullRows() {
    	char target = GameControl.CELL_ANDROID; 
    	char exploding = GameControl.CELL_DESTROYED;
    	boolean update = false;
    	// Check every row
    	for (int row = 0; row < mGridSize; row++) {
    		boolean fullRow = true;
    		// Skip if not all target type
    		for (int col = 0; col < mGridSize; col++) {
    			if (mBoardModel[col][row] != target) {
    				fullRow = false;
    				break;
    			}
    		}
    		if (fullRow) {
    			// Eliminate the row
    			for (int col = 0; col < mGridSize; col++)
    				mBoardModel[col][row] = exploding;
    			update = true;
    		}
    	}
    	return update;
    }
    
    private void clearExplodedPieces() {
    	for (int col = 0; col < mGridSize; col++)
    		for (int row = 0; row < mGridSize; row++)
    			if (mBoardModel[col][row] == GameControl.CELL_DESTROYED)
    				mBoardModel[col][row] = GameControl.CELL_EMPTY;
		GameControl.CURRENT.setBoard(mBoardModel);
    }
    
    private void unstickStuckPieces() {
    	for (int col = 0; col < mGridSize; col++)
    		for (int row = 0; row < mGridSize; row++)
    			if (mBoardModel[col][row] == GameControl.CELL_CREATED)
    				mBoardModel[col][row] = GameControl.CELL_ANDROID;
		GameControl.CURRENT.setBoard(mBoardModel);
    }

	/*
	 * Drawing
	 */
	
    private void draw(Canvas canvas) {
    	// Get the dimensions
    	Rect bounds = mSurfaceHolder.getSurfaceFrame();
    	int padding = 10;
    	int width = bounds.right - bounds.left;
    	int height = bounds.bottom - bounds.top;
    	int size = (width < height ? width : height) - padding;
    	mCellSize = size / mGridSize;
    	mBoardSize = mCellSize * mGridSize;
    	mBoardLeft = (width - mBoardSize) / 2;
    	mBoardTop = (height - mBoardSize) / 2;
    	// Draw the board
    	canvas.drawRect(mBoardLeft, mBoardTop, mBoardLeft + mBoardSize,
    			mBoardTop + mBoardSize, mPaintBoard);
    	for (int i = 1; i < mGridSize; i++) {
    		float y = mBoardTop + (i * mCellSize);
    		canvas.drawLine(mBoardLeft, y, mBoardLeft + mBoardSize, y, mPaintGrid);
    	}
    	for (int i = 1; i < mGridSize; i++) {
    		float x = mBoardLeft + (i * mCellSize);
    		canvas.drawLine(x, mBoardTop + mBoardSize, x, mBoardTop, mPaintGrid);
    	}
    	canvas.drawRect(mBoardLeft, mBoardTop, mBoardLeft + mBoardSize,
    			mBoardTop + mBoardSize, mPaintBorder);
    	// Update the cells
		for (int c = 0; c < mGridSize; c++) {
			for (int r = 0; r < mGridSize; r++) {
				if (mBoardModel[c][r] == GameControl.CELL_PLAYER)
					drawCell(c, r, canvas, mPaintPlayer);
				else if (mBoardModel[c][r] == GameControl.CELL_ANDROID)
					drawCell(c, r, canvas, mPaintAndroid);
				else if (mBoardModel[c][r] == GameControl.CELL_CREATED)
					drawCell(c, r, canvas, mPaintCreated);
				else if (mBoardModel[c][r] == GameControl.CELL_DESTROYED)
					drawCell(c, r, canvas, mPaintDestroyed);
			}
		}
    }
    
    private void drawCell(int col, int row, Canvas c, Paint p) {
    	float padding = 1;
    	float x = mBoardLeft + (col * mCellSize) + 1;
    	float y = mBoardTop + ((mGridSize - 1 - row) * mCellSize) + 1;
    	float r = (mCellSize - padding - padding) / 2;
    	int color = p.getColor();
    	float gradR = (r * 2) / 3;
    	RadialGradient grad = new RadialGradient(x + padding + gradR, y + padding + gradR, r / 2,
    			Color.WHITE, color, TileMode.CLAMP);
    	p.setShader(grad);
    	c.drawCircle(x + padding + r, y + padding + r, r, p);
    }
}
