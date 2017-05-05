package edu.txstate.mobileapp.tracscompanion;

import android.util.Log;

import com.google.gson.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import edu.txstate.mobileapp.tracscompanion.notifications.tracs.TracsAnnouncement;

import static org.junit.Assert.assertEquals;


@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class TracsAnnouncementTests {
    TracsAnnouncement announcement;
    JsonObject rawAnnouncement;
    private final String expectedTitle = "Announcement Title";
    private final String url = "https://tracs.txstate.edu/direct/announcement/";
    private final String id = "831342dd-fdb6-4878-8b3c-1d29ecb06a14:main:aa4f8f85-a645-4766-bc91-1a1c7bef93df";
    private final String expectedUrl = url + id;
    @Before
    public void setUp() {
        PowerMockito.mockStatic(Log.class);
        PowerMockito.when(Log.e("TAG", "Error Phrase")).thenReturn(1);
        rawAnnouncement = new JsonObject();
        rawAnnouncement.addProperty("title", expectedTitle);
        rawAnnouncement.addProperty("body", "Announcement Description");
        rawAnnouncement.addProperty("id", id);
        announcement = new TracsAnnouncement(rawAnnouncement);
    }

    @Test
    public void validKeyIsExtracted() {
        String actualTitle = announcement.extractKey(rawAnnouncement, "title", String.class);
        assertEquals(expectedTitle, actualTitle);
    }

    @Test
    public void invalidKeyReturnsNull() {

        String value = announcement.extractKey(rawAnnouncement, "gx", String.class);
        assertEquals(null, value);
    }

    @Test
    public void givenIdThenUrlIsFormed() {
        String announcementUrl = announcement.getUrl();
        assertEquals(expectedUrl, announcementUrl);
    }
}