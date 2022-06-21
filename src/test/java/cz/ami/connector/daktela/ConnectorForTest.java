package cz.ami.connector.daktela;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import cz.ami.connector.daktela.testserver.TSWithConstantResponses;
import cz.ami.connector.daktela.testserver.TSWithMemory;
import org.jetbrains.annotations.NotNull;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConnectorForTest {
    private static final Trace LOG = TraceManager.getTrace(ConnectorForTest.class);
    @NotNull
    public static DaktelaConnector createTestDaktelaConnector(String serverUri) {
        LOG.debug("Creation of DaktelaConnector to uri=" + serverUri);
        DaktelaConfiguration configuration = new DaktelaConfiguration();
        DaktelaConnector connector = new DaktelaConnector();
        connector.init(configuration);
        configuration.setServiceAddress(serverUri);
        // we should change the configuration used, for different tests use different servers, and their URIs are set in configuration
        DaktelaConnection.changeINST(new DaktelaConnection(configuration));
        assertEquals(serverUri, DaktelaConnection.getINST().getUriSource()," check server URI got from connection");

        return connector;
    }
}
