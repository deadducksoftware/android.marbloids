package io.github.deadducksoftware.marbloids;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class HelpActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		/*
		 * Get the application version number from the
		 * manifest, default to '1.0' if not available.
		 */
		String versionName = "1.0";
		try {
			PackageManager pm = getPackageManager();
			PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
			versionName = info.versionName;
		}
		catch (NameNotFoundException e) {
		}
		TextView aboutVersion = (TextView) findViewById(R.id.aboutVersion);
		aboutVersion.setText(getString(R.string.aboutVersion, versionName));
		
		Button prefsButton = (Button) findViewById(R.id.prefsButton);
		prefsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openPrefs();
			}
		});
	}
	
	private void openPrefs() {
		startActivity(new Intent(this, PrefsActivity.class));
	}
}
