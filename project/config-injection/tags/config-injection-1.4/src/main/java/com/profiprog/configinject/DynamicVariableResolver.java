package com.profiprog.configinject;


public interface DynamicVariableResolver {

	String resolve(String variableName, VariableResolver variables);

}
