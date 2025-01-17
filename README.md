# Steganography Project

This project implements a web-based steganography application that enables users to hide and extract data within image files. It is built using Java and follows the servlet-based architecture.

---

## Features
- **Data Hiding**: Hide secret messages in image files using steganographic techniques.
- **Data Extraction**: Extract hidden messages from steganographically encoded images.
- **Web Interface**: Provides a user-friendly web interface for interaction.

---

## Project Structure
- **`src/main/java/com/example/steganography`**:
  - `SteganographyServlet.java`: Main servlet handling user requests for encoding and decoding messages.
  - `SteganographyUtils.java`: Utility class for steganography logic, including image processing and data encoding/decoding.
- **`src/main/resources`**:
  - `META-INF/beans.xml`: Bean configuration for dependency injection.
- **`src/main/webapp`**:
  - `WEB-INF/web.xml`: Web application configuration file.

- **`.mvn/`**: Maven wrapper files for build management.
- **`target/`**: Compiled classes, WAR file, and build outputs.

---

## Prerequisites
- **Java**: JDK 8 or later.
- **Maven**: Build automation tool to manage dependencies and compile the project.
- **Web Server**: Wildfly.
