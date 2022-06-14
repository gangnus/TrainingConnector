package cz.ami.connector.daktela;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import cz.ami.connector.daktela.model.Item;
import cz.ami.connector.daktela.model.User;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

import static java.time.temporal.ChronoUnit.SECONDS;


public class DaktelaConnection {
    private static final Trace LOG = TraceManager.getTrace(DaktelaConnection.class);
    static private final Gson gson = new Gson();


    static private DaktelaConnection INST;


    private DaktelaConfiguration configuration;

    static private final HttpClient client = HttpClient.newHttpClient();

    public static final String URI_AFTER_NAME = ".json";


    public static DaktelaConnection getINST() {
        return INST;
    }

    public static void setNewINST(DaktelaConfiguration config) {
        if (DaktelaConnection.INST == null) {
            INST = new DaktelaConnection(config);
        } else {
            INST.configuration = config;
        }
    }

    public static void setINST(DaktelaConnection connection) {
        INST = connection;
    }

    private DaktelaConnection(DaktelaConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getUriSource() {
        return configuration.getServiceAddress();
    }

    public Integer getTimeout() {
        return configuration.getTimeout();
    }

    /**
     * URI parts starting from the server address and ending before the name of the item
     * a URI part for a possible item class
     */
    static private final Map<Class<? extends Item>, String> uriForItem= new HashMap<>() {{
        put(User.class, "/api/v6/users");
    }};


    /**
     * Getting the whole URI for an item by name
     * @param itemClass class of objects to be operated with
     */
    public String uriLineForAnItem(String name, Class<? extends Item> itemClass){
        return startOfUriLineForItems(itemClass) + "/" + name + URI_AFTER_NAME;
    }
    /**
     * Getting the whole URI for all items
     * @param itemClass class of objects to be operated with
     */
    public String uriLineForAllItems(Class<? extends Item> itemClass){
        return startOfUriLineForItems(itemClass) + URI_AFTER_NAME;
    }
    /**
     * Getting of URI part starting from the server address and ending before the name of the item
     * @param itemClass class of objects to be operated with
     */
    public String startOfUriLineForItems(Class<? extends Item> itemClass){
        String secondPiece = uriForItem.get(itemClass);
        if(secondPiece == null){
            errorReaction("No URI exists for the class " + itemClass.getName());
        }
        return getUriSource() + secondPiece;
    }

    static private void errorReaction(String message){
        LOG.error(message);
        throw new ConnectorException(message);
    }

    /**
     * Return one instance of Items
     * URI = something as /api/v6/users/{NAME}.json
     * @param itemClass class of objects to be operated with
     * @param <I> class of objects to be operated with
     */
    public <I extends Item> I read(String name, Class<I> itemClass){
        LOG.debug("----------- before single user request -----------------");
        I item = null;
        String opMessage = "single "+ itemClass.getSimpleName()  + name + "reading";
        HttpRequest.Builder requestBuilder = preprepareRequest(uriLineForAnItem(name, itemClass),opMessage)
                .GET();

        LOG.debug("------------------- a request created, but not sent yet --------------------- ");
        HttpResponse<String> response = prepareResponse(requestBuilder, opMessage);
        String jsonString = response.body();
        LOG.debug("----------- ready jsonString -----------------");
        LOG.debug(jsonString);
        item = gson.fromJson(jsonString, itemClass);
        LOG.debug("----------- ready Item -----------------");
        return item;
    }

    /**
     * Return collection of Items models
     * URI = something as /api/v6/users.json
     * @param itemClass class of objects to be operated with
     * @param <I> class of objects to be operated with
     */
    public <I extends Item> List<I> readAll(Class<I> itemClass) {


        String opMessage = "all "+ itemClass.getSimpleName() + "s reading";
        LOG.debug("----------- before " + opMessage + " request -----------------");
        HttpRequest.Builder requestBuilder = preprepareRequest(uriLineForAllItems(itemClass), opMessage)
                .GET();
        LOG.debug("----------- after " + opMessage + " request preparation -----------------");
        HttpResponse<String> response = prepareResponse(requestBuilder, opMessage);
        LOG.debug("----------- after " + opMessage + " getting response -----------------");

        String jsonString = response.body();

        JsonParser jsonParser = new JsonParser();
        JsonArray jsonArray  = (JsonArray)jsonParser.parse(jsonString);

        List<I> items = new ArrayList<>();
        jsonArray.forEach(jsonElement -> {
            items.add(gson.fromJson(jsonElement, itemClass));
            LOG.debug("---------------name["+(items.size()-1) + "]=" +  items.get(items.size()-1).getName());
        });
        return items;
    }

    /**
     * Create one instance of Items
     * URI = something as /api/v6/users.json
     * @param item object to be operated with
     */
    public void createRecord( Item item) {
        String jsonString = gson.toJson(item);
        String opMessage = "single "+ item.getClass().getSimpleName() + " creation ";
        HttpRequest.Builder requestBuilder = preprepareRequest(uriLineForAnItem(item.getName(), item.getClass()), opMessage)
                .POST(HttpRequest.BodyPublishers.ofByteArray(jsonString.getBytes(StandardCharsets.UTF_8)));
        HttpResponse<String> response = prepareResponse(requestBuilder, opMessage);
        checkResponseStatus("Creation ", item.getClass(), response);
    }

    /**
     * Update fields in one instance of Items
     * URI = something as /api/v6/users/{NAME}.json
     * @param item object to be operated with
     */
    public void updateRecord(Item item) {
        String jsonString = gson.toJson(item);
        String opMessage = "single "+ item.getClass().getSimpleName() + " update ";
        HttpRequest.Builder requestBuilder = preprepareRequest(uriLineForAnItem(item.getName(), item.getClass()),opMessage)
                .PUT(HttpRequest.BodyPublishers.ofByteArray(jsonString.getBytes(StandardCharsets.UTF_8)));
        HttpResponse<String> response = prepareResponse(requestBuilder, opMessage);
        checkResponseStatus("Update ", item.getClass(), response);
    }

    private <I extends Item> void checkResponseStatus(String opName, Class<I> itemClass, HttpResponse<String> response) {
        if (response == null) {
            errorReaction(opName + " of " + itemClass.getName() + " failed with response = null");
        }
        Integer code = response.statusCode();
        if (code == null) {
            errorReaction(opName + " of " + itemClass.getName() + " failed with response.code = null");
        }
        if( ! Arrays.asList(200,201,202, 204).contains(code) ){
            errorReaction(opName + " of " + itemClass.getName() + " failed with response status code = " + code);
        }
    }


    private HttpRequest.Builder preprepareRequest(String uriLine, String opMessage){
        LOG.debug("uri ="+uriLine);
        LOG.debug("timeout ="+getTimeout());
        try {
            return HttpRequest.newBuilder()
                    .uri(new URI(uriLine))
                    .timeout(Duration.of(getTimeout(), SECONDS))
                    .setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        } catch (URISyntaxException e) {
            e.printStackTrace();
            errorReaction("Error in URI for " + opMessage + " " + uriLine + "\n" + e.getMessage());
        }
        // never really reached
        return null;
    }

    static HttpResponse<String> prepareResponse(HttpRequest.Builder requestBuilder, String opMessage){
        if (requestBuilder == null) {
            errorReaction(opMessage + " failed, the builder before build = null");
        }
        HttpRequest request = requestBuilder.build();
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
            errorReaction("IO error while " + opMessage + " " + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            errorReaction("interrupted while " + opMessage + " " + e.getMessage());
        }
        // never really reached
        return null;
    }
}
