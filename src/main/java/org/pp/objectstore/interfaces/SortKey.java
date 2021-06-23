package org.pp.objectstore.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
/**
 * Indicate field annotated as SortKey can be used as Sort Key
 * @author prasantsmac
 *
 */
public @interface SortKey {
	
}
