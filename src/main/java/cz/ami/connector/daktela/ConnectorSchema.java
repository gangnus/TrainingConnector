package cz.ami.connector.daktela;

import cz.ami.connector.daktela.model.User;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.spi.operations.SearchOp;

import java.util.HashSet;
import java.util.Set;

public class ConnectorSchema {

    public static final ObjectClass CLASS_USER = new ObjectClass("user");

    public static final String ATTR_PASSWORD = "password";
    public static final String ATTR_FULLNAME = "fullname";

    public static Schema getSchema() {

        final SchemaBuilder schemaBuilder = new SchemaBuilder(TrainingConnector.class);

        // USER
        Set<AttributeInfo> userAttributes = new HashSet<>();
        userAttributes.add(AttributeInfoBuilder.define(Uid.NAME).setCreateable(false).setUpdateable(false).setRequired(true).build());
        userAttributes.add(AttributeInfoBuilder.define(Name.NAME).setRequired(true).build());
        userAttributes.add(AttributeInfoBuilder.define(ATTR_FULLNAME).build());

        schemaBuilder.defineObjectClass(CLASS_USER.getObjectClassValue(), userAttributes);
        schemaBuilder.defineOperationOption(OperationOptionInfoBuilder.buildPageSize(), SearchOp.class);
        return schemaBuilder.build();
	}

    public static ConnectorObject createConnectorObject(User user) {
        ConnectorObjectBuilder cob = new ConnectorObjectBuilder();
        cob.setObjectClass(CLASS_USER);

        cob.setUid(user.getName());
        cob.setName(user.getName());

        cob.addAttribute(ATTR_FULLNAME, user.getFullName());
        cob.addAttribute(ATTR_PASSWORD, user.getPassword());

        return cob.build();
    }
}
