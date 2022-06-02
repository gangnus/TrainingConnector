package cz.ami.connector.daktela;


import org.identityconnectors.framework.common.objects.*;

import java.util.HashSet;
import java.util.Set;

public class DaktelaSchema {

    public static final ObjectClass CLASS_USER = new ObjectClass("user");

    public static final String ATTR_FIRSTNAME = "firstname";
    public static final String ATTR_LASTNAME = "lastname";

	public static Schema getSchema() {

        final SchemaBuilder schemaBuilder = new SchemaBuilder(DaktelaConnector.class);

        // USER
        Set<AttributeInfo> userAttributes = new HashSet<>();
        userAttributes.add(AttributeInfoBuilder.define(Uid.NAME).setCreateable(false).setUpdateable(false).setRequired(true).build());
        userAttributes.add(AttributeInfoBuilder.define(Name.NAME).setRequired(true).build());
        userAttributes.add(AttributeInfoBuilder.define(ATTR_FIRSTNAME).build());
        userAttributes.add(AttributeInfoBuilder.define(ATTR_LASTNAME).build());

        schemaBuilder.defineObjectClass(CLASS_USER.getObjectClassValue(), userAttributes);
        return schemaBuilder.build();
	}
}
