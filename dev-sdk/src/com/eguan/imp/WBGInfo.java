package com.eguan.imp;

public class WBGInfo {
    private String SSID;
    private String BSSID;
    private String Level;
    private String LocationAreaCode;
    private String CellId;
    private String CollectionTime;
    private String GeographyLocation;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    private String ip;

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public String getLevel() {
        return Level;
    }

    public void setLevel(String level) {
        Level = level;
    }

    public String getLocationAreaCode() {
        return LocationAreaCode;
    }

    public void setLocationAreaCode(String locationAreaCode) {
        LocationAreaCode = locationAreaCode;
    }

    public String getCellId() {
        return CellId;
    }

    public void setCellId(String cellId) {
        CellId = cellId;
    }

    public String getCollectionTime() {
        return CollectionTime;
    }

    public void setCollectionTime(String collectionTime) {
        CollectionTime = collectionTime;
    }

    public String getGeographyLocation() {
        return GeographyLocation;
    }

    public void setGeographyLocation(String geographyLocation) {
        GeographyLocation = geographyLocation;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("WBGInfo [" + Integer.toHexString(hashCode()) + "]  SSID:").append(SSID).append("; ")
                .append("BSSID: ").append(BSSID).append("; ").append("Level: ").append(Level).append("; ")
                .append("LocationAreaCode: ").append(LocationAreaCode).append("; ").append("CollectionTime: ")
                .append(CollectionTime).append("; ").append("CellId: ").append(CellId).append("; ")
                .append("GeographyLocation: ").append(GeographyLocation).append("; ");
        return sb.toString();
    }
}
