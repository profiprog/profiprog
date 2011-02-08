package com.profiprog.configinject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.profiprog.configinject.LiveFile.FileLoader;

public class PropertyFileVariableSource implements InicializableVariableSource, ChangeableVariableSource, FileLoader {
	
	private static final Logger logger = LoggerFactory.getLogger(PropertyFileVariableSource.class);
	
	private final AtomicReference<Properties> properties = new AtomicReference<Properties>();
	private final LiveFileHandler fileHandler = new LiveFileHandler();

	private VariableSourceChangeHandler changeHandler;

	public void setPropertyFileName(String fileName) {
		fileHandler.setPropertyFile(fileName);
	}
	
	public void setSystemResourceAsTemplate(String template) {
		fileHandler.setTemplateResource(template);
	}
	
	@Override
	public String getRawValue(String variableName) throws NullPointerException {
		fileHandler.checkChanges(this);
		return properties.get().getProperty(variableName);
	}
	
	public void setCheckPeriodInSeconds(int seconds) {
		fileHandler.setChangesCheckPeriod(seconds);
	}
	
	@Override
	public void loadFile(File file) throws IOException {
		logger.info("Loading configuration from {}", file);
		properties.set(new Properties(file));
		if (changeHandler != null)
			changeHandler.notifyVariableSourceChange(this);
	}

	@Override
	public void initSource(VariableResolver variables) throws IOException {
		fileHandler.setVariables(variables);
		fileHandler.checkChanges(this);
	}
	
	public Properties getProperties() {
		fileHandler.checkChanges(this);
		return properties.get();
	}
	
	public Map<String,String> extractDirectChildrenProperties(String prefix) {
		Map<String,String> result = new HashMap<String, String>();
		
		Properties subProperties = properties.get().select(prefix);
		for (String child : subProperties) {
			if (child.indexOf('.') == -1) {
				result.put(child, subProperties.getProperty(child));
			}
		}
		return result;
	}

	@Override
	public void setVariableSourceChangeHandler(VariableSourceChangeHandler changeHandler) {
		this.changeHandler = changeHandler; 
	}

}
