package com.zephreo.reactorgen;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

	/**
	 * Splits a String according to a regex, keeping the splitter at the end of each substring
	 * 
	 * @param input The input String
	 * @param regex The regular expression upon which to split the input
	 * @return An array of Strings
	 */
	public static String[] split(String input, String regex) {
	    ArrayList<String> res = new ArrayList<String>();
	    Pattern p = Pattern.compile(regex);
	    Matcher m = p.matcher(input);
	    int pos = 0;
	    while (m.find()) {
	        res.add(input.substring(pos, m.end()));
	        pos = m.end();
	    }
	    if(pos < input.length()) {
	    	res.add(input.substring(pos));
	    }
	    return res.toArray(new String[res.size()]);
	}

	/**
	 * Whole string is lowercase except those after spaces or underscores (including first character)
	 * 
	 * @param input
	 * @return
	 */
	public static String toTitleCase(String input) {
		String out = "";
		for(String str : split(input, " |_")) {
			out += str.toString().substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
		}
		return out;
	}

	public static void prl(Object message) {
		System.out.print(message + "\n");
	}

	public static void pr(Object message) {
		System.out.print(message);
	}

	static float round(float num, int places) {
		return Math.round(num * 10 * places) / (10 * (float) places);
	}

	

}
