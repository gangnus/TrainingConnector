package cz.ami.connector.daktela;

import cz.ami.connector.daktela.testserver.TSWithConstantResponses;
import org.jetbrains.annotations.NotNull;

public class ConnectorForTest {
    @NotNull
    public static DaktelaConnector createTestDaktelaConnector() {
        DaktelaConfiguration configuration = new DaktelaConfiguration();
        DaktelaConnector connector = new DaktelaConnector();
        connector.init(configuration);
        configuration.setServiceAddress(TSWithConstantResponses.TEST_SERVER_URI);
        return connector;
    }
}
