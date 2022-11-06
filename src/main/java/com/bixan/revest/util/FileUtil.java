/**
 * 
 */
package com.bixan.revest.util;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/**
 * @author sambit.bixan@gmail.com
 *
 */
@Component
public class FileUtil {	
	public static String getNameSansExt(File f) {
		if (null == f) {
			return null;
		}
		
		if (f.isDirectory()) {
			return "";
		}
		
		if (f.isFile()) {
			String name = f.getName();
			
			int dot = name.lastIndexOf('.');
			if (dot == -1 || dot == 0) {
				return name;
			}
			
			String result = name.substring(0, dot);
			return result;
		}
		
		return "";
	}
	
	public static File getFileFromResource(String fileName) throws IOException {
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		Resource resource = resourceLoader.getResource("classpath:static/data/" + fileName);
		
		if (null != resource) {
			return resource.getFile();
		}
		
		return null;
	}
}
