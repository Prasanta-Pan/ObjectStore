package org.pp.objectstore.test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * 
 * @author prasantsmac
 *
 */
public class CustomerClassLoader extends ClassLoader {
	/**
	 * Base package of test classes and resources
	 */
    static final String BASE_PKG = "org/pp/objectstore/";
    /**
     * Resource package path
     */
    static final String RES_PKG = "resources/";
    /**
     * Domain class path
     */
    static final String TEST_PKG = "test/domain/";
    /**
     * The package where source file exist
     */
    static final String PKG = "org.pp.objectstore.test.domain.";
   	/**
	 * To support class loading of resources from class path
	 */
	private String baseUri;
	
	/**
	 * To ensure we get the proper class path
	 * @param parent
	 * @throws Exception 
	 */
	public CustomerClassLoader(ClassLoader parent) throws Exception {
		super(parent);
		// path to customer class
		String resPkg = "resources/CustomerImp.java";
		// get corresponding URL
		URL url = getResource(BASE_PKG + resPkg);
		// ensure Customer class found
		if (url == null)
			throw new Exception("Not able to locate CustomerImp.java");
		// get URL path only
		baseUri = url.toURI().toString();
		// get URL base path
		baseUri = baseUri.substring(0, baseUri.length() - resPkg.length());
	}
	
	/**
	 * Copy java or class file from resource to test package
	 * @param path
	 * @return
	 */
    final void copy(String src, String dst) throws Exception {
		// get source path
	    Path srcPath = Paths.get(new URI(baseUri + src));
	    // get destination path
	    Path dstPath = Paths.get(new URI(baseUri + dst));
		// now do atomic move
		Files.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);		
	}	
		
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class loadClass(String name) throws ClassNotFoundException {
		try {
			// we have no business with other packages
			if (!name.startsWith(PKG))
				return super.loadClass(name);
			// get the actual class name
			String nameOnly = name.substring(name.lastIndexOf('.') + 1, name.length());
			// Customer class must be resolved by parent class
			if ("Customer".equals(nameOnly))
				return super.loadClass(name);
			// move source file to target folder as Customer
			copy(RES_PKG + nameOnly + ".java", TEST_PKG + "CustomerImp.java");
			// compile Customer.java
			File srcFile = new File(new URI(baseUri + TEST_PKG + "CustomerImp.java"));
			// get Compiler API
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			// compile Customer.java
			compiler.run(null, null, null, srcFile.getPath());
			// read entire customer class data 2726
			byte[] classData = Files.readAllBytes(Paths.get(new URI(baseUri + TEST_PKG + "CustomerImp.class")));
			// redefine Customer class
			return defineClass(PKG + "CustomerImp", classData, 0, classData.length);	
		} catch (Exception e) {
			throw new ClassNotFoundException(e.getMessage());
		}
	}

}
