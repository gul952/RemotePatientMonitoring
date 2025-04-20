# Remote Patient Monitoring System

The Remote Patient Monitoring System is a Java-based project that was made as a university assignment. The system allows patients, doctors, and administrators to interact with various functionalities such as patient vital sign tracking, appointment scheduling, and doctor‑patient feedback. The project has been extended with advanced features—including an Emergency Alert System, Chat & Video Consultation, and Notifications & Reminders.

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

All these features have been integrated without changing the original functionality or altering core aspects of the system.

## Features

### Original Features

#### Patient Management

- **User Identification**: Patients are registered with unique IDs.  
- **Vital Signs Tracking**: Record heart rate, oxygen levels, blood pressure, and temperature.  
- **Appointment Scheduling**: Request and view appointment statuses.  
- **Feedback & Prescription Viewing**: Review doctor feedback and prescriptions.  
- **Medical History**: Comprehensive log of patient records.

#### Doctor Management

- **Patient Data Viewing**: View patients’ vital signs and medical histories.  
- **Feedback & Prescription Writing**: Provide feedback and write prescriptions.  
- **Appointment Management**: Approve or cancel pending requests.  
- **Viewing Approved Appointments**: See IDs of patients with approved appointments.

#### Administrator Functions

- **User Management**: Add, update, or remove patients and doctors.  
- **System Logs**: Record all events and actions in the system.  
- **User ID Overview**: View all registered Patient and Doctor IDs.

### New Features

#### Module 1: Emergency Alert System

- **EmergencyAlert**: Monitors vital signs and triggers alerts if thresholds are exceeded.  
- **NotificationService**: Sends simulated email and SMS alerts, stored in user dashboards.  
- **PanicButton**: Immediate alert trigger.

#### Module 2: Chat & Video Consultation

- **ChatServer**: Central hub for message transfer and storage.  
- **ChatClient**: Sends/receives messages with sender/receiver context; stores chat histories.  
- **VideoCall**: Simulates a video consultation link.

#### Module 3: Notifications & Reminders

- **ReminderService**: Sends medication and appointment reminders.  
- **Notifiable Interface**: Defines `sendNotification(String recipient, String message)`.  
- **EmailNotification & SMSNotification**: Implement Notifiable to simulate delivery.

#### Enhanced User Dashboards

- **Patient Dashboard**: Shows vitals, appointments, feedback/prescriptions, history, notifications, and chat history.  
- **Doctor Dashboard**: Shows patient details, appointment management, received alerts, and chat history.

## Modules and Components

### Module 1: Emergency Alert System

- **EmergencyAlert**: Checks vitals against thresholds and triggers alerts.  
- **NotificationService**: Dispatches alerts via Notifiable and stores them.  
- **PanicButton**: Manual alert trigger.

### Module 2: Chat & Video Consultation

- **ChatServer**: Routes and stores messages.  
- **ChatClient**: Handles sending/receiving; logs history.  
- **VideoCall**: Generates a mock video call link.

### Module 3: Notifications & Reminders

- **ReminderService**: Fires scheduled reminders.  
- **Notifiable Interface**: Common notification contract.  
- **EmailNotification / SMSNotification**: Simulated notification implementations.

## Installation and Running the Project

### Prerequisites

- **JDK 8+** (verify with `java -version`)  
- **(Optional) JavaMail API** for email/notification features: download the latest Jakarta Mail jars (`mail.jar` and `activation.jar`) and place them in a folder named `lib/` at the root of this project.

### Steps to Compile and Run

1. **Clone the repository**  
   ```bash
   git clone https://github.com/gul952/RemotePatientMonitoring.git
   cd RemotePatientMonitoring
   ```

2. **Create an output directory**  
   ```bash
   mkdir -p bin
   ```

3. **Compile all source files**  
   - **Without email/notification support**  
     ```bash
     javac -d bin -sourcepath src $(find src -name "*.java")
     ```  
   - **With JavaMail (email/notification) support**  
     ```bash
     javac -cp "lib/mail.jar:lib/activation.jar" \
       -d bin \
       -sourcepath src \
       $(find src -name "*.java")
     ```

4. **Run the application**  
   - **Without JavaMail** (email/chat features skipped)  
     ```bash
     java -cp bin healthcare.main
     ```  
   - **With JavaMail**  
     ```bash
     java -cp "bin:lib/mail.jar:lib/activation.jar" healthcare.main
     ```

> **Notes:**  
> - **Main class**: `healthcare.main` (in `src/healthcare/main.java`)  
> - **Email/notification helper**: `src/javamailutil/JavaMailUtil.java` (requires Jakarta Mail jars)  
> - You can also open in NetBeans (includes `nbproject/` and `build.xml`) and use **Build ▶ Run**.

## Usage

When you run the program, a main menu appears with options for different user roles and extra features.

### Instructions to run email feature
1. Navigate to the javaMailUtil.java file and where the email has been set to xxxx write your email.
2. For the password you need to have an Apppassword generated
3. Navigate to main in the javaMailUtil folder and here set the recipient email to whatever you want

### Patients

1. Enter your **Patient ID** (e.g., `P001`).  
2. Use menus to:  
   - Enter and view vital signs  
   - Schedule and check appointments  
   - View feedback and prescriptions  
   - View medical history  
   - Check notification inbox (alerts/reminders)  
   - View chat history

### Doctors

1. Enter your **Doctor ID** (e.g., `D001`).  
2. Use menus to:  
   - View patient vitals and history  
   - Write feedback and prescriptions  
   - Approve/cancel appointments  
   - View received alerts  
   - View chat history

### Administrators

- Access admin menu to add/update/remove users.  
- View system logs and all registered IDs.

### Extra Features

- **Emergency Alert**: Simulate vital checks and alerts.  
- **Panic Button**: Trigger immediate alert.  
- **Chat & Video**: Exchange messages and generate video call links.  
- **Reminders**: Send medication and appointment reminders.

All alerts and chats are stored per user dashboard.

**HealthcareManagement.java**

This file contains:  
- Original system code (user management, appointments, vitals, history).  
- New modules (Emergency Alert, Chat & Video, Notifications & Reminders).  
- Enhanced dashboards for patients and doctors.

