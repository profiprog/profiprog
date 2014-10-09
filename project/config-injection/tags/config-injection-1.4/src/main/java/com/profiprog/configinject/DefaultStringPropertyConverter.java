package com.profiprog.configinject;



class DefaultStringPropertyConverter implements PropertyConverter<String> {

	@Override
	public String convert(VariableResolver variables, String name, String defaultValue) {
		return variables.resolveValue(name, defaultValue.length() == 0 ? null : defaultValue);
	}

	@Override
	public Class<String> getType() {
		return String.class;
	}
}
