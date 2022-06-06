package cz.ami.connector.daktela.http;

import java.net.http.HttpClient;

//TODO this class will contain the common client-connection info if all data classes will work through the same connection
public class Connection {
    private String uri;
    private Integer timeoutSecs;
    HttpClient client = HttpClient.newHttpClient();
    Connection(String uri, Integer timeoutSecs){

    }
}
