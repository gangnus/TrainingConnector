package cz.ami.connector.daktela.model;

import lombok.Data;

@Data
public class RightsToCall {
    private String name;
    private String title;
    private String time;

    //TODO the source says that the type must be json, but immediately says it is some sort of regex-like code.
    private String rules;
}
