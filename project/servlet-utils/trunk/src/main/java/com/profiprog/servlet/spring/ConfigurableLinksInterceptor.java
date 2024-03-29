package com.profiprog.servlet.spring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.profiprog.configinject.VariableResolver;
import com.profiprog.configinject.jses.VariableSourceChangedEvent;
import com.profiprog.configinject.jses.VariableSourceChangedEventHandler;

/**
 * This {@link HandlerInterceptor} provides configurable links data for views.
 * 
 * <fieldset class="propertiesFile"><legend>Input data from project configuration (e.g. config.properties)</legend>
 * <pre>
# comma-separated and ordered list of link identifiers
<b>externalLinks</b> = contact,help,about

# only one parameter for every link is required "url"
<b>externalLinks</b>.contact.url = http://domain.name/contact
# rendered contact link would be: &lt;a href="http://domain.name/contact"&gt;contact&lt;/a&gt;

# anything what can be as value of href attribute
<b>externalLinks</b>.help.url = javascript:alert('TOTO: configure help link')
# optionally you can specify additional attributes "class", "id", "style", "tabindex", "title" and "target"
<b>externalLinks</b>.help.target = _blank
# rendered help link would be: &lt;a href="javascript:alert('TOTO: configure help link')" target="_blank"&gt;help&lt;/a&gt;

# if text link isn't appropriate as link identifier you can change it 
<b>externalLinks</b>.about = About us
<b>externalLinks</b>.about.url = /about.html
# rendered help link would be: &lt;a href="/about.html"&gt;About us&lt;/a&gt;</pre>
 * </fieldset>
 * <fieldset class="xmlFile"><legend>Modify spring configuration for dispatcher servlet</legend>
 * <pre>
    &lt;mvc:interceptors&gt;
        &lt;bean class="com.profiprog.servlet.spring.ConfigurableLinksInterceptor"&gt;
            &lt;!-- You can specify property base key as constructor's value
                to other than default value (which is 'externalLinks'). --&gt;
            &lt;constructor-arg value="<b>externalLinks</b>"/&gt;
        &lt;/bean&gt;
    &lt;/mvc:interceptors&gt;</pre>
 * </fieldset>
 * <fieldset class="jspFile"><legend>Use prepared links in JSP view</legend>
 * <pre>
    &lt;c:forEach items="${<b>externalLinks</b>}" varStatus="status"&gt;
        ${status.current}
        &lt;c:if test="${!status.last}"&gt;
            &lt;span class="separator"&gt;|&lt;/span&gt;
        &lt;/c:if&gt;
    &lt;/c:forEach&gt;</pre>
 * </fieldset>
 * 
 * TODO document ability for change links definition in runtime  
 *
 */
public class ConfigurableLinksInterceptor extends HandlerInterceptorAdapter implements VariableSourceChangedEventHandler, InitializingBean {

	@SuppressWarnings("serial")
	public static class LinkData extends LinkedHashMap<String, String> {

		public LinkData(String url, String text) {
			put("href", url);
			put(null, text);
		}

		@Override
		public String toString() {
			StringBuilder result = new StringBuilder("<a");

			for (java.util.Map.Entry<String, String> attribute : entrySet()) {
				String key = attribute.getKey();
				String value = attribute.getValue();

				if (key != null && value != null)
					result.append(' ').append(key).append("=\"").append(value).append('"');
			}

			return result.append(">").append(get(null)).append("</a>").toString();
		}
	}


	private static final String MISSING_URL_MSG = "No URL is configured for external link '{}'! Will be skipped.\n"
		+ "Please put url into your config file as value of key: '{}.{}.url'.";

	private static final Logger logger = LoggerFactory.getLogger(ConfigurableLinksInterceptor.class);

	private static final String[] OPTIONAL_ATTRIBUTES = { "class", "id", "style", "tabindex", "title", "target" };

	private static final String DEFAULT_PROPERTY_BASENAME = "externalLinks";

	private String propertyBasename;
	private VariableResolver variableResolver;
	private boolean cacheOn = true;

	private List<Object> links = Collections.emptyList();
	
	public ConfigurableLinksInterceptor() {
		this(DEFAULT_PROPERTY_BASENAME);
	}
	
	@Autowired
	public void setVariableResolver(VariableResolver variableResolver) {
		this.variableResolver = variableResolver;
	}
	
	public void setCacheOn(boolean cacheOn) {
		this.cacheOn = cacheOn;
	}
	
	public void setPropertyBasename(String propertyBasename) {
		this.propertyBasename = propertyBasename;
	}
	
	/**
	 * @deprecated since 1.2 use {@link #setPropertyBasename(String)} instead
	 * @param propertyBasename
	 */
	public ConfigurableLinksInterceptor(String propertyBasename) {
		setPropertyBasename(propertyBasename);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (cacheOn) links = loadLinks();
	}

	public List<Object> loadLinks() {
		String list = variableResolver.resolveValue(propertyBasename, "");

		List<Object> links = new ArrayList<Object>();
		for (String item : list.split("\\s*,\\s*"))
			if (item.length() > 0) {
				String url = variableResolver.resolveValue(propertyBasename + "." + item + ".url");
				if (url == null) {
					logger.warn(MISSING_URL_MSG, params(item, propertyBasename, item));
					continue;
				}

				String text = variableResolver.resolveValue(propertyBasename + "." + item, item);

				LinkData link = new LinkData(url, text);
				for (String attribute : OPTIONAL_ATTRIBUTES) {
					String value = variableResolver.resolveValue(propertyBasename + "." + item + "." + attribute);
					if (value != null)
						link.put(attribute, value);
				}
				links.add(cacheOn ? Collections.unmodifiableMap(link) : link);
			}
		return cacheOn ? Collections.unmodifiableList(links) : links;
	}

	private static Object[] params(Object...params) {
		return params;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		if (modelAndView == null) return;
		assert modelAndView.getModel().get(propertyBasename) == null : "Links provided by controller aren't supported yet.";
		modelAndView.getModel().put(propertyBasename, cacheOn ? links : loadLinks());
	}

	@Override
	public void onPropertyFileChanged(VariableSourceChangedEvent event) {
		if (!cacheOn) return;
		logger.info("RELOADING LINKS");
		links = loadLinks();
	}
}
