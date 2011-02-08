package com.profiprog.configinject.jses;

import com.profiprog.configinject.VariableResolver;
import com.profiprog.configinject.VariableSource;
import com.profiprog.jses.AbstractEvent;

public class VariableSourceChangedEvent extends AbstractEvent<VariableSourceChangedEventHandler, VariableSource> {

	public VariableSourceChangedEvent(VariableSource source) {
		super(source);
	}

	public VariableResolver getVariables() {
		VariableSource source = getSource();
		return source instanceof VariableResolver ? (VariableResolver) source : null;
	}

	@Override
	protected void dispatch(VariableSourceChangedEventHandler handler) {
		handler.onPropertyFileChanged(this);
	}

}
