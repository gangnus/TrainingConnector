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
class DaktelaConnectorTest {
    @Mock
    DaktelaConnection mockConnection;
    @InjectMocks
    DaktelaConnector connector;
    @Captor
    ArgumentCaptor<User> userCaptor;

    @Test
    void create() {
    }

    static Set<AttributeDelta> createDeltaSetFromMap(Map<String, Object> map){
        Set<AttributeDelta> set = new HashSet<>();
        map.forEach((name,value) -> {
           set.add(AttributeDeltaBuilder.build(name,Arrays.asList(value)));
        });
        return set;
    }

    static void assertUserCaptorFields(Map<String, Object> map, User user, String uid){
        assertEquals(uid, user.getName());
        assertEquals(map.get(Name.NAME), user.getTitle());
        assertEquals(map.get(DaktelaSchema.ATTR_ALIAS), user.getAlias());
        assertEquals(map.get(DaktelaSchema.ATTR_DESCRIPTION), user.getDescription());
        assertEquals(map.get(DaktelaSchema.ATTR_PASSWORD), user.getPassword());
        assertEquals(map.get(DaktelaSchema.ATTR_CLID), user.getClid());
        assertEquals(map.get(DaktelaSchema.ATTR_EMAIL), user.getEmail());
    }

    /** -------------- test a correct update of a user
     *
      */
    @Test
    public void testUpdateDeltaAllBaseFieldsCorrect() {

        DaktelaConfiguration configuration = new DaktelaConfiguration();
        connector.init(configuration);
        DaktelaConnection.setINST(mockConnection);

        Map<String, Object> map = new HashMap<>(){{
            put(Name.NAME,"Professor User");
            put(DaktelaSchema.ATTR_ALIAS, "alias Prof");
            put(DaktelaSchema.ATTR_DESCRIPTION, "test description for a prof");
            put(DaktelaSchema.ATTR_PASSWORD, "pwdPWD");
            put(DaktelaSchema.ATTR_CLID, "+420 000 111 222");
            put(DaktelaSchema.ATTR_EMAIL, "prof@uni.com");
        }};

        Set<AttributeDelta> set =createDeltaSetFromMap(map);

        doNothing().when(mockConnection).updateRecord(userCaptor.capture());

        connector.updateDelta(new ObjectClass("User"), new Uid("user1"), set, null);

        verify(mockConnection).updateRecord(userCaptor.capture());
        assertUserCaptorFields(map, userCaptor.getValue(), "user1");

    }

    /** -------------- test a correct update of aт empty user
     *
     */
    @Test
    public void testUpdateDeltaNoBaseFieldsCorrect() {

        DaktelaConfiguration configuration = new DaktelaConfiguration();
        connector.init(configuration);
        DaktelaConnection.setINST(mockConnection);

        Set<AttributeDelta> set =createDeltaSetFromMap(new HashMap<>());

        connector.updateDelta(new ObjectClass("User"), new Uid("user1"), set, null);

        verify(mockConnection, never()).updateRecord(any());

    }

    /** -------------- test attempt to change a uid - must fail
     *
     */
    @Test
    public void testUpdateDeltaAttemptToChangeUid() {

        DaktelaConfiguration configuration = new DaktelaConfiguration();
        connector.init(configuration);
        DaktelaConnection.setINST(mockConnection);
        Map<String, Object> map = new HashMap<>(){{
            put(Uid.NAME,"NewUser");
            put(Name.NAME,"Professor User");

        }};
        ConnectorException thrown = Assertions.assertThrows(ConnectorException.class, () -> {
            Set<AttributeDelta> set =createDeltaSetFromMap(map);
            connector.updateDelta(new ObjectClass("User"), new Uid("user1"), set, null);
        });




    }
}