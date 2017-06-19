package edu.txstate.mobile.tracs.util;

public class Setting {
    private String settingLabel;
    private String settingId;
    private boolean settingStatus;

    public Setting(String label, String id, boolean status) {
        this.settingLabel = label;
        this.settingId = id;
        this.settingStatus = status;
    }

    public String getSettingLabel() {
        return settingLabel;
    }

    public void setSettingLabel(String settingLabel) {
        this.settingLabel = settingLabel;
    }

    public String getSettingId() {
        return settingId;
    }

    public void setSettingId(String settingId) {
        this.settingId = settingId;
    }

    public boolean isSettingStatus() {
        return settingStatus;
    }

    public void setSettingStatus(boolean settingStatus) {
        this.settingStatus = settingStatus;
    }

    public String toString() {
        return this.settingLabel + ":" + this.settingId + ":" + (this.settingStatus ? "true" : "false");
    }
}
