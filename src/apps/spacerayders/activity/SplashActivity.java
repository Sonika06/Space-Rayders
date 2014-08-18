package apps.spacerayders.activity;

import java.util.ArrayList;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.google.android.gms.games.Games;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import apps.spacerayders.data.GlobalState;

import apps.spacerayders.activity.R;

public class SplashActivity extends BluetoothActivity {

	private long levelDurationMili;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startjoin);

		setAdapter();
		setupBluetoothDetection();
		enableBluetooth();

		findViewById(R.id.show_achievements).setOnClickListener(this);
		findViewById(R.id.show_leaderboard).setOnClickListener(this);

		findViewById(R.id.sign_in_button).setOnClickListener(this);
		findViewById(R.id.sign_out_button).setOnClickListener(this);

		// getRoot();
	}

	@Override
	public void onStart() {
		super.onStart();
		// The rest of your onStart() code.
		EasyTracker.getInstance(this).activityStart(this); // Add this method.
		initItOrders();
	}

	@Override
	public void onStop() {
		super.onStop();
		// The rest of your onStop() code.
		EasyTracker.getInstance(this).activityStop(this); // Add this method.
	}

	public void initLevelFeatures(int level) {
		switch (level) {
		case 1:
			levelDurationMili = 16000;

			break;
		case 2:
			levelDurationMili = 31000;

			break;
		case 3:
			levelDurationMili = 46000;

			break;
		case 4:
			levelDurationMili = 61000;

			break;
		case 5:
			levelDurationMili = 76000;

			break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.startGameOption:
			Intent loadSettings = new Intent(this, SettingsActivity.class);
			startActivity(loadSettings);
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}
	}

	/*
	 * private void getRoot(){ Process p; try { p =
	 * Runtime.getRuntime().exec("su"); } catch (Exception e) {
	 * Log.e("ROOT","Getting root priveleges failed for some reason"); } }
	 */

	private void initItOrders() {
		GlobalState.itLists = new ArrayList<String[]>();

		String[] list1 = { "Red", "Blue", "Green", "Silver", "Black" };
		String[] list2 = { "Red", "Green", "Black", "Silver", "Blue" };
		String[] list3 = { "Blue", "Green", "Silver", "Red", "Black" };
		String[] list4 = { "Blue", "Black", "Silver", "Green", "Red" };
		String[] list5 = { "Green", "Silver", "Black", "Red", "Blue" };
		String[] list6 = { "Green", "Red", "Blue", "Black", "Silver" };
		String[] list7 = { "Red", "Green", "Black", "Silver", "Blue" };
		String[] list8 = { "Blue", "Silver", "Black", "Red", "Green" };
		String[] list9 = { "Silver", "Blue", "Black", "Red", "Green" };
		String[] list10 = { "Silver", "Red", "Green", "Blue", "Black" };
		String[] list11 = { "Black", "Blue", "Green", "Silver", "Red" };
		String[] list12 = { "Black", "Green", "Silver", "Blue", "Red" };

		GlobalState.itLists.add(list1);
		GlobalState.itLists.add(list2);
		GlobalState.itLists.add(list3);
		GlobalState.itLists.add(list4);
		GlobalState.itLists.add(list5);
		GlobalState.itLists.add(list6);
		GlobalState.itLists.add(list7);
		GlobalState.itLists.add(list8);
		GlobalState.itLists.add(list9);
		GlobalState.itLists.add(list10);
		GlobalState.itLists.add(list11);
		GlobalState.itLists.add(list12);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.startgame, menu);
		return true;
	}

	public void gotoDevicesTrackerActivity(View view) {
		if (GlobalState.currentPlayers != null
				&& GlobalState.currentPlayers.size() > 0) {
			if (adapter.getName().equals(GlobalState.playerName)) {
				// Log.d("Tag", "Playing with: " +
				// GlobalState.currentPlayers.toString());
				Intent loadGame = new Intent(this, DevicesTrackerActivity.class);
				startActivity(loadGame);
			} else {
				Toast.makeText(this, "You forgot to add yourself as a player.",
						Toast.LENGTH_LONG);
			}
		} else {
			Toast.makeText(this,
					"You must select a list of players from Settings first.",
					Toast.LENGTH_LONG).show();
		}
	}

	public void goToHelp(View view) {
		Intent help = new Intent(this, Help.class);
		startActivity(help);

	}

	public void goToHigh(View view) {
		Intent help = new Intent(this, FinalScoreActivity.class);
		startActivity(help);

	}

	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}

	public void onLoad(long loadTime) {

		// May return null if EasyTracker has not been initialized with a
		// property
		// ID.
		Tracker easyTracker = EasyTracker.getInstance(this);

		easyTracker.send(MapBuilder.createTiming("resources", // Timing category
				// (required)
				loadTime, // Timing interval in milliseconds (required)
				"Main", // Timing name
				null) // Timing label
				.build());
	}

	@Override
	public void onSignInSucceeded() {
		// show sign-out button, hide the sign-in button
		findViewById(R.id.sign_in_button).setVisibility(View.GONE);
		findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);

		// (your code here: update UI, enable functionality that depends on sign
		// in, etc)
	}

	@Override
	public void onSignInFailed() {
		// Sign in has failed. So show the user the sign-in button.
		findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
		findViewById(R.id.sign_out_button).setVisibility(View.GONE);
	}

	@Override
	public void onClick(View view) {

		if (view.getId() == R.id.sign_in_button) {
			// start the asynchronous sign in flow
			beginUserInitiatedSignIn();
		} else if (view.getId() == R.id.sign_out_button) {

			signOut();

			// show sign-in button, hide the sign-out button
			findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
			findViewById(R.id.sign_out_button).setVisibility(View.GONE);
		}

		else if (view.getId() == R.id.show_achievements) {

			if (getApiClient().isConnected()) {
				startActivityForResult(
						Games.Achievements
								.getAchievementsIntent(getApiClient()),
						1);
			} else {
				beginUserInitiatedSignIn();
			}
		} else if (view.getId() == R.id.show_leaderboard) {

			if (getApiClient().isConnected()) {
				startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
						getApiClient(),
						getString(R.string.number_guesses_leaderboard)), 2);
			} else {
				beginUserInitiatedSignIn();
			}
		}

	}

}
