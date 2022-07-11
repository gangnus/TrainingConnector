package cz.ami.connector.daktela;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cz.ami.connector.daktela.model.*;
import org.identityconnectors.common.security.SecurityUtil;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class DaktelaConnection {

    public DaktelaConfiguration configuration;
    private GsonBuilder builder = new GsonBuilder();
    private Gson gson = builder.serializeNulls().setPrettyPrinting().create();

    //TODO nepoužívá se zatím
    private static final Map<Class<? extends Item>, String> uriForClass = ImmutableMap.of(
            DaktelaUser.class,"users",
            Role.class,"roles");

    private static final Trace LOG = TraceManager.getTrace(DaktelaConnection.class);
    private static final String URI_JSON_END = ".json";
    // pro případ nevalidních HTTPS certifikátů
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

    static private void errorReaction(String message){
        LOG.error(message);
        throw new ConnectorException(message);
    }

    private String makeRequest(String uid) {
        LOG.debug("Reading item uid={}", uid);
        HttpRequest request = preprepareRequest(null, uid).GET().build();
        LOG.debug("Request uri: {}", request.uri().toString());
        LOG.debug("Request info: {}", request.headers());

        HttpResponse<String> response = sendRequest(request);
        String jsonString = response.body();
        LOG.debug("response body: {}", jsonString);
        return jsonString;
    }

    /**
     * Return one instance of Items
     * URI = something as /api/v6/users/{NAME}.json
     * @param itemClass class of objects to be operated with
     */
    public DaktelaUser read(String uid, Class itemClass){
        String jsonString = makeRequest(uid);
        DaktelaUserResponse a = gson.fromJson(jsonString, DaktelaUserResponse.class);
        DaktelaUser b = a.getResult();
        return b;
    }

    /**
     * Return collection of Items models
     * URI = something as /api/v6/users.json
     * @param itemClass class of objects to be operated with
     */
    public List<DaktelaUser> readAll(Class itemClass) {
        String jsonString = makeRequest(null);
        DaktelaUserListResponse a = new Gson().fromJson(jsonString, DaktelaUserListResponse.class);
        DaktelaUserListResponse.UserList b = a.getResult();
        DaktelaUser[] c = b.getData();
        return List.of(c);
    }

    /**
     * Create one instance of Items
     * URI = something as /api/v6/users.json
     * @param item object to be operated with
     */
    public void createRecord( Item item) {
        LOG.debug("Creation of item-" + item.getClass().getSimpleName() + ", name="+ item.getName());
        String jsonString = gson.toJson(item);
        HttpRequest request = preprepareRequest(DaktelaUser.class, null)
                .POST(HttpRequest.BodyPublishers.ofByteArray(jsonString.getBytes(StandardCharsets.UTF_8))).build();
        HttpResponse<String> response = sendRequest(request);
        LOG.debug("Response accepted");
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
        HttpRequest request = preprepareRequest(DaktelaUser.class, null)
                .PUT(HttpRequest.BodyPublishers.ofByteArray(jsonString.getBytes(StandardCharsets.UTF_8))).build();
        HttpResponse<String> response = sendRequest(request);
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
        //clientBuilder.followRedirects(HttpClient.Redirect.ALWAYS);
        clientBuilder.version(HttpClient.Version.HTTP_1_1);

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

    private HttpRequest.Builder preprepareRequest(Class<? extends Item> itemClass, String uid){
        String url = getConfiguration().getServiceAddress();
        url += "/users";
        if (uid != null) {
            url += "/" + uid;
        } else {
            url += URI_JSON_END;
        }
        url += "?accessToken=" + SecurityUtil.decrypt(getConfiguration().getAccessToken());
        LOG.debug("uri: " + url);
        LOG.debug("timeout: " + getConfiguration().getTimeout());
        try {
            return HttpRequest.newBuilder().uri(new URI(url));
//                    .setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        } catch (URISyntaxException e) {
            e.printStackTrace();
            errorReaction("Error in URL: " + url + "\n" + e.getMessage());
        }
        // never really reached
        return null;
    }

    private HttpResponse<String> sendRequest(HttpRequest request){
        HttpResponse response = null;
        try {
            //TODO to trace, create setting log level
//            if(LOG.isDebugEnabled()) {
//                LOG.debug("request body= " + requestBodyToString(request));
//            }
            response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        LOG.debug("Response status code {}", response.statusCode());
        return response;
    }
/*
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
*/
}
