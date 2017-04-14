package edu.txstate.mobileapp.mobileandroid;


import com.google.gson.JsonObject;

import org.junit.Before;
import org.junit.Test;

import edu.txstate.mobileapp.mobileandroid.notifications.NotificationsBundle;
import edu.txstate.mobileapp.mobileandroid.notifications.TracsAppNotification;
import edu.txstate.mobileapp.mobileandroid.notifications.tracs.TracsAnnouncement;

import static org.junit.Assert.assertTrue;

public class NotificationsBundleTests {
    NotificationsBundle notifications;
    JsonObject rawAnnouncement;

    private final String expectedTitle = "Announcement Title";
    private final String expectedBody = "Announcement Body";
    private final String url = "https://tracs.txstate.edu/direct/announcement/";
    private final String id = "831342dd-fdb6-4878-8b3c-1d29ecb06a14:main:aa4f8f85-a645-4766-bc91-1a1c7bef93df";

    @Before
    public void setUp() {
        rawAnnouncement = new JsonObject();
        rawAnnouncement.addProperty("title", expectedTitle);
        rawAnnouncement.addProperty("id", id);
        rawAnnouncement.addProperty("body", expectedBody);
        notifications = new NotificationsBundle();
    }

    @Test
    public void givenAnAnnouncementThenItIsAddedToBundle() {
        TracsAppNotification announcement = new TracsAnnouncement(rawAnnouncement);
        notifications.addOne(announcement);
        assertTrue(notifications.size() == 1);
    }
}
