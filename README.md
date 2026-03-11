# Project BSPQ26-E1

This is the setup guide to start the local development environment for the project. The system consists of a database running in containers and a Spring Boot backend application.

## 📋 Prerequisites

Before you begin, make sure you have the following installed on your machine:

* **[Docker Desktop](https://docs.docker.com/desktop/)**: Required to start the database container.
* **[Java JDK](https://adoptium.net/)**: Required to compile and run the Spring Boot project (make sure the `JAVA_HOME` environment variable is configured).
* **[Maven]**: You will need Maven installed and added to your `PATH`, install the -bin.zip file and extract it, after all put until the folder \bin inn the `PATH`.

---

## 🚀 How to start the project

Follow these steps in order to bring up the full environment:

### 1. Start the Database

First, we need to start the database container with Docker.

**Important note:** Make sure the *Docker Desktop* application is open and running before executing the command.

1. Open a terminal.
2. Navigate to the database folder:
   ```bash
   cd database
   ```
3. Run the following command to start the container in the background:
   ```bash
   docker compose up -d
   ```

> If you get the error "The term 'docker' is not recognized...", check that Docker Desktop is installed, that it is open, and that you have restarted your terminal.

### 2. Start the Backend (Spring Boot)

Once the database is ready, we can start the application.

1. Open a terminal.
2. Navigate to the Java project folder:
   ```bash
   cd maven-basic-project
   ```
3. Run the application using the Maven Wrapper (recommended):
   ```bash
   mvn spring-boot:run
   ```

> If the console gets stuck or the command is not recognized, make sure you are inside the `maven-basic-project` folder and that you have restarted the terminal after installing Java/Maven.

---
