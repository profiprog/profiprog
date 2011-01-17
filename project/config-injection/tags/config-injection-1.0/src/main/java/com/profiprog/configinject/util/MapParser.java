package com.profiprog.configinject.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MapParser {
	
	private MapParser() {}
	
	public static Map<String,String> parseMap(String string) {
		Map<String,String> result = new LinkedHashMap<String, String>();
		string = string.trim();
		if (string.isEmpty()) return result;

		int endEntry = -1;
		do {
			int beginEntry = endEntry + 1;

			endEntry = indexOf(string, endEntry, ',');
			if (beginEntry == endEntry) continue;
			
			String entry = string.substring(beginEntry, endEntry).trim();
			if (entry.isEmpty()) continue;
			
			int separator = indexOf(entry, -1, ':');
			
			String key = clean(separator == entry.length() ? entry : entry.substring(0, separator).trim());
			String val = clean(separator == entry.length() ? null : entry.substring(separator + 1).trim());
			
			result.put(key, val);
		} while(endEntry < string.length());
		return result;
	}

	private static String clean(String string) {
		if (string == null) return null;
		else string = string.trim();
		
		if (string.length() < 2) return string;
		
		// remove quotes
		char firstChar = string.charAt(0);
		if ((firstChar == '\'' || firstChar == '"') && firstChar == string.charAt(string.length() - 1)) {
			string = string.substring(1, string.length() - 1);
		}
		
		int begin = 0, end = string.indexOf('\\');
		if (end == -1 || end == string.length() - 1) return string;
		
		StringBuilder sb = new StringBuilder(string.length() - 1);
		do {
			sb.append(string.substring(begin, end));
			begin = end + 1;
			end = string.indexOf('\\', begin);
		} while (begin < string.length() && end > begin);
		
		sb.append(string.substring(begin, string.length()));

		return sb.toString();
	}

	private static int indexOf(String string, int fromIndex, char ch) {
		assert fromIndex < string.length();
		
		char inQuotes = 0;
		boolean ignoreChar = false; 
		
		while(++fromIndex < string.length()) {
			if (ignoreChar) {
				ignoreChar = false;
				continue;
			}
			char c = string.charAt(fromIndex);
			switch (c) {
			case '\\':
				ignoreChar = true;
				break;
			case '\'':
			case '"':
				if (inQuotes == 0) inQuotes = c;
				else if (inQuotes == c) inQuotes = 0;
				break;
			default:
				if (c == ch && inQuotes == 0) return fromIndex;
				break;
			}
		}
		return fromIndex;
	}

}
