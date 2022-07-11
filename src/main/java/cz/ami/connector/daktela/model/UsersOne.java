package cz.ami.connector.daktela.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class UsersOne {

    private String[] error;
    private DaktelaUser result;
    @SerializedName("_time")
    private String time;

}
