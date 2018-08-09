package com.square.renov.swipevoicechat.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SystemCheck {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("os")
    @Expose
    private String os;
    @SerializedName("version")
    @Expose
    private String version;
    @SerializedName("minVersion")
    @Expose
    private String minVersion;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMinVersion() {
        return minVersion;
    }

    public void setMinVersion(String minVersion) {
        this.minVersion = minVersion;
    }

    public boolean isUsableVersion(String currentVersion) {
        return getMinVersion() == null || currentVersion == null
                || compareVersion(currentVersion, getMinVersion()) >= 0;
    }

    protected static int compareVersion(String v1, String v2) {
        String[] vals1 = v1.replaceAll("[^\\.\\d].*$", "").split("\\.");
        String[] vals2 = v2.replaceAll("[^\\.\\d].*$", "").split("\\.");
        int i = 0;
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }

        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return diff < 0 ? -1 : diff == 0 ? 0 : 1;
        }
        return vals1.length < vals2.length ? -1 : vals1.length == vals2.length ? 0 : 1;
    }
}

