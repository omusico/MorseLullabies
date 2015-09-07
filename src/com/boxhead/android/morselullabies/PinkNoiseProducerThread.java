package com.boxhead.android.morselullabies;

import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

final class PinkNoiseProducerThread extends Thread {

	private static final double RANGE = 128;
	private static final int WHITE_VALUES_LENGTH = 6;
	private static final double DIVISOR = RANGE / (double) WHITE_VALUES_LENGTH;
	private static final int MAX_KEY = 0x1f;

	private int key = 0;
	private double whiteValues[] = new double[WHITE_VALUES_LENGTH];
	private double maxSumEver = 90D;

	@SuppressLint("TrulyRandom")
	private Random rnd = new Random();

	private VaryingValue noiseFrequency;
	private DoubleBuffer buf;
	private Context ctx;
	private int sampleCount = 0;

	PinkNoiseProducerThread(Context ctx, DoubleBuffer buf,
			VaryingValue noiseFrequency) {
		this.ctx = ctx;
		this.buf = buf;
		this.noiseFrequency = noiseFrequency;
		for (int i = 0; i < whiteValues.length; i++)
			whiteValues[i] = rnd.nextFloat() * DIVISOR;
		this.setName(getClass().getSimpleName());
	}

	// return a pink noise value
	private double pink() {
		int last_key = key;

		key++;
		if (key > MAX_KEY)
			key = 0;

		// Exclusive-Or previous value with current value. This gives
		// a list of bits that have changed.
		int diff = last_key ^ key;
		double sum = 0D;

		for (int i = 0; i < whiteValues.length; i++) {
			// If bit changed get new random number for corresponding
			// white_value
			if ((diff & (1 << i)) != 0)
				whiteValues[i] = rnd.nextFloat() * DIVISOR;

			sum += whiteValues[i];
		}

		if (sum > maxSumEver)
			maxSumEver = sum;

		sum = 2D * (sum / maxSumEver) - 1D;
		return sum;
	}

	public void run() {
		try {
			while (isInterrupted() == false) {
				this.sampleCount++;
				double noiseSample = pink();

				double noisePeriod = (double) Constants.AUDIO_SAMPLE_RATE
						/ noiseFrequency.getValue();
				double noiseAngle = Math.PI * (double) sampleCount * 2.0D
						/ noisePeriod;
				double noiseSampleAdj = noiseSample * Math.sin(noiseAngle);
				buf.addDouble(noiseSampleAdj);
			}
		} catch (InterruptedException ie) {
		} catch (Exception e) {
			Log.e(getClass().getName(), ".run()", e);
			MorseTextReceiver.publish(this.ctx, e);
		}
	}
}
