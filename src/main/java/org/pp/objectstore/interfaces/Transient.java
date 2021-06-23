package org.pp.objectstore.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
/**
 *  Just to ensure field annotated with 
 *  transient never get persisted
 * @author prasantsmac
 *
 */
public @interface Transient {

}
