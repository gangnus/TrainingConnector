package cz.ami.connector.daktela.testserver;

import cz.ami.connector.daktela.model.Item;
import lombok.Data;

@Data
public class SimpleUser extends Item {
    private String alias;

    public SimpleUser(String name, String title, String description, String alias, String password, String clid, String email) {
        this.setName(name);
        this.setTitle(title);
        this.setDescription(description);
        this.alias = alias;
        this.password = password;
        this.clid = clid;
        this.email = email;
    }

    private String password;
    private String clid;
    private String email;
}
