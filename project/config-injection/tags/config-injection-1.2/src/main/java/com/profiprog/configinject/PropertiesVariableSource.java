package com.profiprog.configinject;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

public class PropertiesVariableSource implements VariableSource, InitializingBean {
    private Properties properties;
    private String charset = "ISO-8859-1";
    private String source;

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setSource(String source) throws IOException {
        this.source = source;
    }

    @Override
    public String getRawValue(String variableName) throws NullPointerException {
        return properties.getProperty(variableName);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        properties = new Properties(new ClassPathResource(source).getURL(), charset);
    }
}
