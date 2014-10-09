package com.profiprog.configinject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HostNameVariableSource implements VariableSource {

	private static final Logger logger = LoggerFactory.getLogger(HostNameVariableSource.class);

	private String variableName = "hostname";
	private String failValue = null;

	public void setVariableName(String variableName) {
		Assert.notNull(variableName);
		this.variableName = variableName;
	}

	public void setFailValue(String failValue) {
		this.failValue = failValue;
	}

	@Override
	public String getRawValue(String variableName) throws NullPointerException {
		return this.variableName.equals(variableName) ? resolveHostName() : null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	private String resolveHostName() {
		try {
			String hostName = InetAddress.getLocalHost().getHostName();
			if (hostName != null) return hostName;
			logger.error("Hostname was resolved to null, using failValue ${{}}={}", variableName, failValue);
		} catch (UnknownHostException e) {
			logger.error("Unable to resolve hostname ${" + variableName + "}", e);
		}
		return failValue;
	}
}
