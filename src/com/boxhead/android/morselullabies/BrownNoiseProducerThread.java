package com.boxhead.android.morselullabies;

import java.util.Random;

import android.content.Context;
import android.util.Log;

final class BrownNoiseProducerThread extends Thread {
	private static final double SLOPE = 2.0D / 20D;

	private Context ctx;
	private DoubleBuffer buf;
	private double currentValue = 0D;
	private Random random = new Random();

	BrownNoiseProducerThread(Context ctx, DoubleBuffer buf) {
		this.ctx = ctx;
		this.buf = buf;
		this.setName(getClass().getSimpleName());
	}

	private double getNext() {
		double whiteNoise = (random.nextFloat() * 2D - 1D) * SLOPE;

		double v = currentValue;
		double next = v + whiteNoise;
		if (next < -1D || next > 1D)
			next = v - whiteNoise;

		currentValue = next;
		return next;
	}

	public void run() {
		try {
			while (isInterrupted() == false)
				buf.addDouble(getNext());
		} catch (InterruptedException ie) {
		} catch (Exception e) {
			Log.e(getClass().getName(), ".run()", e);
			MorseTextReceiver.publish(this.ctx, e);
		}
	}

}
