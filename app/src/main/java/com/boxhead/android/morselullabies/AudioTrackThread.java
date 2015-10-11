package com.boxhead.android.morselullabies;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

final class AudioTrackThread extends Thread {

	private static void doubleBufferToByteBuffer(double[] dbl, byte[] out) {
		for (int i = 0; i < dbl.length; i++) {
			int val = (int) (dbl[i] * 32767D);

			out[i * 2] = (byte) (val >>> 0 & 255);
			out[i * 2 + 1] = (byte) (val >>> 8 & 255);
		}
	}
	private static double[] mix(double[] from, double[] to) {
		for (int i = 0; i < from.length; i++)
			to[i] = to[i] + from[i];

		return to;
	}
	private Context ctx;

	private DoubleBufferPool pool;

	private List<BlockingQueue<DoubleBufferPoolItem>> producerQueues;

	AudioTrackThread(Context ctx, DoubleBufferPool pool,
			List<BlockingQueue<DoubleBufferPoolItem>> producerQueues) {
		this.ctx = ctx;
		this.pool = pool;
		this.producerQueues = producerQueues;
		this.setName(getClass().getSimpleName());
	}

	public void run() {
		AudioTrack audioTrack = null;

		try {
			audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
					Constants.AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					Constants.AUDIO_BUFFER_SIZE, AudioTrack.MODE_STREAM);

			audioTrack.play();
			double[] finalBuf = new double[Constants.DOUBLE_BUFFER_SIZE];
			byte[] audioBuf = new byte[finalBuf.length * 2];

			while (isInterrupted() == false) {
				for (int i = 0; i < finalBuf.length; i++)
					finalBuf[i] = 0.0D;

				for (BlockingQueue<DoubleBufferPoolItem> producerQueue : producerQueues) {
					DoubleBufferPoolItem audioBuffer = producerQueue.take();
					mix(audioBuffer.getDoubleBuffer(), finalBuf);
					pool.checkin(audioBuffer);
				}

				doubleBufferToByteBuffer(finalBuf, audioBuf);
				audioTrack.write(audioBuf, 0, audioBuf.length);
			}
		} catch (InterruptedException ie) {
		} catch (Exception e) {
			Log.e(getClass().getName(), ".run()", e);
			MorseTextReceiver.publish(this.ctx, e);
		} finally {
			try {
				if (audioTrack != null) {
					audioTrack.stop();
					audioTrack.release();
				}
			} catch (Exception e) {
				Log.e(getClass().getName(), ".run(): finally", e);
			}
		}
	}

}