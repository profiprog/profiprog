package com.adaptiweb.gwt.widget.menu;

import com.google.gwt.resources.client.ImageResource;

@Deprecated
public interface MenuRegistration {

	boolean isVisible();

	void setVisible(boolean on);

	boolean isEnabled();

	void setEnabled(boolean on);

	void setIcon(ImageResource resource);

	String getLabel();

	void setLabel(String label);

	MenuRegistration setPerformHandler(MenuPerformHandler handler);
}
