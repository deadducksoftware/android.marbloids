package io.github.deadducksoftware.marbloids;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class ScoresActivity extends Activity {
	
	private DataStore mDataStore;
	private TextView mMessageView;
	private ListView mListView;
	private ArrayList<Score> mScoreList;
	private ScoreAdapter mScoreAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scores);
		mDataStore = new DataStore(this);
		mMessageView = (TextView) findViewById(R.id.scoresMessage);
        mListView = (ListView) findViewById(R.id.scoresList);
        mListView.setDividerHeight(0);
        updateListView();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDataStore != null)
			mDataStore.close();
	}
	
	private void updateListView() {
		mDataStore.deleteLowScoringGames(GameControl.SCORES_TOTAL);
		mScoreList = mDataStore.getScores();
		if (mScoreList != null) {
			mScoreAdapter = new ScoreAdapter(this,
					R.layout.scores_list,
					R.id.hiscoresListPlayer, mScoreList);
			mListView.setAdapter(mScoreAdapter);
			mMessageView.setVisibility(mScoreList.size() == 0 ?
					View.VISIBLE : View.GONE);
		}
		else {
			mMessageView.setVisibility(View.VISIBLE);
		}
	}
}
