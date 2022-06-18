package cz.ami.connector.daktela;

import com.google.gson.Gson;
import cz.ami.connector.daktela.model.User;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {
    @Test
    public void TestUserStructure() throws IOException {
        String jsonString = TestResourceFilesReader.readStringContentFromFile("novak.json");
        Gson gson = new Gson();
        User user = gson.fromJson(jsonString, User.class);
        assertEquals( "NJ",user.getAlias());
        assertEquals( "Novak",user.getName());
        assertEquals( "Novák Jan",user.getTitle());

        assertEquals( "admin",user.getRole().get(0).getName());
        assertEquals( "Administrator",user.getRole().get(0).getTitle());
        assertEquals( "rights to change logins",user.getRole().get(0).getDescription());
        assertEquals( "",user.getRole().get(0).getShortcuts());
        assertEquals( "",user.getRole().get(0).getOptions());

        assertEquals( "mg",user.getRole().get(1).getName());
        assertEquals( "Manager",user.getRole().get(1).getTitle());
        assertEquals( "rights to sort logins",user.getRole().get(1).getDescription());
        assertEquals( "{\"ctrl+a\":\"command1\"}",user.getRole().get(1).getShortcuts());
        assertEquals( "{\"o32\":\"high\"}",user.getRole().get(1).getOptions());

        assertEquals( "newbie",user.getProfile().get(0).getName());
        assertEquals( "newbie",user.getProfile().get(0).getTitle());
        assertEquals( "newbie",user.getProfile().get(0).getDescription());
        assertEquals( 1,user.getProfile().get(0).getMaxActivities());
        assertEquals( 2,user.getProfile().get(0).getMaxOutRecords());
        assertEquals( false,user.getProfile().get(0).getDeleteMissedActivity());
        assertEquals( false,user.getProfile().get(0).getNoQueueCallsAllowed());
        assertEquals( "None",user.getProfile().get(0).getCanTransferCall());
        assertEquals( "",user.getProfile().get(0).getOptions());
        assertEquals( "",user.getProfile().get(0).getCustomViews());

        assertEquals( "oldie",user.getProfile().get(1).getName());
        assertEquals( "oldie",user.getProfile().get(1).getTitle());
        assertEquals( "oldie",user.getProfile().get(1).getDescription());
        assertEquals( 3,user.getProfile().get(1).getMaxActivities());
        assertEquals( 10,user.getProfile().get(1).getMaxOutRecords());
        assertEquals( true,user.getProfile().get(1).getDeleteMissedActivity());
        assertEquals( true,user.getProfile().get(1).getNoQueueCallsAllowed());
        assertEquals( "Only assisted transfer",user.getProfile().get(1).getCanTransferCall());
        assertEquals( "{\"o1\":\"+\"}",user.getProfile().get(1).getOptions());
        assertEquals( "{\"fullscreen\":\"forbidden\"}",user.getProfile().get(1).getCustomViews());

        assertEquals( 25.3f,user.getNpsScore());
        assertEquals( "the most problem user",user.getDescription());
        assertEquals( "don't touch!",user.getCallSteeringDescription());
        assertEquals( "pwd",user.getPassword());
        assertEquals( "ext",user.getExtension());

        assertEquals( "read",user.getAcl().get(0).getName());
        assertEquals( "reading",user.getAcl().get(0).getTitle());
        assertEquals( "300",user.getAcl().get(0).getTime());
        assertEquals( "",user.getAcl().get(0).getRules());

        assertEquals( "write",user.getAcl().get(1).getName());
        assertEquals( "writing",user.getAcl().get(1).getTitle());
        assertEquals( "600",user.getAcl().get(1).getTime());
        assertEquals( "",user.getAcl().get(1).getRules());


        assertEquals( "online",user.getExtState());
        assertEquals( "+420 155 111 258",user.getClid());
        assertEquals( true,user.getIfStaticLogin());
        assertEquals( true,user.getAllowRecordingInterruption());
        assertEquals( "Do not record",user.getRecordAtCallStart());

        assertEquals( "algoName",user.getAlgo().getName());
        assertEquals( "algoTitle",user.getAlgo().getTitle());

        assertEquals( "integrName",user.getAlgo().getIntegration().getName());

        assertEquals( "AUTH",user.getAlgo().getIntegration().getType());
        assertEquals( "integrTitle",user.getAlgo().getIntegration().getTitle());
        assertEquals( "integrDesc",user.getAlgo().getIntegration().getDescription());

        assertEquals( "integrInfo",user.getAlgo().getIntegration().getAuthInfo());
        assertEquals( "integrImageName",user.getAlgo().getIntegration().getImageName());
        assertEquals( "integrIconName",user.getAlgo().getIntegration().getIcon());
        assertEquals( false,user.getAlgo().getIntegration().getActive());
        assertEquals( false,user.getAlgo().getIntegration().getAuth());
        assertEquals( false,user.getAlgo().getIntegration().getConfig());

        assertEquals( "{\"error\":\"111\"}",user.getAlgo().getIntegration().getError());


        assertEquals( true,user.getAlgo().getActive());
        assertEquals( "{\"auth\":\"algoAuth\"}",user.getAlgo().getAuth());
        assertEquals( "{\"config\":\"algoConfig\"}",user.getAlgo().getConfig());

        assertEquals( "name@server.cz",user.getEmail());
        assertEquals( "name2@server2.com",user.getEmailAuth());
        assertEquals( "iconAsString",user.getIcon());

        assertEquals( "<p>me</p>",user.getOptions().getSign());
        assertEquals( "none",user.getOptions().getTarget());

        assertEquals( true,user.getBackofficeUser());
        assertEquals( "+420 123 456 789",user.getForwardingNumber());
        assertEquals( false,user.getDeactivated());
        assertEquals( false,user.getDeleted());
    }

}