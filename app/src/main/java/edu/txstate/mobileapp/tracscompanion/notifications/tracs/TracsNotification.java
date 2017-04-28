package edu.txstate.mobileapp.tracscompanion.notifications.tracs;

import edu.txstate.mobileapp.tracscompanion.notifications.TracsAppNotification;

public interface TracsNotification extends TracsAppNotification {
    /**
     * Forms the appropriate url to point as close as possible to the applicable
     * notification
     * @return A complete string URL
     */
    String getUrl();


    /**
     * Fetches the notification title which will be used in the app.
     * @return The notification title
     */
    String getTitle();

    /**
     * Fetches the subtitle used in the app
     * @return The notification subtitle
     */
    String getSubtitle();

    boolean isNull();
}
