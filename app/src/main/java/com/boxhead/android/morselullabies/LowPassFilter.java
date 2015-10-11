package com.boxhead.android.morselullabies;

final class LowPassFilter implements AudioEffect {

	private VaryingValue theta;

	LowPassFilter(VaryingValue theta) {
		this.theta = theta;
	}

	@Override
	public void process(double[] d) {
		for (int i = 1; i < d.length - 1; i += 1)
			d[i] = d[i - 1] + theta.getValue() * (d[i] - d[i - 1]);
	}
}
