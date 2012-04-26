package com.profiprog.configinject.jses;

import com.profiprog.configinject.ChangeableVariableSource;
import com.profiprog.configinject.ChangeableVariableSource.VariableSourceChangeHandler;
import com.profiprog.jses.EventDispatcher;

public class VariableSourceChangedEventBlaster implements VariableSourceChangeHandler {
	
	private EventDispatcher dispacher;

	public VariableSourceChangedEventBlaster(EventDispatcher dispacher) {
		this.dispacher = dispacher;
	}

	@Override
	public void notifyVariableSourceChange(ChangeableVariableSource changedSource) {
		dispacher.fireEvent(new VariableSourceChangedEvent(changedSource));
	}

}
