package cz.ami.connector.daktela.http;

import lombok.Data;

@Data
public class Role {
    private String name;
    private String title;
    private String description;
    private String shortcuts;
    private String options;
}
