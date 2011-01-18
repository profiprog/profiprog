package com.profiprog.gwt.conf;

import java.util.StringTokenizer;

import org.springframework.util.AntPathMatcher;

import com.profiprog.configinject.VariableResolver;

public class UriRelatedConfiguration {

	private static final AntPathMatcher pathMatcher = new AntPathMatcher();

	private String[] includes;
	private String[] excludes;

	public UriRelatedConfiguration() {}

	public UriRelatedConfiguration(VariableResolver variables, String variableBaseName) {
		setIncludes(variables.resolveValue(variableBaseName + ".includes", ""));
		setExcludes(variables.resolveValue(variableBaseName + ".excludes", ""));
	}

	public void setIncludeList(String[] includes) {
		validatePathPatterns(includes);
		this.includes = this.includes == null ? includes : mergeArrays(this.includes, includes);
	}

	public void setIncludes(String includes) {
		setIncludeList(split(includes));
	}

	public void setExcludeList(String[] excludes) {
		validatePathPatterns(excludes);
		this.excludes = this.excludes == null ? excludes : mergeArrays(this.excludes, excludes);
	}

	public void setExcludes(String excludes) {
		setExcludeList(split(excludes));
	}

	private static String[] split(String patterns) {
		StringTokenizer tokenizer = new StringTokenizer(patterns);
		String[] result = new String[tokenizer.countTokens()];
		int counter = 0;

		while (tokenizer.hasMoreTokens())
			result[counter++] = tokenizer.nextToken();

		return result;
	}

	private static String[] validatePathPatterns(String[] paths) {
		for (int i = 0; i < paths.length; i++)
			if (!paths[i].startsWith("/"))
				paths[i] = "/" + paths[i];
		return paths;
	}

	public boolean matchesUri(String requestUri) {
		return includes(requestUri) && !excludes(requestUri);
	}

	private boolean includes(String requestUri) {
		if (includes == null) return true;
		for (String include : includes)
			if (pathMatcher.match(include, requestUri)) return true;
		return false;
	}

	private boolean excludes(String requestUri) {
		if (excludes != null)
			for (String exclude : excludes)
				if (pathMatcher.match(exclude, requestUri)) return true;
		return false;
	}

	public static String[] mergeArrays(String[] array1, String[] array2) {
		int size1 = array1 == null ? 0 : array1.length;
		int size2 = array2 == null ? 0 : array2.length;
		String[] result = new String[size1 + size2];
		if (size1 > 0) System.arraycopy(array1, 0, result, 0, size1);
		if (size2 > 0) System.arraycopy(array2, 0, result, size1, size2);
		return result;
	}
}
