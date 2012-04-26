package com.profiprog.configinject;

import java.io.File;
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
	
	private static class FileReference {
		final String fileName;
		final File file;
		final AtomicLong lastChanged = new AtomicLong(0L);
		
		FileReference(String fileName) {
			this(new File(fileName), fileName);
		}

		public FileReference(File file, String fileName) {
			this.fileName = fileName;
			this.file = file;
		}
	}

	private static final String CLASSPATH_PREFIX = "classpath:";
	private StringValueResolver variables;
	private String fileName;
	private long changesCheckPeriod = 2 * 60 * 1000; //default 2 minutes
	private final AtomicLong lastChecked = new AtomicLong(0L);
	private final AtomicReference<FileReference> fileReference = new AtomicReference<FileReference>();
	private String templateResource;
	protected final FileLoader loader;
	private boolean fileNameChecking;
	
	public LiveFileHandler(FileLoader loader) {
		this.loader = loader;
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
        if (isQuiteTime()) return;

        FileReference reference = getFileReference();

        long lastChanged = reference.lastChanged.get();
        long changedTime = reference.file.lastModified();
        if (changedTime == 0 && templateResource != null && !reference.file.exists())
            changedTime = createDefaultFile(reference.file);

        if (changedTime != lastChanged && reference.lastChanged.compareAndSet(lastChanged, changedTime)) {
            try {
                loadFile(loader, reference.file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
	
	protected void loadFile(FileLoader loader, File file) throws IOException {
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

	private FileReference getFileReference() {
		if (this.fileName == null)
			throw new IllegalStateException("Attribute 'propertyFile' wasn't initialized!");
		
		FileReference reference = fileReference.get();
		String fileName = fileNameChecking || reference == null ? substituteVariables(this.fileName) : null;
		
		if (reference == null || fileNameChecking && !fileName.equals(reference.fileName)) {
            reference = new FileReference(fileName);
			fileReference.set(reference);
		}
		return reference;
	}

	public long createDefaultFile(File file) {
		String templateResource = substituteVariables(this.templateResource);
		InputStream is = null;
		OutputStream os = null;
		try {
			FileUtils.forceMkdir(file.getParentFile());

			if (templateResource.startsWith(CLASSPATH_PREFIX)) {
				templateResource = templateResource.substring(CLASSPATH_PREFIX.length());
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
