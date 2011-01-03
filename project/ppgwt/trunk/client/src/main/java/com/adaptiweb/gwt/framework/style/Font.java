package com.adaptiweb.gwt.framework.style;

import com.google.gwt.user.client.Element;

public enum Font implements DynamicStyle {
	NORMAL("fontWeight"),
	BOLD("fontWeight"),
	ITALIC("fontStyle"),
	LINE_THROUGH("textDecoration"),
	UNDERLINE("textDecoration");

	private final String style;

	Font(String stylePropertyName) {
		style = stylePropertyName;
	}

	public void apply(Element element) {
		element.getStyle().setProperty(style, name().replaceAll("_", "-").toLowerCase());
	}

	public void cancel(Element element) {
		element.getStyle().setProperty(style, "");
	}
}
