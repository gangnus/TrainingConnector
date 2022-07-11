package cz.ami.connector.daktela.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Users {

    private String[] error;
    private UsersData result;
    @SerializedName("_time")
    private String time;

    public class UsersData {
        private DaktelaUser[] data;
        private int total;

        public DaktelaUser[] getData() { return data; }
    }

}
