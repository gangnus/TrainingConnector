
package cz.ami.connector.daktela;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;

import java.util.Set;

/**
 * Using Java 9 immutable Map constructions
 * Using Java 11 Http client
 * Tested on Midpoint 4.4.1 with ConnId 1.5.0.18
 */
@ConnectorClass(displayNameKey = "helios.connector.display", configurationClass = DaktelaConfiguration.class)
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
            // udelat objekt a poslat ho
            ConnectorObjectBuilder cob = new ConnectorObjectBuilder();
            cob.setObjectClass(DaktelaSchema.CLASS_USER);
    
            cob.setUid("1");
            cob.setName("tomas.mraz");
            cob.addAttribute(DaktelaSchema.ATTR_FIRSTNAME,"Tomas");
            cob.addAttribute(DaktelaSchema.ATTR_LASTNAME, "Mraz");

            resultsHandler.handle(cob.build());
        }
    }
}
