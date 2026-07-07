# Health Monitoring System

A medical and disease management system designed to monitor community and individual health status. The project features an intuitive Android mobile application connected to a robust backend server and a cloud-hosted database.

## 📥 Download

- **Android Version (APK):** [Download app-release.apk](https://github.com/AbakanDev/health-monitoring-system/releases/latest/download/app-release.apk)
*(Note: Ensure you have created a Release on GitHub and uploaded the APK file with the corresponding name for this link to work correctly).*

## ✨ Key Features

*   **User Management:** Register, log in, and manage medical profile information.
*   **Vaccination Tracking:** Store and update vaccination history for individual users.
*   **Quarantine Management:** Securely monitor and update medical quarantine status.

## 🛠️ Technologies Used

*   **Frontend:** Android application developed using **Kotlin** on IntelliJ IDEA.
*   **Backend:** **Node.js** for handling business logic and APIs.
*   **Database:** **MySQL** (deployed and hosted on the cloud via **Aiven**).

## 🚀 Installation and Setup

### Prerequisites
*   IntelliJ IDEA (or Android Studio) to build the Android application.
*   Windows Subsystem for Linux (WSL) with Ubuntu recommended for the development environment.
*   Node.js installed to run the backend server.
*   An Aiven account to manage and connect to the database.

### Setup Steps
1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/AbakanDev/health-monitoring-system.git](https://github.com/AbakanDev/health-monitoring-system.git)
    ```
2.  **Server Configuration (Node.js):**
    *   Navigate to the backend source code directory.
    *   Run `npm install` to install dependencies.
    *   Create a `.env` file and provide the connection string to your MySQL database on Aiven.
    *   Start the server: `npm start`.
3.  **Android Application Configuration:**
    *   Open the project's Android directory using IntelliJ IDEA.
    *   Sync Project with Gradle Files to ensure all team members have the same configuration.
    *   Connect a physical device or an Android emulator and click **Run** to launch the app.

## 👥 Contributors

*   **Abakan** 
*   **Tùng Ân**
*   **Đại Ân**
*   **Thảo Ân**
*   **Thu Giang**
