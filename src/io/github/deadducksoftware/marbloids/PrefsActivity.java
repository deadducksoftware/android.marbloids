package io.github.deadducksoftware.marbloids;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;

public class PrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	public static final String PLAYER_NAME = "prefsPlayerName";
	public static final String PLAYER_COLOR = "prefsPlayerColor";
	public static final String ANDROID_COLOR = "prefsAndroidColor";
	public static final String CREATED_COLOR = "prefsCreatedColor";
	public static final String DESTROYED_COLOR = "prefsDestroyedColor";

	public static final String PLAYER_NAME_DEFAULT = "Player";
	public static final int PLAYER_COLOR_DEFAULT = 0xffff0000;
	public static final int ANDROID_COLOR_DEFAULT = 0xffa0a0a0;
	public static final int CREATED_COLOR_DEFAULT = 0xffffffff;
	public static final int DESTROYED_COLOR_DEFAULT = 0xff00ff00;

	private SharedPreferences mSharedPreferences;
	private EditTextPreference mPlayerName;
	private ColorDialogPreference mPlayerColor;
	private ColorDialogPreference mAndroidColor;
	private ColorDialogPreference mCreatedColor;
	private ColorDialogPreference mDestroyedColor;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.prefsTitle);
		addPreferencesFromResource(R.xml.prefs);
		mSharedPreferences = getPreferenceScreen().getSharedPreferences();
		mPlayerName = (EditTextPreference) findPreference(PLAYER_NAME);
		mPlayerColor = (ColorDialogPreference) findPreference(PLAYER_COLOR);
		mAndroidColor = (ColorDialogPreference) findPreference(ANDROID_COLOR);
		mCreatedColor = (ColorDialogPreference) findPreference(CREATED_COLOR);
		mDestroyedColor = (ColorDialogPreference) findPreference(DESTROYED_COLOR);
		mPlayerColor.setDefaultColor(PLAYER_COLOR_DEFAULT);
		mAndroidColor.setDefaultColor(ANDROID_COLOR_DEFAULT);
		mCreatedColor.setDefaultColor(CREATED_COLOR_DEFAULT);
		mDestroyedColor.setDefaultColor(DESTROYED_COLOR_DEFAULT);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
		updatePlayerName();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if (key.equals(PLAYER_NAME))
			updatePlayerName();
		else if (key.equals(PLAYER_COLOR))
			mPlayerColor.updateSummaryColor();
		else if (key.equals(ANDROID_COLOR))
			mAndroidColor.updateSummaryColor();
		else if (key.equals(CREATED_COLOR))
			mCreatedColor.updateSummaryColor();
		else if (key.equals(DESTROYED_COLOR))
			mDestroyedColor.updateSummaryColor();
	}
	
	private void updatePlayerName() {
		String name = mPlayerName.getText().trim();
		if (name.length() == 0)
			mPlayerName.setSummary(R.string.prefsPlayerNameSummaryEmpty);
		else
			mPlayerName.setSummary(getString(
					R.string.prefsPlayerNameSummary, mPlayerName.getText()));
	}
}
