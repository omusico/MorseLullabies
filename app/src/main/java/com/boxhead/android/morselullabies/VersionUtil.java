package com.boxhead.android.morselullabies;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.zip.ZipFile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

final class VersionUtil {
	private static Date getBuildDate(Context ctx) {
		try {
			return new Date(new ZipFile(ctx.getPackageManager()
					.getApplicationInfo(ctx.getPackageName(), 0).sourceDir)
					.getEntry("classes.dex").getTime());
		} catch (Exception e) {
			Log.e(VersionUtil.class.getName(), "getBuildDate", e);
		}

		return null;
	}

	@SuppressLint("SimpleDateFormat")
	public static String getVersionText(Context ctx) {
		StringBuffer sb = new StringBuffer();
		sb.append(ctx.getString(R.string.app_name));
		sb.append(" ");
		sb.append(MainActivity.class.getPackage().getName());
		sb.append(" www.paulrowe.com");
		Date buildDate = getBuildDate(ctx);
		if (buildDate != null) {
			sb.append(" ");
			sb.append(new SimpleDateFormat("yyyyMMdd-HHmm").format(buildDate));
		}

		PackageManager pm = ctx.getPackageManager();
		if (pm != null) {
			try {
				PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), 0);
				if (pi != null) {
					sb.append(" ");
					sb.append(pi.versionCode);
					sb.append(" ");
					sb.append(pi.versionName);
				}
			} catch (NameNotFoundException nnfe) {
			}
		}

		return sb.toString();
	}

	private VersionUtil() {
	}
}
