package cz.ami.connector.daktela;

import cz.ami.connector.daktela.model.DaktelaUser;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.spi.operations.SearchOp;

import java.util.HashSet;
import java.util.Set;

public class DaktelaSchema {

    public static final ObjectClass CLASS_USER = new ObjectClass("user");

    private static final String ATTR_NAME = "name"; // ICFS UID
    private static final String ATTR_TITLE = "title"; // ICFS NAME
    private static final String ATTR_DESCRIPTION = "description";
    private static final String ATTR_ALIAS = "alias";
    private static final String ATTR_EMAIL = "e-mail";
    private static final String ATTR_EMOJI = "emoji";
    private static final String ATTR_ICON = "icon";
    private static final String ATTR_DELETED = "deleted";
    private static final String ATTR_DEACTIVATED = "deactivated";
    private static final String ATTR_PROFILE_TITLE = "profile.title";
    private static final String ATTR_ROLE = "role";

    public static Schema getSchema() {

        final SchemaBuilder schemaBuilder = new SchemaBuilder(DaktelaConnector.class);

        // USER
        Set<AttributeInfo> userAttributes = new HashSet<>();
        userAttributes.add(AttributeInfoBuilder.define(Uid.NAME).setCreateable(false).setUpdateable(false).setRequired(true).build());
        userAttributes.add(AttributeInfoBuilder.define(Name.NAME).setRequired(true).build());
        userAttributes.add(AttributeInfoBuilder.define(ATTR_EMAIL).build());
        userAttributes.add(AttributeInfoBuilder.define(ATTR_ALIAS).build());
        userAttributes.add(AttributeInfoBuilder.define(ATTR_DESCRIPTION).build());
        userAttributes.add(AttributeInfoBuilder.define(ATTR_ICON).build());
        userAttributes.add(AttributeInfoBuilder.define(ATTR_EMOJI).build());
        userAttributes.add(AttributeInfoBuilder.define(ATTR_DELETED).setType(boolean.class).build());
        userAttributes.add(AttributeInfoBuilder.define(ATTR_DEACTIVATED).setType(boolean.class).build());

        userAttributes.add(AttributeInfoBuilder.define(ATTR_PROFILE_TITLE).build());

        userAttributes.add(AttributeInfoBuilder.define(ATTR_ROLE).build());

        schemaBuilder.defineObjectClass(CLASS_USER.getObjectClassValue(), userAttributes);
        schemaBuilder.defineOperationOption(OperationOptionInfoBuilder.buildPageSize(), SearchOp.class);
        return schemaBuilder.build();
	}

    public static ConnectorObject createConnectorObject(DaktelaUser user) {
        ConnectorObjectBuilder cob = new ConnectorObjectBuilder();
        cob.setObjectClass(CLASS_USER);

        cob.setUid(user.getName());
        cob.setName(user.getTitle());

        cob.addAttribute(ATTR_DESCRIPTION, user.getDescription());
        cob.addAttribute(ATTR_EMAIL, user.getEmail());
        cob.addAttribute(ATTR_ALIAS, user.getAlias());
        cob.addAttribute(ATTR_ICON, user.getIcon());
        cob.addAttribute(ATTR_EMOJI, user.getEmoji());
        cob.addAttribute(ATTR_DEACTIVATED, user.getDeactivated());
        cob.addAttribute(ATTR_DELETED, user.getDeleted());

        if (user.getProfile() != null) {
            cob.addAttribute(ATTR_PROFILE_TITLE, user.getProfile().getTitle());
        }

        if (user.getRole() != null) {
            cob.addAttribute(ATTR_ROLE, user.getRole().getName());
        }

//        if (ArrayUtils.isNotEmpty(user.getMemberOfRoles())) {
//            cob.addAttribute(ATTR_MEMBERSHIP_ROLE, Arrays.asList(user.getMemberOfRoles()));
//        }

        return cob.build();
    }
}
