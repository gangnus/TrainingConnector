package cz.ami.connector.daktela;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;

public class ServerForTesting {
    private static final Trace LOG = TraceManager.getTrace(ServerForTesting.class);
    static public final int TEST_PORT = 8001;
    static public final String TEST_SERVER_URI = "http://localhost:" + TEST_PORT;
    private HttpServer server;
    private static ServerForTesting instance = null;



    private String requestBody;

    private ServerForTesting() throws IOException {
    }

    public void launch() throws Exception {
        LOG.debug("launching the server...");
        LOG.debug("setting contexts...");
        server.createContext("/api/v6/users/Novak.json", new ReadNovakHandler());
        server.createContext("/api/v6/users/Vlcek.json", new ReadVlcekHandler());
        server.createContext("/api/v6/users.json", new ReadNovakVlcekHandler());
        server.createContext("/api/v6/users/user1.json", new CorrectUpdateHandler());
        server.createContext("/api/v6/users/user2.json", new FailedUpdateHandler());
        LOG.debug("setting executors...");
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        server.setExecutor(threadPoolExecutor);
        //server.setExecutor(null); // creates a default executor
        LOG.debug("server starting...");
        server.start();
        LOG.debug("server started.");
    }
    public void stop(){
        server.stop(1);
    }


    class CorrectUpdateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestMethod = exchange.getRequestMethod();
            if (requestMethod.equalsIgnoreCase("PUT")) {
                setRequestBody(exchange);
                setResponse(exchange, 200, "OK");
            }
        }
    }
    class FailedUpdateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            LOG.debug("starting the handle");
            String requestMethod = exchange.getRequestMethod();
            if (requestMethod.equalsIgnoreCase("PUT")) {
                setResponse(exchange, 300, "failed");
            }
        }
    }
    class ReadNovakHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestMethod = exchange.getRequestMethod();
            if (requestMethod.equalsIgnoreCase("GET")) {
                setResponse(exchange, 200, new String(getClass().getClassLoader().getResourceAsStream("novak.json").readAllBytes()));
            }
        }
    }
    class ReadVlcekHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestMethod = exchange.getRequestMethod();
            if (requestMethod.equalsIgnoreCase("GET")) {
                setResponse(exchange, 200, new String(getClass().getClassLoader().getResourceAsStream("vlcek.json").readAllBytes()));
            }
        }
    }
    class ReadNovakVlcekHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestMethod = exchange.getRequestMethod();
            if (requestMethod.equalsIgnoreCase("GET")) {
                String jsonString =
                        "[" +
                                new String(getClass().getClassLoader().getResourceAsStream("novak.json").readAllBytes()) +
                                ", " +
                                new String(getClass().getClassLoader().getResourceAsStream("vlcek.json").readAllBytes()) +
                                "]";
                setResponse(exchange, 200, jsonString);
            }
        }
    }
    private void setResponse(HttpExchange exchange, int stateCode, String responseBody) {
        LOG.debug("crerating the response");
        byte[] bodyBytes = null;
        try {
            bodyBytes = responseBody.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            LOG.error("failed transferring string to bytes. String = " + responseBody);
        }
        try {
            exchange.sendResponseHeaders(stateCode, bodyBytes.length);
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("failed setting code = " + stateCode + " and responseBody Length = " + bodyBytes.length);
        }
        LOG.debug("try to set response body = " + responseBody);
        OutputStream os = exchange.getResponseBody();
        try {
            os.write(bodyBytes);
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("failed writing responseBody to stream");
        }
        try {
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("failed flushing responseBody to stream");
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("failed sending back the response");
        }

    }

    public String getRequestBody() {
        return requestBody;
    }

    private void setRequestBody(HttpExchange exchange) throws IOException {
        this.requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);;
    }

    static public ServerForTesting createServerForTesting() throws Exception {
        if (instance == null) {
            instance = new ServerForTesting();
            try {
                instance.server = HttpServer.create(new InetSocketAddress(8001), 0);
                instance.launch();

            } catch (IOException e) {
                e.printStackTrace();
                LOG.error("----------------- result = FAILED TEST SERVER CREATION");
                return null;
            }
        }

        return instance;
    }
}
