package apps.spacerayders.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.games.Games;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import apps.spacerayders.data.GlobalState;

import apps.spacerayders.activity.R;

/**
 * This activity is ran during the actual game play. It extends the
 * RemoteServiceClient which is used to bind to another app used for monitoring
 * and recording the Accelerometer data. RemoteServiceClient extends the
 * BluetoothActivity class which gives this class access to all of the necessary
 * methods to use Bluetooth successfully.
 */
public class DevicesTrackerActivity extends RemoteServiceClient {

	private SharedPreferences gamePrefs;
	public static final String GAME_PREFS = "Space Rayders";

	private Intent loadScore;
	private Intent startIntent;

	private Intent currIntent;

	private Timer cancelScheduler;
	private Timer startScheduler;
	private Timer itScheduler;

	private CancelDiscoveryTask cancelTask;
	private ChangeItTask itTask;

	private TextView scoreBoard;
	private int score;
	private String[] playerList;
	private String it;
	private int index = 0;
	private int foundCount = 0;

	private SoundPool pool;
	private int soundID;

	private static final int START_SCORE = 0;
	private static final int SCORE_OFFSET = 10;
	private static final int GAME_DURATION = 300;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.devices);

		gamePrefs = getSharedPreferences(GAME_PREFS, 0);

		// Adds the flag to keep the app awake during game play
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

		scoreBoard = (TextView) findViewById(R.id.scoreLabel);

		if (savedInstanceState != null) {
			int oldScore = savedInstanceState.getInt("score");
			scoreBoard.setText("Score: " + oldScore);
		}

	}

	@Override
	public void onStart() {
		super.onStart();

		index = 0;
		init();

	}

	private int getScore() {

		int fade = 0;
		String scoreStr = scoreBoard.getText().toString();
		try {
			fade = Integer.parseInt(scoreStr.substring(scoreStr
					.lastIndexOf(" ") + 1));
		} catch (Exception e) { /* log if you want */
			// // fade = 1500;
			// catch (NumberFormatException e) {
			// Log.e("Exception", e.toString());
		}

		return fade;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		int oldScore = getScore();

		savedInstanceState.putInt("score", oldScore);
		// savedInstanceState.putInt("level", level);
		super.onSaveInstanceState(savedInstanceState);
	}

	protected void onDestroy() {
		setHighScore();

		super.onDestroy();
	}

	private void setHighScore() {
		// int oldScore = GlobalState.myScore;
		int oldScore = getScore();

		if (oldScore > 0) {
			SharedPreferences.Editor scoreEdit = gamePrefs.edit();

			DateFormat dateForm = new SimpleDateFormat("dd MMMM");
			String dateOutput = dateForm.format(new Date());

			String scores = gamePrefs.getString("highScores", "");

			if (scores.length() > 0) {
				List<Score> scoreStrings = new ArrayList<Score>();
				String[] oldScores = scores.split("\\|");
				for (String eSc : oldScores) {
					String[] parts = eSc.split(" - ");
					scoreStrings.add(new Score(parts[0], Integer
							.parseInt(parts[1])));
				}
				Score newScore = new Score(dateOutput, oldScore);
				scoreStrings.add(newScore);
				Collections.sort(scoreStrings);
				StringBuilder scoreBuild = new StringBuilder("");
				for (int s = 0; s < scoreStrings.size(); s++) {
					if (s >= 10)
						break;
					if (s > 0)
						scoreBuild.append("|");
					scoreBuild.append(scoreStrings.get(s).getScoreText());
				}
				scoreEdit.putString("highScores", scoreBuild.toString());
				scoreEdit.commit();

			} else {
				scoreEdit.putString("highScores", "" + dateOutput + " - "
						+ oldScore);
				scoreEdit.commit();
			}
		}
	}

	private void init() {
		initSound(); // sets up the sound effects for the gameplay
		resetScore();
		setPlayerList(); // sets the order of the player list up
		setIntents();

		gameStart(); // binds to accelerometer service if it exists

		initTimerItems();

		score = START_SCORE;
		GlobalState.myScore = score;
		scoreBoard.setText("Score: " + score);

		setAdapter(); // must be called before discovarableAccepeted();
		setUpBluetooth(); // enables bluetooth and prompts the user
	}

	/**
	 * After the user is prompted to enable bluetooth and start discovery mode
	 * this method handles the request that comes back. Once enabling and
	 * discovering is confirmed then setItOrder() is called.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == BluetoothActivity.REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this,
						"You must enable Bluetooth to play this game",
						Toast.LENGTH_LONG).show();
				startActivity(startIntent);
			}
		}
		if (requestCode == BluetoothActivity.REQUEST_DISCOV_BT) {
			if (resultCode == GAME_DURATION) {
				setItOrder();
			}
			if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this,
						"You must be discoverable to play this game",
						Toast.LENGTH_LONG).show();
				startActivity(startIntent);
			}
		}
	}

	private void resetScore() {
		GlobalState.myScore = START_SCORE;
	}

	/**
	 * Uses the current minute (1 - 60) to create a unique order for the player
	 * list. Dividing the minute by 5 gives the option of 12 different ordered
	 * lists. These lists were created in the SplashActivity for reference.
	 */
	private void setPlayerList() {
		Calendar c = Calendar.getInstance();
		int itOrderIndex = c.get(Calendar.MINUTE) / 5;
		playerList = GlobalState.itLists.get(itOrderIndex);
		GlobalState.itOrder = playerList;
	}

	private void setIntents() {
		loadScore = new Intent(this, FinalScoreActivity.class);
		startIntent = new Intent(this, SplashActivity.class);
	}

	/**
	 * The interval is set to be approximately one minute. This restriction is
	 * due to Bluetooth only being able to be discoverable for 5 minutes with
	 * Android 2.0.1. Since there are 5 players this interval time works.
	 */
	private void setItOrder() {
		// int length = playerList.length;
		int interval = 62000; // (GAME_DURATION / length) * 1000;
		// 62000
		updateIt();
		setItLabel();
		setItScheduling(interval);
		playItAlert();
	}

	private void updateIt() {
		if (index < playerList.length) {
			String player = playerList[index];
			if (isPlaying(player)) {
				it = player;
			} else {
				setNextIt();
			}
		} else {
			setNextIt();
		}
	}

	/**
	 * Sets the scheduler that changes who is it every minute.
	 */
	private void setItScheduling(int interval) {
		itScheduler = new Timer();
		itTask = new ChangeItTask();
		itScheduler.schedule(itTask, interval, interval);
	}

	private void setItLabel() {
		TextView itLabel = (TextView) findViewById(R.id.itLabel);
		itLabel.setText(it + " is IT");
	}

	private void initTimerItems() {
		startScheduler = new Timer();
		cancelTask = new CancelDiscoveryTask();
	}

	protected void setUpBluetooth() {
		super.setupBluetoothDetection();
		super.enableBluetooth();
		super.makeDiscoverable(GAME_DURATION);
		// Log.e("TIME EXTENDED", "1 minute");
		// ensureBluetoothDiscoverability();
	}

	@Override
	protected void registerListeners() {
		super.registerListeners();
		registerListener(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
	}

	@Override
	public void onStop() {
		stopAdapter();
		cancelScheduledTasks();

		super.onStop();
	}

	@Override
	public void onBackPressed() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					onStop();

					releaseService();
					it = "nobody";
					startActivity(startIntent);
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					dialog.cancel();
					break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to quit?")
				.setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("No", dialogClickListener).show();
	}

	/*
	 * @Override public void onAttachedToWindow() {
	 * this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
	 * super.onAttachedToWindow(); }
	 */

	private void stopAdapter() {
		adapter.cancelDiscovery();
	}

	private String intToString(int value) {
		return Integer.toString(value);
	}

	@Override
	protected BroadcastReceiver initReceiver() {
		return new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				actionHandler(action, intent);
			}
		};
	}

	private void actionHandler(String action, Intent intent) {
		if (isActionFound(action))
			deviceDiscoveredHandler(intent);
		else if (isDiscoveryFinished(action))
			discoveryFinishedHandler();
	}

	private void deviceDiscoveredHandler(Intent intent) {
		BluetoothDevice device = getRemoteDevice(intent);
		if (isPhone(device) && isPlayer(device)) {
			foundCount++;
			short strength = getAbsoluteSignalStrength(intent);
			setScoreFromSignalStrength(device, strength);
		}
	}

	private boolean isPlayer(BluetoothDevice device) {
		for (String player : playerList) {
			if (player.equals(device.getName())) {
				return true;
			}
		}

		return false;
	}

	private short getAbsoluteSignalStrength(Intent intent) {
		return (short) Math.abs(getSignalStrength(intent));
	}

	private short getSignalStrength(Intent intent) {
		return intent
				.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
	}

	private void setScoreFromSignalStrength(BluetoothDevice device,
			short strength) {
		if (thisPlayerIsIt())
			addFoundPoints(device, strength);
	}

	private boolean thisPlayerIsIt() {
		return it.equals(GlobalState.playerName);
	}

	private void addFoundPoints(BluetoothDevice device, short strength) {
		if (isPlayer(device)) {
			score += (SCORE_OFFSET - strength);
			updateScoreLabel();
		}
	}

	private void updateScoreLabel() {
		scoreBoard.setText("Score: " + score);

		setHighScore();
		if (getApiClient().isConnected()) {
			Games.Achievements.increment(getApiClient(),
					getString(R.string.correct_guess_achievement),
					GlobalState.myScore);

			Games.Leaderboards.submitScore(getApiClient(),
					getString(R.string.number_guesses_leaderboard),
					GlobalState.myScore);
		}

	}

	private boolean devicesFound() {
		return foundCount > 0;
	}

	private void discoveryFinishedHandler() {
		setItLabel();

		resetDiscoveryIfIt();
		nobodyDiscoveredHandler();

		clearDiscoveredDevices();
	}

	private void resetDiscoveryIfIt() {
		if (thisPlayerIsIt())
			adapter.startDiscovery();
	}

	private void nobodyDiscoveredHandler() {
		if (isItAndFoundNobody()) {
			if (scoreWillBeLessThanZero()) {
				score = 0;
			} else {
				score -= 10;
			}

			updateScoreLabel();
		}
	}

	private boolean isItAndFoundNobody() {
		return thisPlayerIsIt() && !devicesFound();
	}

	private boolean scoreWillBeLessThanZero() {
		return (score - 10) < 0;
	}

	private void clearDiscoveredDevices() {
		foundCount = 0;
	}

	private void cancelScheduledTasks() {
		cancelDiscoveryTimers();
		cancelItTimers();
		cancelStartTimers();
	}

	private void cancelDiscoveryTimers() {
		if (cancelScheduler != null)
			cancelScheduler.cancel();
	}

	private void cancelItTimers() {
		if (itScheduler != null)
			itScheduler.cancel();
		if (itTask != null)
			itTask.cancel();
	}

	private void cancelStartTimers() {
		if (startScheduler != null)
			startScheduler.cancel();
		if (cancelTask != null)
			cancelTask.cancel();
	}

	private void initSound() {
		pool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		soundID = pool.load(this, R.raw.youreit, 1);
	}

	private void playItAlert() {

		try {
			if (thisPlayerIsIt()) {
				adapter.startDiscovery();
				startScheduler.scheduleAtFixedRate(cancelTask, 4000, 4000);
				pool.play(soundID, 1, 1, 1, 0, 1);
			} else if (startScheduler != null) {
				cancelTask.cancel();
				cancelTask = new CancelDiscoveryTask();
			}
		} catch (IllegalStateException e) {
			// Do nothing, the timer has already been cancelled;
		}
		makeVibrate(500);
	}

	private void makeVibrate(int length) {
		Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibe.vibrate(length);
	}

	private boolean isPlaying(String playerID) {
		for (int i = 0; i < playerList.length; i++) {
			if (i < GlobalState.currentPlayers.size()
					&& playerID.equals(GlobalState.currentPlayers.get(i))) {
				return true;
			}
		}

		return false;
	}

	public class CancelDiscoveryTask extends TimerTask {
		@Override
		public void run() {
			adapter.cancelDiscovery();
		}
	}

	public class StartDiscoveryTask extends TimerTask {
		@Override
		public void run() {
			adapter.cancelDiscovery();
		}
	}

	private void setNextIt() {
		index++;
		adapter.cancelDiscovery();
		if (index < playerList.length) {
			// Log.d("Tag", "setNextIt, index is " + index);
			if (isPlaying(playerList[index])) {
				// Log.d("Tag", playerList[index] + "is playing");
				// ensureBluetoothDiscoverability();
				it = playerList[index];
			} else {
				// Log.d("Tag", "Recursively calling setNextIt");
				setNextIt();
			}
		} else {
			endGame();
		}
	}

	private void endGame() {
		// Log.d("Tag",
		// "The game is over because we are out of people to be it");
		cancelScheduledTasks();
		adapter.cancelDiscovery();

		GlobalState.myScore = score;
		setHighScore();
		if (getApiClient().isConnected()) {
			Games.Achievements.increment(getApiClient(),
					getString(R.string.correct_guess_achievement),
					GlobalState.myScore);

			Games.Leaderboards.submitScore(getApiClient(),
					getString(R.string.number_guesses_leaderboard),
					GlobalState.myScore);
		}

		releaseService();
		it = "nobody";
		// impo
		startActivity(loadScore);
	}

	private class ChangeItTask extends TimerTask {
		@Override
		public void run() {
			runOnUiThread(new Runnable() {
				public void run() {
					setNextIt();
					setItLabel();
					playItAlert();
				}
			});

		}
	}

}
