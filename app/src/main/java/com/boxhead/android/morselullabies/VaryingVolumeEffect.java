package com.boxhead.android.morselullabies;

final class VaryingVolumeEffect implements AudioEffect {

	private VaryingValue value;
	private double adjust = 1.0D;

	VaryingVolumeEffect(VaryingValue value) {
		this.value = value;
	}
	
	void setAdjust(double adjust)
	{
		this.adjust=adjust;
	}

	@Override
	public void process(double[] doubleBuffer) {
		for (int i = 0; i < doubleBuffer.length; i++) {
			doubleBuffer[i] = this.value.getValue() * doubleBuffer[i] * this.adjust;
		}
	}
}
