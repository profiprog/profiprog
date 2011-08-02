package com.profiprog.configinject;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.springframework.util.StringValueResolver;

@Deprecated
public class LiveJavaProperties extends java.util.Properties implements LiveFile.FileLoader {
	
	private StringValueResolver variables;
	private LiveFile liveFile;
	
	public void setVariableResolver(StringValueResolver variables) {
		this.variables = variables;
	}
	
	public void setLiveFile(LiveFile liveFile) {
		this.liveFile = liveFile;
	}
	
	@Override
	public String getProperty(String key) {
		liveFile.checkChanges(this);
		String result = super.getProperty(key);
		return variables == null ? result : variables.resolveStringValue(result);
	}
	
	@Override
	public void loadFile(File file) throws IOException {
		replaceContent(file.exists() ? new Properties(file).toMap() : Collections.<String,String>emptyMap());
	}
	
	private synchronized void replaceContent(Map<String,String> values) {
		this.clear();
		this.putAll(values);
	}

}
