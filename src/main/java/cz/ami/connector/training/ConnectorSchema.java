package cz.ami.connector.training;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import cz.ami.connector.training.model.User;
import cz.ami.connector.training.tools.LogMessages;
import cz.ami.connector.training.tools.ProblemsAndErrors;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.spi.operations.SearchOp;

import java.util.HashSet;
import java.util.Set;

public class ConnectorSchema {
    private static final Trace LOG = TraceManager.getTrace(ConnectorSchema.class);
    public static final ObjectClass CLASS_USER = new ObjectClass("user");

    public static final String ATTR_PASSWORD = "password";
    public static final String ATTR_FULLNAME = "fullname";

    public static Schema getSchema() {
        LOG.info(">>> " + LogMessages.TRAINING_CONNECTOR_GETTING_SCHEMA);

        try {
            final SchemaBuilder schemaBuilder = new SchemaBuilder(TrainingConnector.class);

            // USER
            Set<AttributeInfo> userAttributes = new HashSet<>();
            userAttributes.add(AttributeInfoBuilder.define(Uid.NAME).setCreateable(true).setUpdateable(false).setRequired(true).build());
            userAttributes.add(AttributeInfoBuilder.define(Name.NAME).setCreateable(true).setUpdateable(false).setRequired(true).build());
            userAttributes.add(AttributeInfoBuilder.define(ATTR_FULLNAME).setCreateable(true).setUpdateable(true).setRequired(false).build());
            userAttributes.add(AttributeInfoBuilder.define(ATTR_PASSWORD).setCreateable(true).setUpdateable(true).setRequired(false).build());

            schemaBuilder.defineObjectClass(CLASS_USER.getObjectClassValue(), userAttributes);
            schemaBuilder.defineOperationOption(OperationOptionInfoBuilder.buildPageSize(), SearchOp.class);
            Schema schema = schemaBuilder.build();
            LOG.error("schema = " + schema.toString());
            LOG.info("<<< " + LogMessages.TRAINING_CONNECTOR_GETTING_SCHEMA);
            return schema;
        } catch (Exception e) {
            LOG.error("-----------------------------getSchema failed-------------------------------");
            LOG.error(ExceptionUtils.getStackTrace(e));
            ProblemsAndErrors.uncheckedExcReaction(LOG, "getSchema failed");
            return null;
        }
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
