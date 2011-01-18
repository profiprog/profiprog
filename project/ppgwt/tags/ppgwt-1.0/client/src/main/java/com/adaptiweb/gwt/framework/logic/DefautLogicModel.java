package com.adaptiweb.gwt.framework.logic;

public class DefautLogicModel extends AbstractLogicModel {

	public DefautLogicModel() {
		super();
	}

	public DefautLogicModel(boolean initialLogicValue) {
		super(initialLogicValue);
	}

	//changed visibility
	@Override
	public void setLogicValue(boolean value) {
		super.setLogicValue(value);
	}
	
}
