package edu.txstate.mobile.tracs.notifications.tracs;

abstract class TracsNotificationAbs implements TracsNotification {

    private String title;
    private String id;
    private String siteId;
    private String siteName;
    private boolean isError;

    TracsNotificationAbs() {
        this.title = NOT_SET;
        this.siteId = NOT_SET;
        this.siteName = NOT_SET;
        this.isError = false;
    }

    public String getTitle() {
        return this.title;
    }

    public String getSiteId() {
        return this.siteId;
    }

    public String getId() {
        return this.id;
    }

    public String getSiteName() { return this.siteName; }

    public boolean isNull() {
        return this.id == null || "".equals(this.id);
    }

    public boolean hasSiteName() {
        return !NOT_SET.equals(this.siteName);
    }

    public boolean isError() {
        return this.isError;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    void setId(String id) {
        this.id = id;
    }

    public String toString() {
        return this.getSiteName() + " - " + this.getTitle();
    }
}
