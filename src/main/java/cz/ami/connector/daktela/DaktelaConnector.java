package cz.ami.connector.daktela;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import cz.ami.connector.daktela.model.DaktelaUser;
import cz.ami.connector.daktela.model.DaktelaUserListResponse;
import cz.ami.connector.daktela.model.DaktelaUserResponse;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Using Java 11 Http client
 * Tested on Midpoint 4.4.1 with ConnId 1.5.0.18
 */
@ConnectorClass(displayNameKey = "daktela.connector.display", configurationClass = DaktelaConfiguration.class)
public class DaktelaConnector implements Connector, CreateOp, TestOp, SchemaOp, SearchOp<Filter>, UpdateOp {

	private static final Trace LOG = TraceManager.getTrace(DaktelaConnector.class);
    private DaktelaConfiguration configuration;
    private DaktelaConnection connection;

    @Override
    public void init(Configuration configuration) {
        this.configuration = (DaktelaConfiguration) configuration;
        connection = new DaktelaConnection(this.configuration);
    }

    DaktelaConnection getConnection(){
        return connection;
    }

    void setConnection(DaktelaConnection connection) {
        this.connection = connection;
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
    }

    @Override
    public DaktelaConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public Schema schema() {
        LOG.trace("Daktela connector getting schema");
        return DaktelaSchema.getSchema();
    }

	@Override
	public void test() {
		// TODO do something
	    schema();
	}

    @Override
    public FilterTranslator<Filter> createFilterTranslator(ObjectClass objectClass, OperationOptions options) {
        return CollectionUtil::newList;
    }

    static private void errorReaction(String message){
        LOG.error(message);
        throw new ConnectorException(message);
    }

    @Override
    public void executeQuery(ObjectClass objectClass, Filter filter, ResultsHandler resultsHandler, OperationOptions options) {
        LOG.debug("SEARCH objectClass: " + objectClass);
        LOG.debug("SEARCH filter " + filter);
        LOG.debug("SEARCH options " + options);
        LOG.debug("SEARCH options.getPagedResultsOffset(): " + options.getPagedResultsOffset());
        LOG.debug("SEARCH options.getPageSize(): " + options.getPageSize());

        // USER
        if (DaktelaSchema.CLASS_USER.equals(objectClass)) {
            if (filter == null) {
                // všechny objekty
                LOG.debug("reading all users");
                List<DaktelaUser> users = connection.readAll(DaktelaUserResponse.class);
                LOG.debug("get {} objects from end system", users.size());
                users.forEach(user -> resultsHandler.handle(DaktelaSchema.createConnectorObject(user)));
            } else if (filter.getClass().getName().equals("org.identityconnectors.framework.common.objects.filter.EqualsFilter")) {
                // jeden objekt
                EqualsFilter equalsFilter = (EqualsFilter)filter;
                String attrName  = equalsFilter.getAttribute().getName();
                String attrValue = (String) equalsFilter.getAttribute().getValue().get(0);
                LOG.debug("EqualsFilter, attribute name: {}", attrName);
                LOG.debug("EqualsFilter, attribute value: {}", attrValue);
                if (attrName == Uid.NAME) {
                    LOG.debug("EqualsFilter, search by UID");
                    DaktelaUser user = connection.read(attrValue, DaktelaUserListResponse.class);
                    resultsHandler.handle(DaktelaSchema.createConnectorObject(user));
                } else {
                    throw new ConnectorException("search by " + attrName + " not implemented");
                }
            }
        }

    }

    @Override
    public Uid create(ObjectClass objectClass, Set<Attribute> set, OperationOptions operationOptions) {
        LOG.debug("Start Create of the user " );
        if (set == null || set.isEmpty()) {
            errorReaction("attributes not provided or empty");
        }
        Uid uid = null;
        if (objectClass.getObjectClassValue().equals(DaktelaUser.class.getSimpleName())) {
            uid = createUser(set);
        }

        return uid;
    }

    @Nullable
    private Uid createUser(Set<Attribute> set) {
        Uid uid = null;
        DaktelaUser user = new DaktelaUser();

        if (set != null){
            for (Attribute attribute : set) {

                Uid newUid = insertAttrIntoANewUser(user, attribute);
                if (newUid != null) uid = newUid;
            }
        }
        if(user.getName()!=null) {
            connection.createRecord(user);
        } else {
            LOG.warn("user cannot be created. Reason: Empty Name/UID. Title/Name = " + user.getTitle());
        }
        return uid;
    }

    private Uid insertAttrIntoANewUser(DaktelaUser user, Attribute attribute) {
        Uid uid = null;
        String name = attribute.getName();
        String value = null;
        try {
            value = attribute.getValue().get(0).toString();
        } catch(Exception e){
            LOG.warn("name of an attribute = " + name + " has no values. err="+ e.getMessage());
            LOG.warn("attribute = " + attribute.toString());
            return null;
        }

        // __UID__
        if (name.equals(Uid.NAME)) {
            uid = new Uid(value);
            user.setName(value);
        }
        else {
            //insertNonUidPropertyIntoUser(user, name, value);
        }
        return uid;
    }
/*
    public Set<AttributeDelta> updateDelta(ObjectClass objectClass, Uid uid, Set<AttributeDelta> set, OperationOptions operationOptions) {
        LOG.debug("Start updateDelta of the user with UID = " + uid.getUidValue());

        User user = new User();
        user.setName(uid.getUidValue());
        Boolean userChanged = false;
        if (set!= null){
            for (AttributeDelta delta : set) {

                String value = null;
                try {
                    value = delta.getValuesToReplace().get(0).toString();
                } catch(Exception e){}
                String name = delta.getName();
                String valueAdd = null;
                try {
                    valueAdd = delta.getValuesToAdd().get(0).toString();
                } catch(Exception e){}
                String valueRem = null;
                try {
                    valueRem = delta.getValuesToRemove().get(0).toString();
                } catch(Exception e){}
                LOG.debug("name of an attribute = " + delta.getName() + ",  replace=" + value + ",  Add=" + valueAdd + ",  Remove=" + valueRem);
                // __UID__
                if (name.equals(Uid.NAME) && !uid.getUidValue().equals(value)) {
                    // Doesn't support to modify 'uid'
                    errorReaction("UID/Name cannot be changed. There was an attempt to change from " + uid.getUidValue() + " to " + value);
                }
                // __NAME__
                else if (name.equals(Name.NAME)) {
                    user.setTitle(value);
                    userChanged = true;
                } else

                    switch (name) {

                        case DaktelaSchema.ATTR_ALIAS:
                            user.setAlias(value);
                            userChanged = true;
                            break;
                        case DaktelaSchema.ATTR_DESCRIPTION:
                            user.setDescription(value);
                            userChanged = true;
                            break;
                        case DaktelaSchema.ATTR_PASSWORD:
                            user.setPassword(value);
                            userChanged = true;
                            break;
                        case DaktelaSchema.ATTR_CLID:
                            user.setClid(value);
                            userChanged = true;
                            break;
                        case DaktelaSchema.ATTR_EMAIL:
                            user.setEmail(value);
                            userChanged = true;
                            break;
                    }
            }
        }
        if (userChanged) {
            DaktelaConnection.getINST().updateRecord(user);
        }
        return null;
    }*/

    @Override
    public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> set, OperationOptions operationOptions) {
        LOG.debug("Start update of the user with UID = " + uid.getUidValue());

        // empty delta
        if (set == null || set.isEmpty()) {
            return uid;
        }
        if (objectClass.getObjectClassValue().equals(DaktelaUser.class.getSimpleName())){
            DaktelaUser user = new DaktelaUser();
            user.setName(uid.getUidValue());
            Boolean userChanged = false;
            if (set != null) {
                for (Attribute attribute : set) {
                    //userChanged = insertAttrIntoUser(uid, user, attribute) || userChanged;
                }
            }
            if (userChanged) {
                connection.updateRecord(user);
            }
        }
        return uid;
    }
/*
    private Boolean insertAttrIntoUser(Uid uid, DaktelaUser user, Attribute attribute) {
        Boolean userChanged = false;
        String name = attribute.getName();
        String value = null;
        try {
            value = attribute.getValue().get(0).toString();
        } catch(Exception e){
            LOG.warn("name of an attribute = " + name + " has no values. err="+ e.getMessage());
            LOG.warn("attribute = " + attribute.toString());
            return false;
        }

        LOG.debug("name of an attribute = " + name + ", Value=" + value);
        // __UID__
        if (name.equals(Uid.NAME) && !uid.getUidValue().equals(value)) {
            // Doesn't support modification of 'uid'
            errorReaction("UID/Name cannot be changed. There was an attempt to change from " + uid.getUidValue() + " to " + value);
        }

        else {
            userChanged = insertNonUidPropertyIntoUser(user, name, value);
        }
        return userChanged;
    }

    private Boolean insertNonUidPropertyIntoUser(DaktelaUser user, String name, String value) {
        Boolean userChanged = false;
        if (name.equals(Name.NAME)) {
            // __NAME__
            user.setTitle(value);
            userChanged = true;
        } else

            switch (name) {

                case DaktelaSchema.ATTR_EMAIL:
                    user.setEmail(value);
                    userChanged = true;
                    break;
                case DaktelaSchema.ATTR_ALIAS:
                    user.setAlias(value);
                    userChanged = true;
                    break;
                case DaktelaSchema.ATTR_DESCRIPTION:
                    user.setDescription(value);
                    userChanged = true;
                    break;
            }
        return userChanged;
    }
 */

}
