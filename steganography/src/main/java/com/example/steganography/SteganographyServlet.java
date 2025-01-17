package com.example.steganography;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;

@WebServlet("/api/steganography")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024, // 1MB
        maxFileSize = 5 * 1024 * 1024,   // 5MB
        maxRequestSize = 20 * 1024 * 1024 // 20MB
)
public class SteganographyServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action"); // "encode" or "decode"
        String secretKey = request.getParameter("secretKey");

        if (secretKey == null || secretKey.length() != 16) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid secret key. It must be 16 characters.");
            return;
        }

        try {
            if ("encode".equalsIgnoreCase(action)) {
                handleEncode(request, response, secretKey);
            } else if ("decode".equalsIgnoreCase(action)) {
                handleDecode(request, response, secretKey);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action. Use 'encode' or 'decode'.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred: " + e.getMessage());
        }
    }

    private void handleEncode(HttpServletRequest request, HttpServletResponse response, String secretKey) throws Exception {
        Part imagePart = request.getPart("image");
        String message = request.getParameter("message");

        if (imagePart == null || message == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Both image and message are required for encoding.");
            return;
        }

        // Save the uploaded file temporarily
        Path tempImagePath = Files.createTempFile("uploaded", ".png");
        Files.copy(imagePart.getInputStream(), tempImagePath, StandardCopyOption.REPLACE_EXISTING);

        // Encode the message into the image
        File inputFile = tempImagePath.toFile();
        BufferedImage encodedImage = SteganographyUtils.encode(inputFile, message, secretKey);

        // Respond with the modified image
        response.setContentType("image/png");
        ImageIO.write(encodedImage, "png", response.getOutputStream());
        response.getOutputStream().flush();

        // Clean up
        Files.deleteIfExists(tempImagePath);
    }

    private void handleDecode(HttpServletRequest request, HttpServletResponse response, String secretKey) throws Exception {
        Part imagePart = request.getPart("image");

        if (imagePart == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Image is required for decoding.");
            return;
        }

        // Save the uploaded file temporarily
        Path tempImagePath = Files.createTempFile("uploaded", ".png");
        Files.copy(imagePart.getInputStream(), tempImagePath, StandardCopyOption.REPLACE_EXISTING);

        // Decode the message from the image
        File inputFile = tempImagePath.toFile();
        String decodedMessage = SteganographyUtils.decode(inputFile, secretKey);

        // Respond with the decoded message
        response.setContentType("text/plain");
        response.getWriter().write(decodedMessage);
        response.getWriter().flush();

        // Clean up
        Files.deleteIfExists(tempImagePath);
    }
}
