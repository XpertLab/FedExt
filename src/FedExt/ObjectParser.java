package FedExt;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * This class ObjectParser is used internally by FedExtService to parse the given MobiS class
 * in order to discover generically the mappable members
 * Creation, 28.10.2014
 * Last modification, 24.11.2014
 *
 * @author Laurent
 * @version 1.0
 */
public class ObjectParser {
    private Map<String, ObjectProperty> fields;

    /**
     * Constructor with internal initialisation
     */
    public ObjectParser() {
        fields = new HashMap<String, ObjectProperty>();
    }

    /**
     * Constructor with internal initialisation
     *
     * @param clazz Name of the Class to parse
     */
    public ObjectParser(Class clazz) throws Exception {
        this();
        getClassMembers(clazz);
    }

    /**
     * Fill the list of MobiS object members to map, which can be set :
     *      - directly if the field is public, or
     *      - through a public setter beginning with "set" or "add" followed by the field name
     *
     * @param clazz Name of the Class to parse
     */
    private void getClassMembers(Class clazz) throws Exception {
        // getFields gives all public fields up the entire class hierarchy
        for (Field field : clazz.getFields()) {
            ObjectProperty fieldProperty = new ObjectProperty(clazz, field, null);
            fields.put(clazz.getSimpleName() + "." + field.getName().toLowerCase(), fieldProperty);

            // if the field is a class, his fields must be analysed by recursive call
            if (fieldProperty.isClass())
                getClassMembers(fieldProperty.getClazz());
        }

        // getMethods gives all public methods up the entire class hierarchy.
        for (Method method : clazz.getMethods()) {
            // Looking for public setters which begins traditionally with "set" or "add"
            if (method.getName().toLowerCase().substring(0, 3).equals("set") || method.getName().toLowerCase().substring(0, 3).equals("add")) {
                // Initialisation of params
                String setter = method.getName();

                // The char ! at the line begin ensures a correct replacement (for instance "getAddress" must not be modified)
                String fieldName = ("!" + setter.toLowerCase()).replace("!set", "").replace("!add", "");

                // Add field in the mappable list if not exists in order to privilegiate setter vs direct access to field
                findPrivateField(clazz, fieldName, setter);
            }
        }
    }

    /**
     * Fill the list of MobiS object members to map, which can be set :
     *      - directly if the field is public, or
     *      - through a public setter beginning with "set" or "add" followed by the field name
     *
     * @param clazz Name of the Class to parse
     */
    private void findPrivateField(Class clazz, String fieldName, String setter) throws Exception {
        // getDeclaredFields gives all fields (no matter the accessibility)
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().toLowerCase().equals(fieldName)) {
                // Private or protected field has been found
                ObjectProperty fieldProperty = new ObjectProperty(clazz, field, setter);

                fields.put(clazz.getSimpleName() + "." + field.getName().toLowerCase(), fieldProperty);

                // If the field is a class, his fields member must be analysed by recursive call
                if (fieldProperty.checkIfClass(fieldProperty.getClassShortName()))
                    getClassMembers(fieldProperty.getClazz());
            }
        }
    }

    /**
     * Return the list of MobiS object members available for mapping
     *
     * @return the list of mappable MobiS object members separated by <cr>
     */
    public String memberList() {
        String fieldList = new String("");

        if (fields.size() > 0) {
            for (Map.Entry<String, ObjectProperty> entry : fields.entrySet()) {
                String field = entry.getKey();
                String fieldInfo = "";
                ObjectProperty fieldProperty = entry.getValue();

                if (fieldProperty.getCollectionType().isEmpty()) {
                    fieldInfo += field + " : " + fieldProperty.getClassShortName();
                } else {
                    fieldInfo += field + " : " + fieldProperty.getCollectionType() + " of " + fieldProperty.getClassShortName();
                }

                // Setter
                if (!fieldProperty.getSetter().isEmpty())
                    fieldInfo += " -> " + fieldProperty.getSetter() + "()";

                fieldList += fieldInfo + "\n";
            }
        }

        return fieldList;
    }

    /**
     * Return the list of MobiS object members available for mapping
     *
     * @param key Name of the field in the json file
     * @return the corresponding property in the class, null if nothing has been found
     */
    public ObjectProperty map(String className, String key) {
        return fields.get(className + "." + key.toLowerCase());
    }

    /**
     * Return the number of mappable fields
     *
     * @return the number of mappable fields
     */
    public int getFieldNumber() {
        return fields.size();
    }
}
