# Remote Patient Monitoring System

The Remote Patient Monitoring System is a Java-based project that was made as a university assignment. The system allows patients, doctors, and administrators to interact with various functionalities such as patient vital sign tracking, appointment scheduling, and doctor-patient feedback.The project has been extended with advanced features—including an Emergency Alert System, Chat & Video Consultation, and Notifications & Reminders.

## Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
  - [Original Features](#original-features)
  - [New Features](#new-features)
- [Modules and Components](#modules-and-components)
- [Installation and Running the Project](#installation-and-running-the-project)
  - [Prerequisites](#prerequisites)
  - [Steps to Compile and Run](#steps-to-compile-and-run)
- [Usage](#usage)
- [Project Structure](#project-structure)

## Project Overview

This project started as a Remote Patient Monitoring System with core functionalities for managing patient and doctor data, managing appointments, entering vital signs, and maintaining medical histories. The system also included user roles (Patient, Doctor, and Administrator).

Recently, the system has been enhanced by adding three new modules:

- **Emergency Alert System**: Monitors patient vitals, triggers alerts via simulated email and SMS (displayed on user dashboards), and includes a panic button for immediate alerts.
- **Chat & Video Consultation**: Simulates chat interactions between patients and doctors and generates a video call link for consultation.
- **Notifications & Reminders**: Sends medication and appointment reminders using a unified notification system that implements a common interface (Notifiable), with simulated email and SMS notifications.

All these features have been integrated into one single Java file without changing the original functionality or altering user IDs or similar core aspects of the system.

## Features

### Original Features

#### Patient Management:

- **User Identification**: Patients are registered with unique IDs.
- **Vital Signs Tracking**: Patients can record their heart rate, oxygen levels, blood pressure, and temperature. These are stored and linked to their medical history.
- **Appointment Scheduling**: Patients can request new appointments and view the status of existing ones.
- **Feedback and Prescription Viewing**: Patients can review doctor feedback and prescriptions.
- **Medical History**: A comprehensive log of the patient’s records is maintained.

#### Doctor Management:

- **Patient Data Viewing**: Doctors can view patients’ vital signs and medical histories.
- **Feedback and Prescription Writing**: Doctors can provide feedback and write prescriptions.
- **Appointment Management**: Doctors can manage appointments, including approval or cancellation of pending requests.
- **Viewing Approved Appointments**: Doctors can see the IDs of patients with approved appointments.

#### Administrator Functions:

- **User Management**: Administrators can add, update, or remove patients and doctors.
- **System Logs**: A log system records all events and actions within the system.
- **User ID Overview**: Administrators can view all registered Patient and Doctor IDs.

### New Features

#### Module 1: Emergency Alert System

- **EmergencyAlert**: Monitors patient vital signs and triggers alerts if readings exceed specified thresholds.
- **NotificationService**: Sends simulated email and SMS alerts. These notifications are stored and viewable on user dashboards.
- **PanicButton**: Immediately triggers alerts when pressed.

#### Module 2: Chat & Video Consultation

- **ChatServer**: Acts as a central hub, facilitating the message transfer between users.
- **ChatClient**: Handles sending and receiving messages. An overloaded method allows specifying both sender and receiver, with messages stored in respective chat histories.
- **VideoCall**: Simulates a video consultation by printing a join link (e.g., a mock Zoom link).

#### Module 3: Notifications & Reminders

- **ReminderService**: Sends medication and appointment reminders.
- **Notifiable Interface**: Ensures all notification types have a consistent method to send messages.
- **EmailNotification and SMSNotification**: Implement the Notifiable interface to simulate delivery of notifications (and store them in user dashboards).

#### Enhanced User Dashboards:

- **Patient Dashboard**: Displays vital sign data, appointment status, feedback/prescriptions, complete medical history, an inbox for notifications/reminders, and a chat message history.
- **Doctor Dashboard**: Provides functionality to view patient details, manage appointments, see received alerts, and access a chat message history.

## Modules and Components

### Module 1: Emergency Alert System

- **EmergencyAlert**: Evaluates vital signs and triggers alerts if thresholds are exceeded.
- **NotificationService**: Dispatches alerts via the Notifiable interface and stores them in user dashboards.
- **PanicButton**: Provides an immediate alert mechanism.

### Module 2: Chat & Video Consultation

- **ChatServer**: Manages the transfer and storage of chat messages.
- **ChatClient**: Sends messages with a sender and receiver context; messages are stored for both parties.
- **VideoCall**: Generates a simulated video call link for consultations.

### Module 3: Notifications & Reminders

- **ReminderService**: Sends reminders for medications and appointments.
- **Notifiable Interface**: Defines the method `sendNotification(String recipient, String message)`.
- **EmailNotification**: Simulates sending email alerts.
- **SMSNotification**: Simulates sending SMS alerts.

## Installation and Running the Project

### Prerequisites

- **Java Development Kit (JDK)**: Version 8 or later.
- A terminal or command prompt for compiling and running the Java file.

```markdown
## Steps to Compile and Run

1. **Clone the repo**  
   ```bash
   git clone https://github.com/gul952/RemotePatientMonitoring.git
   cd RemotePatientMonitoring
   ```

2. **Install prerequisites**  
   - **JDK 8+** (check with `java -version`)  
   - **JavaMail API** (for email reminders): download `mail.jar` + `activation.jar` and put them in a `lib/` folder  

3. **Prepare output folder**  
   ```bash
   mkdir -p bin
   ```

4. **Compile**  
   ```bash
   # Without email support:
   javac -d bin -sourcepath src $(find src -name "*.java")

   # With email support:
   javac -cp "lib/mail.jar:lib/activation.jar" -d bin -sourcepath src $(find src -name "*.java")
   ```

5. **Run**  
   ```bash
   # No email:
   java -cp bin healthcare.main

   # With email:
   java -cp "bin:lib/mail.jar:lib/activation.jar" healthcare.main
   ```

> **Notes:**  
> - Main class: `healthcare.main` (src/healthcare/main.java)  
> - Email helper: `javamailutil/JavaMailUtil.java` (requires the JavaMail jars)  
> - You can also open in NetBeans (nbproject/ + build.xml) and use the IDE’s Build ▶ Run.  
```

## Usage

Upon running the project, you will see a main menu offering several user roles and extra features.

### For Patients:

- Enter your Patient ID to access functionalities such as entering vital signs, scheduling appointments, viewing feedback and prescriptions, and checking your medical history (e.g., P001). New IDS can be added through the admin menu if you wish
- You can also view your notification inbox (alerts/reminders) and chat message history.

### For Doctors:

- Enter your Doctor ID to view patients’ vital signs, write feedback, provide prescriptions, and manage appointments (e.g., D001).
- You can view alerts sent (from emergency alerts or panic button actions) and view your chat message history.

### For Administrators:

- Use the admin menu to add, update, or remove patients and doctors.
- View system logs and all registered user IDs.

### Extra Features:

- Access the Extra Features menu to use the Emergency Alert, Panic Button, Chat & Video Consultation, and Reminders/Notifications modules.
- For example, you can simulate a vital sign check that triggers an alert, send chat messages between users, or generate a video call link.

All alerts and chat messages are stored in the respective user dashboards, allowing both patients and doctors to see past notifications and communications.

## Project Structure

All functionalities are implemented in a single Java file: **HealthcareManagement.java**. This file includes:

- The original Healthcare Management System code (user management, appointment scheduling, vital sign tracking, etc.).
- New modules for Emergency Alerts, Chat & Video Consultation, and Notifications & Reminders.
- Enhanced user dashboards for both patients and doctors (to view notifications and chat messages).



