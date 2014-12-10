package FedExt;

/**
 * Singleton containing the default values of the External Wrapping Server
 * Created by Laurent on 10.11.14.
 * Last modification, 19.11.2014
 *
 * @author Laurent
 * @version 1.0
 */
public class FedExtServer {
    private static String url;
    private static String userName;
    private static String password;
    private static FedExtServer instance = null;

    /**
     * Singleton Instanciator with internal initialisation
     */
    public static FedExtServer getInstance() {
        if (instance == null)
            instance = new FedExtServer();

        url = new String("http://localhost:8080/users/get");
        userName = new String("");
        password = new String("");

        return instance;
    }

    /**
     * Return the default URL of the External Wrapping Server
     *
     * @return the URL
     */
    public String getURL() {
        return url;
    }

    /**
     * Return the default URL of the External Wrapping Server
     *
     * @return the Login
     */
    public String getLogin() {
        return userName;
    }

    /**
     * Return the default password of the External Wrapping Server
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }
}
