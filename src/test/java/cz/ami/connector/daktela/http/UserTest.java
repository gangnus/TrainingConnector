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
        /*String jsonString = new String(getClass().getClassLoader().getResourceAsStream("user1.json").readAllBytes());
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
            Assert.assertEquals(user.getProfile().get(0).getMaxActivities(), "1");
            Assert.assertEquals(user.getProfile().get(0).getMaxOutRecords(), "2");
            Assert.assertEquals(user.getProfile().get(0).getDeleteMissedActivity(), "false");
            Assert.assertEquals(user.getProfile().get(0).getNoQueueCallsAllowed(), "false");
            Assert.assertEquals(user.getProfile().get(0).getCanTransferCall(), "None");
            Assert.assertEquals(user.getProfile().get(0).getOptions(), "");
            Assert.assertEquals(user.getProfile().get(0).getCustomViews(), "");

            Assert.assertEquals(user.getProfile().get(1).getName(), "oldie");
            Assert.assertEquals(user.getProfile().get(1).getTitle(), "oldie");
            Assert.assertEquals(user.getProfile().get(1).getDescription(), "oldie");
            Assert.assertEquals(user.getProfile().get(1).getMaxActivities(), "3");
            Assert.assertEquals(user.getProfile().get(1).getMaxOutRecords(), "10");
            Assert.assertEquals(user.getProfile().get(1).getDeleteMissedActivity(), "true");
            Assert.assertEquals(user.getProfile().get(1).getNoQueueCallsAllowed(), "true");
            Assert.assertEquals(user.getProfile().get(1).getCanTransferCall(), "Only assisted transfer");
            Assert.assertEquals(user.getProfile().get(1).getOptions(), "{\"o1\":\"+\"}");
            Assert.assertEquals(user.getProfile().get(1).getCustomViews(), "{\"fullscreen\":\"forbidden\"}");

        Assert.assertEquals(user.getNpsScore(), "25.3");
        Assert.assertEquals(user.getDescription(), "the most problem user");
        Assert.assertEquals(user.getCallSteeringDescription(), "don't touch!");
        Assert.assertEquals(user.getPassword(), "pwd");
        Assert.assertEquals(user.getExtension(), "ext");

            Assert.assertEquals(user.getAcl().getName(), "read");
            Assert.assertEquals(user.getAcl().getTitle(), "reading");
            Assert.assertEquals(user.getAcl().getTime(), "300");
            Assert.assertEquals(user.getAcl().getRules(), "");
/*
            Assert.assertEquals(user.getName(), "write");
                Assert.assertEquals(user.getTitle(), "writing");
                Assert.assertEquals(user.getTime(), "600");
                Assert.assertEquals(user.getRules(), "");
        }
  ],

        Assert.assertEquals(user.getExtension_state(), "online");
                Assert.assertEquals(user.getClid(), "+420 155 111 258");
                Assert.assertEquals(user.getStatic(), "true");
                Assert.assertEquals(user.getAllowRecordingInterruption(), "true");
                Assert.assertEquals(user.getRecordAtCallStart(), "Do not record");
                "algo": {
            Assert.assertEquals(user.getName(), "algoName");
                    Assert.assertEquals(user.getTitle(), "algoTitle");

                    "integration": {
                Assert.assertEquals(user.getName(), "integrName");

                        Assert.assertEquals(user.getType(), "AUTH");
                        Assert.assertEquals(user.getTitle(), "integrTitle");
                        Assert.assertEquals(user.getDescription(), "integrDesc");

                        Assert.assertEquals(user.getAuthInfo(), "integrInfo");
                        Assert.assertEquals(user.getImageName(), "integrImageName");
                        Assert.assertEquals(user.getIcon(), "integrIconName");
                        Assert.assertEquals(user.getActive(), "false");
                        Assert.assertEquals(user.getAuth(), "false");
                        Assert.assertEquals(user.getConfig(), "false");

                        Assert.assertEquals(user.getError(), "{\"error\":\"111\"}");
            },

            Assert.assertEquals(user.getActive(), "true");
                    Assert.assertEquals(user.getAuth(), "{\"auth\":\"algoAuth\"}");
                    Assert.assertEquals(user.getConfig(), "{\"config\":\"algoConfig\"}");
                    Assert.assertEquals(user.getError(), "{\"error\":\"1024\"}");
        },
        Assert.assertEquals(user.getEmail(), "name@server.cz");
                Assert.assertEquals(user.getEmailAuth(), "name2@server2.com");
                Assert.assertEquals(user.getIcon(), "iconAsString");
                Assert.assertEquals(user.getEmoji(), "user's emoji");
                "options":{
            Assert.assertEquals(user.getSign(), "<p>me</p>");
                    Assert.assertEquals(user.getTarget(), "none");
        },
        Assert.assertEquals(user.getBackoffice_user(), "true");
                Assert.assertEquals(user.getForwarding_number(), "+420 123 456 789");
                Assert.assertEquals(user.getDeactivated(), "false");
                Assert.assertEquals(user.getDeleted(), "false");*/
    }

}