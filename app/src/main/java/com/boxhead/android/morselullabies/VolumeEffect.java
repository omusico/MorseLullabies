package com.boxhead.android.morselullabies;

final class VolumeEffect implements AudioEffect {

	private VaryingValue value;

	VolumeEffect(VaryingValue value) {
		this.value = value;
	}

	@Override
	public void process(double[] doubleBuffer) {
		for (int i = 0; i < doubleBuffer.length; i++) {
			doubleBuffer[i] = this.value.getValue() * doubleBuffer[i];
		}
	}
}
