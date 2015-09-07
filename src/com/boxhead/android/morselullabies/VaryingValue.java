package com.boxhead.android.morselullabies;

import java.security.SecureRandom;
import java.util.Random;

import android.annotation.SuppressLint;

final class VaryingValue {

	@SuppressLint("TrulyRandom")
	private Random rnd = new SecureRandom();

	private double value;
	private double value2;
	private double direction;

	private double directionChangeRate;
	private double minDirection;
	private double maxDirection;
	private double valueFloor;
	private double valueCeiling;
	private double maxValue;
	private double minValue;

	private int changeInterval = 0;
	private int count = 0;

	private VaryingValueListener listener;

	VaryingValue(int changeInterval, double directionChangeRate,
			double minmaxDirection, double minValue, double maxValue) {
		this(changeInterval, directionChangeRate, -minmaxDirection,
				minmaxDirection, minValue, maxValue, minValue, maxValue);
	}

	VaryingValue(int changeInterval, double directionChangeRate,
			double minmaxDirection, double valueFloor, double valueCeiling,
			double minValue, double maxValue) {
		this(changeInterval, directionChangeRate, -minmaxDirection,
				minmaxDirection, valueFloor, valueCeiling, minValue, maxValue);
	}

	private VaryingValue(int changeInterval, double directionChangeRate,
			double minDirection, double maxDirection, double valueFloor,
			double valueCeiling, double minValue, double maxValue) {
		this.changeInterval = changeInterval;
		this.directionChangeRate = directionChangeRate;
		this.minDirection = minDirection;
		this.maxDirection = maxDirection;
		this.valueFloor = valueFloor;
		this.valueCeiling = valueCeiling;
		this.maxValue = maxValue;
		this.minValue = minValue;

		this.value = minValue + (rnd.nextDouble() * (maxValue - minValue));
		this.value2 = this.value;
		this.direction = minDirection
				+ (rnd.nextDouble() * (maxDirection - minDirection));
		if (rnd.nextBoolean())
			this.direction = -this.direction;
		updateValue();
	}

	double getValue() {
		count++;
		if (count >= changeInterval) {
			count = 0;
			updateValue();
		}

		return this.value2;
	}

	void setListener(VaryingValueListener listener) {
		this.listener = listener;
		this.listener.onValueChanged(this.value2);
	}

	private void updateValue() {
		if (rnd.nextBoolean())
			this.direction += this.directionChangeRate;
		else
			this.direction -= this.directionChangeRate;

		if (this.direction < this.minDirection)
			this.direction = this.minDirection;

		if (this.direction > this.maxDirection)
			this.direction = this.maxDirection;

		if (rnd.nextBoolean())
			this.direction = -this.direction;

		this.value += this.direction;

		if (this.value < this.valueFloor)
			this.value = this.valueFloor;

		if (this.value > this.valueCeiling)
			this.value = this.valueCeiling;

		this.value2 = this.value;
		if (this.value2 < minValue)
			this.value2 = minValue;
		else if (this.value2 > this.maxValue)
			this.value2 = this.maxValue;

		if (listener != null)
			listener.onValueChanged(this.value2);
	}

}
