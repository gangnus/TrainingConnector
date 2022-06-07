package cz.ami.connector.daktela.http;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import cz.ami.connector.daktela.DaktelaConnector;
import lombok.Data;
import lombok.Setter;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.time.temporal.ChronoUnit.SECONDS;

//TODO The source often says nothing about if the field is a collection or a single item.
// As the source often has errors about using the "s" at the end of words, we definitely cannot rely on them.
//
@Data
public class User extends Item implements Create, Update, Delete{

    private String name;
    private String title;
    private String alias;
    private List<Role> role;
    private List<Profile> profile;
    @SerializedName("nps_score")
    private Float npsScore;
    private String description;
    @SerializedName("call_steering_description")
    private String callSteeringDescription;
    private String password;
    private String extension;
    private List<RightsToCall> acl;
    //TODO If we have to use the enum?
    @SerializedName("extension_state")
    private String extState;
    private String clid;
    @SerializedName("static")
    private Boolean ifStaticLogin;
    private Boolean allowRecordingInterruption;
    //TODO for enum - what will be used in the input json string - names or values?
    private String recordAtCallStart;
    private IntegrationConfigs algo;
    private String email;
    private String emailAuth;
    private String icon;
    private String emoji;
    private Options options;
    @SerializedName("backoffice_user")
    private Boolean backofficeUser;
    @SerializedName("forwarding_number")
    private String forwardingNumber;
    private Boolean deactivated;
    private Boolean deleted;

    static private Gson gson = new Gson();
    static private Type userListType = new TypeToken<ArrayList<User>>(){}.getType();

    public static final String API_V_6_USERS = "/api/v6/users";
    public static final String DOT_JSON = ".json";
    private static final Trace LOG = TraceManager.getTrace(User.class);

    static public User read(HttpClient client, Integer timeout, String uriSource, String name){
        LOG.debug("----------- before single user request -----------------");
        User user = null;
        String opMessage = "single user" + name + "reading";
        HttpRequest request = preprepareRequest(uriLineWithName(uriSource, name), timeout, opMessage)
                .GET()
                .build();

        LOG.debug("------------------- a request created, but not sent yet --------------------- ");
        HttpResponse<String> response = prepareResponse(client, request, opMessage);
        String jsonString = response.body();
        LOG.debug("----------- ready jsonString -----------------");
        LOG.debug(jsonString);
        user = gson.fromJson(jsonString, User.class);
        LOG.debug("----------- ready user -----------------");

        LOG.debug("name="+user.getName());
        LOG.debug("title="+user.getTitle());
        LOG.debug("alias="+user.getAlias());
        LOG.debug("e-mail="+user.getEmail());

        return user;
    }
    static private String uriLineWithName(String uriSource, String name){
        return uriSource+ API_V_6_USERS + "/" + name + DOT_JSON;
    }
    static private String uriLineWithoutName(String uriSource){
        return uriSource + API_V_6_USERS + DOT_JSON;
    }

    static private HttpRequest.Builder preprepareRequest(String uriLine, Integer timeout, String opMessage){
        LOG.debug("uri ="+uriLine);
        LOG.debug("timeout ="+timeout);

        try {
            return HttpRequest.newBuilder()
                    .uri(new URI(uriLine))
                    .timeout(Duration.of(timeout, SECONDS));

        } catch (URISyntaxException e) {
            e.printStackTrace();
            LOG.error("--------------------- Error in URI for all users reading" + uriLine + "\n" + e.getMessage());
            throw new ConnectorException("--------------------- Error in URI for " + opMessage + " " + uriLine + "\n" + e.getMessage());
        }
    }

    static HttpResponse<String> prepareResponse(HttpClient client, HttpRequest request, String opMessage){
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("--------------------- IO error while " + opMessage + " " + e.getMessage());
            throw new ConnectorException("--------------------- IO error while " + opMessage + " " + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            LOG.error("--------------------- interrupted while " + opMessage + " " + e.getMessage());
            throw new ConnectorException("--------------------- interrupted while " + opMessage + " " + e.getMessage());
        }
    }

    public static Collection<User> readAll(HttpClient client, Integer timeout, String uriSource) {
        LOG.debug("----------- before all users request -----------------");
        List<User> users = null;
        String opMessage = "all users reading";
        HttpRequest request = preprepareRequest(uriLineWithoutName(uriSource), timeout, opMessage)
                .GET()
                .build();

        HttpResponse<String> response = prepareResponse(client, request, opMessage);

        String jsonString = response.body();

        users = gson.fromJson(jsonString, userListType);
        Item.setUriSources(uriSource, users);
        return users;
    }


    @Override
    public void createRecord(HttpClient client, Integer timeout) throws URISyntaxException, IOException, InterruptedException {
        /*String jsonString = gson.toJson(this);
        HttpRequest request = preprepareRequest(uriLineWithoutName(uriSource), timeout, opMessage)
                .POST(HttpRequest.BodyPublishers.ofByteArray(jsonString.getBytes(StandardCharsets.UTF_8)))
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());*/
    }

    @Override
    public void deleteRecord(HttpClient client, Integer timeout) throws URISyntaxException, IOException, InterruptedException {
       /* HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(uriSource+ API_V_6_USERS + "/" + name + DOT_JSON))
                .timeout(Duration.of(timeout, SECONDS))
                .DELETE()
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());*/

    }

    @Override
    public void updateRecord(HttpClient client, Integer timeout) throws IOException, InterruptedException, URISyntaxException {
        /*String jsonString = gson.toJson(this);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(uriSource+ API_V_6_USERS + "/" + name + DOT_JSON))
                .timeout(Duration.of(timeout, SECONDS))
                .PUT(HttpRequest.BodyPublishers.ofByteArray(jsonString.getBytes(StandardCharsets.UTF_8)))
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());*/
    }
}
