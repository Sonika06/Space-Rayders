package apps.spacerayders.activity;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import apps.spacerayders.data.GlobalState;

import apps.spacerayders.activity.R;

public class FinalScoreActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.final_score);
		setBackground();

		TextView scoreView = (TextView) findViewById(R.id.scoreList);

		SharedPreferences scorePrefs = getSharedPreferences(
				DevicesTrackerActivity.GAME_PREFS, 0);
		// scores
		String[] savedScores = scorePrefs.getString("highScores", "").split(
				"\\|");

		StringBuilder scoreBuild = new StringBuilder("");
		for (String score : savedScores) {
			scoreBuild.append(score + "\n");
		}
		// display
		scoreView.setText(scoreBuild.toString());
	}

	public void start(View view) {
		Intent menu = new Intent(this, SplashActivity.class);
		startActivity(menu);
	}

	@Override
	public void onStart() {
		super.onStart();
		// The rest of your onStart() code.
		EasyTracker.getInstance(this).activityStart(this); // Add this method.

	}

	@Override
	public void onStop() {
		super.onStop();
		// The rest of your onStop() code.
		EasyTracker.getInstance(this).activityStop(this); // Add this method.
	}

	//
	// @Override
	// public void onStart() {
	// super.onStart();
	// init();
	// BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
	// }
	//
	// private void init() {
	// TextView scoreView = (TextView) findViewById(R.id.scoreList);
	// scoreView.setText(Integer.toString(GlobalState.myScore));
	// }

	private void setBackground() {
		RelativeLayout relLay = (RelativeLayout) findViewById(R.id.scoreLayout);
		Drawable bg = relLay.getBackground();
		bg.setAlpha(100);
	}

	@Override
	public void onBackPressed() {
		gotoSplashActivity();
	}

	private void gotoSplashActivity() {
		Intent restart = new Intent(this, SplashActivity.class);
		startActivity(restart);
	}

	public void onLoad(long loadTime) {

		// May return null if EasyTracker has not been initialized with a
		// property
		// ID.
		Tracker easyTracker = EasyTracker.getInstance(this);

		easyTracker.send(MapBuilder.createTiming("resources", // Timing category
				// (required)
				loadTime, // Timing interval in milliseconds (required)
				"HighScore", // Timing name
				null) // Timing label
				.build());
	}

}
