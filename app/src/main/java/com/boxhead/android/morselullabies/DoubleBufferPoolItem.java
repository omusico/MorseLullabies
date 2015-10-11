package com.boxhead.android.morselullabies;

final class DoubleBufferPoolItem {
	private int id;
	private double[] buf;

	DoubleBufferPoolItem(int id, double[] buf) {
		this.id = id;
		this.buf = buf;
	}

	double[] getDoubleBuffer() {
		return buf;
	}

	int getId() {
		return id;
	}
}
