package com.profiprog.gwt.conf;

import java.util.ArrayList;
import java.util.Collection;

import com.profiprog.configinject.VariableResolver;

public class GwtModulePreferences extends UriRelatedConfiguration {

	private Collection<GwtModuleJsApi> jsApis;

	public GwtModulePreferences() {
		super();
	}

	public GwtModulePreferences(VariableResolver variables, String variableBaseName) {
		super(variables, variableBaseName);

		String jsApisDef = variables.resolveValue(variableBaseName + ".jsApis");
		if (jsApisDef != null) {
			ArrayList<GwtModuleJsApi> jsApis = new ArrayList<GwtModuleJsApi>();
			for (String jsApiBaseName : jsApisDef.split(",")) {
				jsApiBaseName = jsApiBaseName.trim();
				jsApis.add(new GwtModuleJsApi(variables, jsApiBaseName));
			}
			setJsApis(jsApis);
		}
	}

	public Collection<GwtModuleJsApi> getJsApis() {
		return jsApis;
	}

	public void setJsApis(Collection<GwtModuleJsApi> jsApis) {
		this.jsApis = jsApis;
	}

}
