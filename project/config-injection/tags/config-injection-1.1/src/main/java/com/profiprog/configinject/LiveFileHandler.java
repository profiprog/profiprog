package com.profiprog.configinject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringValueResolver;

public class LiveFileHandler implements LiveFile {
	
	private static class FileRefenrece {
		final String fileName;
		final File file;
		final AtomicLong lastChanged = new AtomicLong(0L);
		
		FileRefenrece(String fileName) {
			this(new File(fileName), fileName);
		}

		public FileRefenrece(File file, String fileName) {
			this.fileName = fileName;
			this.file = file;
		}
	}

	private static final String DEFAULT_CHECK_PERIOD_SYSTEM_PROPERTY = "liveFile_defaultCheckPeriod";
	private static final String DEVELOPMENT_MODE_SYSTEM_PROPERTY = "liveFile_developmentMode";
	private static final String CLASSPATH_URL = "classpath:";
	private StringValueResolver variables;
	private String fileName;
	private long changesCheckPeriod = 2 * 60 * 1000; //default 2 minutes
	private final AtomicLong lastChecked = new AtomicLong(0L);
	private final AtomicReference<FileRefenrece> fileReference = new AtomicReference<FileRefenrece>();
	private String templateResource;
	private boolean developmentMode = false;
	protected final FileLoader loader;
	private boolean fileNameChecking;
	
	@Deprecated
	public LiveFileHandler() {
		this(null);
	}

	public LiveFileHandler(FileLoader loader) {
		this.loader = loader;
		
		String changesCheckPeriod = System.getProperty(DEFAULT_CHECK_PERIOD_SYSTEM_PROPERTY);
		if (changesCheckPeriod != null) setChangesCheckPeriod(Integer.parseInt(changesCheckPeriod));

		String developmentMode = System.getProperty(DEVELOPMENT_MODE_SYSTEM_PROPERTY);
		if (developmentMode != null) this.developmentMode = Boolean.parseBoolean(developmentMode);
	}
	
	public void setFileNameChecking(boolean fileNameChecking) {
		this.fileNameChecking = fileNameChecking;
	}

	public void setPropertyFile(String fileName) {
		this.fileName = fileName;
		this.fileReference.set(null);
	}
	
	public File getFile() {
		return getFileReference().file;
	}

	/**
	 * Enables substituting variables in values. Optional.
	 */
	public void setVariables(StringValueResolver variables) {
		this.variables = variables;
	}

	/**
	 * Minimal change checking period in seconds.
	 * Optional, default is 2 minutes.
	 */
	public void setChangesCheckPeriod(int changesCheckPeriod) {
		this.changesCheckPeriod = changesCheckPeriod * 1000L;
	}
	
	public int getChangesCheckPeriod() {
		return (int) (changesCheckPeriod / 1000L);
	}

	/**
	 * Enable auto creating missing file during initialization. Optional.
	 */
	public void setTemplateResource(String templateResource) {
		this.templateResource = templateResource;
	}

	public void checkChanges() {
		checkChanges(loader);
	}
	
	@Deprecated
	public void checkChanges(FileLoader loader) {
		if (isQuiteTime()) return;

		FileRefenrece reference = getFileReference();

		long lastChanged = reference.lastChanged.get();
		long changedTime = reference.file.lastModified();
		if (changedTime == 0 && templateResource != null && !reference.file.exists())
			changedTime = createDefaultFile(reference.file);

		if (changedTime != lastChanged && reference.lastChanged.compareAndSet(lastChanged, changedTime)) {
			try {
				loatFile(loader, reference.file);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected void loatFile(FileLoader loader, File file) throws IOException {
		loader.loadFile(file);
	}

	private boolean isQuiteTime() {
		long currentTime = System.currentTimeMillis();
		long lastChecked = this.lastChecked.get();
		return currentTime - lastChecked < changesCheckPeriod || !this.lastChecked.compareAndSet(lastChecked, currentTime);
	}

	private String substituteVariables(String string) {
		return variables == null ? string : variables.resolveStringValue(string);
	}

	private FileRefenrece getFileReference() {
		if (this.fileName == null)
			throw new IllegalStateException("Attribute 'propertyFile' wasn't initialized!");
		
		FileRefenrece reference = fileReference.get();
		String fileName = fileNameChecking || reference == null ? substituteVariables(this.fileName) : null;
		
		if (reference == null || fileNameChecking && !fileName.equals(reference.fileName)) {
			reference = prepareFileReference(fileName);
			fileReference.set(reference);
		}
		return reference;
	}

	private FileRefenrece prepareFileReference(String fileName) {
		if (developmentMode && templateResource != null) {
			String templateName = substituteVariables(templateResource);
			File templateFile = resourceAsFile(templateName);
			if (templateFile != null && templateFile.exists()) {
				return new FileRefenrece(templateFile, fileName);
			}
		}
		return new FileRefenrece(fileName);
	}

	private File resourceAsFile(String fileName) {
		try {
			return ResourceUtils.getFile(fileName);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	public long createDefaultFile(File file) {
		String templateResource = substituteVariables(this.templateResource);
		InputStream is = null;
		OutputStream os = null;
		try {
			FileUtils.forceMkdir(file.getParentFile());

			if (templateResource.startsWith(CLASSPATH_URL)) {
				templateResource = templateResource.substring(CLASSPATH_URL.length());
				is = new ClassPathResource(templateResource).getInputStream();
				os = new FileOutputStream(file);
				IOUtils.copy(is, os);
			} else {
				FileUtils.copyFile(ResourceUtils.getFile(templateResource), file, false);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os);
		}
		return file.lastModified();
	}
}
