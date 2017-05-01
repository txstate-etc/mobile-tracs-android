package edu.txstate.mobileapp.tracscompanion;


import com.google.gson.JsonObject;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import edu.txstate.mobileapp.tracscompanion.notifications.NotificationsBundle;
import edu.txstate.mobileapp.tracscompanion.notifications.TracsAppNotification;
import edu.txstate.mobileapp.tracscompanion.notifications.tracs.TracsAnnouncement;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

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

    @Test
    public void notificationTypeIsAddedAndCounted() {
        TracsAppNotification announcement = new TracsAnnouncement(rawAnnouncement);
        notifications.addOne(announcement);
        notifications.addOne(announcement);
        notifications.addOne(announcement);
        notifications.addOne(announcement);
        assertEquals(notifications.countAnnouncements(), 4);
    }

    @Test
    public void notificationTypeIsRemovedAndCounted() {
        TracsAppNotification announcement = new TracsAnnouncement(rawAnnouncement);

        notifications.addOne(announcement);
        notifications.addOne(announcement);
        notifications.addOne(announcement);
        notifications.addOne(announcement);

        notifications.remove(announcement.getId());
        notifications.remove(announcement.getId());
        notifications.remove(announcement.getId());

        assertEquals(1, notifications.countAnnouncements());
    }
}
