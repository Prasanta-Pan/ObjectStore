package org.pp.objectstore.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Collection {
	/**
	 * Name of the collection. 
	 * Class name will be used by default if not specified
	 * @return
	 */
	public String value() default "";	
}
