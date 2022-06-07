package cz.ami.connector.daktela.http;

import java.net.http.HttpClient;

//TODO I don't know, where is the start point fron which this instance should be set.
public class Connection {
    public static Connection connection;
    private String uriSource;
    private Integer timeoutSecs;

    private HttpClient client = HttpClient.newHttpClient();

    public static final String API_V_6_USERS = "/api/v6/users";
    public static final String DOT_JSON = ".json";

    private Connection(String uriSource, Integer timeout){
        this.uriSource = uriSource;
        this.timeoutSecs = timeoutSecs;
    }

    public static void createInst(String uriSource, Integer timeout){
        connection = new Connection(uriSource, timeout);
    }

    public String uriLineForUser(String name){
        return uriSource+ API_V_6_USERS + "/" + name + DOT_JSON;
    }
    public String uriLineFofAllUsers(){
        return uriSource + API_V_6_USERS + DOT_JSON;
    }
    public HttpClient getClient() {
        return client;
    }

}
