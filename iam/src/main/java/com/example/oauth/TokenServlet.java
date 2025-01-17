package com.example.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Date;

@WebServlet("/oauth/token")
public class TokenServlet extends HttpServlet {

    private static final String SECRET_KEY = "replace_this_with_a_long_random_key";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (email == null || password == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing email or password.");
            return;
        }

        if (!isValidUser(email, password)) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials.");
            return;
        }

        String accessToken = generateJwt(email);

        resp.setContentType("application/json");
        resp.getWriter().write("{\"access_token\":\"" + accessToken + "\"}");
    }

    private boolean isValidUser(String email, String password) {
        String hashedPassword = hashPassword(password);

        try (Connection conn = DatabaseUtil.getConnection()) {
            String query = "SELECT email FROM users WHERE email = ? AND password_hash = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, email);
                stmt.setString(2, hashedPassword);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String hashPassword(String password) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password.", e);
        }
    }

    private String generateJwt(String email) {
        byte[] secretBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        Key hmacKey = new SecretKeySpec(secretBytes, SignatureAlgorithm.HS256.getJcaName());

        return Jwts.builder()
                .setSubject(email) // Email as subject
                .setHeaderParam("typ", "JWT")
                .setIssuer("auth-server")
                .setAudience("example-client")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 3600_000)) // 1-hour validity
                .claim("scope", "read write") // Optional custom claims
                .signWith(hmacKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
