package cz.ami.connector.daktela.http;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import lombok.Setter;


import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.time.temporal.ChronoUnit.SECONDS;

public class DaktelaUser extends DaktelaItem implements Create, Update, Delete{

    private String name;
    private String title;
    private String alias;
    private DaktelaRole role;
    private DaktelaProfile profile;
    @SerializedName("nps_score")
    private Float npsScore;
    private String description;
    @SerializedName("call_steering_description")
    private String callSteeringDescription;
    private String password;
    private String extension;
    private DaktelaRightsToCall acl;
    @SerializedName("static")
    private Boolean ifStaticLogin;
    private Boolean allowRecordingInterruption;
    private String recordAtCallStart;
    private DaktelaIntegrationConfigs algo;
    private String email;
    private String emailAuth;
    private String icon;
    private String emoji;
    private DaktelaUserOptions options;
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
    static private Type userListType = new TypeToken<ArrayList<DaktelaUser>>(){}.getType();

    public static final String API_V_6_USERS = "/api/v6/users";
    public static final String DOT_JSON = ".json";

    private class DaktelaIntegrationConfigs {
        private String sign;
        private String target;
    }

    static public DaktelaUser readAndCreate(HttpClient client, Integer timeout, String uriSource, String name) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(uriSource+ API_V_6_USERS + "/" + name + DOT_JSON))
                .timeout(Duration.of(timeout, SECONDS))
                .headers("name", name)
                .GET()
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());
        String jsonString = response.body();
        DaktelaUser user = gson.fromJson(jsonString, DaktelaUser.class);
        user.uriSource = uriSource;
        return user;
    }

    static Collection<DaktelaUser> fetch(HttpClient client, Integer timeout, String uriSource) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(uriSource+ API_V_6_USERS + DOT_JSON))
                .timeout(Duration.of(timeout, SECONDS))
                .GET()
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());
        String jsonString = response.body();

        List<DaktelaUser> users = gson.fromJson(jsonString, userListType);
        DaktelaItem.setUriSources(uriSource, users);
        return users;
    }


    @Override
    public void createFarRecord() {

    }

    @Override
    public void deleteFarRecord() {

    }

    @Override
    public void updateFarRecord() {

    }
}
