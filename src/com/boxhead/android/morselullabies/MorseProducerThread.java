package com.boxhead.android.morselullabies;

import java.security.SecureRandom;
import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

final class MorseProducerThread extends Thread {
	private static final String SPACES = "                   ";
	private MorseAudioWriter mab;
	private MorseTextProducer morseTextProducer;
	private Context ctx;

	@SuppressLint("TrulyRandom")
	private Random rnd = new SecureRandom();

	MorseProducerThread(Context ctx, MorseTextProducer morseText,
			VaryingValue textRate, VaryingValue frequency, DoubleBuffer buf) {
		this.morseTextProducer = morseText;
		this.ctx = ctx;
		this.mab = new MorseAudioWriter(Constants.AUDIO_SAMPLE_RATE, textRate,
				frequency, buf);
		this.setName(getClass().getSimpleName());
	}

	public void run() {
		try {
			while (isInterrupted() == false) {
				String morseText = this.morseTextProducer.readText();
				MorseTextReceiver.publish(ctx, morseText);
				mab.writeMorse(morseText);
				mab.writeMorse(SPACES.substring(
						0,
						rnd.nextInt(SPACES.length()
								- Constants.MORSE_TEXT_MIN_SPACES)
								+ Constants.MORSE_TEXT_MIN_SPACES));
			}
		} catch (InterruptedException ie) {
		} catch (Exception e) {
			Log.e(getClass().getName(), ".run()", e);
			MorseTextReceiver.publish(this.ctx, e);
		}
	}
}