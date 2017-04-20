package edu.txstate.mobileapp.tracscompanion;

import com.google.gson.JsonObject;

import org.junit.Before;
import org.junit.Test;

import edu.txstate.mobileapp.tracscompanion.notifications.DispatchNotification;

import static junit.framework.Assert.assertTrue;

public class DispatchNotificationTests {
    private DispatchNotification notification;
    private JsonObject mockAnnouncement;
    private String type = "mockAnnouncement";
    private String provider_id = "tracs";
    private String object_id = "831342dd-fdb6-4878-8b3c-1d29ecb06a14:main:aa4f8f85-a645-4766-bc91-1a1c7bef93df";
    private String site_id = "831342dd-fdb6-4878-8b3c-1d29ecb06a14";
    private String tool_id = "b85ac198-1610-424f-9c2e-df4408b8c334";

    @Before
    public void setUp() {
        JsonObject keys = new JsonObject();
        JsonObject other_keys = new JsonObject();
        mockAnnouncement = new JsonObject();
        keys.addProperty("type", type);
        keys.addProperty("provider_id", provider_id);
        keys.addProperty("object_id", object_id);

        other_keys.addProperty("site_id", site_id);
        other_keys.addProperty("tool_id", tool_id);

        mockAnnouncement.addProperty("id", "testId");
        mockAnnouncement.addProperty("seen", "testId");
        mockAnnouncement.addProperty("read", "testId");
        mockAnnouncement.add("keys", keys);
        mockAnnouncement.add("other_keys", other_keys);

        notification = new DispatchNotification(mockAnnouncement);
    }

    @Test
    public void typeIsReadCorrectlyFromNestedObject() {
        assertTrue(type.equals(notification.getType()));
    }

    @Test
    public void entityIdIsStoredAndReadCorrectly() {
        assertTrue(object_id.equals(notification.getObjectId()));
    }

    @Test
    public void providerIdIsStoredAndReadCorrectly() {
        assertTrue(provider_id.equals(notification.getProviderId()));
    }

    @Test
    public void missingKeysWillSetValuesToNull() {
        mockAnnouncement.remove("keys");
        notification = new DispatchNotification(mockAnnouncement);
        assertTrue(notification.getObjectId() == null);
    }

    @Test
    public void siteIdIsStoredAndReadCorrectly() {
        assertTrue(site_id.equals(notification.getSiteId()));
    }
}
