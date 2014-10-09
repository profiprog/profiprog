package com.profiprog.configinject;

public class VariableParser {

	private final String value;
	private int startIndex = 0;
	private int endIndex = 0;
	private int substitutionLength = 0;
	private String variableName;
	private String defaultValue;

	public VariableParser(String value) {
		this.value = value;
	}

	public boolean find() {
		while (startIndex >= 0) {
			startIndex = value.indexOf('$', startIndex + substitutionLength);
			if (startIndex < 0) return false;

			substitutionLength = 1;
			defaultValue = null;
			if (startIndex + 1 == value.length()) continue;

			if (value.charAt(startIndex + 1) == '{') {
				int endBracket = findRelevantChar('}', startIndex + 2, value.length());
				if (endBracket == -1) continue;
				else {
					int defaultValueSeparator = findRelevantChar(':', startIndex + 2, endBracket);
					if (defaultValueSeparator == -1) {
						variableName = value.substring(startIndex + 2, endBracket);
						substitutionLength = endBracket + 1 - startIndex;
					}
					else {
						variableName = value.substring(startIndex + 2, defaultValueSeparator);
						defaultValue = value.substring(defaultValueSeparator + 1, endBracket);
					}
					substitutionLength = endBracket + 1 - startIndex;
					return true;
				}
			}
			if (value.charAt(startIndex + 1) == '$') {
				variableName = "$";
				substitutionLength = 2;
				return true;
			}
			int end = findFirstNonValidNameCharacter(startIndex + 1);
			if (end == startIndex + 1) continue;
			variableName = value.substring(startIndex + 1, end);
			substitutionLength = end - startIndex;
			return true;
		}
		return false;
	}

	private int findFirstNonValidNameCharacter(int fromIndex) {
		while (fromIndex < value.length() && isValidNameCharacter(value.charAt(fromIndex))) fromIndex++;
		return fromIndex;
	}

	private boolean isValidNameCharacter(char c) {
		return Character.isJavaIdentifierPart(c) && c != '$' || c == '.' || c == '-';
	}

	private int findRelevantChar(char c, int fromIndex, int toIndex) {
		int stack = 0;
		while (fromIndex < toIndex && (stack > 0 || value.charAt(fromIndex) != c)) {
			switch (value.charAt(fromIndex)) {
				case '{': stack++; break;
				case '}': stack--; break;
			}
			fromIndex++;
		}
		return fromIndex < toIndex ? fromIndex : -1;
	}

	public void appendReplacement(StringBuffer sb, String replacement) {
		sb.append(value.substring(endIndex, startIndex)).append(replacement);
		endIndex += substitutionLength;
	}

	public void appendTail(StringBuffer sb) {
		sb.append(value.substring(endIndex));
		endIndex = value.length();
	}

	public void appendReplacement(StringBuilder sb, String replacement) {
		sb.append(value.substring(endIndex, startIndex)).append(replacement);
		endIndex = startIndex + substitutionLength;
	}

	public void appendTail(StringBuilder sb) {
		sb.append(value.substring(endIndex));
		endIndex = value.length();
	}

	public String definition() {
		return value.substring(startIndex, startIndex + substitutionLength);
	}

	public String variableName() {
		return variableName;
	}

	public String defaultValue() {
		return defaultValue;
	}
}
