package cz.ami.connector.daktela.model;

import lombok.Data;

@Data
public class Role extends Item {

    private String shortcuts;
    private String options;
}
