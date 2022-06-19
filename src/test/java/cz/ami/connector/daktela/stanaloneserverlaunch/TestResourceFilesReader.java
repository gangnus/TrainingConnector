package cz.ami.connector.daktela.stanaloneserverlaunch;

import org.xml.sax.InputSource;

import java.io.*;
import java.net.URL;

/**
 * The fastest Java method of resource reading
 */
public class TestResourceFilesReader {
    public static String readStringContentFromFile(String resourceName) throws IOException {
        // This way we should always have the correct classloader
        InputStream is =  Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = is.read(buffer)) != -1; ) {
            result.write(buffer, 0, length);
        }

        return result.toString("UTF-8");
        //String jsonString = Files.readString(Path.of("novak.json"), StandardCharsets.UTF_8);
        //String jsonString = new String(getClass().getClassLoader().getResourceAsStream("novak.json").readAllBytes());
    }
}
