package com.example.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@WebServlet("/oauth/authorize")
public class AuthorizationServlet extends HttpServlet {

    private static final Map<String, String> authorizationCodes = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String clientId = req.getParameter("client_id");
        String redirectUri = req.getParameter("redirect_uri");
        String codeChallenge = req.getParameter("code_challenge");

        if (clientId == null || redirectUri == null || codeChallenge == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
            return;
        }

        String authorizationCode = UUID.randomUUID().toString();
        authorizationCodes.put(authorizationCode, codeChallenge);

        resp.sendRedirect(redirectUri + "?code=" + authorizationCode);
    }

    public static String getCodeChallenge(String authorizationCode) {
        return authorizationCodes.get(authorizationCode);
    }
}
