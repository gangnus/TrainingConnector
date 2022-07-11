package cz.ami.connector.daktela.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class DaktelaUserListResponse {

    private String[] error;
    private UserList result;
    @SerializedName("_time")
    private String time;

    public class UserList {
        private DaktelaUser[] data;
        private int total;

        public DaktelaUser[] getData() { return data; }
    }

}
