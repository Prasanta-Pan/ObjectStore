package org.pp.objectstore.interfaces;

public interface FieldRenameHandler {
    /**
     * Rename existing field to a new name in database
     * @param existingFieldName
     * @return
     */
	public String newFieldName(String existingFieldName);
}
