package cz.ami.connector.daktela.http;

import lombok.Data;

@Data
public class IntegrationConfigs {
    private String name;
    private String title;

    private Integrations integration;

    private Boolean active;
    //TODO if it is a usual branch of json tree, we should know the inner structure.
    private String auth;
    private String config;
    private String error;

}
