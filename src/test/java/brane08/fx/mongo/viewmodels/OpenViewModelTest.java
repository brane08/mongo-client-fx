package brane08.fx.mongo.viewmodels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import brane08.fx.mongo.services.ConfigService;
import brane08.fx.mongo.support.FxToolkitSupport;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpenViewModelTest {

    @Mock
    ConfigService configService;

    @BeforeAll
    static void initFx() {
        FxToolkitSupport.ensureStarted();
    }

    @Test
    void getNames_wrapsConfigServiceNames() {
        when(configService.getConnectionNames()).thenReturn(List.of("prod", "staging"));

        var names = new OpenViewModel(configService).getNames();

        assertEquals(List.of("prod", "staging"), names);
    }
}
