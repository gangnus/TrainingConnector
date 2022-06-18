package cz.ami.connector.daktela;

import com.evolveum.midpoint.util.logging.TraceManager;
import com.google.gson.Gson;
import cz.ami.connector.daktela.model.User;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static cz.ami.connector.daktela.ConnectorUnitTest.assertUserFields;
import static cz.ami.connector.daktela.ServerForTesting.createServerForTesting;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConnectorFunctionalTest {
    static private final Gson gson = new Gson();
    DaktelaConnector connector = ConnectorForTest.createTestDaktelaConnector();
    ServerForTesting server = createServerForTesting();

    ConnectorFunctionalTest() throws Exception {
    }

    static Set<AttributeDelta> createDeltaSetFromMap(Map<String, Object> map){
        Set<AttributeDelta> set = new HashSet<>();
        map.forEach((name,value) -> {
           set.add(AttributeDeltaBuilder.build(name,Arrays.asList(value)));
        });
        return set;
    }
    static Set<Attribute> createAttributeSetFromMap(Map<String, Object> map){
        Set<Attribute> set = new HashSet<>();
        map.forEach((name,value) -> {
            set.add(AttributeBuilder.build(name,Arrays.asList(value)));
        });
        return set;
    }

    /** -------------- Functional test update - a correct update of a user
     *
     */
    @Test
    public void funcTestUpdateSomeBaseFieldsCorrect() throws Exception {
        ServerForTesting server = createServerForTesting();
        Map<String, Object> map = Map.of(
            Name.NAME,"Professor User",
            DaktelaSchema.ATTR_ALIAS, "alias Prof"

        );

        Set<Attribute> set =createAttributeSetFromMap(map);

        connector.update(new ObjectClass("User"), new Uid("user1"), set, null);

        // check request body
        assertNotNull(server, " test server after tested call ");
        String jsonString = server.getRequestBody();
        assertNotNull(jsonString, " response json string ");
        User userSent = gson.fromJson(jsonString, User.class);
        assertUserFields(map, userSent, "user1", " some base user fields ");
    }

    /** -------------- Functional test update - a failed update of a user
     *
     *
     */
    @Test
    public void funcTestUpdateSomeBaseFieldsFailed() throws Exception {
        Map<String, Object> map = Map.of(
                Name.NAME,"Professor User",
                DaktelaSchema.ATTR_ALIAS, "alias Prof"

        );

        Set<Attribute> set =createAttributeSetFromMap(map);

        ConnectorException thrown = Assertions.assertThrows(ConnectorException.class, () -> {
            connector.update(new ObjectClass("User"), new Uid("user2"), set, null);
        },"An exception must be thrown, due to the wrong response status code");

    }

    /** -------------- Functional test update - a correct update of a user
     *
     */
    @Test
    public void funcTestCreateSomeBaseFieldsCorrect() throws Exception {
        Map<String, Object> map = Map.of(
                Uid.NAME, "user1",
                Name.NAME,"Professor User",
                DaktelaSchema.ATTR_ALIAS, "alias Prof"

        );

        Set<Attribute> set =createAttributeSetFromMap(map);

        Uid uid = connector.create(new ObjectClass("User"), set, null);

        assertEquals("user1", uid.getUidValue());
        // check request body
        assertNotNull(server, " test server after tested call ");
        String jsonString = server.getRequestBody();
        assertNotNull(jsonString, " response json string ");
        User userSent = gson.fromJson(jsonString, User.class);
        assertUserFields(map, userSent, "user1", " some base user fields ");
    }


}