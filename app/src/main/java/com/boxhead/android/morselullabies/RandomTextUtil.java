package com.boxhead.android.morselullabies;

import java.util.Random;

import android.annotation.SuppressLint;

/**
 * The purpose of this class is to return random snippets of text. It is a
 * helper class used to facilitate debugging.
 * 
 * @author <A HREF="mailto:paul@paulrowe.com">Paul &quot;Boxhead&quot; Rowe</A>
 */
@SuppressLint("DefaultLocale")
final class RandomTextUtil {
	static Random r = new Random();

	/**
	 * Get a random letter.
	 * 
	 * @return String
	 */
	static String getRandomLetter() {
		int lt = (int) (r.nextDouble() * 26.0) + 65;

		char[] ch = new char[1];

		ch[0] = (char) lt;

		String out = new String(ch);
		out = out.toUpperCase();
		return out;
	}

	/**
	 * Get a random number
	 * 
	 * @return String
	 * @param int maximum number of digits in the number
	 * @param int minimum number of digits in the number
	 */
	static String getRandomNumber(int maxLength, int lowerBound) {
		int length = (int) (r.nextDouble() * (double) (maxLength - lowerBound))
				+ lowerBound;

		StringBuffer sb = new StringBuffer();

		int n = 0;
		while (n < length) {
			sb.append(getRandomNumberDigit());
			n++;
		}
		return sb.toString();
	}

	/**
	 * Get a random number digit.
	 * 
	 * @return String
	 */
	static String getRandomNumberDigit() {
		int lt = (int) (r.nextDouble() * 10.0) + 48;
		char[] ch = new char[1];
		ch[0] = (char) lt;

		return new String(ch);
	}

	/**
	 * Get a random sentence of words. overloaded method. calls associated
	 * method with lower bound of words as 10, upper bound of words as 50,
	 * maximum word length of 10 and minimum word length of 3.
	 * 
	 * @return String
	 */
	static String getRandomSentence() {
		return getRandomSentence(50, 10, 10, 3);
	}

	/**
	 * Get a random sentence of words. overloaded method. calls associated
	 * method with lower bound of words as 10, maximum word length of 10 and
	 * minimum word length of 3.
	 * 
	 * @return String
	 * @param int maximum number of words
	 */
	static String getRandomSentence(int maxWords) {
		return getRandomSentence(maxWords, 10, 10, 3);
	}

	/**
	 * Get a random sentence of words
	 * 
	 * @return String
	 * @param int maximum number of words
	 * @param int minimum number of words
	 * @param int maximum word length
	 * @param int minimum word length
	 */
	static String getRandomSentence(int maxWords, int lowerBound,
			int maxWordLength, int wordLowerBound) {
		int words = (int) (r.nextDouble() * (double) (maxWords - lowerBound))
				+ lowerBound;

		StringBuffer sb = new StringBuffer();

		int n = 0;
		while (n < words) {
			if (n == (words - 1)) {
				sb.append(getRandomWord(maxWordLength, wordLowerBound) + ".");
			} else {
				sb.append(getRandomWord(maxWordLength, wordLowerBound) + " ");
			}
			n++;
		}
		return sb.toString();
	}

	/**
	 * Get a random word.
	 * 
	 * @return String
	 * @param int maximum length of the word
	 * @param int minimum length of the word
	 */
	static String getRandomWord(int maxLength, int lowerBound) {
		int length = (int) (r.nextDouble() * (double) (maxLength - lowerBound))
				+ lowerBound;
		StringBuffer sb = new StringBuffer();
		int n = 0;
		while (n < length) {
			sb.append(getRandomLetter());
			n++;
		}
		return sb.toString();
	}

	private RandomTextUtil() {
	}

}
