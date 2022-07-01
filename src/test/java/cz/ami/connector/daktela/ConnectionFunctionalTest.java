package cz.ami.connector.daktela;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import cz.ami.connector.daktela.model.User;
import cz.ami.connector.daktela.testserver.TSWithConstantResponses;
import cz.ami.connector.daktela.tools.TestResourceFiles;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.*;

import static cz.ami.connector.daktela.testserver.TSWithConstantResponses.createServerForTesting;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ConnectionFunctionalTest {
    static GsonBuilder builder = new GsonBuilder();
    static Gson gson = builder.serializeNulls().setPrettyPrinting().create();

    DaktelaConnector connector = ConnectorForTest.createTestDaktelaConnector(TSWithConstantResponses.TEST_SERVER_URI);
    TSWithConstantResponses server = createServerForTesting();

    ConnectionFunctionalTest() throws Exception {
    }

    @Test
    public void TestReadFromTestServer() throws Exception {


        User user = connector.getConnection().read("Novak", User.class);
        String jsonStringMust = TestResourceFiles.readStringContentFromFile("novak.json");

        User userFromFile  = gson.fromJson(jsonStringMust, User.class);
        assertEquals(userFromFile.getName(), user.getName());
        assertEquals(userFromFile.getDescription(), user.getDescription());
        assertEquals(userFromFile.getPassword(), user.getPassword());
        assertEquals(userFromFile.getClid(), user.getClid());

    }

    @Test
    public void TestReadAllThroughConnection() throws Exception {

        List<User> usersFromServer = connector.getConnection().readAll(User.class);

        String jsonStringMust =
                "[" +
                        TestResourceFiles.readStringContentFromFile("novak.json") +
                        ", " +
                        TestResourceFiles.readStringContentFromFile("vlcek.json") +
                        "]";

        Type userListType = new TypeToken<ArrayList<User>>(){}.getType();
        List<User> usersFromFiles  = new ArrayList<>(gson.fromJson(jsonStringMust, userListType));

        assertEquals(usersFromServer.size(), 2);
        assertEquals(usersFromServer.get(0).getName(), usersFromFiles.get(0).getName());
        assertEquals(usersFromServer.get(0).getDescription(), usersFromFiles.get(0).getDescription());
        assertEquals(usersFromServer.get(1).getName(), usersFromFiles.get(1).getName());
        assertEquals(usersFromServer.get(1).getDescription(), usersFromFiles.get(1).getDescription());

    }
}