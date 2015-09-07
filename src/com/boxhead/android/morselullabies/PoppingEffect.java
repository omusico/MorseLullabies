package com.boxhead.android.morselullabies;

import java.security.SecureRandom;
import java.util.Random;

import android.annotation.SuppressLint;

final class PoppingEffect implements AudioEffect {

	private static final double POPPING_EFFECT_MIN_POP_PCT = 0.0000001D;
	private static final double POPPING_EFFECT_MAX_POP_PCT = 0.00001D;

	private static final int POPPING_EFFECT_MIN_POP_STRENGTH = 10;
	private static final int POPPING_EFFECT_MAX_POP_STRENGTH = 90;

	private static final double POPPING_EFFECT_POP = 0.9D;

	@SuppressLint("TrulyRandom")
	private Random rnd = new SecureRandom();

	@Override
	public void process(double[] doubleBuffer) {

		double pct = POPPING_EFFECT_MIN_POP_PCT
				+ (rnd.nextDouble() * (POPPING_EFFECT_MAX_POP_PCT - POPPING_EFFECT_MIN_POP_PCT));

		int cnt = (int) ((double) doubleBuffer.length * pct);
		for (int i = 0; i < cnt; i++) {
			int idx = rnd.nextInt(doubleBuffer.length);
			int strength = POPPING_EFFECT_MIN_POP_STRENGTH
					+ rnd.nextInt(POPPING_EFFECT_MAX_POP_STRENGTH
							- POPPING_EFFECT_MIN_POP_STRENGTH);

			for (int j = 0; j < strength; j++) {
				if (j + idx >= doubleBuffer.length)
					break;
				doubleBuffer[j + idx] = POPPING_EFFECT_POP;
			}
		}
	}
}
