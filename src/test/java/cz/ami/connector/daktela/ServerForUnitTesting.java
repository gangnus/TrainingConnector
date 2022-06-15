package cz.ami.connector.daktela;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
public class ServerForUnitTesting {

    HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);

    public ServerForUnitTesting() throws IOException {
    }

    public void launch() throws Exception {

        server.createContext("/api/v6/users/user1.json", new CorrectUpdateHandler());
        server.createContext("/api/v6/users/user2.json", new FailedUpdateHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }
    public void stop(){
        server.stop(1);
    }


    class CorrectUpdateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "OK";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            stop();
        }
    }
    class FailedUpdateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "failed";
            exchange.sendResponseHeaders(300, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
            stop();
        }
    }
}
