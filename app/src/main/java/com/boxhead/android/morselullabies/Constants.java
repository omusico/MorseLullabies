package com.boxhead.android.morselullabies;

final class Constants {

	static final long ONE_SECOND = 1000L;
	static final long FIFTEEN_SECONDS = 15L * ONE_SECOND;
	static final long ONE_MINUTE = 60L * ONE_SECOND;
	static final long FIVE_MINUTES = 5L * ONE_MINUTE;
	static final long ONE_HOUR = 60L * ONE_MINUTE;

	static final int AUDIO_SAMPLE_RATE = 44100;
	static final int AUDIO_BUFFER_SIZE = AUDIO_SAMPLE_RATE * 4;
	static final int DOUBLE_BUFFER_SIZE = AUDIO_SAMPLE_RATE * 4;
	static final int DOUBLE_BUFFER_POOL_SIZE = 25;
	static final int DOUBLE_BUFFER_QUEUE_SIZE = 5;
	static final int NUMBER_OF_MORSE_AUDIO_TRACKS = 1;

	static final int MORSE_VOLUME_CHANGE_INTERVAL = AUDIO_SAMPLE_RATE / 10;
	static final double MORSE_VOLUME_DIRECTION_CHANGE_RATE = 0.0005D;
	static final double MORSE_VOLUME_MINMAX_CHANGE_DIRECTION = 0.005D;
	static final double MORSE_VOLUME_FLOOR = -0.05D;
	static final double MORSE_VOLUME_MAX = 0.15D;
	static final double MORSE_VOLUME_CEILING = MORSE_VOLUME_MAX+0.2D;

	static final int NOISE_VOLUME_CHANGE_INTERVAL = AUDIO_SAMPLE_RATE / 10;
	static final double NOISE_VOLUME_DIRECTION_CHANGE_RATE = 0.001D;
	static final double NOISE_VOLUME_MINMAX_CHANGE_DIRECTION = 0.05D;
	static final double NOISE_VOLUME_FLOOR = 0.3D;
	static final double NOISE_VOLUME_MAX = 1.0D - MORSE_VOLUME_MAX;
	static final double NOISE_VOLUME_CEILING = NOISE_VOLUME_MAX + 0.1D;

	static final int PLAYER_VOLUME_CHANGE_INTERVAL = 1;
	static final double PLAYER_VOLUME_DIRECTION_CHANGE_RATE = 0.001D;
	static final double PLAYER_VOLUME_MINMAX_CHANGE_DIRECTION = 0.05D;
	static final double PLAYER_VOLUME_FLOOR = -0.3D;
	static final double PLAYER_VOLUME_MAX = 0.9D;
	static final double PLAYER_VOLUME_CEILING = 0.7D;
	static final long MEDIA_TRACK_MIN_SLEEP_TIME = ONE_MINUTE;
	static final long MEDIA_TRACK_MAX_SLEEP_TIME = FIVE_MINUTES;
    static final long MEDIA_TRACK_VOLUME_CHANGE_SLEEP_TIME = 250L;
    
	static final String MORSE_TEXT_OPML_FILE = "/feedly.2014-02-15.opml";
	static final int MORSE_TEXT_TIMEOUT = (int) (5L * ONE_SECOND);
	static final int MORSE_TEXT_DOWNLOAD_THREAD_COUNT = 5;
	static final int MORSE_TEXT_MIN_SPACES = 3;

	static final int MORSE_WRITER_TEXT_RATE_CHANGE_INTERVAL = 10;
	static final double MORSE_WRITER_TEXT_RATE_DIRECTION_CHANGE_RATE = 0.05D;
	static final double MORSE_WRITER_TEXT_RATE_MINMAX_CHANGE_DIRECTION = 0.25D;
	static final double MORSE_WRITER_TEXT_RATE_MIN = 14.0D;
	static final double MORSE_WRITER_TEXT_RATE_MAX = 18.0D;

	static final int MORSE_WRITER_FREQUENCY_CHANGE_INTERVAL = 10;
	static final double MORSE_WRITER_FREQUENCY_DIRECTION_CHANGE_RATE = 0.10D;
	static final double MORSE_WRITER_FREQUENCY_MINMAX_CHANGE_DIRECTION = 5.0D;
	static final double MORSE_WRITER_FREQUENCY_MIN = 500.0D;
	static final double MORSE_WRITER_FREQUENCY_MAX = 900.0D;

	static final long DELAY_BEFORE_AUDIO_TRACK_THREAD_START = FIFTEEN_SECONDS;

	static final int LOW_PASS_FILTER_CHANGE_INTERVAL = AUDIO_SAMPLE_RATE / 10;
	static final double LOW_PASS_FILTER_CHANGE_RATE = 0.001D;
	static final double LOW_PASS_FILTER_MINMAX_DIRECTION = 0.01D;
	static final double LOW_PASS_FILTER_MIN = 0.10D;
	static final double LOW_PASS_FILTER_MAX = 0.50D;

	static final int VOLUME_DISPLAY_PRECISION = 2;

	static final int MORSE_VOLUME_CONTROL_MAX_VALUE = 1000;
}
