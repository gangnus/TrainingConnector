package cz.ami.connector.daktela.tools;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * The fastest Java method of resource reading
 */
public class TestFilesInRoot {
    static final String dir="c:/IDM_testServerMemory";
    public static String readMemory(String resourceName) throws IOException {
        String fullfilename = dir + "/" + resourceName;
        File memoryFile = new File(fullfilename);
        InputStream is =  new FileInputStream(memoryFile);
        if(is == null ){
            throw new IOException();
        }
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = is.read(buffer)) != -1; ) {
            result.write(buffer, 0, length);
        }

        return result.toString("UTF-8");
    }
    public static void writeMemory(String resourceName, String content) throws IOException {
        File dirFile = new File(dir);
        if(!dirFile.exists()){
            dirFile.mkdir();
        }
        String fullfilename = dir + "/" + resourceName;
        OutputStreamWriter writer =
                     new OutputStreamWriter(new FileOutputStream(fullfilename), StandardCharsets.UTF_8);
        writer.write(content);
        writer.close();

    }
}
