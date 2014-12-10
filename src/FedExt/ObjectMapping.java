package FedExt;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * This class ObjectMapping will be used during the json parsing to save the read data
 * Creation, 22.11.2014
 * Last modification, 02.12.2014
 *
 * @author Laurent
 * @version 1.2
 */
public class ObjectMapping {
    private Object oObject2Return;
    private ArrayList<Object> oArrayList2Return;
    private ObjectProperty property;
    private boolean initialClass;
    private int level;

    /**
     * Constructor
     */
    public ObjectMapping() {
        this.level = -1;
    }

    /**
     * Constructor
     *
     * @param clazz          of object to fill
     * @param collectionType of object to fill
     */
    public ObjectMapping(Class clazz, String collectionType) {
        this();
        property = new ObjectProperty(clazz, collectionType);

        // Create an instance to return
        this.createInstance(clazz, collectionType);
    }

    /**
     * Constructor
     *
     * @param property Object to fill
     */
    public ObjectMapping(ObjectProperty property) {
        if (property != null) {
            this.property = property;
            this.createInstance(this.property.getClazz(), this.property.getCollectionType());
        }
    }

    /**
     * Constructor
     *
     * @param property Object to fill
     */
    public ObjectMapping(ObjectProperty property, boolean initialClass) {
        this(property);
        this.initialClass = initialClass;
    }

    /**
     * Instantiation of object to return
     */
    private void createInstance(Class clazz, String collectionType) {
        if (clazz != null) {
            // Create the instance to fill
            try {
                // Instantiate local Object used to fill values
                this.oObject2Return = clazz.newInstance();

                // Instantiate Object to be returned
                if (!collectionType.isEmpty()) {
                    this.oArrayList2Return = new ArrayList<>();
                }
            } catch (Exception e) {

            }
        }
    }

    /**
     * Assign the string Value to the mapped field
     *
     * @param propertyMap properties of the mapped value
     * @param value       value to set
     */
    public void setValue(ObjectProperty propertyMap, Object value) {
        if (propertyMap != null) {
            String valueStr = "";

            if (value.getClass().getSimpleName().equals("String"))
                valueStr = value.toString();

            if (!propertyMap.getSetter().isEmpty()) {
                // Assign value using a public setter
                try {
                    // Declare Class
                    Class[] cArg = new Class[1];

                    // Assign Class Type according to the MobiS Object
                    switch (propertyMap.getClassShortName()) {
                        case "String":
                            cArg[0] = String.class;
                            break;

                        case "Char":
                            cArg[0] = char.class;
                            break;

                        case "byte":
                            cArg[0] = byte.class;
                            break;

                        case "Byte":
                            cArg[0] = Byte.class;
                            break;

                        case "int":
                            cArg[0] = int.class;
                            break;

                        case "Integer":
                            cArg[0] = Integer.class;
                            break;

                        case "long":
                            cArg[0] = long.class;
                            break;

                        case "Long":
                            cArg[0] = Long.class;
                            break;

                        case "short":
                            cArg[0] = short.class;
                            break;

                        case "Short":
                            cArg[0] = Short.class;
                            break;

                        case "double":
                            cArg[0] = double.class;
                            break;

                        case "Double":
                            cArg[0] = Double.class;
                            break;

                        case "float":
                            cArg[0] = float.class;
                            break;

                        case "Float":
                            cArg[0] = Float.class;
                            break;

                        case "boolean":
                            cArg[0] = boolean.class;
                            break;

                        case "Boolean":
                            cArg[0] = Boolean.class;
                            break;

                        default:
                            cArg[0] = propertyMap.getClazz();
                            break;
                    }

                    // Declare the setter according to his name
                    try {
                        Method m = this.property.getClazz().getDeclaredMethod(propertyMap.getSetter(), cArg);

                        // Assign Value using the setter
                        switch (propertyMap.getClassShortName().toLowerCase()) {
                            case "string":
                                m.invoke(this.oObject2Return, valueStr);
                                break;

                            case "char":
                                if (valueStr.length() > 0)
                                    m.invoke(this.oObject2Return, valueStr.charAt(0));
                                break;

                            case "byte":
                                m.invoke(this.oObject2Return, valueStr.getBytes(Charset.forName("UTF-8")));
                                break;

                            case "int":
                            case "integer":
                                m.invoke(this.oObject2Return, (int) Double.parseDouble(valueStr));
                                break;

                            case "long":
                                m.invoke(this.oObject2Return, (long) Double.parseDouble(valueStr));
                                break;

                            case "short":
                                m.invoke(this.oObject2Return, (short) Double.parseDouble(valueStr));
                                break;

                            case "double":
                            case "Double":
                                m.invoke(this.oObject2Return, Double.parseDouble(valueStr));
                                break;

                            case "float":
                                m.invoke(this.oObject2Return, Float.parseFloat(valueStr));
                                break;

                            case "boolean":
                                if (valueStr.equalsIgnoreCase("true") || valueStr.equalsIgnoreCase("false"))
                                    m.invoke(this.oObject2Return, Boolean.valueOf(valueStr));
                                break;

                            default:
                                break;
                        }
                    } catch (NullPointerException e) {

                    }
                } catch (NoSuchMethodException e) {

                } catch (IllegalAccessException e) {

                } catch (InvocationTargetException e) {

                }
            } else {
                try {
                    Field field = propertyMap.getField();

                    switch (propertyMap.getClassShortName().toLowerCase()) {
                        case "string":
                            field.set(this.oObject2Return, valueStr);
                            break;

                        case "char":
                            if (valueStr.length() > 0)
                                field.set(this.oObject2Return, valueStr.charAt(0));
                            break;

                        case "byte":
                            field.set(this.oObject2Return, valueStr.getBytes(Charset.forName("UTF-8")));
                            break;

                        case "int":
                        case "integer":
                            field.set(this.oObject2Return, (int) Double.parseDouble(valueStr));
                            break;

                        case "long":
                            field.set(this.oObject2Return, (long) Double.parseDouble(valueStr));
                            break;

                        case "short":
                            field.set(this.oObject2Return, (short) Double.parseDouble(valueStr));
                            break;

                        case "double":
                        case "Double":
                            field.set(this.oObject2Return, Double.parseDouble(valueStr));
                            break;

                        case "float":
                            field.set(this.oObject2Return, Float.parseFloat(valueStr));
                            break;

                        case "boolean":
                            if (valueStr.equalsIgnoreCase("true") || valueStr.equalsIgnoreCase("false"))
                                field.set(this.oObject2Return, Boolean.valueOf(valueStr));
                            break;

                        default:
                            field.set(this.oObject2Return, value);
                            break;
                    }
                } catch (IllegalAccessException e) {

                }
            }
        }
    }

    /**
     * Assign the property Value of the subObject to the mapped field of the mainObject
     *
     * @param subObject Object to transfer in the current object
     */
    public void setObjectValue(ObjectMapping subObject) {
        if (this.property != null)
            setValue(subObject.property, subObject.oObject2Return);

        // Reset the subObject
        resetInstance(subObject);
    }

    /**
     * Add filled object in the array
     */
    public void addInArray() {
        this.oArrayList2Return.add(this.oObject2Return);
    }

    /**
     * Get the object properties
     *
     * @return the object properties
     */
    public ObjectProperty getProperty() {
        return this.property;
    }

    /**
     * Check if started
     *
     * @return the start indicator
     */
    public boolean isStarted() { return this.level >= 0; }

    /**
     * Start mapping
     */
    public void start(int level) {
        this.initialClass = true;
        this.level = level;
    }

    /**
     * Get the level
     *
     * @return the level
     */
    public int getLevel() {
        return this.level;
    }

    /**
     * Get the property class
     *
     * @return the property class
     */
    public Class getPropertyClazz() {
        if (this.property == null)
            return null;
        else
            return this.property.getClazz();
    }

    /**
     * Get the property class
     *
     * @return the property class
     */
    public Class getParentClass() {
        if (this.property == null)
            return null;
        else
            return this.property.getParentClass();
    }

    /**
     * Get the property class
     *
     * @return the property class
     */
    public boolean isInitialClass() {
        return this.initialClass;
    }

    /**
     * Get the collection type
     *
     * @return the type of collection (Array, ArrayList or empty)
     */
    public String getCollectionType() {
        if (this.property == null)
            return null;
        else
            return this.property.getCollectionType();
    }

    /**
     * Get the object to return
     *
     * @return the object
     */
    public <T> T getObjectToReturn() {
        return (T) this.oObject2Return;
    }

    /**
     * Get the Array to return
     *
     * @return the Array
     */
    public <T> T[] getArrayToReturn() {
        int nb = this.oArrayList2Return.size();

        if (nb == 0)
            return null;
        else {
            Class<?> c = this.getPropertyClazz();
            Object o = Array.newInstance(c, nb);

            for (int i=0; i<nb; i++)
                Array.set(o, i, this.oArrayList2Return.get(i));

            return (T[]) o;
        }
    }

    /**
     * Transfer the result
     */
    public void transfer(ObjectMapping mainObject, boolean reset) {
        if (!reset)
            this.oObject2Return = mainObject.oObject2Return;

        if (!this.getCollectionType().isEmpty())
            this.addInArray();

        if (reset)
            resetInstance(mainObject);
    }

    /**
     * Reset the object to return
     */
    public void resetInstance(ObjectMapping object2Reset) {
        try {
            object2Reset.oObject2Return = object2Reset.property.getClazz().newInstance();
        } catch(IllegalAccessException e) {

        } catch(InstantiationException e) {

        }
    }

}
