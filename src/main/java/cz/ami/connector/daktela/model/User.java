package cz.ami.connector.daktela.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

//TODO The source often says nothing about if the field is a collection or a single item.
// As the source often has errors about using the "s" at the end of words, we definitely cannot rely on them.
//
@Data
public class User extends Item {

    private String alias;
    private List<Role> role;
    private List<Profile> profile;
    @SerializedName("nps_score")
    private Float npsScore;
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
    public User(String name, String title, String description, String alias, String password, String clid, String email) {
        this.setName(name);
        this.setTitle(title);
        this.setDescription(description);
        this.alias = alias;
        this.password = password;
        this.clid = clid;
        this.email = email;
    }
    public User(){}

}
