package com.boxhead.android.morselullabies;

import java.math.BigDecimal;

import android.content.Context;
import android.content.Intent;

final class VaryingValueBroadcaster implements VaryingValueListener {

	private final Context ctx;
	private final String intentNameToBroadcast;
	private final String extraFieldName;
	private final int precision;
	private String currentValue = ".0";

	VaryingValueBroadcaster(Context ctx, String intentNameToBroadcast,
			String extraFieldName, int precision) {
		this.ctx = ctx;
		this.intentNameToBroadcast = intentNameToBroadcast;
		this.extraFieldName = extraFieldName;
		this.precision = precision;
	}

	@Override
	public void onValueChanged(double value2) {
		String value = new BigDecimal(String.valueOf(value2)).setScale(
				precision, BigDecimal.ROUND_HALF_UP).toPlainString();

		if (currentValue.equals(value) == false) {
			currentValue = value;
			Intent intent = new Intent(intentNameToBroadcast);
			intent.putExtra(extraFieldName, currentValue);
			ctx.sendBroadcast(intent);
		}
	}

}
