package com.profiprog.configinject;

import java.util.HashMap;
import java.util.Map;

import com.profiprog.configinject.util.MapParser;

public class MapVariableSource implements VariableSource {
	
	private final Map<String, String> map;

	public MapVariableSource(Map<String,String> map) {
		this.map = map;
	}
	
	public MapVariableSource() {
		this(new HashMap<String, String>());
	}

	public MapVariableSource(String string) {
		map = MapParser.parseMap(string);
	}

	@Override
	public String getRawValue(String variableName) throws NullPointerException {
		return map.get(variableName);
	}

}
