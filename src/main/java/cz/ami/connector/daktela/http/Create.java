package cz.ami.connector.daktela.http;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;

public interface Create {
    void createFarRecord(HttpClient client, Integer timeout) throws URISyntaxException, IOException, InterruptedException;
}
