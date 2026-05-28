package com.socialmediablog.platform.services.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cloudinary")
public class CloudinaryProperties {

    private String cloudName = "";
    private String apiKey = "";
    private String apiSecret = "";
    private String avatarFolder = "social-blog/avatars";

    public boolean configured() {
        return !cloudName.isBlank() && !apiKey.isBlank() && !apiSecret.isBlank();
    }

    public String getCloudName() {
        return cloudName;
    }

    public void setCloudName(String cloudName) {
        this.cloudName = cloudName == null ? "" : cloudName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret == null ? "" : apiSecret;
    }

    public String getAvatarFolder() {
        return avatarFolder;
    }

    public void setAvatarFolder(String avatarFolder) {
        this.avatarFolder = avatarFolder == null ? "" : avatarFolder;
    }
}
