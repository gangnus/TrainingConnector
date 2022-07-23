package cz.ami.connector.daktela;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import cz.ami.connector.daktela.data.Users;
import cz.ami.connector.daktela.model.User;
import cz.ami.connector.daktela.tools.AttributesSets;
import cz.ami.connector.daktela.tools.ProblemsAndErrors;
import org.apache.commons.lang.StringUtils;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.*;

import java.util.List;
import java.util.Set;

/**
 * Using Java 17
 * Placing of all in map in memory
 * Tested on Midpoint 4.4 with ConnId 1.5.0.18
 */
@ConnectorClass(displayNameKey = "training.connector.display", configurationClass = TrainingConfiguration.class)
public class TrainingConnector implements Connector, CreateOp, DeleteOp, TestOp, SchemaOp, SearchOp<Filter>, UpdateOp {

	private static final Trace LOG = TraceManager.getTrace(TrainingConnector.class);
    private TrainingConfiguration configuration;

    @Override
    public void init(Configuration configuration) {
        LOG.info(">>> Initializing Training connector");
        this.configuration = (TrainingConfiguration) configuration;
        LOG.info(">>> Training connector initialization finished");
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
    }

    @Override
    public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {
        LOG.info(">>> delete of the user with UID = " + uid.getUidValue());
        if (uid == null) {
            ProblemsAndErrors.uncheckedExcReaction(LOG, "uid for deleting not provided");
        }
        try {
            Users.delete(uid.getUidValue());
        } catch (Exception e) {
            ProblemsAndErrors.uncheckedExcReaction(LOG, "Deleting failed");
        }
        LOG.info(">>> delete of the user with UID = " + uid.getUidValue());
    }

    @Override
    public TrainingConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public Schema schema() {
        LOG.info("Training connector getting schema");
        return ConnectorSchema.getSchema();
    }

	@Override
	public void test() {
        LOG.info(">>> test training connector");
        schema();
        LOG.info("<<< test training connector");
	}

    @Override
    public FilterTranslator<Filter> createFilterTranslator(ObjectClass objectClass, OperationOptions options) {
        return CollectionUtil::newList;
    }


    @Override
    public void executeQuery(ObjectClass objectClass, Filter filter, ResultsHandler resultsHandler, OperationOptions options) {
        LOG.debug("SEARCH objectClass: " + objectClass);
        LOG.debug("SEARCH filter " + filter);
        LOG.debug("SEARCH options " + options);
        LOG.debug("SEARCH options.getPagedResultsOffset(): " + options.getPagedResultsOffset());
        LOG.debug("SEARCH options.getPageSize(): " + options.getPageSize());

        // USER
        if (ConnectorSchema.CLASS_USER.equals(objectClass)) {
            if (filter == null) {
                // všechny objekty
                LOG.debug("reading all users");
                List<User> users = Users.getAll();
                LOG.debug("get {} objects from end system", users.size());
                users.forEach(user -> resultsHandler.handle(ConnectorSchema.createConnectorObject(user)));
            } else if (filter.getClass().getName().equals("org.identityconnectors.framework.common.objects.filter.EqualsFilter")) {
                // jeden objekt
                EqualsFilter equalsFilter = (EqualsFilter)filter;
                String attrName  = equalsFilter.getAttribute().getName();
                String attrValue = (String) equalsFilter.getAttribute().getValue().get(0);
                LOG.debug("EqualsFilter, attribute name: {}", attrName);
                LOG.debug("EqualsFilter, attribute value: {}", attrValue);
                if (attrName == Uid.NAME) {
                    LOG.debug("EqualsFilter, search by UID");
                    User user = null;
                    try {
                        user = Users.read(attrValue);
                    } catch (Exception e) {
                        ProblemsAndErrors.uncheckedExcReaction(LOG,"Reading of one user failed.");
                    }
                    resultsHandler.handle(ConnectorSchema.createConnectorObject(user));
                } else {
                    ProblemsAndErrors.uncheckedExcReaction(LOG,"search by " + attrName + " not implemented");
                }
            }
        }

    }

    @Override
    public Uid create(ObjectClass objectClass, Set<Attribute> set, OperationOptions operationOptions) {
        LOG.info(">>> Creation User for Training Connector");
        // Check and clean the set from uids
        if (set == null || set.isEmpty()) {
            ProblemsAndErrors.uncheckedExcReaction(LOG,"attributes not provided or empty");
        }
        Uid uid = null;
        try {
            uid = getUidFromAttributesSet(set);
        } catch (Exception e) {
            ProblemsAndErrors.uncheckedExcReaction(LOG,"Attempt to create a user without uid");
        }

        // use the set
        // if we update the User objects
        if (objectClass.getObjectClassValue().equals(User.class.getSimpleName())) {
            try {
                createUser(uid, set);
            } catch (Exception e) {
                ProblemsAndErrors.uncheckedExcReaction(LOG, "User creation failed");
            }
        }
        LOG.info("<<< Creation User for Training Connector");

        return uid;
    }

    private void createUser(Uid uid, Set<Attribute> set) throws Exception {
        User user = new User();
        user.setName(uid.getUidValue());
        insertAttributesIntoUser(user, set);
        Users.add(user);
    }

    Uid getUidFromAttributesSet(Set<Attribute> set) throws Exception {
        String uidFromSet = AttributesSets.extractSingle(set,Uid.NAME);
        String nameFromSet = AttributesSets.extractSingle(set,Name.NAME);
        if (StringUtils.isBlank(uidFromSet) && StringUtils.isBlank(nameFromSet)){
            ProblemsAndErrors.checkedExcReaction(LOG,"No uids or names in the set.");
        }
        String uidString = null;
        if (!StringUtils.isBlank(uidFromSet)) {
            return new Uid(uidFromSet.trim());
        }
        if (!StringUtils.isBlank(nameFromSet)) {
            return new Uid(nameFromSet.trim());
        }
        // never reached
        return null;
    }

    @Override
    public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> set, OperationOptions operationOptions) {
        LOG.info(">>> update of the user with UID = " + uid.getUidValue());

        // Check and clean the set from uids
        if (set == null || set.isEmpty()) {
            ProblemsAndErrors.uncheckedExcReaction(LOG,"attributes not provided or empty");
        }
        try {
            removeUidAndNameFromAttributesSet(uid, set);
        } catch (Exception e) {
            ProblemsAndErrors.uncheckedExcReaction(LOG,"Contradictions in uids and/or names");
        }
        if (set.isEmpty()) {
            ProblemsAndErrors.uncheckedExcReaction(LOG,"Nothing to update");
        }

        // use the set
        // if we update the User objects
        if (objectClass.getObjectClassValue().equals(User.class.getSimpleName())){
            User user = new User();
            user.setName(uid.getUidValue());
            Boolean userChanged =  insertAttributesIntoUser(user, set);

            if (userChanged) {
                try {
                    Users.update(user);
                } catch (Exception e) {
                    ProblemsAndErrors.uncheckedExcReaction(LOG, "User updating failed");
                }
            } else {
                ProblemsAndErrors.uncheckedExcReaction(LOG,"user has not changed");
            }
        }
        LOG.info("<<< update of the user with UID = " + uid.getUidValue());
        return uid;
    }

    void removeUidAndNameFromAttributesSet(Uid uid, Set<Attribute> set) throws Exception {
        String uidFromSet = AttributesSets.extractSingle(set,Uid.NAME);
        String nameFromSet = AttributesSets.extractSingle(set,Name.NAME);
        if (uidFromSet != null) {
            // uid attr was present
            if(!uidFromSet.equals(uid.getUidValue())){
                ProblemsAndErrors.checkedExcReaction(LOG,"Contradictory uids. Uid from set = " + uidFromSet + ", uid by parameter = " + uid.getUidValue());
            }
        }
        if (nameFromSet != null) {
            // name attribute was present
            if(!nameFromSet.equals(uid.getUidValue())){
                ProblemsAndErrors.checkedExcReaction(LOG,"Contradictory uid and name. Name from set = " + nameFromSet + ", uid by parameter = " + uid.getUidValue());
            }
        }
    }

    private Boolean insertAttributesIntoUser(User user, Set<Attribute> set) {
        Boolean userChanged = false;
        for (Attribute attribute : set) {

            String name = attribute.getName();
            String value = null;
            try {
                value = attribute.getValue().get(0).toString();
            } catch (Exception e) {
                LOG.warn("name of an attribute = " + name + " has no values. err=" + e.getMessage());
                LOG.warn("attribute = " + attribute.toString());
                return false;
            }

            LOG.debug("name of an attribute = " + name + ", Value=" + value);

            userChanged |= insertNonUidPropertyIntoUser(user, name, value);
        }
        return userChanged;
    }

    private Boolean insertNonUidPropertyIntoUser(User user, String name, String value) {
        Boolean userChanged = false;
        switch (name) {

            case ConnectorSchema.ATTR_FULLNAME:
                user.setFullName(value);
                userChanged = true;
                break;
            case ConnectorSchema.ATTR_PASSWORD:
                user.setPassword(value);
                userChanged = true;
                break;
        }
        return userChanged;
    }


}
