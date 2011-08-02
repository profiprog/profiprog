package com.adaptiweb.gwt.framework;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.event.shared.SimpleEventBus;

@Deprecated
public class AbstractHasHandlers implements HasHandlers {
	
	protected final EventBus handlers;
	
	protected AbstractHasHandlers() {
		handlers = new SimpleEventBus();
	}

	public final void fireEvent(GwtEvent<?> event) {
		handlers.fireEvent(event);
	}

}
