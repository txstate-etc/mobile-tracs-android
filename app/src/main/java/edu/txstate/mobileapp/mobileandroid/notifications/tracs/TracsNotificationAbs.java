package edu.txstate.mobileapp.mobileandroid.notifications.tracs;

abstract class TracsNotificationAbs implements TracsNotification {
    private static final String TITLE_NOT_SET = "Title Not Set";
    private static final String SUBTITLE_NOT_SET = "Subtitle Not Set";

    private String title;
    private String id;
    private String subtitle;

    TracsNotificationAbs() {
        this.title = TITLE_NOT_SET;
        this.subtitle = SUBTITLE_NOT_SET;
    }

    public String getTitle() {
        return this.title;
    }

    public String getSubtitle() {
        return this.subtitle;
    }

    public String getId() {
        return this.id;
    }

    public boolean isNull() {
        return this.id == null || "".equals(this.id);
    }

    void setTitle(String title) {
        this.title = title;
    }

    void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    void setId(String id) {
        this.id = id;
    }


}
