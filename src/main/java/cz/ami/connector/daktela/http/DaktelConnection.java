package cz.ami.connector.daktela.http;

import java.net.http.HttpClient;

public class DaktelConnection {
    private String uri;
    private Integer timeoutSecs;
    HttpClient client = HttpClient.newHttpClient();
    DaktelConnection(String uri, Integer timeoutSecs){

    }
}
