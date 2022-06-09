package cz.ami.connector.daktela.http;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import cz.ami.connector.daktela.DaktelaConfiguration;
import cz.ami.connector.daktela.model.Item;
import cz.ami.connector.daktela.model.User;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

import java.io.IOException;
import java.lang.reflect.Type;
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
    static private Gson gson = new Gson();


    static private DaktelaConnection INST;


    private DaktelaConfiguration configuration;

    static private HttpClient client = HttpClient.newHttpClient();

    public static final String URI_AFTER_NAME = ".json";


    public static DaktelaConnection getINST() {
        return INST;
    }

    public static void setINST(DaktelaConfiguration config) {
        if (DaktelaConnection.INST == null) {
            INST = new DaktelaConnection(config);
        } else {
            INST.configuration = config;
        }
    }

    private DaktelaConnection(DaktelaConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getUriSource() {
        return configuration.getServiceAddress();
    }

    public Integer getTimeout() {
        return configuration.getTimeoutSeconds();
    }

    /**
     * URI parts starting from the server address and ending before the name of the item
     * a URI part for a possible item class
     */
    static private Map<Class, String> uriForItem= new HashMap<>() {{
        put(User.class, "/api/v6/users");
    }};


    /**
     * Getting the whole URI for an item by name
     * @param itemClass
     * @return
     */
    public String uriLineForAnItem(String name, Class itemClass){
        return startOfUriLineForItems(itemClass) + "/" + name + URI_AFTER_NAME;
    }
    /**
     * Getting the whole URI for all items
     * @param itemClass
     * @return
     */
    public String uriLineForAllItems(Class itemClass){
        return startOfUriLineForItems(itemClass) + URI_AFTER_NAME;
    }
    /**
     * Getting of URI part starting from the server address and ending before the name of the item
     * @param itemClass
     * @return
     */
    public String startOfUriLineForItems(Class itemClass){
        String secondPiece = uriForItem.get(itemClass);
        if(secondPiece == null){
            errorReaction("No URI exists for the class " + itemClass.getName());
        }
        return getUriSource() + secondPiece;
    }
    public HttpClient getClient() {
        return client;
    }

    static private void errorReaction(String message){
        LOG.error(message);
        throw new ConnectorException(message);
    }

    public <I extends Item> I read(String name, Class<I> itemClass){
        LOG.debug("----------- before single user request -----------------");
        I item = null;
        String opMessage = "single "+ itemClass.getSimpleName()  + name + "reading";
        HttpRequest request = preprepareRequest(uriLineForAnItem(name, itemClass),opMessage)
                .GET()
                .build();

        LOG.debug("------------------- a request created, but not sent yet --------------------- ");
        HttpResponse<String> response = prepareResponse(request, opMessage);
        String jsonString = response.body();
        LOG.debug("----------- ready jsonString -----------------");
        LOG.debug(jsonString);
        item = gson.fromJson(jsonString, itemClass);
        LOG.debug("----------- ready Item -----------------");
        return item;
    }


    public <I extends Item> List<I> readAll(Class<I> itemClass) {


        String opMessage = "all "+ itemClass.getSimpleName() + "s reading";
        LOG.debug("----------- before " + opMessage + " request -----------------");
        HttpRequest request = preprepareRequest(uriLineForAllItems(itemClass), opMessage)
                .GET()
                .build();
        LOG.debug("----------- after " + opMessage + " request preparation -----------------");
        HttpResponse<String> response = prepareResponse(request, opMessage);
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



    public <I extends Item> void createRecord(Class<I> itemClass, Item item) {
        String jsonString = gson.toJson(item);
        String opMessage = "single "+ itemClass.getSimpleName() + "creation ";
        HttpRequest request = preprepareRequest(uriLineForAnItem(item.getName(), itemClass), opMessage)
                .POST(HttpRequest.BodyPublishers.ofByteArray(jsonString.getBytes(StandardCharsets.UTF_8)))
                .build();
        HttpResponse<String> response = prepareResponse(request, opMessage);
        int code = response.statusCode();
        if( ! Arrays.asList(200,201,202, 204).contains(code) ){
            errorReaction("Creation of " + itemClass.getName() + " failed with response satus code = " + code);
        }
    }


    public void deleteRecord() throws URISyntaxException, IOException, InterruptedException {
       /* HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(uriSource+ API_V_6_USERS + "/" + name + DOT_JSON))
                .timeout(Duration.of(timeout, SECONDS))
                .DELETE()
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());*/

    }


    public void updateRecord() throws IOException, InterruptedException, URISyntaxException {
        /*String jsonString = gson.toJson(this);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(uriSource+ API_V_6_USERS + "/" + name + DOT_JSON))
                .timeout(Duration.of(timeout, SECONDS))
                .PUT(HttpRequest.BodyPublishers.ofByteArray(jsonString.getBytes(StandardCharsets.UTF_8)))
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());*/
    }

    private HttpRequest.Builder preprepareRequest(String uriLine, String opMessage){
        LOG.debug("uri ="+uriLine);
        LOG.debug("timeout ="+getTimeout());
        try {
            return HttpRequest.newBuilder()
                    .uri(new URI(uriLine))
                    .timeout(Duration.of(getTimeout(), SECONDS));

        } catch (URISyntaxException e) {
            e.printStackTrace();
            errorReaction("Error in URI for " + opMessage + " " + uriLine + "\n" + e.getMessage());
        }
        // never really reached
        return null;
    }

    static HttpResponse<String> prepareResponse(HttpRequest request, String opMessage){
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
