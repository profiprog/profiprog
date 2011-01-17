package com.profiprog.configinject;

import java.io.File;
import java.io.IOException;

public interface LiveFile {
	
	interface FileLoader {
		void loadFile(File file) throws IOException; 
	}
	
	void checkChanges(FileLoader loader);
	
}
