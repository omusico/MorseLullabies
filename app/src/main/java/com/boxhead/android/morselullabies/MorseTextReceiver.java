package com.boxhead.android.morselullabies;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

final class MorseTextReceiver extends BroadcastReceiver {

	private static final String MORSE_TEXT_INTENT = "MORSE_TEXT";
	private static final String MORSE_TEXT_INTENT_EXTRA = "MORSE_TEXT_EXTRA";

	static void publish(Context ctx, Exception e) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		e.printStackTrace(out);
		out.flush();
		out.close();
		publish(ctx, baos.toString());
	}

	static void publish(Context ctx, String morseText) {
		Intent intent = new Intent(MORSE_TEXT_INTENT);
		intent.putExtra(MORSE_TEXT_INTENT_EXTRA, morseText);
		ctx.sendBroadcast(intent);
	}

	private MorseTextReceiverAction action;

	MorseTextReceiver(MorseTextReceiverAction action) {
		this.action = action;
	}

	public void onReceive(Context context, Intent intent) {
		try {
			if (MORSE_TEXT_INTENT.equals(intent.getAction())) {
				this.action.onMorseText(intent
						.getStringExtra(MORSE_TEXT_INTENT_EXTRA));
			}
		} catch (Exception e) {
			Log.e(getClass().getName(), ".onReceive()", e);
		}
	}

	void register(Context ctx) {
		ctx.registerReceiver(this, new IntentFilter(MORSE_TEXT_INTENT));
	}

	void unregister(Context ctx) {
		ctx.unregisterReceiver(this);
	}
}