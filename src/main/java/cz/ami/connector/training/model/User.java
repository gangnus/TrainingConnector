package cz.ami.connector.training.model;

import lombok.Data;

//TODO The source often says nothing about if the field is a collection or a single item.
// As the source often has errors about using the "s" at the end of words, we definitely cannot rely on them.
//
@Data
public class User extends Item {

    private String fullName;
    private String password;

}
