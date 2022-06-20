package cz.ami.connector.daktela.testserver;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import cz.ami.connector.daktela.model.Item;
import cz.ami.connector.daktela.model.User;
import cz.ami.connector.daktela.tools.TestFilesInRoot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class TSWithMemory {
    private static final Trace LOG = TraceManager.getTrace(TSWithMemory.class);
    static public final int TEST_PORT_WITH_MEMORY = 8002;
    static public final String TEST_SERVER_URI = "http://localhost:" + TEST_PORT_WITH_MEMORY;
    private HttpServer server;
    private static TSWithMemory instance = null;

    static private final String usersMemoryFileName = "UsersMemory.json";
    private Map<String, User> users;
    static private final String userUniversalUri = "/api/v6/users/";
    static private final String userReadAllOrCreateUri = "/api/v6/users.json";

    static GsonBuilder builder = new GsonBuilder();
    static Gson gson = builder.serializeNulls().setPrettyPrinting().create();

    private String requestBody;

    private TSWithMemory() throws IOException {
        users = loadUserMemory();
    }

    /** load users from the user memory file in test resources
     *
     */
    public static Map<String,User> loadUserMemory() {
        Map<String,User> users = null;
        try {
            String jsonString = TestFilesInRoot.readMemory(usersMemoryFileName);
            Type userMapType = new TypeToken<Map<String, User>>() {}.getType();
            users = gson.fromJson(jsonString, userMapType);
        } catch (IOException e) {
            users = new HashMap<>();
        }
        return users;
    }

    public void launch() throws Exception {
        LOG.debug("launching the server...");
        LOG.debug("setting contexts...");
        server.createContext(userUniversalUri, new UniversalUserHandler());
        server.createContext(userReadAllOrCreateUri, new ReadAllOrCreateHandler());
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
    class UniversalUserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            String userName = getUserName(exchange);
            String requestMethod = exchange.getRequestMethod();
            String requestBodyString = getRequestBodyString(exchange);
            setRequestBody(requestBodyString);
            User user = users.get(userName);
            if (!users.containsKey(user)){
                setResponse(exchange, 404, "unknown user = '" + userName + "'");
            } else

            if (requestMethod.equalsIgnoreCase("GET")) {
                // read one user by name
                if (users.get(userName) == null){
                    setResponse(exchange, 404, "user '" + userName + "' has no info to be read");
                } else {
                    setResponse(exchange, 200, gson.toJson(user));
                }
            } else
            if (requestMethod.equalsIgnoreCase("PUT")) {
                // Update one user by name
                if (users.get(userName) == null){
                    setResponse(exchange, 404, "user '" + userName + "' has no info to be read");
                } else {
                    user = changeItemByJsonString(user, requestBodyString);
                    users.put(userName, user);
                    saveUsers(exchange);
                    setResponse(exchange, 200, "OK");
                }
            } else {
                setResponse(exchange, 404, "unknown request method = '" + requestMethod + "'");
            }
        }
    }

    @NotNull
    static private String getUserName(HttpExchange exchange) {
        String uriEnding = exchange.getRequestURI().toString().substring(userUniversalUri.length());
        String userName = uriEnding.substring(0, uriEnding.length()-5);
        return userName;
    }

    /**
     * save current users into memory file
     */
    private void saveUsers(HttpExchange exchange) {
        String jsonString = gson.toJson(users);
        try {
            TestFilesInRoot.writeMemory(usersMemoryFileName, jsonString);
        } catch (IOException e){
            e.printStackTrace();
            setResponse(exchange, 500, "Fail on writing." + e.getMessage());
        }

    }

    /**
     * the item will be changed and returned
     *
     * @param item the object to be changed
     * @param changeJsonString the json string containing changes
     * @param <T>
     * @return the changed item
     */
    public static <T extends Item> T changeItemByJsonString(T item, String changeJsonString){
        String sourceJsonString = gson.toJson(item);
        Type stringMapType = new TypeToken<Map<String, String>>() {}.getType();
        Map<String,String> sourceMap = gson.fromJson(sourceJsonString, stringMapType);
        Map<String,String> changeMap = gson.fromJson(changeJsonString, stringMapType);
        sourceMap.forEach((name,value) -> {
           if(changeMap.containsKey(name)){
               sourceMap.put(name, changeMap.get(name));
           }
        });
        String resultJsonString = gson.toJson(sourceMap);
        T result = (T) gson.fromJson(resultJsonString, item.getClass());
        return result;
    }

    class ReadAllOrCreateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange)  {
            String requestMethod = exchange.getRequestMethod();
            String requestBodyString = getRequestBodyString(exchange);
            setRequestBody(requestBodyString);
            if (requestMethod.equalsIgnoreCase("GET")) {
                // Read All Users
                List<User> userList = new ArrayList<>();
                users.forEach((key, value) -> {
                    userList.add(value);
                });
                String jsonString = gson.toJson(userList);
                setResponse(exchange, 200, jsonString);
            } else if (requestMethod.equalsIgnoreCase("POST")) {
                // Create a user

                User user = gson.fromJson(requestBodyString, User.class);
                users.put(user.getName(), user);
                saveUsers(exchange);

                setResponse(exchange, 200, "OK");
            } else {
                setResponse(exchange, 404, "unknown request method = '" + requestMethod + "'");
            }

        }
    }

    private static void setResponse(HttpExchange exchange, int stateCode, String responseBody) {
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

    private void setRequestBody(String bodyContent) {
        this.requestBody = bodyContent;
    }

    @Nullable
    static private String getRequestBodyString(HttpExchange exchange) {
        String requestBodyString = null;
        try {
            requestBodyString = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            setResponse(exchange, 400, "Request body badly encoded." + e.getMessage() + "\n" + exchange.getRequestBody().toString());
        }
        return requestBodyString;
    }

    static public TSWithMemory createServerForTesting() throws Exception {
        if (instance == null) {
            instance = new TSWithMemory();
            try {
                instance.server = HttpServer.create(new InetSocketAddress(TEST_PORT_WITH_MEMORY), 0);
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
