
package cz.ami.connector.daktela;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import cz.ami.connector.daktela.model.User;
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
 * Using Java 9 immutable Map constructions
 * Using Java 11 Http client
 * Tested on Midpoint 4.4.1 with ConnId 1.5.0.18
 */
@ConnectorClass(displayNameKey = "daktela.connector.display", configurationClass = DaktelaConfiguration.class)
public class DaktelaConnector implements Connector, CreateOp, TestOp, SchemaOp, SearchOp<Filter>, UpdateDeltaOp {
        
	private static final Trace LOG = TraceManager.getTrace(DaktelaConnector.class);

    private DaktelaConfiguration configuration;

    @Override
    public void init(Configuration configuration) {

        this.configuration = (DaktelaConfiguration) configuration;
        DaktelaConnection.setNewINST(this.configuration);
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

            //klient se pripoji
            //getne usera s id !

            if(filter == null){
                LOG.debug("------------------- before reading all users ---------------------");

                List<User> users = DaktelaConnection.getINST().readAll(User.class);
                if(users == null) {
                    throw new ConnectorException("Users list not found");
                }

                users.stream().forEach(user -> addUserToHandler(user, resultsHandler));


            } else if (filter.getClass().getName().equals("org.identityconnectors.framework.common.objects.filter.EqualsFilter")){
                LOG.debug("------------------- before reading a user ---------------------");
                String uidName  = ((EqualsFilter)filter).getAttribute().getName();
                if(uidName == null) {
                    errorReaction("A user can be searched by uid only");
                }
                List<Object> uidValue  = ((EqualsFilter)filter).getAttribute().getValue();
                if(uidValue == null || uidValue.size()==0) {
                    errorReaction("A uid for a user search is not set");
                }
                if(uidValue.size()>1) {
                    errorReaction("A uid for a user search is not single");
                }
                String uidString = uidValue.get(0).toString();

                User user = DaktelaConnection.getINST().read(uidString, User.class);

                addUserToHandler(user, resultsHandler);


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
        User user = new User();

        if (set!= null){
            for (Attribute attribute : set) {
                LOG.debug("name of an attribute = " + attribute.getName() + ",  value=" + attribute.getValue().get(0).toString());
                String value = attribute.getValue().get(0).toString();
                String name = attribute.getName();
                // __UID__
                if (name.equals(Uid.NAME)) {
                    uid = new Uid(value);
                    user.setName(value);
                }
                // __NAME__
                else if (name.equals(Name.NAME)) {
                    user.setTitle(value);
                } else

                    switch (name) {

                        case DaktelaSchema.ATTR_ALIAS:
                            user.setAlias(value);
                            break;
                        case DaktelaSchema.ATTR_DESCRIPTION:
                            user.setDescription(value);
                            break;
                        case DaktelaSchema.ATTR_PASSWORD:
                            user.setPassword(value);
                            break;
                        case DaktelaSchema.ATTR_CLID:
                            user.setClid(value);
                            break;
                        case DaktelaSchema.ATTR_EMAIL:
                            user.setEmail(value);
                            break;
                    }
            }
        }

        DaktelaConnection.getINST().createRecord(user);


        return uid;
    }

    @Override
    public Set<AttributeDelta> updateDelta(ObjectClass objectClass, Uid uid, Set<AttributeDelta> set, OperationOptions operationOptions) {
        LOG.debug("Start updateDelta of the user with UID = " + uid.getUidValue());

        User user = new User();
        user.setName(uid.getUidValue());
        Boolean userChanged = false;
        if (set!= null){
            for (AttributeDelta delta : set) {
                LOG.debug("name of an attribute = " + delta.getName() + ",  delta=" + delta.toString());
                String value = delta.getValuesToReplace().get(0).toString();
                String name = delta.getName();
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
    }
    /**
     *  udelat objekt a poslat ho
      */
    private void addUserToHandler(User user, ResultsHandler resultsHandler){
        ConnectorObjectBuilder cob = new ConnectorObjectBuilder();
        cob.setObjectClass(DaktelaSchema.CLASS_USER);

        cob.setUid(user.getName());
        cob.setName(user.getTitle());
        cob.addAttribute(DaktelaSchema.ATTR_ALIAS, user.getAlias());
        cob.addAttribute(DaktelaSchema.ATTR_DESCRIPTION, user.getDescription());
        cob.addAttribute(DaktelaSchema.ATTR_PASSWORD, user.getPassword());
        cob.addAttribute(DaktelaSchema.ATTR_CLID, user.getClid());
        cob.addAttribute(DaktelaSchema.ATTR_EMAIL, user.getEmail());

        resultsHandler.handle(cob.build());
    }

}
