package com.codepath.examples.audiovideodemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	
	public void onAudio(View v) {
		startActivity(new Intent(this, AudioActivity.class));
	}
	
	public void onVideo(View v) {
		startActivity(new Intent(this, VideoActivity.class));
	}
	public void onPCM(View v) {
		startActivity(new Intent(this, PCMActivity.class));
	}
	public void onYUV(View v) {
		startActivity(new Intent(this, VideoCaptureActivity.class));
	}
	public void onWTF(View v) {
		startActivity(new Intent(this, SilentCameraActivity.class));
	}
	public void startcapture() {
		startActivity(new Intent(this, VideoCaptureActivity.class));
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
