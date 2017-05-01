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
     * Fetches the site id of the notification
     * @return The notification site id
     */
    String getSiteId();

    /**
     * Gets the site name the notification came from
     * @return The site name associated with the notification
     */
    String getSiteName();

    void setTitle(String siteTitle);
    void setSiteId(String siteId);
    void setSiteName(String siteName);


    boolean isNull();

    boolean hasSiteName();
}
