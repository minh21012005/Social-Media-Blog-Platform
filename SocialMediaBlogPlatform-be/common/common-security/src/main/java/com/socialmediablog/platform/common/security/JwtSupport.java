package com.socialmediablog.platform.common.security;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.jwt.Jwt;

public final class JwtSupport {

    private JwtSupport() {
    }

    public static RSAPrivateKey rsaPrivateKey(JwtProperties jwtProperties) {
        String pem = keyMaterial(jwtProperties.privateKey(), jwtProperties.privateKeyPath(), "private");
        try {
            byte[] decoded = decodePem(pem, "PRIVATE KEY");
            return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(decoded));
        } catch (Exception exception) {
            throw new IllegalArgumentException("JWT private key must be a valid PKCS#8 RSA private key", exception);
        }
    }

    public static RSAPublicKey rsaPublicKey(JwtProperties jwtProperties) {
        String pem = keyMaterial(jwtProperties.publicKey(), jwtProperties.publicKeyPath(), "public");
        try {
            byte[] decoded = decodePem(pem, "PUBLIC KEY");
            return (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(decoded));
        } catch (Exception exception) {
            throw new IllegalArgumentException("JWT public key must be a valid X.509 RSA public key", exception);
        }
    }

    private static String keyMaterial(String inlineKey, String keyPath, String keyType) {
        if (inlineKey != null && !inlineKey.isBlank()) {
            return inlineKey;
        }
        if (keyPath != null && !keyPath.isBlank()) {
            try {
                return Files.readString(resolvePath(keyPath));
            } catch (Exception exception) {
                throw new IllegalArgumentException("JWT " + keyType + " key file cannot be read: " + keyPath, exception);
            }
        }
        throw new IllegalArgumentException("JWT " + keyType + " key or key path must be configured");
    }

    private static Path resolvePath(String keyPath) {
        Path configuredPath = Path.of(keyPath);
        if (configuredPath.isAbsolute() || Files.exists(configuredPath)) {
            return configuredPath;
        }

        Path current = Path.of("").toAbsolutePath();
        for (int depth = 0; depth < 5 && current != null; depth++) {
            Path candidate = current.resolve(configuredPath).normalize();
            if (Files.exists(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        return configuredPath;
    }

    private static byte[] decodePem(String pem, String type) {
        String normalized = pem
                .replace("-----BEGIN " + type + "-----", "")
                .replace("-----END " + type + "-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(normalized);
    }

    public static Set<String> rolesFrom(Jwt jwt) {
        Object roles = jwt.getClaims().get("roles");
        if (roles instanceof Collection<?> collection) {
            return collection.stream()
                    .map(String::valueOf)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        if (roles instanceof String value && !value.isBlank()) {
            return Set.of(value.split(","));
        }
        return Set.of();
    }

    public static String rolesHeader(Set<String> roles) {
        return roles == null ? "" : String.join(",", roles);
    }
}
