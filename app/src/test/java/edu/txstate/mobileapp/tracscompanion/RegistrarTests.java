package edu.txstate.mobileapp.tracscompanion;

import android.test.mock.MockContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

import edu.txstate.mobileapp.tracscompanion.util.AppInstanceId;
import edu.txstate.mobileapp.tracscompanion.util.Registrar;

import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AppInstanceId.class})
public class RegistrarTests {
    private Registrar registration;
    private MockContext context;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(AppInstanceId.class);
        context = new MockContext();
        PowerMockito.when(AppInstanceId.getInstanceId(context)).thenReturn(UUID.randomUUID());
        registration = Registrar.getRegistrationInfo(context);
    }

    @Test
    public void twoCallsToRegistrarProduceSameUUID() {
        UUID firstCallUUID = UUID.fromString(Registrar.getRegistrationInfo(context).getJsonRegistration().get("token").getAsString());
        UUID secondCallUUID = UUID.fromString(Registrar.getRegistrationInfo(context).getJsonRegistration().get("token").getAsString());

        assertTrue(firstCallUUID.equals(secondCallUUID));
    }

    @Test
    public void registrarHasUserIdKey() {
        assertTrue(registration.getJsonRegistration().has("user_id"));
    }

    @Test
    public void registrarHasTokenKey() {
        assertTrue(registration.getJsonRegistration().has("token"));
    }

    @Test
    public void registrarHasAppIdKey() {
        assertTrue(registration.getJsonRegistration().has("app_id"));
    }

    @Test
    public void registrarHasBlacklistKey() {
        assertTrue(registration.getJsonRegistration().has("blacklist"));
    }

    @Test
    public void registrarHasPlatformKey() {
        assertTrue(registration.getJsonRegistration().has("platform"));
    }

    @Test
    public void registrarHasGlobalDisableKey() {
        assertTrue(registration.getJsonRegistration().has("global_disable"));
    }
}
