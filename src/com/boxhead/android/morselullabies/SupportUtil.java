package com.boxhead.android.morselullabies;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Date;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

final class SupportUtil {
	private SupportUtil() {
	}

	@SuppressLint("RtlHardcoded")
	static void sendSupportEmail(final Context ctx, final Display display) {
		AlertDialog.Builder alert = new AlertDialog.Builder(ctx);

		alert.setTitle(ctx.getString(R.string.support_dialog_title));
		alert.setMessage(ctx.getString(R.string.support_dialog_message));

		final EditText input = new EditText(ctx);
		input.setInputType(EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
		input.setMinLines(5);
		input.setLines(5);
		input.setMaxLines(5);
		input.setVerticalScrollBarEnabled(true);
		input.setGravity(Gravity.TOP | Gravity.LEFT);
		input.setSingleLine(false);
		input.setImeOptions(EditorInfo.IME_NULL);
		alert.setView(input);

		alert.setPositiveButton(ctx.getString(R.string.email_button),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SupportUtil.sendSupportEmail(ctx, display, input
								.getText().toString());
					}
				});

		alert.setNegativeButton(ctx.getString(R.string.cancel_button),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

		alert.show();
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private static final void sendSupportEmail(Context ctx, Display display,
			String text) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream body = new PrintStream(baos);
		body.println("Hi Paul,");
		body.println();
		body.println("  I have a feature request and/or bug report.");
		body.println();
		body.println(text);
		body.println();
		body.println("App Version: " + VersionUtil.getVersionText(ctx));

		body.println("OS Version: " + System.getProperty("os.version") + "("
				+ android.os.Build.VERSION.INCREMENTAL + ")");
		body.println("OS API Level: " + android.os.Build.VERSION.SDK_INT);
		body.println("Release: " + android.os.Build.VERSION.RELEASE);
		body.println("Device: " + android.os.Build.DEVICE);
		body.println("Model: " + android.os.Build.MODEL);
		body.println("Product: " + android.os.Build.PRODUCT);
		body.println("Brand: " + android.os.Build.BRAND);
		body.println("Display: " + android.os.Build.DISPLAY);
		body.println("CPU Abi: " + android.os.Build.CPU_ABI);
		body.println("CPU Abi2: " + android.os.Build.CPU_ABI2);
		body.println("Unknown: " + android.os.Build.UNKNOWN);
		body.println("Hardware: " + android.os.Build.HARDWARE);
		body.println("Id: " + android.os.Build.ID);
		body.println("Manufacturer: " + android.os.Build.MANUFACTURER);
		body.println("User: " + android.os.Build.USER);
		body.println("Host: " + android.os.Build.HOST);
		body.println("Board: " + android.os.Build.BOARD);
		body.println("Bootloader: " + android.os.Build.BOOTLOADER);
		body.println("Brand: " + android.os.Build.BRAND);
		body.println("Fingerprint: " + android.os.Build.FINGERPRINT);
		body.println("Tags: " + android.os.Build.TAGS);
		body.println("Time: " + new Date(android.os.Build.TIME));
		body.println("Type: " + android.os.Build.TYPE);
		body.println("Now: " + new Date());
		body.println("TimeZone: " + TimeZone.getDefault().getID());

		if (android.os.Build.VERSION.SDK_INT >= 13) {
			Point pt = new Point();
			display.getSize(pt);
			body.println("Display Size: " + pt);
		} else {
			body.println("Display Size: " + display.getWidth() + "x"
					+ display.getHeight());
		}

		DisplayMetrics dm = new DisplayMetrics();
		display.getMetrics(dm);
		DisplayMetrics dm2 = null;
		if (android.os.Build.VERSION.SDK_INT >= 17) {
			dm2 = new DisplayMetrics();
			display.getRealMetrics(dm2);
		}
		body.println("Display Metrics: " + dm + ", " + dm2);
		body.println("Display Rotation: " + display.getRotation());
		body.println();

		try {
			Process process = Runtime.getRuntime().exec(
					"logcat -d -v threadtime");
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));

			String line;
			while ((line = bufferedReader.readLine()) != null)
				body.println(line);
		} catch (IOException e) {
			e.printStackTrace(body);
		}

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/html");
		intent.putExtra(Intent.EXTRA_EMAIL,
				new String[] { "contact@paulrowe.com" });
		intent.putExtra(Intent.EXTRA_SUBJECT, ctx.getString(R.string.app_name)
				+ " Support Request");
		intent.putExtra(Intent.EXTRA_TEXT, baos.toString());
		intent.setType("message/rfc822");

		ctx.startActivity(Intent.createChooser(intent, "Send Email"));

	}
}
