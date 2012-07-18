package com.profiprog.configinject;

import java.io.IOException;


public interface InicializableVariableSource extends VariableSource {
	
	void initSource(VariableResolver variables) throws IOException;
}
