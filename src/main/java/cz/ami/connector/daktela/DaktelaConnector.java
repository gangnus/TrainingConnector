
package cz.ami.connector.daktela;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;
import cz.ami.connector.daktela.http.User;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Using Java 9 immutable Map constructions
 * Using Java 11 Http client
 * Tested on Midpoint 4.4.1 with ConnId 1.5.0.18
 */
@ConnectorClass(displayNameKey = "daktela.connector.display", configurationClass = DaktelaConfiguration.class)
public class DaktelaConnector extends DaktelaConfiguration implements Connector, TestOp, SchemaOp, SearchOp<Filter>, UpdateOp {
        
	private static final Trace LOG = TraceManager.getTrace(DaktelaConnector.class);
    private DaktelaConfiguration configuration;
    
    @Override
    public void init(Configuration configuration) {
        this.configuration = (DaktelaConfiguration) configuration;
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
    public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> attributes, OperationOptions options) {
        LOG.debug("Start update of UID " + uid.getUidValue());
        return uid;
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
        if (DaktelaSchema.CLASS_USER.equals(objectClass)) {

            //klient se pripoji
            //getne usera s id !

            if(filter == null){
                LOG.debug("------------------- before reading all users ---------------------");

                Collection<User> users = User.readAll(HttpClient.newBuilder().build(), 100, configuration.getServiceAddress());
                if(users == null) {
                    throw new ConnectorException("Users list not found");
                }

                users.stream().forEach(user -> addUserToHandler(user, resultsHandler));


            } else if (filter.getClass().getName().equals("org.identityconnectors.framework.common.objects.filter.EqualsFilter")){
                LOG.debug("------------------- before reading a user ---------------------");
                String uidName  = ((EqualsFilter)filter).getAttribute().getName();
                if(uidName == null) {
                    throw new ConnectorException("A user can be searched by uid only");
                }
                List<Object> uidValue  = ((EqualsFilter)filter).getAttribute().getValue();
                if(uidValue == null || uidValue.size()==0) {
                    throw new ConnectorException("A uid for a user search is not set");
                }
                if(uidValue.size()>1) {
                    throw new ConnectorException("A uid for a user search is not single");
                }
                String uidString = uidValue.get(0).toString();

                User user = User.read(HttpClient.newBuilder().build(), 100, configuration.getServiceAddress(), "Novak");

                addUserToHandler(user, resultsHandler);

            }
        }
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

        resultsHandler.handle(cob.build());
    }
}
