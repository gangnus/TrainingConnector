package cz.ami.connector.daktela;

import cz.ami.connector.daktela.model.User;
import org.identityconnectors.framework.common.objects.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    @Test
    public void testUpdateDelta() {

        DaktelaConfiguration configuration = new DaktelaConfiguration();
        connector.init(configuration);
        DaktelaConnection.setINST(mockConnection);

        Set<AttributeDelta> set = new HashSet<>();
        AttributeDelta delta = AttributeDeltaBuilder.build(Name.NAME,Arrays.asList("Professor User"));
        set.add(delta);

        doNothing().when(mockConnection).updateRecord(userCaptor.capture());

        connector.updateDelta(new ObjectClass("User"), new Uid("user1"), set, null);

        verify(mockConnection).updateRecord(userCaptor.capture());
        assertEquals("user1", userCaptor.getValue().getName());
    }
}