package com.profiprog.configinject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringValueResolver;

import java.io.IOException;
import java.util.*;

/**
 * Provides method {@link #resolveStringValue(String)} for replacing variables in string.
 * This class also implements {@link VariableSource} and it allows simply define
 * multi variable sources in one.
 */
public final class VariableResolver implements ChangeableVariableSource, StringValueResolver {
	
	private static final Logger logger = LoggerFactory.getLogger(VariableResolver.class);

	private final VariableSource[] sources;
	
	private int initStatus;

	private final ThreadLocal<LinkedList<String>> evaluatingDynamicProperties = 
		new ThreadLocal<LinkedList<String>>() {
			protected LinkedList<String> initialValue() {
				return new LinkedList<String>();
			}
		};
	
	/**
	 * Variable sources passed in this constructor are used for searching variables.
	 * Searching is in same order as sources are. 
	 * @param sources ordered list of Variable sources (first sources can overshadow last sources)
	 */
	public VariableResolver(VariableSource... sources) {
		this.sources = sources;
		initStatus = sources.length;
		try {
			while (initStatus > 0) {
				VariableSource source = this.sources[initStatus - 1];
				if (source instanceof InicializableVariableSource)
					((InicializableVariableSource) source).initSource(this);
				initStatus--;
			}
		} catch (IOException e) {
			throw new IllegalStateException("Error while initializing " + (initStatus + 1) + ". variable source.", e);
		}
	}
	
	/**
	 * In given string substitute variable references with its values.
	 * Variable reference can be specified with two forms:<ol>
	 * <li><b>$<i>variableName</i></b> - this is general form</li>
	 * <li><b>${<i>variableName</i>}</b> - this form must be used if variable name
	 * is immediately followed by character allowed in variableName.</li>
	 * </ol>
	 * Variables are searched in variable sources defined in constructor. 
	 * @param string which can contains variable references.
	 * @return string with replaced variable references, if variables was found. 
	 * @see #VariableResolver(VariableSource...)
	 */
	@Override
	public String resolveStringValue(String string) {
		if (string == null) return null;

		VariableParser m = new VariableParser(string);
		if (!m.find()) return string;

		StringBuilder sb = new StringBuilder(string.length());
		do {
			String variableName = m.variableName();
			String defaultValue = m.defaultValue();

			String variableValue = "$".equals(variableName) ? "$" : resolveValue(variableName, defaultValue);
			if (variableValue == null) throw new IllegalStateException("Missing property " + variableName);
			else m.appendReplacement(sb, variableValue);

		} while (m.find());
		m.appendTail(sb);
		
		return sb.toString();
	}

	private IllegalStateException circularSubstitutionError(LinkedList<String> trace, String key) {
		StringBuilder sb = new StringBuilder("Circular substitution ");
		for (String path : trace) {
			sb.append(path);
			if (path.equals(key)) sb.append('*');
			sb.append(" <- ");
		}
		sb.append(key);
		return new IllegalStateException(sb.toString());
	}

	/**
	 * Search value of variable specified by name.
	 * Values are searched in variable sources defined in {@link #VariableResolver(VariableSource...) constructor}.
	 * @see VariableSource#getRawValue(java.lang.String)
	 */
	public String getRawValue(String variableName) {
		for(int i = initStatus; i < sources.length; i++) {
			String variableValue = sources[i].getRawValue(variableName);
			if(variableValue != null) return variableValue;
		}
		return null;
	}
	
	public String resolveValue(String variableName) {
		return resolveValue(variableName, null);
	}
	
	public String resolveValue(String variableName, String defaultValue) {
		LinkedList<String> trace = evaluatingDynamicProperties.get();
		if (trace.contains(variableName)) throw circularSubstitutionError(trace, variableName);
		trace.addLast(variableName);
		try {
			String resolvedVariableName = resolveStringValue(variableName);
			String rawValue = getRawValue(resolvedVariableName);
			return rawValue != null ? resolveStringValue(rawValue) : resolveStringValue(defaultValue);
		} finally {
			trace.removeLast();
			if(trace.size() == 0) evaluatingDynamicProperties.remove();
		}
	}
	
	/**
	 * Resolve all variables in values of given map. 
	 * @param map with values replaced
	 * @return new map with resolved variables.
	 */
	public Map<String, String> resolveValues(Map<String, String> map) {
		Map<String, String> result = new HashMap<String, String>(map.size());
		
		for(Map.Entry<String, String> entry : map.entrySet())
			result.put(entry.getKey(), resolveStringValue(entry.getValue()));
		
		return result;
	}
	
	public Map<String, String> resolveAndReplaceValues(Map<String, String> map) {
		for(Map.Entry<String, String> entry : map.entrySet())
			map.put(entry.getKey(), resolveStringValue(entry.getValue()));
		return map;
	}

	/**
	 * Resolve all variables in keys and values of given map. 
	 * @param collection collection which can contains variables
	 * @return new list with resolved variables.
	 */
	public List<String> resolveItems(Collection<String> collection) {
		List<String> result = new ArrayList<String>(collection.size());
		for(String item : collection) result.add(resolveStringValue(item));
		return result;
	}
	
	public void resolveAndReplaceItems(Collection<String> collection) {
		Collection<String> aux = new ArrayList<String>(collection.size());
		for(String item : collection) aux.add(resolveStringValue(item));
		
		collection.clear();
		collection.addAll(aux);
	}

	/**
	 * Create new VariableSroucer by wrapping map.
	 * @param map 
	 * @return new VariableSroucer by wrapping map.
	 */
	public static VariableSource asSource(final Map<String,String> map) {
		return new VariableSource() {
			public String getRawValue(String variableName) throws NullPointerException {
				return map.get(variableName);
			}
		};
	}

	/**
	 * Create new VariableSource by wrapping Properties instance.
	 * @param properties
	 * @return new VariableSource by wrapping Properties instance.
	 */
	public static VariableSource asSource(final Properties properties) {
		return new VariableSource() {
			public String getRawValue(String variableName) throws NullPointerException {
				return properties.getProperty(variableName);
			}
		};
	}

	@Override
	public void setVariableSourceChangeHandler(final VariableSourceChangeHandler changeHandler) {
		VariableSourceChangeHandler handler = new VariableSourceChangeHandler() {
			@Override
			public void notifyVariableSourceChange(ChangeableVariableSource changedSource) {
				changeHandler.notifyVariableSourceChange(VariableResolver.this);
			}
		};
		for (VariableSource source : sources)
			if (source instanceof ChangeableVariableSource)
				((ChangeableVariableSource) source).setVariableSourceChangeHandler(handler);
	}

}
