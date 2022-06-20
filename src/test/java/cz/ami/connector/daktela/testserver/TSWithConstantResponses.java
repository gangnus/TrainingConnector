package cz.ami.connector.daktela.testserver;

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
import cz.ami.connector.daktela.tools.TestResourceFiles;

public class TSWithConstantResponses {
    private static final Trace LOG = TraceManager.getTrace(TSWithConstantResponses.class);
    static public final int TEST_PORT = 8001;
    static public final String TEST_SERVER_URI = "http://localhost:" + TEST_PORT;
    private HttpServer server;
    private static TSWithConstantResponses instance = null;



    private String requestBody;

    private TSWithConstantResponses() throws IOException {
    }

    public void launch() throws Exception {
        LOG.debug("launching the server...");
        LOG.debug("setting contexts...");
        server.createContext("/api/v6/users/Novak.json", new ReadNovakHandler());
        server.createContext("/api/v6/users/Vlcek.json", new ReadVlcekHandler());
        server.createContext("/api/v6/users.json", new ReadAllOrCreateHandler());
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
            } else {
                setResponse(exchange, 404, "unknown URI");
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
            } else {
                setResponse(exchange, 404, "unknown URI");
            }
        }
    }
    class ReadNovakHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestMethod = exchange.getRequestMethod();
            if (requestMethod.equalsIgnoreCase("GET")) {
                setResponse(exchange, 200, TestResourceFiles.readStringContentFromFile("novak.json"));
            } else {
                setResponse(exchange, 404, "unknown URI");
            }
        }
    }
    class ReadVlcekHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestMethod = exchange.getRequestMethod();
            if (requestMethod.equalsIgnoreCase("GET")) {
                setResponse(exchange, 200, TestResourceFiles.readStringContentFromFile("vlcek.json"));
            } else {
                setResponse(exchange, 404, "unknown URI");
            }
        }
    }
    class ReadAllOrCreateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestMethod = exchange.getRequestMethod();
            if (requestMethod.equalsIgnoreCase("GET")) {
                // Read All Users
                String jsonString =
                        "[" +
                                TestResourceFiles.readStringContentFromFile("novak.json") +
                                ", " +
                                TestResourceFiles.readStringContentFromFile("vlcek.json") +
                                "]";
                setResponse(exchange, 200, jsonString);
            } else if (requestMethod.equalsIgnoreCase("POST")) {
                // Create a user
                setRequestBody(exchange);
                setResponse(exchange, 200, "OK");
            } else {
                setResponse(exchange, 404, "unknown URI");
            }

        }
    }
    private void setResponse(HttpExchange exchange, int stateCode, String responseBody) {
        LOG.debug("creating the response");
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

    static public TSWithConstantResponses createServerForTesting() throws Exception {
        if (instance == null) {
            instance = new TSWithConstantResponses();
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
