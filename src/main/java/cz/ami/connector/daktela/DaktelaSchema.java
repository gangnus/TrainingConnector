package cz.ami.connector.daktela;


import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.spi.operations.SearchOp;

import java.util.HashSet;
import java.util.Set;

public class DaktelaSchema {

    public static final ObjectClass CLASS_USER = new ObjectClass("user");

    public static final String ATTR_FIRSTNAME = "firstname";
    public static final String ATTR_LASTNAME = "lastname";
    public static final String ATTR_ALIAS = "alias";
    public static final String ATTR_DESCRIPTION = "description";
    public static final String ATTR_PASSWORD = "password";
    public static final String ATTR_CLID = "clid";
    public static final String ATTR_EMAIL = "e-mail";

    public static Schema getSchema() {

        final SchemaBuilder schemaBuilder = new SchemaBuilder(DaktelaConnector.class);

        // USER
        Set<AttributeInfo> userAttributes = new HashSet<>();
        userAttributes.add(AttributeInfoBuilder.define(Uid.NAME).setCreateable(false).setUpdateable(false).setRequired(true).build());
        userAttributes.add(AttributeInfoBuilder.define(Name.NAME).setCreateable(true).setUpdateable(true).setRequired(true).build());
        //userAttributes.add(AttributeInfoBuilder.define(ATTR_FIRSTNAME).build());
        userAttributes.add(AttributeInfoBuilder.define(ATTR_ALIAS).setCreateable(true).setUpdateable(true).setRequired(false).build());
        userAttributes.add(AttributeInfoBuilder.define(ATTR_DESCRIPTION).setCreateable(true).setUpdateable(true).setRequired(false).build());
        userAttributes.add(AttributeInfoBuilder.define(ATTR_PASSWORD).setCreateable(true).setUpdateable(true).setRequired(true).build());
        userAttributes.add(AttributeInfoBuilder.define(ATTR_CLID).setCreateable(true).setUpdateable(true).setRequired(true).build());
        userAttributes.add(AttributeInfoBuilder.define(ATTR_EMAIL).setCreateable(true).setUpdateable(true).setRequired(true).build());

        schemaBuilder.defineObjectClass(CLASS_USER.getObjectClassValue(), userAttributes);
        schemaBuilder.defineOperationOption(OperationOptionInfoBuilder.buildPageSize(), SearchOp.class);
        return schemaBuilder.build();
	}
}
