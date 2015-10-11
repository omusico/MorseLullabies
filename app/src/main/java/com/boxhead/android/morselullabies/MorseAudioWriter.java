package com.boxhead.android.morselullabies;

final class MorseAudioWriter {
	private static final double PARIS_TICKS = 52D;
	private static final double PARIS_CHARS = 32D;
	private static final double PARIS_SPACE = 20D;

	private static int RAMP_MS = 7;

	private int sampleRate;
	private DoubleBuffer os;

	private VaryingValue textRate;
	private VaryingValue frequency;

	MorseAudioWriter(int sampleRate, VaryingValue textRate,
			VaryingValue frequency, DoubleBuffer os) {
		this.sampleRate = sampleRate;
		this.os = os;
		this.textRate = textRate;
		this.frequency = frequency;
	}

	@SuppressWarnings("unused")
	private void _unusedWriteToneWithPhaseShift(double ms)
			throws InterruptedException {
		int length = (int) (sampleRate * ms / Constants.ONE_SECOND);
		int rampLength = (int) (sampleRate * RAMP_MS / Constants.ONE_SECOND);
		double phaseShift = 0.0D;
		double lastFrequency = -1.0D;

		for (int i = 0; i < length; i++) {
			double frequency = this.frequency.getValue();
			double period = (double) sampleRate / frequency;
			double volume;

			if (i < rampLength) {
				volume = (double) i / (double) rampLength;
			} else if (i > length - rampLength) {
				volume = (double) (length - i) / (double) rampLength;
			} else {
				volume = 1.0D;
			}

			double angle = 2.0D * Math.PI * i / period;

			if (lastFrequency > 0 && lastFrequency != frequency) {
				double lastPeriod = (double) sampleRate / lastFrequency;
				double lastAngle = 2.0D * Math.PI * (i - 1) / lastPeriod;
				phaseShift -= angle - lastAngle;
			}

			double val = Math.sin(angle + phaseShift) * volume;
			os.addDouble(val);
			lastFrequency = frequency;
		}
	}

	void writeMorse(String morseText) throws InterruptedException {
		for (int i = 0; i < morseText.length(); i++) {
			char ch = morseText.charAt(i);

			String morse = MorseTableUtil.getMorse(ch);

			if (morse == null)
				continue;

			double charDuration = 0;

			for (int j = 0; j < morse.length(); j++) {

				double tick = Constants.ONE_MINUTE
						/ (this.textRate.getValue() * PARIS_TICKS);
				double wordTick = ((PARIS_TICKS * tick) - (PARIS_CHARS * tick))
						/ PARIS_SPACE;

				double ditDuration = tick;
				double dahDuration = ditDuration * 3D;
				charDuration = wordTick * 3D - ditDuration;
				double wordDuration = wordTick * 7D - charDuration
						- ditDuration;

				char c = morse.charAt(j);

				switch (c) {
				case '.':
					writeTone(ditDuration);
					break;
				case '-':
					writeTone(dahDuration);
					break;
				case ' ':
					writeSilence(wordDuration);
					break;
				default:
					break;
				}
				writeSilence(ditDuration);
			}
			writeSilence(charDuration);
		}
	}

	private void writeSilence(double ms) throws InterruptedException {
		int length = (int) (sampleRate * ms / Constants.ONE_SECOND);

		for (int i = 0; i < length; i++)
			os.addDouble(0.0D);
	}

	private void writeTone(double ms) throws InterruptedException {
		int length = (int) (sampleRate * ms / Constants.ONE_SECOND);
		int rampLength = (int) (sampleRate * RAMP_MS / Constants.ONE_SECOND);
		double frequency = this.frequency.getValue();
		double period = (double) sampleRate / frequency;

		for (int i = 0; i < length; i++) {
			double volume;

			if (i < rampLength) {
				volume = (double) i / (double) rampLength;
			} else if (i > length - rampLength) {
				volume = (double) (length - i) / (double) rampLength;
			} else {
				volume = 1.0D;
			}

			double angle = 2.0D * Math.PI * i / period;

			double val = Math.sin(angle) * volume;
			os.addDouble(val);
		}
	}
}
