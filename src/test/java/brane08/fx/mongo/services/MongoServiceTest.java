package brane08.fx.mongo.services;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import brane08.fx.mongo.config.Connection;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.ListCollectionNamesIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MongoServiceTest {

    @Test
    void execute_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> new MongoService().execute());
    }

    @Test
    void testConnection_returnsTrueWhenClientListsCollections() {
        var client = mock(MongoClient.class);
        var database = mock(MongoDatabase.class);
        var names = mock(ListCollectionNamesIterable.class);
        when(client.getDatabase("admin")).thenReturn(database);
        when(database.listCollectionNames()).thenReturn(names);
        when(names.spliterator()).thenReturn(java.util.List.of("a", "b").spliterator());

        try (var mocked = mockStatic(MongoClients.class)) {
            mocked.when(() -> MongoClients.create(any(MongoClientSettings.class))).thenReturn(client);
            assertTrue(new MongoService().testConnection("cluster.example.net", "user", "pass", "admin"));
            assertTrue(new MongoService()
                    .testConnection(new Connection("n", "cluster.example.net", "user", "pass", "admin")));
        }
        verify(client, times(2)).close();
    }

    @Test
    void testConnection_returnsFalseOnMongoException() {
        try (var mocked = mockStatic(MongoClients.class)) {
            mocked.when(() -> MongoClients.create(any(MongoClientSettings.class)))
                    .thenThrow(new MongoException("boom"));
            assertFalse(new MongoService().testConnection("cluster.example.net", "user", "pass", "admin"));
        }
    }

    @Test
    void testConnection_specialCharsInPassword_doNotThrow_andReachCredential() {
        var client = mock(MongoClient.class);
        var database = mock(MongoDatabase.class);
        var names = mock(ListCollectionNamesIterable.class);
        when(client.getDatabase("admin")).thenReturn(database);
        when(database.listCollectionNames()).thenReturn(names);
        when(names.spliterator()).thenReturn(java.util.List.<String>of().spliterator());
        var captor = ArgumentCaptor.forClass(MongoClientSettings.class);

        try (var mocked = mockStatic(MongoClients.class)) {
            mocked.when(() -> MongoClients.create(captor.capture())).thenReturn(client);
            assertTrue(new MongoService().testConnection("cluster.example.net", "us@er", "p@ss/w:rd%", "admin"));
        }

        var credential = captor.getValue().getCredential();
        assertEquals("us@er", credential.getUserName());
        assertEquals("admin", credential.getSource());
        assertArrayEquals("p@ss/w:rd%".toCharArray(), credential.getPassword());
    }

    @Test
    void testConnection_malformedServers_returnsFalse() {
        assertFalse(new MongoService().testConnection("localhost:notaport", "user", "pass", "admin"));
    }

    @Test
    void uri_bareHost_usesSrv() {
        assertEquals("mongodb+srv://cluster.example.net/admin?authSource=admin",
                MongoService.uri("cluster.example.net", "admin"));
    }

    @Test
    void uri_hostWithPortOrList_usesPlainScheme() {
        assertEquals("mongodb://localhost:27017/db?authSource=admin", MongoService.uri("localhost:27017", "db"));
        assertEquals("mongodb://a:1,b:2/db?authSource=admin", MongoService.uri("a:1,b:2", "db"));
    }

    @Test
    void uri_explicitScheme_isKept() {
        assertEquals("mongodb+srv://c.example.net/db?authSource=admin",
                MongoService.uri("mongodb+srv://c.example.net/", "db"));
    }
}
