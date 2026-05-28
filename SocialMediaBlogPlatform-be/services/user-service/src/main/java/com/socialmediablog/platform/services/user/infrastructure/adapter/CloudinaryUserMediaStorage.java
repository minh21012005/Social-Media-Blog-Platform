package com.socialmediablog.platform.services.user.infrastructure.adapter;

import com.socialmediablog.platform.services.user.application.port.out.UserMediaStorage;
import com.socialmediablog.platform.services.user.application.result.StoredUserMedia;
import com.socialmediablog.platform.services.user.config.CloudinaryProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class CloudinaryUserMediaStorage implements UserMediaStorage {

    private final CloudinaryProperties properties;
    private final RestClient restClient;

    public CloudinaryUserMediaStorage(CloudinaryProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.create();
    }

    @Override
    public StoredUserMedia uploadAvatar(String originalFilename, String mimeType, byte[] content) {
        if (!properties.configured()) {
            throw new IllegalStateException("Cloudinary is not configured for user media storage");
        }
        long timestamp = System.currentTimeMillis() / 1000;
        String folder = properties.getAvatarFolder();

        Map<String, String> signedParameters = new TreeMap<>();
        signedParameters.put("timestamp", Long.toString(timestamp));
        if (!folder.isBlank()) {
            signedParameters.put("folder", folder);
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new NamedByteArrayResource(content, originalFilename));
        body.add("api_key", properties.getApiKey());
        body.add("timestamp", Long.toString(timestamp));
        body.add("signature", sign(signedParameters));
        if (!folder.isBlank()) {
            body.add("folder", folder);
        }

        Map<String, Object> response = restClient.post()
                .uri("https://api.cloudinary.com/v1_1/{cloudName}/image/upload", properties.getCloudName())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (response == null) {
            throw new IllegalStateException("Cloudinary returned an empty response");
        }
        return new StoredUserMedia(
                stringValue(response.get("public_id")),
                stringValue(response.get("secure_url")),
                originalFilename,
                mimeType,
                longValue(response.get("bytes")),
                intValue(response.get("width")),
                intValue(response.get("height"))
        );
    }

    private String sign(Map<String, String> parameters) {
        StringBuilder payload = new StringBuilder();
        parameters.forEach((key, value) -> {
            if (!value.isBlank()) {
                if (!payload.isEmpty()) {
                    payload.append('&');
                }
                payload.append(key).append('=').append(value);
            }
        });
        payload.append(properties.getApiSecret());
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return HexFormat.of().formatHex(digest.digest(payload.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-1 is not available", ex);
        }
    }

    private static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private static long longValue(Object value) {
        return value instanceof Number number ? number.longValue() : 0;
    }

    private static Integer intValue(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private static final class NamedByteArrayResource extends ByteArrayResource {

        private final String filename;

        private NamedByteArrayResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
