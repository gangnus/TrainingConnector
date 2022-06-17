package cz.ami.connector.daktela;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cz.ami.connector.daktela.model.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.*;

import static cz.ami.connector.daktela.ServerForTesting.createServerForTesting;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ConnectionFunctionalTest {
    static private final Gson gson = new Gson();
    DaktelaConnector connector = ConnectorForTest.createTestDaktelaConnector();
    ServerForTesting server = createServerForTesting();

    ConnectionFunctionalTest() throws Exception {
    }

    @Test
    public void TestReadFromTestServer() throws Exception {


        User user = DaktelaConnection.getINST().read("Novak", User.class);
        String jsonStringMust = new String(getClass().getClassLoader().getResourceAsStream("novak.json").readAllBytes());

        User userFromFile  = gson.fromJson(jsonStringMust, User.class);
        assertEquals(userFromFile.getName(), user.getName());
        assertEquals(userFromFile.getDescription(), user.getDescription());
        assertEquals(userFromFile.getPassword(), user.getPassword());
        assertEquals(userFromFile.getClid(), user.getClid());

    }

    @Test
    public void TestReadAllThroughConnection() throws Exception {

        List<User> usersFromServer = DaktelaConnection.getINST().readAll(User.class);

        String jsonStringMust =
                "[" +
                        new String(getClass().getClassLoader().getResourceAsStream("novak.json").readAllBytes()) +
                        ", " +
                        new String(getClass().getClassLoader().getResourceAsStream("vlcek.json").readAllBytes()) +
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