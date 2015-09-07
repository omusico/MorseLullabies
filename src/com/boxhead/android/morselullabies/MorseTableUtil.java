package com.boxhead.android.morselullabies;

import java.util.Map;
import java.util.TreeMap;

final class MorseTableUtil {
	private static Map<String, String> morseTable = new TreeMap<String, String>(
			String.CASE_INSENSITIVE_ORDER);

	static {
		morseTable.put("A", ".-");
		morseTable.put("B", "-...");
		morseTable.put("C", "-.-.");
		morseTable.put("D", "-..");
		morseTable.put("E", ".");
		morseTable.put("F", "..-.");
		morseTable.put("G", "--.");
		morseTable.put("H", "....");
		morseTable.put("I", "..");
		morseTable.put("J", ".---");

		morseTable.put("K", "-.-");
		morseTable.put("L", ".-..");
		morseTable.put("M", "--");
		morseTable.put("N", "-.");
		morseTable.put("O", "---");
		morseTable.put("P", ".--.");
		morseTable.put("Q", "--.-");
		morseTable.put("R", ".-.");
		morseTable.put("S", "...");
		morseTable.put("T", "-");

		morseTable.put("U", "..-");
		morseTable.put("V", "...-");
		morseTable.put("W", ".--");
		morseTable.put("X", "-..-");
		morseTable.put("Y", "-.--");
		morseTable.put("Z", "--..");
		morseTable.put("/", "-..-.");
		morseTable.put(".", ".-.-.-");
		morseTable.put(",", "--..--");
		morseTable.put("?", "..--..");

		morseTable.put("0", "-----");
		morseTable.put("1", ".----");
		morseTable.put("2", "..---");
		morseTable.put("3", "...--");
		morseTable.put("4", "....-");
		morseTable.put("5", ".....");
		morseTable.put("6", "-....");
		morseTable.put("7", "--...");
		morseTable.put("8", "---..");
		morseTable.put("9", "----.");
		morseTable.put(" ", " ");
	}

	public static String getMorse(char ch) {
		return morseTable.get(String.valueOf(ch));
	}

	private MorseTableUtil() {
	}
}
