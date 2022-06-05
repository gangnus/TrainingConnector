package cz.ami.connector.daktela.http;

import java.net.http.HttpClient;

public class Connection {
    private String uri;
    private Integer timeoutSecs;
    HttpClient client = HttpClient.newHttpClient();
    Connection(String uri, Integer timeoutSecs){

    }
}
