package com.ceiba.bicycles.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String jwksUri;
    private final String expectedIssuer;
    private final RestClient restClient;
    private Map<String, ECPublicKey> publicKeys = new HashMap<>();

    public JwtService(
            @Value("${app.security.jwt.jwks-uri}") String jwksUri,
            @Value("${app.security.jwt.issuer}") String expectedIssuer) {
        this.jwksUri = jwksUri;
        this.expectedIssuer = expectedIssuer;
        this.restClient = RestClient.create();
    }

    @PostConstruct
    public void init() {
        try {
            fetchKeys();
        } catch (Exception e) {
            log.warn("Could not fetch JWKS at startup. Will retry on first request: {}",
                    e.getMessage());
        }
    }

    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public boolean isValid(String token) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(getPublicKey(token))
                    .build()
                    .parseSignedClaims(token);
            return expectedIssuer.equals(jws.getPayload().getIssuer());
        } catch (Exception e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getPublicKey(token))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private ECPublicKey getPublicKey(String token) {
        String kid = extractKid(token);
        ECPublicKey key = publicKeys.get(kid);
        if (key == null) {
            log.info("kid '{}' not in cache, refreshing JWKS", kid);
            fetchKeys();
            key = publicKeys.get(kid);
        }
        if (key == null) {
            throw new IllegalStateException("No public key found for kid: " + kid);
        }
        return key;
    }

    private String extractKid(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Malformed JWT");
            }
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            Map<String, Object> header = objectMapper.readValue(headerJson, Map.class);
            Object kid = header.get("kid");
            if (kid == null) {
                throw new IllegalArgumentException("JWT header has no 'kid'");
            }
            return kid.toString();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse JWT header", e);
        }
    }

    private synchronized void fetchKeys() {
        try {
            String response = restClient.get()
                    .uri(jwksUri)
                    .retrieve()
                    .body(String.class);
            JwkSet jwkSet = objectMapper.readValue(response, JwkSet.class);
            Map<String, ECPublicKey> newKeys = new HashMap<>();
            for (JwkSet.Jwk jwk : jwkSet.keys()) {
                if (!"EC".equals(jwk.kty()) || !"P-256".equals(jwk.crv())) {
                    continue;
                }
                ECPublicKey key = buildECPublicKey(jwk);
                newKeys.put(jwk.kid(), key);
            }
            this.publicKeys = newKeys;
            log.info("Loaded {} public key(s) from JWKS", newKeys.size());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch JWKS from " + jwksUri, e);
        }
    }

    private ECPublicKey buildECPublicKey(JwkSet.Jwk jwk) {
        try {
            byte[] xBytes = Base64.getUrlDecoder().decode(jwk.x());
            byte[] yBytes = Base64.getUrlDecoder().decode(jwk.y());
            BigInteger xInt = new BigInteger(1, xBytes);
            BigInteger yInt = new BigInteger(1, yBytes);
            ECPoint point = new ECPoint(xInt, yInt);

            AlgorithmParameters params = AlgorithmParameters.getInstance("EC");
            params.init(new ECGenParameterSpec("secp256r1"));
            ECParameterSpec ecSpec = params.getParameterSpec(ECParameterSpec.class);

            ECPublicKeySpec keySpec = new ECPublicKeySpec(point, ecSpec);
            KeyFactory kf = KeyFactory.getInstance("EC");
            return (ECPublicKey) kf.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build EC public key for kid: " + jwk.kid(), e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JwkSet(List<JwkSet.Jwk> keys) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Jwk(String kid, String kty, String crv, String x, String y) {}
    }
}
