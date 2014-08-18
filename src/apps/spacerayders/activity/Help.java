package apps.spacerayders.activity;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

import apps.spacerayders.activity.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Help extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.how_to_play);
	}

	@Override
	public void onBackPressed() {
		Intent startmenu = new Intent(this, SplashActivity.class);
		startActivity(startmenu);
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

	public void goToActivity(View view) {
		Intent dir = new Intent(this, DirectionsActivity.class);
		startActivity(dir);
	}

	public void goToPointsActivity(View view) {
		Intent dir = new Intent(this, Points.class);
		startActivity(dir);
	}

	public void goToPlayGameActivity(View view) {
		Intent dir = new Intent(this, TheGame.class);
		startActivity(dir);
	}

	public void onLoad(long loadTime) {

		// May return null if EasyTracker has not been initialized with a
		// property
		// ID.
		Tracker easyTracker = EasyTracker.getInstance(this);

		easyTracker.send(MapBuilder.createTiming("resources", // Timing category
				// (required)
				loadTime, // Timing interval in milliseconds (required)
				"Help", // Timing name
				null) // Timing label
				.build());
	}

}