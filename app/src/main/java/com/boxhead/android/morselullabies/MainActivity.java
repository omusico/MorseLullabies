package com.boxhead.android.morselullabies;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.boxhead.android.morselullabies.LullabyService.LullabyServiceBinder;

public class MainActivity extends Activity {

	private class DataUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (intent.getAction().equals(MORSE_VOLUME_INTENT_NAME)) {
					((TextView) findViewById(R.id.morseVolumeValue))
							.setText(intent.getStringExtra(INTENT_EXTRA_NAME));
				} else if (intent.getAction().equals(NOISE_VOLUME_INTENT_NAME)) {
					((TextView) findViewById(R.id.noiseVolumeValue))
							.setText(intent.getStringExtra(INTENT_EXTRA_NAME));
				} else if (intent.getAction().equals(UPTIME_INTENT_NAME)) {
					updateUptimeTextfield();
				}
			} catch (Exception e) {
				Log.e(getClass().getName(), ".onReceive()", e);
			}
		}
	}

	static final String MORSE_VOLUME_INTENT_NAME = MainActivity.class.getName()
			+ ".MORSE_VOLUME";
	static final String NOISE_VOLUME_INTENT_NAME = MainActivity.class.getName()
			+ ".NOISE_VOLUME";
	static final String UPTIME_INTENT_NAME = MainActivity.class.getName()
			+ ".UPTIME";
	static final String INTENT_EXTRA_NAME = MainActivity.class.getName()
			+ ".VOLUME_EXTRA";

	private Intent serviceIntent;
	private LullabyService lullabyService;
	private MorseTextReceiver morseTextReceiver;
	private DataUpdateReceiver dataUpdateReceiver;

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LullabyServiceBinder binder = (LullabyServiceBinder) service;
			lullabyService = binder.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.activity_main);
			TextView tv = (TextView) findViewById(R.id.textView1);
			tv.setMovementMethod(new ScrollingMovementMethod());
			tv.setText(VersionUtil.getVersionText(this));
			setupMorseVolumeControl();
		} catch (Exception e) {
			Log.e(getClass().getName(), ".onCreate()", e);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (serviceIntent != null) {
			unbindService(serviceConnection);
			stopService(serviceIntent);
		}
		if (dataUpdateReceiver != null) {
			unregisterReceiver(dataUpdateReceiver);
			dataUpdateReceiver = null;
		}
		if (morseTextReceiver != null) {
			morseTextReceiver.unregister(this);
			morseTextReceiver = null;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (dataUpdateReceiver != null) {
			unregisterReceiver(dataUpdateReceiver);
			dataUpdateReceiver = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (this.morseTextReceiver == null) {
			this.morseTextReceiver = new MorseTextReceiver(
					new MorseTextReceiverAction() {
						@SuppressLint("SimpleDateFormat")
						@Override
						public void onMorseText(String morseText) {
							String date = new SimpleDateFormat("HH:mm")
									.format(new Date(System.currentTimeMillis()));
							TextView tv = (TextView) findViewById(R.id.textView1);
							tv.setText(date + ": " + morseText + "\n"
									+ tv.getText());
						}
					});
			this.morseTextReceiver.register(this);
		}
		if (this.dataUpdateReceiver == null) {
			this.dataUpdateReceiver = new DataUpdateReceiver();
			registerReceiver(this.dataUpdateReceiver, new IntentFilter(
					MORSE_VOLUME_INTENT_NAME));
			registerReceiver(this.dataUpdateReceiver, new IntentFilter(
					NOISE_VOLUME_INTENT_NAME));
			registerReceiver(this.dataUpdateReceiver, new IntentFilter(
					UPTIME_INTENT_NAME));
		}
		updateUptimeTextfield();
	}

	@Override
	protected void onStart() {
		super.onStart();
		try {
			if (serviceIntent == null) {
				serviceIntent = new Intent(this, LullabyService.class);
				bindService(serviceIntent, serviceConnection,
						Context.BIND_AUTO_CREATE);
				startService(serviceIntent);
			}
		} catch (Exception e) {
			Log.e(getClass().getName(), ".onStart()", e);
		}
	}

	private void updateUptimeTextfield() {
		try {
			TextView upTime = ((TextView) findViewById(R.id.uptimeValue));
			if (upTime != null && lullabyService != null)
				upTime.setText(lullabyService.getUptimeString());
		} catch (Exception e) {
			Log.e(getClass().getName(), ".updateUptimeTextfield()", e);
		}
	}

	private void setupMorseVolumeControl() {
		SeekBar volumeControl = (SeekBar) findViewById(R.id.morseVolumeControl);
		volumeControl.setMax(Constants.MORSE_VOLUME_CONTROL_MAX_VALUE);
		volumeControl.setProgress(Constants.MORSE_VOLUME_CONTROL_MAX_VALUE);

		volumeControl.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onProgressChanged(SeekBar arg0, int progress,
					boolean arg2) {

				double actualMorseVolume = (double) progress
						/ (double) Constants.MORSE_VOLUME_CONTROL_MAX_VALUE;
				lullabyService.setMorseVolume(actualMorseVolume);
			}
		});
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setMessage(getString(R.string.exit_question))
				.setPositiveButton(getString(R.string.yes_button),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								MainActivity.super.onBackPressed();
							}
						})
				.setNegativeButton(getString(R.string.no_button), null).show();
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
		if (id == R.id.action_support) {
			SupportUtil.sendSupportEmail(this, getWindow().getWindowManager()
					.getDefaultDisplay());
			return true;
		} else if (id == R.id.action_play) {
			DirectoryChooserDialog dialog = new DirectoryChooserDialog(MainActivity.this,
					new DirectoryChooserDialog.ChosenDirectoryListener() {
						@Override
						public void onChosenDir(String chosenDir) {
							// "/mnt/extSdCard/ITunesPlaylistCreator6b/the conet project"
							lullabyService.startPlayingMediaTrack(new File(
									chosenDir));
						}
					});
			dialog.chooseDirectory();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	

}
