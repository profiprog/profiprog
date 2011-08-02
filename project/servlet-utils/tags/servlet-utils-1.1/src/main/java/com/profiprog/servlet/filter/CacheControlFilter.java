package com.profiprog.servlet.filter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;

public class CacheControlFilter implements Filter {
	
	interface PlaceholderEvaluator { String eval(String params); }
	
	private static final Logger logger = LoggerFactory.getLogger(CacheControlFilter.class);
	private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\w+)(?::([^\\}]*))?}");
	
	private Map<String,Map<String,String>> patternToHeaders = new LinkedHashMap<String, Map<String,String>>();
	private Map<String,PlaceholderEvaluator> evaluators = new HashMap<String, CacheControlFilter.PlaceholderEvaluator>();
	
	@Override
	public void init(FilterConfig config) throws ServletException {
		@SuppressWarnings("unchecked") Enumeration<String> names = config.getInitParameterNames();
		
		while (names.hasMoreElements()) {
			String patterns = names.nextElement();
			Map<String,String> headers = parseHeaders(config.getInitParameter(patterns));
			
			for (String pattern : StringUtils.split(patterns, " \n\t")) {
				pattern = pattern.trim();
				if (pattern.isEmpty()) continue;
				if (pattern.charAt(0) != '/') pattern = "/" + pattern;
				patternToHeaders.put(pattern, headers);
			}
		}
		config.getInitParameter("exclude-patterns");
		
		// register evaluators
		evaluators.put("dateTimeOffset", new PlaceholderEvaluator() {
			private final TimeZone timeZone  = TimeZone.getTimeZone("GMT");
			@Override
			public String eval(String params) {
				int offsetInSecconds = Integer.parseInt(params);
				Date result = new Date(System.currentTimeMillis() + (offsetInSecconds * 1000L));
				SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
				sdf.setTimeZone(timeZone);
				return sdf.format(result);
			}
		});
	}

	private Map<String, String> parseHeaders(String raw) {
		Map<String, String> result = new HashMap<String, String>();
		
		for (String line : StringUtils.split(raw, '\n')) {
			line = line.trim();
			if (line.isEmpty()) continue;
			int index = line.indexOf(':');
			if (index == -1) {
				logger.warn("Invalid header entry: {}", line);
				continue;
			}
			String headerName = line.substring(0, index).trim();
			String headerValue = line.substring(index + 1).trim();
			result.put(headerName, headerValue);
		}
		return result;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		
		String requestPath = req.getRequestURI().substring(req.getContextPath().length());
		AntPathMatcher pathMatcher = new AntPathMatcher();
		
		for (String pattern : patternToHeaders.keySet()) {
			if (pathMatcher.match(pattern, requestPath)) {
				applyHeadersToResponse(patternToHeaders.get(pattern), res);
				logger.debug("Pattern '{}' applied to request '{}'", pattern, requestPath);
				break;
			}
		}
		chain.doFilter(request, response);
	}

	private void applyHeadersToResponse(Map<String, String> headers, HttpServletResponse res) {
		for (Map.Entry<String, String> header : headers.entrySet()) {
			String headerName = header.getKey();
			String headerValue = explandPlaceholders(header.getValue());
			if (headerValue != null) res.setHeader(headerName, headerValue);
		}
	}

	private String explandPlaceholders(String value) {
		Matcher m = PLACEHOLDER.matcher(value);
		if (!m.find()) return value;
		StringBuffer sb = new StringBuffer();
		do {
			String varName = m.group(1);
			PlaceholderEvaluator evaluator = evaluators.get(varName);
			if (evaluator == null) {
				logger.error("Unknow placeholder '{}'", varName);
				return null;
			}
			try {
				String replacement = evaluator.eval(m.group(2));
				if (replacement == null) return null;
				m.appendReplacement(sb, replacement);
			} catch (Throwable e) {
				logger.error("Can't evaluate placeholder: " + m.group(0), e);
				return null;
			}
		} while(m.find());
		m.appendTail(sb);
		return sb.toString();
	}

	@Override
	public void destroy() {}

}
