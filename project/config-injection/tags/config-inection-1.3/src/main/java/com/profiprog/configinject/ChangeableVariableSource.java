package com.profiprog.configinject;

public interface ChangeableVariableSource extends VariableSource {
	
	interface VariableSourceChangeHandler {
		void notifyVariableSourceChange(ChangeableVariableSource changedSource);
	}
	
	void setVariableSourceChangeHandler(VariableSourceChangeHandler changeHandler);
}
