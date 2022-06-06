package cz.ami.connector.daktela.http;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;

public interface Delete {
    void deleteRecord(HttpClient client, Integer timeout) throws IOException, InterruptedException, URISyntaxException;
}
