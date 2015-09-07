package com.boxhead.android.morselullabies;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public final class LullabyService extends Service implements
		OnPreparedListener, OnCompletionListener, OnErrorListener {

	class LullabyServiceBinder extends Binder {
		LullabyService getService() {
			return LullabyService.this;
		}
	}

	private static final String START_AUDIO_TRACK = LullabyService.class
			.getSimpleName() + ".START_AUDIO_TRACK";
	private static final String PLAY_NEXT_MEDIA_FILE = LullabyService.class
			.getSimpleName() + ".PLAY_NEXT_MEDIA_FILE";

	private final IBinder binder = new LullabyServiceBinder();

	private PowerManager.WakeLock wakeLock;
	private DoubleBufferPool doubleBufferPool;
	private List<BlockingQueue<DoubleBufferPoolItem>> producerQueues;
	private List<Thread> threads = new ArrayList<Thread>();
	private MorseTextProducer morseTextProducer;
	private MorseTextReceiver morseTextReceiver;
	private long birthDate = System.currentTimeMillis();
	private AlarmManager alarmManager;
	private PendingIntent uptimeAlarmIntent;
	private PendingIntent audioTrackStartIntent;
	private PendingIntent mediaFileIntent;
	private List<VaryingVolumeEffect> morseVolumeEffects = new ArrayList<VaryingVolumeEffect>();
	private VaryingVolumeEffect noiseVolumeEffect;
	private VaryingValue playerVolumeVaryingValue;
	private MediaPlayer player;
	private File[] playerFiles;
	@SuppressLint("TrulyRandom")
	private Random playerRnd = new SecureRandom();

	private BroadcastReceiver audioTrackStartReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			audioTrackStartIntent = null;
			startAudioTrackThread();
		}
	};

	private BroadcastReceiver mediaFileStartReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			mediaFileIntent = null;

			try {
				player.reset();
				File file = playerFiles[playerRnd
										.nextInt(playerFiles.length)];
				Log.i(getClass().getName(),"playing file="+file);
				player.setDataSource(file.toString());
				player.prepareAsync();
			} catch (IOException ioe) {
				Log.e(getClass().getName(), "EXCEPTION", ioe);
			}
		}
	};

	private void acquireWakeLock() {
		try {
			wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE))
					.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
			wakeLock.acquire();
		} catch (Exception e) {
			Log.e(getClass().getName(), ".acquireWakeLock()", e);
		}
	}

	private void cancelAudioTrackStart() {
		try {
			if (audioTrackStartIntent != null) {
				alarmManager.cancel(audioTrackStartIntent);
				unregisterReceiver(audioTrackStartReceiver);
				audioTrackStartIntent = null;
			}
		} catch (Exception e) {
			Log.e(getClass().getName(), ".cancelAudioTrackStart()", e);
		}
	}

	private void cancelMediaPlayStart() {
		try {
			if (mediaFileIntent != null) {
				alarmManager.cancel(mediaFileIntent);
				unregisterReceiver(mediaFileStartReceiver);
				mediaFileIntent = null;
			}
		} catch (Exception e) {
			Log.e(getClass().getName(), ".cancelMediaPlayStart()", e);
		}
	}

	private void cancelUptimeAlarm() {
		try {
			if (uptimeAlarmIntent != null) {
				alarmManager.cancel(uptimeAlarmIntent);
				uptimeAlarmIntent = null;
			}
		} catch (Exception e) {
			Log.e(getClass().getName(), ".cancelUptimeAlarm()", e);
		}
	}

	@SuppressLint("DefaultLocale")
	String getUptimeString() {
		long now = System.currentTimeMillis();
		long elapsed = now - birthDate;
		long hours = elapsed / Constants.ONE_HOUR;
		long minutes = (elapsed - (hours * Constants.ONE_HOUR))
				/ Constants.ONE_MINUTE;

		return String.format("%d:%02d", hours, minutes);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		player = new MediaPlayer();
		player.setWakeMode(getApplicationContext(),
				PowerManager.PARTIAL_WAKE_LOCK);
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setOnPreparedListener(this);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		try {
			this.alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			if (this.doubleBufferPool != null) {
				this.doubleBufferPool.destroy();
				this.doubleBufferPool = null;
			}

			this.doubleBufferPool = new DoubleBufferPool(
					Constants.DOUBLE_BUFFER_POOL_SIZE,
					Constants.DOUBLE_BUFFER_SIZE);

			acquireWakeLock();
			setupMorseTextReceiver();
			setupProducers();
			scheduleAudioTrackStart();
			scheduleUptimeAlarm();
		} catch (OutOfMemoryError oome) {
			MorseTextReceiver
					.publish(
							this,
							"Uh Oh!  Something bad happened.  The service received an OutOfMemoryError when trying to start.  This application is a CPU and Memory intensive application.  Please try to stop some other applications on your device to free up some memory so this application can run.  Sorry about that.");
		}
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		if (morseTextReceiver != null) {
			morseTextReceiver.unregister(this);
			morseTextReceiver = null;
		}

		for (Thread th : threads)
			th.interrupt();

		threads.clear();

		if (this.doubleBufferPool != null) {
			this.doubleBufferPool.destroy();
			this.doubleBufferPool = null;
		}

		cancelAudioTrackStart();
		cancelMediaPlayStart();
		cancelUptimeAlarm();

		player.stop();
		player.release();

		releaseWakeLock();

		return false;
	}

	private void releaseWakeLock() {
		try {
			if (wakeLock != null)
				wakeLock.release();
		} catch (Exception e) {
			Log.e(getClass().getName(), ".releaseWakeLock()", e);
		}
	}

	private void scheduleAudioTrackStart() {
		try {
			if (audioTrackStartIntent != null)
				cancelAudioTrackStart();

			if (mediaFileIntent != null)
				cancelMediaPlayStart();

			if (audioTrackStartIntent == null) {
				registerReceiver(audioTrackStartReceiver, new IntentFilter(
						START_AUDIO_TRACK));

				MorseTextReceiver
						.publish(
								LullabyService.this,
								"Audio track will start in "
										+ (Constants.DELAY_BEFORE_AUDIO_TRACK_THREAD_START / Constants.ONE_SECOND)
										+ " seconds. "+Build.VERSION.SDK_INT);
				audioTrackStartIntent = PendingIntent.getBroadcast(this, 0,
						new Intent(START_AUDIO_TRACK), 0);
				alarmManager
						.set(AlarmManager.RTC_WAKEUP,
								System.currentTimeMillis()
										+ Constants.DELAY_BEFORE_AUDIO_TRACK_THREAD_START,
								audioTrackStartIntent);
			}
		} catch (Exception e) {
			MorseTextReceiver.publish(this, e);
			Log.e(getClass().getName(), ".scheduleAudioTrackStart()", e);
		}
	}

	private void scheduleUptimeAlarm() {
		try {
			if (uptimeAlarmIntent != null)
				cancelUptimeAlarm();

			if (uptimeAlarmIntent == null) {
				uptimeAlarmIntent = PendingIntent.getBroadcast(this, 0,
						new Intent(MainActivity.UPTIME_INTENT_NAME), 0);
				alarmManager.setInexactRepeating(
						AlarmManager.ELAPSED_REALTIME_WAKEUP,
						Constants.FIFTEEN_SECONDS, Constants.ONE_MINUTE,
						uptimeAlarmIntent);
			}
		} catch (Exception e) {
			Log.e(getClass().getName(), ".scheduleUptimeAlarm()", e);
		}
	}

	private void setupMorseTextReceiver() {
		if (morseTextReceiver == null) {
			this.morseTextReceiver = new MorseTextReceiver(
					new MorseTextReceiverAction() {
						@SuppressWarnings("deprecation")
						@Override
						public void onMorseText(String morseText) {
							String appName = getString(R.string.app_name);
							Notification note = new Notification(
									getNotificationIcon(), appName,
									System.currentTimeMillis());
							Intent i = new Intent(LullabyService.this,
									MainActivity.class);
							i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
									| Intent.FLAG_ACTIVITY_SINGLE_TOP);
							PendingIntent pi = PendingIntent.getActivity(
									LullabyService.this, 0, i, 0);
							note.setLatestEventInfo(LullabyService.this,
									appName, morseText, pi);
							note.flags |= Notification.FLAG_NO_CLEAR;
							startForeground(1337, note);
						}
					});
			this.morseTextReceiver.register(this);
		}
	}

	private void setupProducers() {
		List<BlockingQueue<DoubleBufferPoolItem>> morseProducerQueues = new ArrayList<BlockingQueue<DoubleBufferPoolItem>>();
		for (int i = 0; i < Constants.NUMBER_OF_MORSE_AUDIO_TRACKS; i++)
			morseProducerQueues
					.add(new ArrayBlockingQueue<DoubleBufferPoolItem>(
							Constants.DOUBLE_BUFFER_QUEUE_SIZE));

		BlockingQueue<DoubleBufferPoolItem> noiseProducerQueue = new ArrayBlockingQueue<DoubleBufferPoolItem>(
				Constants.DOUBLE_BUFFER_QUEUE_SIZE);

		this.producerQueues = new ArrayList<BlockingQueue<DoubleBufferPoolItem>>();
		this.producerQueues.addAll(morseProducerQueues);
		this.producerQueues.add(noiseProducerQueue);

		this.morseTextProducer = new MorseTextProducer(this);

		for (int i = 0; i < Constants.NUMBER_OF_MORSE_AUDIO_TRACKS; i++) {
			VaryingValue morseVolumeVaryingValue = new VaryingValue(
					Constants.MORSE_VOLUME_CHANGE_INTERVAL,
					Constants.MORSE_VOLUME_DIRECTION_CHANGE_RATE,
					Constants.MORSE_VOLUME_MINMAX_CHANGE_DIRECTION,
					Constants.MORSE_VOLUME_FLOOR,
					Constants.MORSE_VOLUME_CEILING, 0.0D,
					Constants.MORSE_VOLUME_MAX);
			morseVolumeVaryingValue.setListener(new VaryingValueBroadcaster(
					this, MainActivity.MORSE_VOLUME_INTENT_NAME,
					MainActivity.INTENT_EXTRA_NAME,
					Constants.VOLUME_DISPLAY_PRECISION));
			VaryingVolumeEffect ve = new VaryingVolumeEffect(
					morseVolumeVaryingValue);
			morseVolumeEffects.add(ve);
			Thread morseProducerThread = new MorseProducerThread(
					this,
					this.morseTextProducer,
					new VaryingValue(
							Constants.MORSE_WRITER_TEXT_RATE_CHANGE_INTERVAL,
							Constants.MORSE_WRITER_TEXT_RATE_DIRECTION_CHANGE_RATE,
							Constants.MORSE_WRITER_TEXT_RATE_MINMAX_CHANGE_DIRECTION,
							Constants.MORSE_WRITER_TEXT_RATE_MIN,
							Constants.MORSE_WRITER_TEXT_RATE_MAX),
					new VaryingValue(
							Constants.MORSE_WRITER_FREQUENCY_CHANGE_INTERVAL,
							Constants.MORSE_WRITER_FREQUENCY_DIRECTION_CHANGE_RATE,
							Constants.MORSE_WRITER_FREQUENCY_MINMAX_CHANGE_DIRECTION,
							Constants.MORSE_WRITER_FREQUENCY_MIN,
							Constants.MORSE_WRITER_FREQUENCY_MAX),
					new DoubleBuffer(this.doubleBufferPool, morseProducerQueues
							.get(i), ve));
			morseProducerThread.start();
			this.threads.add(morseProducerThread);
		}

		VaryingValue noiseVolumeVaryingValue = new VaryingValue(
				Constants.NOISE_VOLUME_CHANGE_INTERVAL,
				Constants.NOISE_VOLUME_DIRECTION_CHANGE_RATE,
				Constants.NOISE_VOLUME_MINMAX_CHANGE_DIRECTION,
				Constants.NOISE_VOLUME_FLOOR, Constants.NOISE_VOLUME_CEILING,
				0.0D, Constants.NOISE_VOLUME_MAX);

		noiseVolumeVaryingValue.setListener(new VaryingValueBroadcaster(this,
				MainActivity.NOISE_VOLUME_INTENT_NAME,
				MainActivity.INTENT_EXTRA_NAME,
				Constants.VOLUME_DISPLAY_PRECISION));

		noiseVolumeEffect = new VaryingVolumeEffect(noiseVolumeVaryingValue);

		LowPassFilter lowPassFilter = new LowPassFilter(new VaryingValue(
				Constants.LOW_PASS_FILTER_CHANGE_INTERVAL,
				Constants.LOW_PASS_FILTER_CHANGE_RATE,
				Constants.LOW_PASS_FILTER_MINMAX_DIRECTION,
				Constants.LOW_PASS_FILTER_MIN, Constants.LOW_PASS_FILTER_MAX));

		AudioEffectChain effectsChain = new AudioEffectChain();
		effectsChain.add(lowPassFilter);
		effectsChain.add(noiseVolumeEffect);

		Thread noiseProducerThread = new BrownNoiseProducerThread(this,
				new DoubleBuffer(this.doubleBufferPool, noiseProducerQueue,
						effectsChain));
		noiseProducerThread.start();
		this.threads.add(noiseProducerThread);

		playerVolumeVaryingValue = new VaryingValue(
				Constants.PLAYER_VOLUME_CHANGE_INTERVAL,
				Constants.PLAYER_VOLUME_DIRECTION_CHANGE_RATE,
				Constants.PLAYER_VOLUME_MINMAX_CHANGE_DIRECTION,
				Constants.PLAYER_VOLUME_FLOOR, Constants.PLAYER_VOLUME_CEILING,
				0.0D, Constants.PLAYER_VOLUME_MAX);
	}

	private void startAudioTrackThread() {
		try {
			MorseTextReceiver.publish(this, "Starting audio track");
			Thread audioTrackThread = new AudioTrackThread(this,
					this.doubleBufferPool, this.producerQueues);
			audioTrackThread.start();
			LullabyService.this.threads.add(audioTrackThread);

			// startPlayingMediaTrack();
		} catch (Exception e) {
			MorseTextReceiver.publish(LullabyService.this, e);
			Log.e(getClass().getName(), "AlarmReceiver.onReceive()", e);
		}
	}

	void setMorseVolume(double adjust) {
		for (VaryingVolumeEffect vve : morseVolumeEffects)
			vve.setAdjust(adjust);
	}

	void startPlayingMediaTrack(File file) {
		Thread playerVolumeThread = new Thread() {
			public void run() {
				try {
					while (isInterrupted() == false) {
						Thread.sleep(Constants.MEDIA_TRACK_VOLUME_CHANGE_SLEEP_TIME);
						float volume = (float) playerVolumeVaryingValue
								.getValue();
						try {
							player.setVolume(volume, volume);
						} catch (Exception e) {
						}
					}
				} catch (InterruptedException ie) {
				}
			}
		};

		playerVolumeThread.start();
		threads.add(playerVolumeThread);

		playerFiles = getFilesForFileAsArray(file);
		scheduleNextMediaTrack();
	}

	private void scheduleNextMediaTrack() {
		if (mediaFileIntent == null) {
			registerReceiver(mediaFileStartReceiver, new IntentFilter(
					PLAY_NEXT_MEDIA_FILE));

			mediaFileIntent = PendingIntent.getBroadcast(this, 0, new Intent(
					PLAY_NEXT_MEDIA_FILE), 0);

			long sleepTime = Constants.MEDIA_TRACK_MIN_SLEEP_TIME
					+ playerRnd
							.nextInt((int) (Constants.MEDIA_TRACK_MAX_SLEEP_TIME - Constants.MEDIA_TRACK_MIN_SLEEP_TIME));

			long wakeupTime = System.currentTimeMillis() + sleepTime;
			
			Log.i(getClass().getName(),".scheduleNextMediaTrack(): wakeupTime="+new Date(wakeupTime));
			
			alarmManager.set(AlarmManager.RTC_WAKEUP, wakeupTime, mediaFileIntent);
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		scheduleNextMediaTrack();
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		scheduleNextMediaTrack();
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mp.start();
	}

	private File[] getFilesForFileAsArray(File f) {
		List<File> file = getFilesForFile(f);
		return file.toArray(new File[file.size()]);
	}

	private List<File> getFilesForFile(File f) {
		List<File> out = new ArrayList<File>();

		if (f.isFile() && f.exists() && f.getName().endsWith(".mp3")) {
			out.add(f);
		}

		if (f.isDirectory()) {
			for (File f2 : f.listFiles()) {
				if (f2.isFile() && f2.exists() && f2.getName().endsWith(".mp3")) {
					out.add(f2);
				} else if (f2.isDirectory()) {
					out.addAll(getFilesForFile(f2));
				}
			}
		}

		return out;
	}
	
	private int getNotificationIcon() {
	    boolean whiteIcon = Build.VERSION.SDK_INT >= 21;
	    return whiteIcon ? R.drawable.icon_material : R.drawable.icon;
	}

}
