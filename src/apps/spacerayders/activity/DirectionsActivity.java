package apps.spacerayders.activity;

import apps.spacerayders.activity.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * This activity displays the directions for the game. Simply displays the
 * information in directions.xml file. 
 */
public class DirectionsActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.directions);
	}
	
	/**
	 * Starts the SplashActivity which is the first screen.
	 */ 
	@Override
	public void onBackPressed() {
		Intent restart = new Intent(this, Help.class);
		startActivity(restart);
	}

}
