package cz.ami.connector.daktela;

import cz.ami.connector.daktela.model.User;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DaktelaConnectorFunctionalTest {

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

        DaktelaConfiguration configuration = new DaktelaConfiguration();
        DaktelaConnector connector = new DaktelaConnector();
        connector.init(configuration);
        configuration.setServiceAddress("http://localhost:8001");

        ServerForUnitTesting server = new ServerForUnitTesting();
        server.launch();
        Map<String, Object> map = new HashMap<>(){{
            put(Name.NAME,"Professor User");
            put(DaktelaSchema.ATTR_ALIAS, "alias Prof");

        }};

        Set<Attribute> set =createAttributeSetFromMap(map);

        connector.update(new ObjectClass("User"), new Uid("user1"), set, null);
        server.stop();
    }

    /** -------------- Functional test update - a failed update of a user
     *
     */
    @Test
    public void funcTestUpdateSomeBaseFieldsFailed() throws Exception {

        DaktelaConfiguration configuration = new DaktelaConfiguration();
        DaktelaConnector connector = new DaktelaConnector();
        connector.init(configuration);
        configuration.setServiceAddress("http://localhost:8001");

        ServerForUnitTesting server = new ServerForUnitTesting();
        server.launch();
        Map<String, Object> map = new HashMap<>(){{
            put(Name.NAME,"Professor User");
            put(DaktelaSchema.ATTR_ALIAS, "alias Prof");

        }};

        Set<Attribute> set =createAttributeSetFromMap(map);

        ConnectorException thrown = Assertions.assertThrows(ConnectorException.class, () -> {
            connector.update(new ObjectClass("User"), new Uid("user2"), set, null);
            server.stop();
        });
        server.stop();
    }

}