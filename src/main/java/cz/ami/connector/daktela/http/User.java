package cz.ami.connector.daktela.http;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.Setter;


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
    private RightsToCall acl;
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

    @Expose(serialize = false, deserialize = false)
    @Setter
    private String uriSource;

    static private Gson gson = new Gson();
    static private Type userListType = new TypeToken<ArrayList<User>>(){}.getType();

    public static final String API_V_6_USERS = "/api/v6/users";
    public static final String DOT_JSON = ".json";

    static public User readAndCreate(HttpClient client, Integer timeout, String uriSource, String name) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(uriSource+ API_V_6_USERS + "/" + name + DOT_JSON))
                .timeout(Duration.of(timeout, SECONDS))
                .headers("name", name)
                .GET()
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());
        String jsonString = response.body();
        User user = gson.fromJson(jsonString, User.class);
        user.uriSource = uriSource;
        return user;
    }

    static Collection<User> fetch(HttpClient client, Integer timeout, String uriSource) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(uriSource+ API_V_6_USERS + DOT_JSON))
                .timeout(Duration.of(timeout, SECONDS))
                .GET()
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());
        String jsonString = response.body();

        List<User> users = gson.fromJson(jsonString, userListType);
        Item.setUriSources(uriSource, users);
        return users;
    }


    @Override
    public void createFarRecord(HttpClient client, Integer timeout) throws URISyntaxException, IOException, InterruptedException {
        String jsonString = gson.toJson(this);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(uriSource+ API_V_6_USERS + DOT_JSON))
                .timeout(Duration.of(timeout, SECONDS))
                .POST(HttpRequest.BodyPublishers.ofByteArray(jsonString.getBytes(StandardCharsets.UTF_8)))
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Override
    public void deleteFarRecord(HttpClient client, Integer timeout) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(uriSource+ API_V_6_USERS + "/" + name + DOT_JSON))
                .timeout(Duration.of(timeout, SECONDS))
                .DELETE()
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

    }

    @Override
    public void updateFarRecord(HttpClient client, Integer timeout) throws IOException, InterruptedException, URISyntaxException {
        String jsonString = gson.toJson(this);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(uriSource+ API_V_6_USERS + "/" + name + DOT_JSON))
                .timeout(Duration.of(timeout, SECONDS))
                .headers("name", name)
                .PUT(HttpRequest.BodyPublishers.ofByteArray(jsonString.getBytes(StandardCharsets.UTF_8)))
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
