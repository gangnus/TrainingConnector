package cz.ami.connector.daktela.tools;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * The fastest Java method of resource reading
 */
public class TestResourceFiles {
    public static String readStringContentFromFile(String resourceName) throws IOException {
        // This way we always have the correct classloader
        InputStream is =  Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = is.read(buffer)) != -1; ) {
            result.write(buffer, 0, length);
        }

        return result.toString("UTF-8");

    }

}
