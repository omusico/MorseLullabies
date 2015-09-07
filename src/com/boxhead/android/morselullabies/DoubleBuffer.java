package com.boxhead.android.morselullabies;

import java.util.concurrent.BlockingQueue;

final class DoubleBuffer {
	private int idx = 0;
	private double[] buf;
	private DoubleBufferPoolItem item;
	private BlockingQueue<DoubleBufferPoolItem> queue;
	private AudioEffect effect;
	private DoubleBufferPool pool;

	DoubleBuffer(DoubleBufferPool pool,
			BlockingQueue<DoubleBufferPoolItem> queue, AudioEffect effect) {
		this.queue = queue;
		this.effect = effect;
		this.pool = pool;
		try {
			this.item = pool.checkout();
		} catch (InterruptedException ie) {
			throw new RuntimeException("failed to do initial checkout?", ie);
		}
		this.buf = this.item.getDoubleBuffer();
	}

	void addDouble(double f) throws InterruptedException {
		buf[idx++] = f;
		if (idx >= buf.length) {
			if (this.effect != null)
				this.effect.process(buf);

			this.queue.put(item);
			this.item = pool.checkout();
			this.buf = this.item.getDoubleBuffer();
			this.idx = 0;
		}
	}
}
