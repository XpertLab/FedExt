package FedExt;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This class FedExtService allows to easily implements the wrapper for external data source into MobiS
 * It will generically parse the MobiS Object Structure during the instantiation
 * Creation, 22.11.2014
 * Last modification, 02.12.2014
 *
 * @author Laurent
 * @version 1.2
 */
public class FedExtService {

    private String configName;
    private Map<String, String> params;
    private FedExtServer extServer;
    private ObjectParser members;
    private String jsonData;
    private String errorInfo;
    private URL urlWS;
    private ObjectMapping object2Return;

    /**
     * Constructor
     */
    public FedExtService() {
        // Init External Wrapping Server with default data
        this.extServer = FedExtServer.getInstance();

        // Params initialisation
        this.params = new HashMap<String, String>();
    }

    /**
     * Constructor
     *
     * @param configName Name of the configuration to use on the External Wrapping Server              
     */
    public FedExtService(String configName) throws Exception {
        this();
        this.configName = configName;
    }

    /**
     * Constructor
     *  @param configName Name of the configuration to use on the External Wrapping Server  
     * @param url URL of the Web Service to call directly if no configuration name has been defined            
     */
    public FedExtService(String configName, URL url) throws Exception {
        this(configName);
        this.urlWS = url;
    }

    /**
     * Add a parameter to be used by the Web Service
     *
     * @param param Name of the parameter (for instance "query")
     * @param value Value to set  (for instance "sushi")
     */
    public void setParam(String param, String value) {
        params.put(param, value);
    }

    /**
     * List of all parameters
     *
     * @return string with all parameters and their values
     */
    public String getParamList() {
        String paramList = new String("");
        Integer iCount = 0;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String param = entry.getKey();
            String value = entry.getValue();

            paramList += "Param " + (++iCount) + " : " + param + " = " + value + "\n";
        }

        return paramList;
    }

    /**
     * List of all mappable fields
     *
     * @return string with all mappable fields
     */
    public String getMappableMembers() {
        return this.members.memberList();
    }

    /**
     * Get the JSON response as a string
     *
     * @return string with the read JSON
     */
    public String getJsonData() {
        return this.jsonData;
    }

    /**
     * Call the Web Service and Fill the object
     *
     * @return the request object
     */
    public String getErrorInfo() {
        return this.errorInfo;
    }

    /**
     * Call the Web Service to load the data and fill the object
     */
    public <T> T fillObject(Class clazz) throws Exception {
        // Initialize Object to fill and return
        object2Return = new ObjectMapping(clazz, "");

        startProcess();

        return object2Return.getObjectToReturn();
    }

    /**
     * Call the Web Service to load the data and fill the object
     *
     * @return an Array of the request object
     */
    public <T> T[] fillArray(Class clazz) throws Exception {
        // Initialize Object to fill and return
        object2Return = new ObjectMapping(clazz, "Array");

        startProcess();

        return object2Return.getArrayToReturn();
    }

    /**
     * Call the Web Service and return the data
     */
    public void startProcess() throws Exception {
        // Init value
        this.errorInfo = "";

        // Check for valid class
        if (this.object2Return.getPropertyClazz() == null) {
            this.errorInfo = "missing reference class";
            return;
        }

        // Parse class
        parseClass();
        if (!this.errorInfo.isEmpty())
            return;

        // Routing
        selectRoute();
    }

    /**
     * Parse class and memorize fields properties
     */
    private void parseClass() throws Exception {
        // Parse the class to find the mappable fields
        this.members = new ObjectParser(this.object2Return.getPropertyClazz());

        // Error message if no mappable field has been found
        if (this.members.getFieldNumber() == 0)
            this.errorInfo = "no mappable field has been found";
    }

    /**
     * Select the route according to the given parameters
     */
    private void selectRoute() throws Exception {
        if (this.configName.isEmpty()) {
            if (this.urlWS == null || this.urlWS.toString().equals("")) {
                // Error
                this.errorInfo = "missing configuration name or url";
                return;
            } else {
                // Direct call - extServer is not used
                callWS(this.urlWS);
            }
        } else {
            // Call through extServer
            // If extServer send as error "configuration unknown", openServiceManager could be started
            // ToDo : call the Converter Web Service
            //callFedExtWS();
        }
    }

    /**
     * Call the web Service and return the adapted response
     *
     * @param url        Address of WS where the json must be read
     */
    private void callWS(URL url) throws Exception {
        String fullURL = url.toString();

        // Complete url with parameters
        for (Map.Entry<String, String> entry : params.entrySet()) {
            fullURL += "&" + entry.getKey() + "=" + entry.getValue();
        }

        this.urlWS = new URL(fullURL);

        // Get the JSON response
        getJSON();

        // Error detected ?
        if (!this.errorInfo.isEmpty())
            return;

        // The Object can now be filled
        fill();
    }

    /**
     * Call the Web Service and memorize the json response in a String
     */
    public void getJSON() throws Exception {
        this.jsonData = "";

        try {
            URLConnection fs = this.urlWS.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(fs.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                this.jsonData += inputLine;

            in.close();
        } catch (Exception e) {
            this.errorInfo = e.toString();
        }

        if (this.errorInfo.isEmpty() && this.jsonData.isEmpty())
            this.errorInfo = "JSON is empty";
    }

    /**
     * Parse the JSON and fill the Object to return
     */
    public void fill() throws Exception {
        // Open stream with JSON response
        JsonReader reader = openStream();

        // Parse the json file and fill the object
        parseJSON(reader, this.object2Return, this.object2Return.getPropertyClazz(), false, 0);

        // Close the stream
        reader.close();
    }

    /**
     * Edit the configuration on the External Wrapping Server
     * The configuration name and the Object members are sent for the mapping
     */
    public JsonReader openStream() throws Exception {
        // Read the json by stream
        InputStream stream = new ByteArrayInputStream(this.jsonData.getBytes(StandardCharsets.UTF_8));
        InputStreamReader streamReader = new InputStreamReader(stream);

        return new JsonReader(streamReader);
    }

    /**
     * Edit the configuration on the External Wrapping Server
     * The configuration name and the Object members are sent for the mapping
     */
    public void editConfiguration() throws Exception {
        String extUrl = extServer.getURL();

        if (Desktop.isDesktopSupported()) {
            // Windows & Mac
            Desktop.getDesktop().browse(new URI(extUrl));
        } else {
            // Ubuntu
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("/usr/bin/firefox -new-window " + extUrl);
        }
    }

    /**
     * Check for mapping : search if a field key exists in the given class
     *
     * @param clazz class of property
     * @param key field name to map
     * @return the mapped field or null if nothing has been found
     */
    private ObjectMapping mapping(Class clazz, String key) throws IllegalAccessException, InstantiationException {
        ObjectProperty property = members.map(clazz.getSimpleName(), key);
        ObjectMapping objectMapping = null;

        if (property != null)
            objectMapping = new ObjectMapping(property);

        return objectMapping;
    }

    /**
     * Create the initial Object
     *
     * @param subObject object to link with a parent
     * @param collectionType type of collection (Array, ArrayList or object if empty)
     */
    public ObjectMapping createInitiator(ObjectMapping subObject, String collectionType) {
        Class classParent = subObject.getParentClass();
        ObjectProperty classProperty = new ObjectProperty(classParent, collectionType);
        ObjectMapping initiatorObject = null;

        if (classProperty != null) {
            initiatorObject = new ObjectMapping(classProperty, true);
        }

        return initiatorObject;
    }

    /**
     * Parse the read JSON and fill the requested object
     *
     * @param reader stream of the json to parse
     */
    private void parseJSON(JsonReader reader, ObjectMapping mainObject, Class refClass, boolean isArray, int level) throws IOException, IllegalAccessException, InstantiationException {
        boolean stop = false;
        boolean ignore = false;
        boolean isObjectInArray = isArray;
        int iCnt = 0;
        String key;
        String value;
        ObjectMapping currentField = null;

        while (reader.hasNext() && !stop) {
            // Check if array : it can be an array only during the first iteration
            iCnt++;
            if (isObjectInArray)
                isObjectInArray = (iCnt == 1);

            JsonToken token = reader.peek();
            switch (token) {
                case BEGIN_OBJECT:
                    if (ignore)
                        reader.skipValue();
                    else {
                        reader.beginObject();
                        if (currentField == null) {
                            parseJSON(reader, mainObject, refClass, isObjectInArray, level + 1);
                        } else {
                            parseJSON(reader, currentField, currentField.getPropertyClazz(), isObjectInArray, level + 1);

                            // Assign Property Value to the Object
                            if (this.object2Return.isStarted() && currentField != null) {
                                mainObject.setObjectValue(currentField);

                                if (!(this.object2Return.isStarted() && mainObject.isInitialClass()))
                                    mainObject.transfer(mainObject, false);
                            }
                        }

                        reader.endObject();
                    }
                    break;

                case BEGIN_ARRAY:
                    if (ignore)
                        reader.skipValue();
                    else {
                        reader.beginArray();
                        if (currentField == null)
                            parseJSON(reader, mainObject, refClass, true, level + 1);
                        else {
                            parseJSON(reader, mainObject, currentField.getPropertyClazz(), true, level + 1);
                        }
                        reader.endArray();
                    }
                    break;

                case NAME:
                    key = reader.nextName();
                    currentField = mapping(refClass, key);
                    if (!this.object2Return.isStarted() && currentField != null) {
                        mainObject.start(level);
                    }
                    ignore = (mainObject.isStarted() && currentField == null);
                    break;

                case BOOLEAN:
                    if (ignore)
                        reader.skipValue();
                    else {
                        value = Boolean.toString(reader.nextBoolean());
                        if (this.object2Return.isStarted() && currentField != null)
                            mainObject.setValue(currentField.getProperty(), value);
                    }
                    break;

                case NUMBER:
                    if (ignore)
                        reader.skipValue();
                    else {
                        value = String.valueOf(reader.nextDouble());
                        if (this.object2Return.isStarted() && currentField != null)
                            mainObject.setValue(currentField.getProperty(), value);
                    }
                    break;

                case STRING:
                    if (ignore)
                        reader.skipValue();
                    else {
                        value = reader.nextString();
                        if (this.object2Return.isStarted() && currentField != null)
                            mainObject.setValue(currentField.getProperty(), value);
                    }
                    break;

                case NULL:
                    reader.skipValue();
                    break;

                case END_DOCUMENT:
                    reader.skipValue();
                    stop = true;
                    break;
            }
        }

        if (!isArray && level >= this.object2Return.getLevel() && mainObject.isInitialClass())
            this.object2Return.transfer(mainObject, true);
    }

}
