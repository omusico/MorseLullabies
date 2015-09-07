package com.boxhead.android.morselullabies;

import android.annotation.SuppressLint;
import android.util.Log;

final class DoubleBufferPool {
	private boolean[] checkouts;
	private DoubleBufferPoolItem[] items;

	DoubleBufferPool(int poolSize, int doubleBufferSize) {
		checkouts = new boolean[poolSize];
		items = new DoubleBufferPoolItem[poolSize];

		for (int i = 0; i < items.length; i++) {
			checkouts[i] = false;
			items[i] = new DoubleBufferPoolItem(i, new double[doubleBufferSize]);
		}
	}

	void checkin(DoubleBufferPoolItem item) {
		checkouts[item.getId()] = false;
	}

	@SuppressLint("DefaultLocale")
	DoubleBufferPoolItem checkout() throws InterruptedException {

		while (true) {
			for (int i = 0; i < checkouts.length; i++) {
				if (checkouts[i] == false) {
					checkouts[i] = true;
					return items[i];
				}
			}

			Log.e(DoubleBufferPool.class.getName(),
					"!!! pool exhausted !!!".toUpperCase());
			Thread.sleep(1000L);
		}
	}

	void destroy() {
		for (int i = 0; i < checkouts.length; i++) {
			items[i] = null;
		}
	}
}
