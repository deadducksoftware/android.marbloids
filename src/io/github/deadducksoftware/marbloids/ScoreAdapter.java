package io.github.deadducksoftware.marbloids;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ScoreAdapter extends ArrayAdapter<Score> {

	private Context mContext;
	private int mLayoutId;
	private ArrayList<Score> mScores;	
	
	public ScoreAdapter(Context context, int layoutId, int textViewId, ArrayList<Score> hiScores) {
		super(context, layoutId, textViewId, hiScores);
		mContext = context;
		mLayoutId = layoutId;
		mScores = hiScores;
	}
	
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View layoutView = convertView;
        HiScoreHolder holder = null;
        if (layoutView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            layoutView = inflater.inflate(mLayoutId, parent, false);
            holder = new HiScoreHolder();
            holder.txtScore = (TextView) layoutView.findViewById(R.id.hiscoresListScore);
            holder.txtPlayer = (TextView) layoutView.findViewById(R.id.hiscoresListPlayer);
            layoutView.setTag(holder);
        }
        else {
            holder = (HiScoreHolder) layoutView.getTag();
        }
        Score hiScore = mScores.get(position);
        holder.txtScore.setText(hiScore.getScore());
        holder.txtPlayer.setText(hiScore.getPlayer());
        return layoutView;
    }
    
    private class HiScoreHolder {
    	TextView txtScore;
    	TextView txtPlayer;
    }
}
