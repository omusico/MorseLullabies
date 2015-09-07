package com.boxhead.android.morselullabies;

import java.util.ArrayList;
import java.util.List;

final class AudioEffectChain implements AudioEffect {

	private List<AudioEffect> effects = new ArrayList<AudioEffect>();

	AudioEffectChain() {
	}

	void add(AudioEffect effect) {
		effects.add(effect);
	}

	@Override
	public void process(double[] doubleBuffer) {
		for (AudioEffect effect : effects)
			effect.process(doubleBuffer);
	}
}
