package apps.spacerayders.activity;

import apps.spacerayders.activity.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class TheGame extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.how_to_game);
	}

	@Override
	public void onBackPressed() {
		Intent startmenu = new Intent(this, Help.class);
		startActivity(startmenu);
	}

	public void start(View view) {
		Intent menu = new Intent(this, SplashActivity.class);
		startActivity(menu);
	}
}