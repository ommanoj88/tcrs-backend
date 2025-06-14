package com.tcrs.tcrs_backend.security;

        import io.jsonwebtoken.*;
        import io.jsonwebtoken.io.Decoders;
        import io.jsonwebtoken.security.Keys;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        import org.springframework.beans.factory.annotation.Value;
        import org.springframework.security.core.Authentication;
        import org.springframework.stereotype.Component;

        import java.security.Key;
        import java.util.Date;

        @Component
        public class JwtUtils {

            private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

            @Value("${app.jwt.secret}")
            private String jwtSecret;

            @Value("${app.jwt.expiration}")
            private int jwtExpirationMs;

            @Value("${app.jwt.refresh-expiration}")
            private int jwtRefreshExpirationMs;

            public String generateJwtToken(Authentication authentication) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                return generateTokenFromPhone(userPrincipal.getPhone());
            }

            // Add this new method for phone-based tokens
            public String generateTokenFromPhone(String phone) {
                return Jwts.builder()
                        .setSubject(phone)
                        .setIssuedAt(new Date())
                        .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                        .signWith(key(), SignatureAlgorithm.HS256)
                        .compact();
            }

            // Renamed this method to match our phone-based approach
            public String generateRefreshTokenFromPhone(String phone) {
                return Jwts.builder()
                        .setSubject(phone)
                        .setIssuedAt(new Date())
                        .setExpiration(new Date((new Date()).getTime() + jwtRefreshExpirationMs))
                        .signWith(key(), SignatureAlgorithm.HS256)
                        .compact();
            }

            private Key key() {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
            }

            // Rename this to match our phone-based approach
            public String getPhoneFromJwtToken(String token) {
                return Jwts.parserBuilder()
                        .setSigningKey(key())
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject();
            }

            public boolean validateJwtToken(String authToken) {
                try {
                    Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
                    return true;
                } catch (MalformedJwtException e) {
                    logger.error("Invalid JWT token: {}", e.getMessage());
                } catch (ExpiredJwtException e) {
                    logger.error("JWT token is expired: {}", e.getMessage());
                } catch (UnsupportedJwtException e) {
                    logger.error("JWT token is unsupported: {}", e.getMessage());
                } catch (IllegalArgumentException e) {
                    logger.error("JWT claims string is empty: {}", e.getMessage());
                }
                return false;
            }

            public Date getExpirationDateFromJwtToken(String token) {
                return Jwts.parserBuilder()
                        .setSigningKey(key())
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getExpiration();
            }

            public long getJwtExpirationMs() {
                return jwtExpirationMs;
            }

            public long getJwtRefreshExpirationMs() {
                return jwtRefreshExpirationMs;
            }
        }