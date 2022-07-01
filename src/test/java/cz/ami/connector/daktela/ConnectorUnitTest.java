package cz.ami.connector.daktela;

import cz.ami.connector.daktela.model.User;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectorUnitTest {
    @Mock
    DaktelaConnection mockConnection;
    @InjectMocks
    DaktelaConnector connector;
    @Captor
    ArgumentCaptor<User> userCaptor;

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
    static void assertUserFields(Map<String, Object> map, User user, String uid, String message){
        assertEquals(uid, user.getName(),message);
        assertEquals(map.get(Name.NAME), user.getTitle(),message);
        assertEquals(map.get(DaktelaSchema.ATTR_ALIAS), user.getAlias(),message);
        assertEquals(map.get(DaktelaSchema.ATTR_DESCRIPTION), user.getDescription(),message);
        assertEquals(map.get(DaktelaSchema.ATTR_PASSWORD), user.getPassword(),message);
        assertEquals(map.get(DaktelaSchema.ATTR_CLID), user.getClid(),message);
        assertEquals(map.get(DaktelaSchema.ATTR_EMAIL), user.getEmail(),message);
    }

    /** -------------- test a correct creation of a user
     *
     */
    @Test
    public void testCreateAllBaseFieldsCorrect() {

        DaktelaConfiguration configuration = new DaktelaConfiguration();
        connector.init(configuration);
        connector.setConnection(mockConnection);
        Map<String, Object> map = Map.of(
            Uid.NAME,"user1",
            Name.NAME, "Professor User",
            DaktelaSchema.ATTR_ALIAS, "alias Prof",
            DaktelaSchema.ATTR_DESCRIPTION, "test description for a prof",
            DaktelaSchema.ATTR_PASSWORD, "pwdPWD",
            DaktelaSchema.ATTR_CLID, "+420 000 111 222",
            DaktelaSchema.ATTR_EMAIL, "prof@uni.com"
        );

        Set<Attribute> set = createAttributeSetFromMap(map);

        doNothing().when(mockConnection).createRecord(userCaptor.capture());

        Uid uidCreated = connector.create(new ObjectClass("User"), set, null);

        verify(mockConnection).createRecord(userCaptor.capture());
        // assert user fields
        assertUserFields(map, userCaptor.getValue(), "user1", " user fields ");

        assertEquals("user1", uidCreated.getUidValue(), "assert returned value");
    }

    /** -------------- test updateDelta - a correct update of a user
     *
      */
    @Test
    public void testUpdateDeltaAllBaseFieldsCorrect() {

        DaktelaConfiguration configuration = new DaktelaConfiguration();
        connector.init(configuration);
        connector.setConnection(mockConnection);

        Map<String, Object> map = Map.of(
            Name.NAME,"Professor User",
            DaktelaSchema.ATTR_ALIAS, "alias Prof",
            DaktelaSchema.ATTR_DESCRIPTION, "test description for a prof",
            DaktelaSchema.ATTR_PASSWORD, "pwdPWD",
            DaktelaSchema.ATTR_CLID, "+420 000 111 222",
            DaktelaSchema.ATTR_EMAIL, "prof@uni.com"
        );

        Set<AttributeDelta> set =createDeltaSetFromMap(map);

        //doNothing().when(mockConnection).updateRecord(userCaptor.capture());

        //connector.updateDelta(new ObjectClass("User"), new Uid("user1"), set, null);

        //verify(mockConnection).updateRecord(userCaptor.capture());
        //assertUserFields(map, userCaptor.getValue(), "user1", " user fields ");

    }

    /** -------------- test updateDelta - a correct update of aт empty user
     *
     */
    @Test
    public void testUpdateDeltaNoBaseFieldsCorrect() {

        DaktelaConfiguration configuration = new DaktelaConfiguration();
        connector.init(configuration);
        connector.setConnection(mockConnection);

        Set<AttributeDelta> set =createDeltaSetFromMap(new HashMap<>());

        //connector.updateDelta(new ObjectClass("User"), new Uid("user1"), set, null);

        //erify(mockConnection, never()).updateRecord(any());

    }

    /** -------------- test updateDelta - attempt to change a uid - must fail
     *
     */
    @Test
    public void testUpdateDeltaAttemptToChangeUid() {

        DaktelaConfiguration configuration = new DaktelaConfiguration();
        connector.init(configuration);
        connector.setConnection(mockConnection);
        Map<String, Object> map = Map.of(
            Uid.NAME, "NewUser",
            Name.NAME, "Professor User"

        );
        /*ConnectorException thrown = Assertions.assertThrows(ConnectorException.class, () -> {
            Set<AttributeDelta> set = createDeltaSetFromMap(map);
            connector.updateDelta(new ObjectClass("User"), new Uid("user1"), set, null);
        });*/
    }
    /** -------------- test update - a correct update of a user
     *
     */
    @Test
    public void testUpdateAllBaseFieldsCorrect() {

        DaktelaConfiguration configuration = new DaktelaConfiguration();
        connector.init(configuration);
        connector.setConnection(mockConnection);

        Map<String, Object> map = Map.of(
            Name.NAME,"Professor User",
            DaktelaSchema.ATTR_ALIAS, "alias Prof",
            DaktelaSchema.ATTR_DESCRIPTION, "test description for a prof",
            DaktelaSchema.ATTR_PASSWORD, "pwdPWD",
            DaktelaSchema.ATTR_CLID, "+420 000 111 222",
            DaktelaSchema.ATTR_EMAIL, "prof@uni.com"
        );

        Set<Attribute> set =createAttributeSetFromMap(map);

        doNothing().when(mockConnection).updateRecord(userCaptor.capture());

        Uid returnedUid = connector.update(new ObjectClass("User"), new Uid("user1"), set, null);

        verify(mockConnection).updateRecord(userCaptor.capture());
        assertUserFields(map, userCaptor.getValue(), "user1", " user fields ");
        assertEquals("user1", returnedUid.getUidValue(), "check returned uid");
    }

    /** -------------- test update - an empty update should fail
     *
     */
    @Test
    public void testUpdateNoFieldsShouldFail() {

        DaktelaConfiguration configuration = new DaktelaConfiguration();
        connector.init(configuration);
        connector.setConnection(mockConnection);

        Set<Attribute> set =createAttributeSetFromMap(new HashMap<>(){{
            put(Uid.NAME,"bad_user");
        }});
        Assertions.assertThrows(ConnectorException.class, () -> {
            connector.update(new ObjectClass("User"), new Uid("user1"), set, null);
        });

    }

    /** -------------- test update - attempt to change a uid - must fail
     *
     */
    @Test
    public void testUpdateAttemptToChangeUid() {

        DaktelaConfiguration configuration = new DaktelaConfiguration();
        connector.init(configuration);
        connector.setConnection(mockConnection);
        Map<String, Object> map = Map.of(
            Uid.NAME,"NewUser",
            Name.NAME,"Professor User"

        );
        ConnectorException thrown = Assertions.assertThrows(ConnectorException.class, () -> {
            Set<Attribute> set =createAttributeSetFromMap(map);
            connector.update(new ObjectClass("User"), new Uid("user1"), set, null);
        });



    }


}