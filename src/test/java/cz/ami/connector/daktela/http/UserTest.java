package cz.ami.connector.daktela.http;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cz.ami.connector.daktela.DaktelaConfiguration;
import cz.ami.connector.daktela.model.User;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserTest {
    String uriSource = "https://c15be332-489e-455e-81f0-146b386abbad.mock.pstmn.io";

    @BeforeMethod
    public void initConfiguration(){
        DaktelaConfiguration configuration = new DaktelaConfiguration();
        configuration.setServiceAddress(uriSource);
        configuration.setTimeoutSeconds(100);
        DaktelaConnection.setINST(configuration);
    }

    @Test( enabled=false )
    public void TestReadThroughConnection() throws IOException, URISyntaxException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();

        User user = DaktelaConnection.getINST().read("Novak", User.class);
        String jsonStringMust = new String(getClass().getClassLoader().getResourceAsStream("user1.json").readAllBytes());
        Gson gson = new Gson();
        String jsonStringGot = gson.toJson(user);
        Assert.assertEquals(jsonStringGot, jsonStringMust);
    }

    @Test( enabled=false )
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

        Assert.assertEquals(usersFromServer.size(), 2);
        Assert.assertEquals(usersFromServer.get(0).getName(), usersFromFiles.get(0).getName());
        Assert.assertEquals(usersFromServer.get(0).getDescription(), usersFromFiles.get(0).getDescription());
        Assert.assertEquals(usersFromServer.get(1).getName(), usersFromFiles.get(1).getName());
        Assert.assertEquals(usersFromServer.get(1).getDescription(), usersFromFiles.get(1).getDescription());
    }

    @Test( enabled=false )
    public void TestStructure() throws IOException {
        String jsonString = new String(getClass().getClassLoader().getResourceAsStream("user1.json").readAllBytes());
        Gson gson = new Gson();
        User user = gson.fromJson(jsonString, User.class);
        Assert.assertEquals(user.getAlias(), "NJ");
        Assert.assertEquals(user.getName(), "Novak");
        Assert.assertEquals(user.getTitle(), "Novák Jan");

            Assert.assertEquals(user.getRole().get(0).getName(), "admin");
            Assert.assertEquals(user.getRole().get(0).getTitle(), "Administrator");
            Assert.assertEquals(user.getRole().get(0).getDescription(), "rights to change logins");
            Assert.assertEquals(user.getRole().get(0).getShortcuts(), "");
            Assert.assertEquals(user.getRole().get(0).getOptions(), "");

            Assert.assertEquals(user.getRole().get(1).getName(), "mg");
            Assert.assertEquals(user.getRole().get(1).getTitle(), "Manager");
            Assert.assertEquals(user.getRole().get(1).getDescription(), "rights to sort logins");
            Assert.assertEquals(user.getRole().get(1).getShortcuts(), "{\"ctrl+a\":\"command1\"}");
            Assert.assertEquals(user.getRole().get(1).getOptions(), "{\"o32\":\"high\"}");

            Assert.assertEquals(user.getProfile().get(0).getName(), "newbie");
            Assert.assertEquals(user.getProfile().get(0).getTitle(), "newbie");
            Assert.assertEquals(user.getProfile().get(0).getDescription(), "newbie");
            Assert.assertEquals(user.getProfile().get(0).getMaxActivities(), 1);
            Assert.assertEquals(user.getProfile().get(0).getMaxOutRecords(), 2);
            Assert.assertEquals(user.getProfile().get(0).getDeleteMissedActivity(), false);
            Assert.assertEquals(user.getProfile().get(0).getNoQueueCallsAllowed(), false);
            Assert.assertEquals(user.getProfile().get(0).getCanTransferCall(), "None");
            Assert.assertEquals(user.getProfile().get(0).getOptions(), "");
            Assert.assertEquals(user.getProfile().get(0).getCustomViews(), "");

            Assert.assertEquals(user.getProfile().get(1).getName(), "oldie");
            Assert.assertEquals(user.getProfile().get(1).getTitle(), "oldie");
            Assert.assertEquals(user.getProfile().get(1).getDescription(), "oldie");
            Assert.assertEquals(user.getProfile().get(1).getMaxActivities(), 3);
            Assert.assertEquals(user.getProfile().get(1).getMaxOutRecords(), 10);
            Assert.assertEquals(user.getProfile().get(1).getDeleteMissedActivity(), true);
            Assert.assertEquals(user.getProfile().get(1).getNoQueueCallsAllowed(), true);
            Assert.assertEquals(user.getProfile().get(1).getCanTransferCall(), "Only assisted transfer");
            Assert.assertEquals(user.getProfile().get(1).getOptions(), "{\"o1\":\"+\"}");
            Assert.assertEquals(user.getProfile().get(1).getCustomViews(), "{\"fullscreen\":\"forbidden\"}");

        Assert.assertEquals(user.getNpsScore(), 25.3f);
        Assert.assertEquals(user.getDescription(), "the most problem user");
        Assert.assertEquals(user.getCallSteeringDescription(), "don't touch!");
        Assert.assertEquals(user.getPassword(), "pwd");
        Assert.assertEquals(user.getExtension(), "ext");

            Assert.assertEquals(user.getAcl().get(0).getName(), "read");
            Assert.assertEquals(user.getAcl().get(0).getTitle(), "reading");
            Assert.assertEquals(user.getAcl().get(0).getTime(), "300");
            Assert.assertEquals(user.getAcl().get(0).getRules(), "");

            Assert.assertEquals(user.getAcl().get(1).getName(), "write");
            Assert.assertEquals(user.getAcl().get(1).getTitle(), "writing");
            Assert.assertEquals(user.getAcl().get(1).getTime(), "600");
            Assert.assertEquals(user.getAcl().get(1).getRules(), "");


        Assert.assertEquals(user.getExtState(), "online");
        Assert.assertEquals(user.getClid(), "+420 155 111 258");
        Assert.assertEquals(user.getIfStaticLogin(), true);
        Assert.assertEquals(user.getAllowRecordingInterruption(), true);
        Assert.assertEquals(user.getRecordAtCallStart(), "Do not record");

            Assert.assertEquals(user.getAlgo().getName(), "algoName");
            Assert.assertEquals(user.getAlgo().getTitle(), "algoTitle");

                Assert.assertEquals(user.getAlgo().getIntegration().getName(), "integrName");

                Assert.assertEquals(user.getAlgo().getIntegration().getType(), "AUTH");
                Assert.assertEquals(user.getAlgo().getIntegration().getTitle(), "integrTitle");
                Assert.assertEquals(user.getAlgo().getIntegration().getDescription(), "integrDesc");

                Assert.assertEquals(user.getAlgo().getIntegration().getAuthInfo(), "integrInfo");
                Assert.assertEquals(user.getAlgo().getIntegration().getImageName(), "integrImageName");
                Assert.assertEquals(user.getAlgo().getIntegration().getIcon(), "integrIconName");
                Assert.assertEquals(user.getAlgo().getIntegration().getActive(), false);
                Assert.assertEquals(user.getAlgo().getIntegration().getAuth(), false);
                Assert.assertEquals(user.getAlgo().getIntegration().getConfig(), false);

                Assert.assertEquals(user.getAlgo().getIntegration().getError(), "{\"error\":\"111\"}");


            Assert.assertEquals(user.getAlgo().getActive(), true);
            Assert.assertEquals(user.getAlgo().getAuth(), "{\"auth\":\"algoAuth\"}");
            Assert.assertEquals(user.getAlgo().getConfig(), "{\"config\":\"algoConfig\"}");

        Assert.assertEquals(user.getEmail(), "name@server.cz");
        Assert.assertEquals(user.getEmailAuth(), "name2@server2.com");
        Assert.assertEquals(user.getIcon(), "iconAsString");

            Assert.assertEquals(user.getOptions().getSign(), "<p>me</p>");
            Assert.assertEquals(user.getOptions().getTarget(), "none");

        Assert.assertEquals(user.getBackofficeUser(), true);
        Assert.assertEquals(user.getForwardingNumber(), "+420 123 456 789");
        Assert.assertEquals(user.getDeactivated(), false);
        Assert.assertEquals(user.getDeleted(), false);
    }

}