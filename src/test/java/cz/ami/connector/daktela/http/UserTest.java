package cz.ami.connector.daktela.http;
import com.google.gson.Gson;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;

public class UserTest {
    @Test
    public void TestReadThroughConnection() throws IOException, URISyntaxException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        String uriSource = "https://dddf6eef-93bb-45e8-9a27-572193940135.mock.pstmn.io";
        User user = User.read(client, 100, uriSource, "Novak");
        String jsonStringMust = new String(getClass().getClassLoader().getResourceAsStream("user1.json").readAllBytes());
        Gson gson = new Gson();
        String jsonStringGot = gson.toJson(user);
        //Assert.assertEquals(jsonStringGot, jsonStringMust);
    }
    @Test
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