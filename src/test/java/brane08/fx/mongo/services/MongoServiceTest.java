package brane08.fx.mongo.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import brane08.fx.mongo.config.Connection;
import com.mongodb.MongoException;
import com.mongodb.client.ListCollectionNamesIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.junit.jupiter.api.Test;

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
            mocked.when(() -> MongoClients.create(anyString())).thenReturn(client);
            assertTrue(new MongoService().testConnection("cluster.example.net", "user", "pass", "admin"));
            assertTrue(new MongoService()
                    .testConnection(new Connection("n", "cluster.example.net", "user", "pass", "admin")));
        }
    }

    @Test
    void testConnection_returnsFalseOnMongoException() {
        try (var mocked = mockStatic(MongoClients.class)) {
            mocked.when(() -> MongoClients.create(anyString())).thenThrow(new MongoException("boom"));
            assertFalse(new MongoService().testConnection("cluster.example.net", "user", "pass", "admin"));
        }
    }
}
