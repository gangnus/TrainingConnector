package cz.ami.connector.daktela;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class StandaloneTestServerLauncher {
    /**
     * This "test" is for launching a standalone test server for use by IDM manual testing
     * This is not a real unit test. @Disabled annotation should never been removed
     * @throws Exception
     */
    @Disabled
    @Test
    public void launch() throws Exception {
        ServerForTesting.createServerForTesting();
    }
    /**
     * This "test" is for stopping the standalone test server
     * This is not a real unit test. @Disabled annotation should never been removed
     * @throws Exception
     */
    @Disabled
    @Test
    public void stop() throws Exception {
        ServerForTesting serverforTesting = ServerForTesting.createServerForTesting();
        serverforTesting.stop();
    }
}
