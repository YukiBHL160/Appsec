package com.example.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@WebServlet("/oauth/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Generate a random code verifier
        String codeVerifier = generateCodeVerifier();
        req.getSession().setAttribute("code_verifier", codeVerifier);
        System.out.println("Generated Code Verifier: " + codeVerifier);

        // Generate a code challenge using SHA256
        String codeChallenge = generateCodeChallenge(codeVerifier);
        req.getSession().setAttribute("code_challenge", codeChallenge);
        System.out.println("Generated Code Challenge: " + codeChallenge);

        // Redirect to Authorization Endpoint with PKCE parameters
        String authUrl = req.getContextPath() + "/oauth/authorize"
                + "?response_type=code"
                + "&client_id=example-client"
                + "&redirect_uri=http://127.0.0.1:8080/oauth/callback"
                + "&code_challenge=" + codeChallenge
                + "&code_challenge_method=S256";

        resp.sendRedirect(authUrl);
    }

    private String generateCodeVerifier() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
