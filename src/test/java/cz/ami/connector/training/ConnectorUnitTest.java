package cz.ami.connector.training;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import cz.ami.connector.training.data.Users;
import cz.ami.connector.training.model.User;
import cz.ami.connector.training.tools.LogMessages;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ConnectorUnitTest {
    private static final Trace LOG = TraceManager.getTrace(ConnectorUnitTest.class);

    TrainingConnector connector = new TrainingConnector();

    static Set<Attribute> createAttributeSetFromMap(Map<String, Object> map){
        Set<Attribute> set = new HashSet<>();
        map.forEach((name,value) -> {
            set.add(AttributeBuilder.build(name,Arrays.asList(value)));
        });
        return set;
    }
    static void assertUserFields(Map<String, Object> map, User user, String uid, String message){
        Assertions.assertEquals(uid, user.getName(),message);
        Assertions.assertEquals((String)map.get(ConnectorSchema.ATTR_PASSWORD), user.getPassword(),message);
        Assertions.assertEquals((String)map.get(ConnectorSchema.ATTR_FULLNAME), user.getFullName(),message);
    }



    /** -------------- test update - a correct update of a user
     *
     */
    @Test
    public void testAllBaseFieldsCorrect() throws Exception {

        TrainingConfiguration configuration = new TrainingConfiguration();
        connector.init(configuration);
        Users.clearAll();

        Map<String, Object> map = Map.of(
                Uid.NAME,"user1",
                Name.NAME, "user1",
                ConnectorSchema.ATTR_FULLNAME, "Professor User",
                ConnectorSchema.ATTR_PASSWORD, "pwdPWD"
        );

        Set<Attribute> set = createAttributeSetFromMap(map);
        Uid uidCreated = connector.create(new ObjectClass("User"), set, null);
        assertUserFields(map, Users.read("user1"), "user1", "Creation user fields ");
        Assertions.assertEquals("user1", uidCreated.getUidValue(), "Creation assert returned uid value");

        map = Map.of(
                ConnectorSchema.ATTR_FULLNAME, "Docent User",
                ConnectorSchema.ATTR_PASSWORD, "pwdpwd123"
        );
        set = createAttributeSetFromMap(map);
        Uid returnedUid = connector.update(new ObjectClass("User"), new Uid("user1"), set, null);

        assertUserFields(map, Users.read("user1"), "user1", "Updating user fields ");
        Assertions.assertEquals("user1", returnedUid.getUidValue(), "Updating check returned uid value");

        connector.delete(new ObjectClass("User"), new Uid("user1"), null);
        Assertions.assertEquals(0, Users.getAll().size(),"deleting failed");

    }

    /** -------------- test update - an empty update should fail
     *
     */
    @Test
    public void testUpdateNoFieldsShouldFail() {

        TrainingConfiguration configuration = new TrainingConfiguration();
        connector.init(configuration);
        Users.clearAll();

        Set<Attribute> set =createAttributeSetFromMap(new HashMap<>(){{
            put(Uid.NAME,"user1");
        }});
        Uid uidCreated = connector.create(new ObjectClass("User"), set, null);

        ConnectorException exception = Assertions.assertThrows(ConnectorException.class, () -> {
            connector.update(new ObjectClass("User"), uidCreated, set, null);
        });

        Assertions.assertTrue(exception.getMessage().contains(LogMessages.ATTRIBUTES_NOT_PROVIDED_OR_EMPTY), "wrong exception message = " + exception.getMessage());

    }

    /** -------------- test update - an update for nonexistent user should fail
     *
     */
    @Test
    public void testUpdateNonexistentShouldFail() {

        TrainingConfiguration configuration = new TrainingConfiguration();
        connector.init(configuration);
        Users.clearAll();

        Set<Attribute> set =createAttributeSetFromMap(new HashMap<>(){{
            put(Uid.NAME,"user1");
            put( ConnectorSchema.ATTR_PASSWORD,"111");

        }});
        ConnectorException exception = Assertions.assertThrows(ConnectorException.class, () -> {
            connector.update(new ObjectClass("User"), new Uid("user1"), set, null);
        });
        Assertions.assertTrue(exception.getMessage().contains(LogMessages.USER_UPDATING_FAILED), "wrong exception message = " + exception.getMessage());

    }

    /** -------------- test creation - a user with uid only
     *
     */
    @Test
    public void testCreateUserWithUidOnly() {

        TrainingConfiguration configuration = new TrainingConfiguration();
        connector.init(configuration);
        Users.clearAll();

        Set<Attribute> set =createAttributeSetFromMap(new HashMap<>(){{
            put(Uid.NAME,"emptyUser");
        }});
        Uid uid = connector.create(new ObjectClass("User"), set, null);
        Assertions.assertEquals("emptyUser", uid.getUidValue(), "creation - a user with uid only");


    }

    /** -------------- test update - attempt to change a uid - must fail
     *
     */
    @Test
    public void testUpdateAttemptToChangeUidMustFail() {

        TrainingConfiguration configuration = new TrainingConfiguration();
        connector.init(configuration);
        Users.clearAll();

        Map<String, Object> map = Map.of(
                Uid.NAME, "user1",
                Name.NAME, "user1",
                ConnectorSchema.ATTR_FULLNAME, "Professor User",
                ConnectorSchema.ATTR_PASSWORD, "pwdPWD"
        );

        Set<Attribute> set = createAttributeSetFromMap(map);
        Uid uidCreated = connector.create(new ObjectClass("User"), set, null);

        map = Map.of(
                Uid.NAME, "NewUser",
                Name.NAME, "Professor User"

        );
        Set<Attribute> changedSet = createAttributeSetFromMap(map);
        ConnectorException exception = Assertions.assertThrows(ConnectorException.class, () -> {
            connector.update(new ObjectClass("User"), new Uid("user1"), changedSet, null);
        });
        Assertions.assertTrue(exception.getMessage().contains(LogMessages.CONTRADICTIONS_IN_UIDS_AND_OR_NAMES), "wrong exception message = " + exception.getMessage());

    }
    /** -------------- test update - change user giving excessive uid info
     *
     */
    @Test
    public void testUpdateWithExcessiveUid() throws Exception {

        TrainingConfiguration configuration = new TrainingConfiguration();
        connector.init(configuration);
        Users.clearAll();

        Map<String, Object> map = Map.of(
                Uid.NAME,"user1",
                Name.NAME, "user1",
                ConnectorSchema.ATTR_FULLNAME, "Professor User",
                ConnectorSchema.ATTR_PASSWORD, "pwdPWD"
        );

        Set<Attribute> set = createAttributeSetFromMap(map);
        Uid uidCreated = connector.create(new ObjectClass("User"), set, null);

        Map<String, Object> map2 = Map.of(
                Uid.NAME,"user1",
                Name.NAME, "user1",
                ConnectorSchema.ATTR_FULLNAME, "Docent User",
                ConnectorSchema.ATTR_PASSWORD, "pwdpwd123"
        );
        Set<Attribute>  changedSet =createAttributeSetFromMap(map2);

        connector.update(new ObjectClass("User"), new Uid("user1"), changedSet, null);
        assertUserFields(map2, Users.read("user1"), "user1", "Updating user fields ");

    }
    /** -------------- test create - attempt to create twice - must fail
     *
     */
    @Test
    public void testCreateAttemptTwiceMustFail() {
        TrainingConfiguration configuration = new TrainingConfiguration();
        connector.init(configuration);
        Users.clearAll();

        Map<String, Object> map = Map.of(
                Uid.NAME,"user1",
                Name.NAME, "user1",
                ConnectorSchema.ATTR_FULLNAME, "Professor User",
                ConnectorSchema.ATTR_PASSWORD, "pwdPWD"
        );

        Set<Attribute> set = createAttributeSetFromMap(map);
        Uid uidCreated = connector.create(new ObjectClass("User"), set, null);

        Map<String, Object> map2 = Map.of(
                Uid.NAME,"user1",
                Name.NAME, "user1",
                ConnectorSchema.ATTR_FULLNAME, "Docent User",
                ConnectorSchema.ATTR_PASSWORD, "pwdpwd123"
        );
        Set<Attribute> set2 = createAttributeSetFromMap(map2);
        ConnectorException exception = Assertions.assertThrows(ConnectorException.class, () -> {
            connector.create(new ObjectClass("User"), set2, null);
        });
        Assertions.assertTrue(exception.getMessage().contains(LogMessages.USER_CREATION_FAILED), "wrong exception message = " + exception.getMessage());

    }

}