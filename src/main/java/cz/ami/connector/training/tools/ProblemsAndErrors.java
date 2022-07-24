package cz.ami.connector.training.tools;

import com.evolveum.midpoint.util.logging.Trace;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

/**
 * the class contains functions for work with exceptions
 */
public abstract class ProblemsAndErrors {

    static public void uncheckedExcReaction(Trace LOG, String message){
        LOG.error(message);
        throw new ConnectorException(message);
    }
    static public void checkedExcReaction(Trace LOG, String message) throws Exception {
        LOG.error(message);
        throw new Exception(message);
    }
}
