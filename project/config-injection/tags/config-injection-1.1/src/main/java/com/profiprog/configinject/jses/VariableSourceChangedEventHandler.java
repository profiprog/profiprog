package com.profiprog.configinject.jses;

import com.profiprog.jses.EventHandler;

public interface VariableSourceChangedEventHandler  extends EventHandler {

	void onPropertyFileChanged(VariableSourceChangedEvent event);

}
