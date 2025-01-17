package com.example.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@WebServlet("/oauth/callback")
public class CallbackServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String code = req.getParameter("code");
        String codeVerifier = (String) req.getSession().getAttribute("code_verifier");

        if (code == null || codeVerifier == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters.");
            return;
        }

        String tokenEndpoint = "http://127.0.0.1:8080/oauth/token";

        try {
            URL url = new URL(tokenEndpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String body = "grant_type=authorization_code"
                    + "&code=" + code
                    + "&client_id=example-client"
                    + "&codeVerifier=" + codeVerifier;

            try (OutputStream os = connection.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int status = connection.getResponseCode();
            if (status == 200) {
                try (Scanner scanner = new Scanner(connection.getInputStream(),
                        StandardCharsets.UTF_8.name())) {
                    String responseBody = scanner.useDelimiter("\\A").next();
                    resp.getWriter().write("Token Response: " + responseBody);
                }
            } else {
                try (Scanner scanner = new Scanner(connection.getErrorStream(),
                        StandardCharsets.UTF_8.name())) {
                    String errorBody = scanner.useDelimiter("\\A").next();
                    resp.sendError(status, errorBody);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
