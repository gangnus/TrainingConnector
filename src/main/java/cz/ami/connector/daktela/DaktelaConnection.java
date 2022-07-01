package cz.ami.connector.daktela;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import cz.ami.connector.daktela.model.Item;
import cz.ami.connector.daktela.model.User;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Flow;

import static java.time.temporal.ChronoUnit.SECONDS;


public class DaktelaConnection {

    public DaktelaConfiguration configuration;
    private GsonBuilder builder = new GsonBuilder();
    private Gson gson = builder.serializeNulls().setPrettyPrinting().create();
    private static final Trace LOG = TraceManager.getTrace(DaktelaConnection.class);
    private static final String URI_AFTER_NAME = ".json";
    private static final TrustManager[] trustAllCerts = new TrustManager[]{
            new javax.net.ssl.X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
    };

    DaktelaConnection(DaktelaConfiguration configuration) {
        this.configuration = configuration;
    }

    DaktelaConfiguration getConfiguration() {
        return configuration;
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
        if (secondPiece == null) {
            errorReaction("No URI exists for the class " + itemClass.getName());
        }
        return getConfiguration().getServiceAddress() + secondPiece;
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
        LOG.debug("Reading of item-" + itemClass.getSimpleName() + ", name="+ name);
        I item = null;
        String opMessage = "single "+ itemClass.getSimpleName()  + name + "reading";
        HttpRequest.Builder requestBuilder = preprepareRequest(uriLineForAnItem(name, itemClass),opMessage)
                .GET();

        LOG.debug("------------------- a request created, but not sent yet --------------------- ");
        HttpResponse<String> response = sendRequest(requestBuilder, opMessage);
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
        LOG.debug("Reading all item of class=" + itemClass.getSimpleName());

        String opMessage = "all "+ itemClass.getSimpleName() + "s reading";
        LOG.debug("----------- before " + opMessage + " request -----------------");
        HttpRequest.Builder requestBuilder = preprepareRequest(uriLineForAllItems(itemClass), opMessage)
                .GET();
        LOG.debug("----------- after " + opMessage + " request preparation -----------------");
        HttpResponse<String> response = sendRequest(requestBuilder, opMessage);
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
        LOG.debug("Creation of item-" + item.getClass().getSimpleName() + ", name="+ item.getName());
        String jsonString = gson.toJson(item);
        String opMessage = "single "+ item.getClass().getSimpleName() + " creation ";
        HttpRequest.Builder requestBuilder = preprepareRequest(uriLineForAllItems(item.getClass()), opMessage)
                .POST(HttpRequest.BodyPublishers.ofByteArray(jsonString.getBytes(StandardCharsets.UTF_8)));
        HttpResponse<String> response = sendRequest(requestBuilder, opMessage);
        checkResponseStatus("Creation ", item.getClass(), response);
    }

    /**
     * Update fields in one instance of Items
     * URI = something as /api/v6/users/{NAME}.json
     * @param item object to be operated with
     */
    public void updateRecord(Item item) {
        LOG.debug("Update of item-" + item.getClass().getSimpleName() + ", name="+ item.getName());
        String jsonString = gson.toJson(item);
        String opMessage = "single "+ item.getClass().getSimpleName() + " update ";
        HttpRequest.Builder requestBuilder = preprepareRequest(uriLineForAnItem(item.getName(), item.getClass()),opMessage)
                .PUT(HttpRequest.BodyPublishers.ofByteArray(jsonString.getBytes(StandardCharsets.UTF_8)));
        HttpResponse<String> response = sendRequest(requestBuilder, opMessage);
        LOG.debug("Response accepted");
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

    private HttpClient getHttpClient() {

        HttpClient.Builder clientBuilder = HttpClient.newBuilder();
        clientBuilder.connectTimeout(Duration.of(getConfiguration().getTimeout(), ChronoUnit.SECONDS));
        clientBuilder.followRedirects(HttpClient.Redirect.ALWAYS);
        //clientBuilder.version(HttpClient.Version.HTTP_2);

        // when service has not valid HTTPS certificate
        if (getConfiguration().getTrustAllCertificates()) {
            SSLContext sslContext = null;
            try {
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new SecureRandom());
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                e.printStackTrace();
            }
            clientBuilder.sslContext(sslContext); // HACK SSL ALLOW ALL CERTS
        }

        return clientBuilder.build();
    }

    private HttpRequest.Builder preprepareRequest(String uriLine, String opMessage){
        LOG.debug("uri =" + uriLine);
        LOG.debug("timeout =" + getConfiguration().getTimeout());
        try {
            return HttpRequest.newBuilder()
                    .uri(new URI(uriLine))
                    .timeout(Duration.of(getConfiguration().getTimeout(), SECONDS))
                    .setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        } catch (URISyntaxException e) {
            e.printStackTrace();
            errorReaction("Error in URI for " + opMessage + " " + uriLine + "\n" + e.getMessage());
        }
        // never really reached
        return null;
    }

    private HttpResponse<String> sendRequest(HttpRequest.Builder requestBuilder, String opMessage){
        if (requestBuilder == null) {
            errorReaction(opMessage + " failed, the builder before build = null");
        }
        HttpRequest request = requestBuilder.build();
        try {
            //TODO to trace, create setting log level
            if(LOG.isDebugEnabled()) {
                LOG.debug("request body= " + requestBodyToString(request));
            }
            return getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
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

    static final class StringSubscriber implements Flow.Subscriber<ByteBuffer> {
        final HttpResponse.BodySubscriber<String> wrapped;
        StringSubscriber(HttpResponse.BodySubscriber<String> wrapped) {
            this.wrapped = wrapped;
        }
        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            wrapped.onSubscribe(subscription);
        }
        @Override
        public void onNext(ByteBuffer item) { wrapped.onNext(List.of(item)); }
        @Override
        public void onError(Throwable throwable) { wrapped.onError(throwable); }
        @Override
        public void onComplete() { wrapped.onComplete(); }

    }

    static String requestBodyToString(HttpRequest request){
        if (request.bodyPublisher().isPresent()) {
            String requestBodyAsString = request.bodyPublisher().map(p -> {
                HttpResponse.BodySubscriber<String> bodySubscriber = HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
                StringSubscriber flowSubscriber = new StringSubscriber(bodySubscriber);
                p.subscribe(flowSubscriber);
                return bodySubscriber.getBody().toCompletableFuture().join();
            }).get();
            return requestBodyAsString;
        } else {
            return "empty request body";
        }

    }
}
