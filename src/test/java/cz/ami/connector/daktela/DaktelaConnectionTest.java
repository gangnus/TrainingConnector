package cz.ami.connector.daktela;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cz.ami.connector.daktela.model.User;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DaktelaConnectionTest {

    String uriSource = "https://c15be332-489e-455e-81f0-146b386abbad.mock.pstmn.io";


    public void initConfiguration(){
        DaktelaConfiguration configuration = new DaktelaConfiguration();
        configuration.setServiceAddress(uriSource);
        configuration.setTimeoutSeconds(100);
        DaktelaConnection.setNewINST(configuration);
    }
    @Test
    public void TestReadThroughConnection() throws IOException, URISyntaxException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        initConfiguration();
        User user = DaktelaConnection.getINST().read("Novak", User.class);
        String jsonStringMust = new String(getClass().getClassLoader().getResourceAsStream("user1.json").readAllBytes());
        Gson gson = new Gson();
        String jsonStringGot = gson.toJson(user);
        assertEquals(jsonStringGot, jsonStringMust);
    }

    @Test
    public void TestReadAllThroughConnection() throws IOException {
        HttpClient client = HttpClient.newBuilder().build();

        List<User> usersFromServer = DaktelaConnection.getINST().readAll(User.class);
        String jsonStringMust =
                "[" +
                        new String(getClass().getClassLoader().getResourceAsStream("user1.json").readAllBytes()) +
                        ", " +
                        new String(getClass().getClassLoader().getResourceAsStream("user2.json").readAllBytes()) +
                        "]";

        Gson gson = new Gson();
        Type userListType = new TypeToken<ArrayList<User>>(){}.getType();
        List<User> usersFromFiles  = new ArrayList<>(gson.fromJson(jsonStringMust, userListType));

        assertEquals(usersFromServer.size(), 2);
        assertEquals(usersFromServer.get(0).getName(), usersFromFiles.get(0).getName());
        assertEquals(usersFromServer.get(0).getDescription(), usersFromFiles.get(0).getDescription());
        assertEquals(usersFromServer.get(1).getName(), usersFromFiles.get(1).getName());
        assertEquals(usersFromServer.get(1).getDescription(), usersFromFiles.get(1).getDescription());
    }

    @Test
    public void TestStructure() throws IOException {
        String jsonString = new String(getClass().getClassLoader().getResourceAsStream("user1.json").readAllBytes());
        Gson gson = new Gson();
        User user = gson.fromJson(jsonString, User.class);
        assertEquals(user.getAlias(), "NJ");
        assertEquals(user.getName(), "Novak");
        assertEquals(user.getTitle(), "Novák Jan");

        assertEquals(user.getRole().get(0).getName(), "admin");
        assertEquals(user.getRole().get(0).getTitle(), "Administrator");
        assertEquals(user.getRole().get(0).getDescription(), "rights to change logins");
        assertEquals(user.getRole().get(0).getShortcuts(), "");
        assertEquals(user.getRole().get(0).getOptions(), "");

        assertEquals(user.getRole().get(1).getName(), "mg");
        assertEquals(user.getRole().get(1).getTitle(), "Manager");
        assertEquals(user.getRole().get(1).getDescription(), "rights to sort logins");
        assertEquals(user.getRole().get(1).getShortcuts(), "{\"ctrl+a\":\"command1\"}");
        assertEquals(user.getRole().get(1).getOptions(), "{\"o32\":\"high\"}");

        assertEquals(user.getProfile().get(0).getName(), "newbie");
        assertEquals(user.getProfile().get(0).getTitle(), "newbie");
        assertEquals(user.getProfile().get(0).getDescription(), "newbie");
        assertEquals(user.getProfile().get(0).getMaxActivities(), 1);
        assertEquals(user.getProfile().get(0).getMaxOutRecords(), 2);
        assertEquals(user.getProfile().get(0).getDeleteMissedActivity(), false);
        assertEquals(user.getProfile().get(0).getNoQueueCallsAllowed(), false);
        assertEquals(user.getProfile().get(0).getCanTransferCall(), "None");
        assertEquals(user.getProfile().get(0).getOptions(), "");
        assertEquals(user.getProfile().get(0).getCustomViews(), "");

        assertEquals(user.getProfile().get(1).getName(), "oldie");
        assertEquals(user.getProfile().get(1).getTitle(), "oldie");
        assertEquals(user.getProfile().get(1).getDescription(), "oldie");
        assertEquals(user.getProfile().get(1).getMaxActivities(), 3);
        assertEquals(user.getProfile().get(1).getMaxOutRecords(), 10);
        assertEquals(user.getProfile().get(1).getDeleteMissedActivity(), true);
        assertEquals(user.getProfile().get(1).getNoQueueCallsAllowed(), true);
        assertEquals(user.getProfile().get(1).getCanTransferCall(), "Only assisted transfer");
        assertEquals(user.getProfile().get(1).getOptions(), "{\"o1\":\"+\"}");
        assertEquals(user.getProfile().get(1).getCustomViews(), "{\"fullscreen\":\"forbidden\"}");

        assertEquals(user.getNpsScore(), 25.3f);
        assertEquals(user.getDescription(), "the most problem user");
        assertEquals(user.getCallSteeringDescription(), "don't touch!");
        assertEquals(user.getPassword(), "pwd");
        assertEquals(user.getExtension(), "ext");

        assertEquals(user.getAcl().get(0).getName(), "read");
        assertEquals(user.getAcl().get(0).getTitle(), "reading");
        assertEquals(user.getAcl().get(0).getTime(), "300");
        assertEquals(user.getAcl().get(0).getRules(), "");

        assertEquals(user.getAcl().get(1).getName(), "write");
        assertEquals(user.getAcl().get(1).getTitle(), "writing");
        assertEquals(user.getAcl().get(1).getTime(), "600");
        assertEquals(user.getAcl().get(1).getRules(), "");


        assertEquals(user.getExtState(), "online");
        assertEquals(user.getClid(), "+420 155 111 258");
        assertEquals(user.getIfStaticLogin(), true);
        assertEquals(user.getAllowRecordingInterruption(), true);
        assertEquals(user.getRecordAtCallStart(), "Do not record");

        assertEquals(user.getAlgo().getName(), "algoName");
        assertEquals(user.getAlgo().getTitle(), "algoTitle");

        assertEquals(user.getAlgo().getIntegration().getName(), "integrName");

        assertEquals(user.getAlgo().getIntegration().getType(), "AUTH");
        assertEquals(user.getAlgo().getIntegration().getTitle(), "integrTitle");
        assertEquals(user.getAlgo().getIntegration().getDescription(), "integrDesc");

        assertEquals(user.getAlgo().getIntegration().getAuthInfo(), "integrInfo");
        assertEquals(user.getAlgo().getIntegration().getImageName(), "integrImageName");
        assertEquals(user.getAlgo().getIntegration().getIcon(), "integrIconName");
        assertEquals(user.getAlgo().getIntegration().getActive(), false);
        assertEquals(user.getAlgo().getIntegration().getAuth(), false);
        assertEquals(user.getAlgo().getIntegration().getConfig(), false);

        assertEquals(user.getAlgo().getIntegration().getError(), "{\"error\":\"111\"}");


        assertEquals(user.getAlgo().getActive(), true);
        assertEquals(user.getAlgo().getAuth(), "{\"auth\":\"algoAuth\"}");
        assertEquals(user.getAlgo().getConfig(), "{\"config\":\"algoConfig\"}");

        assertEquals(user.getEmail(), "name@server.cz");
        assertEquals(user.getEmailAuth(), "name2@server2.com");
        assertEquals(user.getIcon(), "iconAsString");

        assertEquals(user.getOptions().getSign(), "<p>me</p>");
        assertEquals(user.getOptions().getTarget(), "none");

        assertEquals(user.getBackofficeUser(), true);
        assertEquals(user.getForwardingNumber(), "+420 123 456 789");
        assertEquals(user.getDeactivated(), false);
        assertEquals(user.getDeleted(), false);
    }

}