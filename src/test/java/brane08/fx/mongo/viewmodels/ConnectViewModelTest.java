package brane08.fx.mongo.viewmodels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import brane08.fx.mongo.config.Connection;
import brane08.fx.mongo.services.ConfigService;
import brane08.fx.mongo.services.MongoService;
import brane08.fx.mongo.support.FxToolkitSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConnectViewModelTest {

    @Mock
    ConfigService configService;

    @Mock
    MongoService mongoService;

    ConnectViewModel viewModel;

    @BeforeAll
    static void initFx() {
        FxToolkitSupport.ensureStarted();
    }

    @BeforeEach
    void setUp() {
        viewModel = new ConnectViewModel(configService, mongoService);
    }

    @Test
    void loadConnection_copiesFieldsFromConfigService() {
        when(configService.getConnectionData("prod"))
                .thenReturn(new Connection("prod", "cluster.example.net", "appuser", "s3cret", "admin"));

        viewModel.loadConnection("prod");

        assertEquals("prod", viewModel.nameProperty().get());
        assertEquals("cluster.example.net", viewModel.connectionStringProperty().get());
        assertEquals("appuser", viewModel.userNameProperty().get());
        assertEquals("s3cret", viewModel.passwordProperty().get());
        assertEquals("admin", viewModel.databaseProperty().get());
    }

    @Test
    void saveConnection_delegatesPropertyValuesToConfigService() {
        viewModel.nameProperty().set("prod");
        viewModel.connectionStringProperty().set("cluster.example.net");
        viewModel.userNameProperty().set("appuser");
        viewModel.passwordProperty().set("s3cret");
        viewModel.databaseProperty().set("admin");

        viewModel.saveConnection();

        verify(configService).save("prod", "cluster.example.net", "appuser", "s3cret", "admin");
    }

    @Test
    void testConnection_setsPropertyFromMongoService() {
        viewModel.connectionStringProperty().set("cluster.example.net");
        viewModel.userNameProperty().set("appuser");
        viewModel.passwordProperty().set("s3cret");
        viewModel.databaseProperty().set("admin");
        when(mongoService.testConnection("cluster.example.net", "appuser", "s3cret", "admin"))
                .thenReturn(true);

        viewModel.testConnection();

        assertTrue(viewModel.testConnectionProperty().get());
        verify(mongoService).testConnection("cluster.example.net", "appuser", "s3cret", "admin");
    }
}
