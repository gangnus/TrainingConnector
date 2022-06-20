package cz.ami.connector.daktela;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cz.ami.connector.daktela.model.User;
import cz.ami.connector.daktela.testserver.TSWithConstantResponses;
import cz.ami.connector.daktela.testserver.TSWithMemory;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.Uid;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static cz.ami.connector.daktela.ConnectorFunctionalTest.createAttributeSetFromMap;
import static cz.ami.connector.daktela.ConnectorUnitTest.assertUserFields;
import static cz.ami.connector.daktela.testserver.TSWithConstantResponses.createServerForTesting;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TSWithMemoryTest {
    static GsonBuilder builder = new GsonBuilder();
    static Gson gson = builder.serializeNulls().setPrettyPrinting().create();

    DaktelaConnector connector = ConnectorForTest.createTestDaktelaConnector();
    {
        connector.getConfiguration().setServiceAddress(TSWithMemory.TEST_SERVER_URI);
    }
    TSWithMemory serverWithMemory = TSWithMemory.createServerForTesting();

    public TSWithMemoryTest() throws Exception {
    }

    @Test
    public void changeItemByJsonStringText(){
        User user = new User("Janička","Jana Velká","the first to be tested","Cihla", "skaut", "100 200 300", "velkajana@volny.cz");
        User userChanged = TSWithMemory.changeItemByJsonString(user, "{\"description\"=\"the second to be tested\"}");
        assertEquals("Janička", userChanged.getName());
        assertEquals("the second to be tested", userChanged.getDescription());
        userChanged = TSWithMemory.changeItemByJsonString(user, "{\"description\"=null}");
        assertEquals("Jana Velká", userChanged.getTitle());
        assertNull(userChanged.getDescription());
        user.setAlias(null);
        user.setPassword(null);
        user.setTitle(null);
        userChanged = TSWithMemory.changeItemByJsonString(user, "{\"alias\"=\"new Alias\", \"password\"=null, \"description\"=null}");
        assertNull(userChanged.getTitle());
        assertNull(userChanged.getPassword());
        assertNull(userChanged.getDescription());
        assertEquals("new Alias", userChanged.getAlias());

    }
    /** -------------- Functional test update - a correct update of a user
     *
     */
    @Test
    public void addTwoUsers() throws Exception {
        assertNotNull(serverWithMemory, " test server before the test method");
        Map<String, Object> map1 = Map.of(
                Uid.NAME, "user1",
                Name.NAME,"Professor User",
                DaktelaSchema.ATTR_ALIAS, "alias Prof"

        );

        Set<Attribute> set =createAttributeSetFromMap(map1);

        Uid uid = connector.create(new ObjectClass("User"), set, null);

        assertEquals("user1", uid.getUidValue());
        // check request body
        assertNotNull(serverWithMemory, " test server after tested call ");
        String jsonString = serverWithMemory.getRequestBody();
        assertNotNull(jsonString, " response json string ");
        User userSent = gson.fromJson(jsonString, User.class);
        assertUserFields(map1, userSent, "user1", " some base user fields ");

        Map<String, Object> map2 = Map.of(
                Uid.NAME, "user2",
                Name.NAME,"Docent User",
                DaktelaSchema.ATTR_ALIAS, "alias Docent"

        );

        Set<Attribute> set2 =createAttributeSetFromMap(map2);

        uid = connector.create(new ObjectClass("User"), set2, null);
        assertEquals("user2", uid.getUidValue());
        Map<String,User> users = TSWithMemory.loadUserMemory();
        assertTrue(users.containsKey("user1"),"check for user1 key");
        assertTrue(users.containsKey("user2"),"check for user2 key");
        assertUserFields(map1, users.get("user1"),"user1", " user1 fields ");
        assertUserFields(map2, users.get("user2"),"user2", " user2 fields ");

    }
    static void assertUserFields(Map<String, Object> map, User user, String uid, String message){
        assertEquals(uid, user.getName(),message);
        assertEquals(map.get(Name.NAME), user.getTitle(),message);
        assertEquals(map.get(DaktelaSchema.ATTR_ALIAS), user.getAlias(),message);
        assertEquals(map.get(DaktelaSchema.ATTR_DESCRIPTION), user.getDescription(),message);
        assertEquals(map.get(DaktelaSchema.ATTR_PASSWORD), user.getPassword(),message);
        assertEquals(map.get(DaktelaSchema.ATTR_CLID), user.getClid(),message);
        assertEquals(map.get(DaktelaSchema.ATTR_EMAIL), user.getEmail(),message);
    }
}
