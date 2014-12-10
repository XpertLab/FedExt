package FedExt;

import java.lang.reflect.Field;

/**
 * This class ObjectProperty is used internally by ObjectParser to determine
 * the property of a field owned by the given MobiS class
 * Creation, 10.11.2014
 * Last modification, 24.11.2014
 *
 * @author Laurent
 * @version 1.0
 */
public class ObjectProperty {
    protected Field field;
    protected String collectionType;
    protected String setter;
    protected Class clazz;
    protected Class clazzParent;
    protected boolean isClass;

    /**
     * Constructor
     */
    public ObjectProperty() {
        this.collectionType = "";
        this.setter = "";
    }

    /**
     * Constructor
     *
     * @param clazz of object to fill
     * @param collectionType type of object to fill
     */
    public ObjectProperty(Class clazz, String collectionType) {
        this();

        this.clazz = clazz;
        this.collectionType = collectionType;
    }

    /**
     * Constructor
     *
     * @param clazz currently parsed
     * @param field to handle
     */
    public ObjectProperty(Class clazz, Field field, String setter) {
        this();
        fieldProperty(clazz, field, setter);
    }

    /**
     * Determine the field properties
     *
     * @param   field to consider
     */
    protected void fieldProperty(Class clazzParent, Field field, String setter) {
        this.clazz = field.getType();
        this.clazzParent = clazzParent;
        this.field = field;
        this.setter = setter;
        this.isClass = checkIfClass(this.getClassShortName());
        this.collectionType = setCollectionType();
    }

    /**
     * Determine if the field is a class
     *
     * @param   field to consider
     * @return  Simplified Field Type Name
     */
    public String getFieldType(Field field) {
        String fieldType = field.getGenericType().toString();
        fieldType = fieldType.replace("class ", "");

        return fieldType;
    }

    /**
     * Determine if the field is a class
     *
     * @return true if it's a class, false otherwise
     */
    public boolean checkIfClass(String fieldType) {
        // Check primitives
        return !(   fieldType.toLowerCase().equals("string") ||
                    fieldType.toLowerCase().equals("boolean") ||
                    fieldType.toLowerCase().equals("double") ||
                    fieldType.toLowerCase().equals("float") ||
                    fieldType.toLowerCase().equals("short") ||
                    fieldType.toLowerCase().equals("long") ||
                    fieldType.toLowerCase().equals("int") ||
                    fieldType.toLowerCase().equals("integer"));
    }

    /**
     * Determine if the field is a collection
     */
    private String setCollectionType() {
        String sCollectionType = "";
        String sClass = this.getClazz().toString();

        if (sClass != null) {
            if (sClass.contains("ArrayList")) {
                // ArrayList
                sCollectionType = "ArrayList";
            } else {
                // Array
                if (sClass.contains("[L")) {
                    sCollectionType = "Array";
                }
            }
        }

        return sCollectionType;
    }

    /**
     * this method cleans the given string in order to consider just the part after the last dot (".")
     *
     * @param fullString to consider
     * @return the string part after the last dot
     */
    private static String getLastPartAfterDot(String fullString) {
        int iPos = fullString.lastIndexOf(".");
        String partString = fullString;

        if (iPos >= 0 && iPos < fullString.length())
            partString = fullString.substring(iPos+1, fullString.length());

        return partString;
    }

    /**
     * return if the current field is a class
     *
     * @return true if the field is a class, false otherwise
     */
    public boolean isClass() { return this.isClass; }

    /**
     * return the class name of the current field
     *
     * @return the class of the field
     */
    public Class getClazz() { return this.clazz; }

    /**
     * return the class name of the current field
     *
     * @return the class of the field
     */
    public Class getParentClass() {
        return this.clazzParent;
    }

    /**
     * return the field name
     *
     * @return the name of the field
     */
    //public String getFieldName() { return this.fieldName; }

    /**
     * return the type of the current field
     *
     * @return the type of the field
     */
    public String getClassShortName() { return this.clazz.getSimpleName(); }

    /**
     * return the type of the current field
     *
     * @return the field
     */
    public Field getField() {
        return this.field;
    }

    /**
     * return the type of the current field
     *
     * @return the type of the field
     */
    public String getCollectionType() {
        return this.collectionType;
    }

    /**
     * return the name of the setter method
     *
     * @return the setter name
     */
    public String getSetter() {
        if (this.setter == null)
            this.setter = "";

        return this.setter;
    }
}
