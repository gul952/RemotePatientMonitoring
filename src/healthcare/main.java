package healthcare; 

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javamailutil.JavaMailUtil;

import java.util.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Properties;

/*
   Main class for my Healthcare Management System.
   It brings together different parts for user management, vital sign tracking, appointment scheduling, and doctor-patient interactions.
*/
public class main {

    // Global databases for patients and doctors, used by the new notification/chat enhancements.
    public static List<Patient> globalPatientDb = new ArrayList<>();
    public static List<Doctor> globalDoctorDb = new ArrayList<>();

    // Helper method to add a notification message.
    public static void addNotification(String recipient, String message) {
        boolean found = false;
        for (Patient p : globalPatientDb) {
            if (p.getName().equalsIgnoreCase(recipient) || p.getUserId().equalsIgnoreCase(recipient)) {
                p.addInboxMessage(message);
                found = true;
            }
        }
        for (Doctor d : globalDoctorDb) {
            if (d.getName().equalsIgnoreCase(recipient) || d.getUserId().equalsIgnoreCase(recipient)) {
                d.addAlert(message);
                found = true;
            }
        }
        // If not found, notification is not stored.
    }

    // Helper method to add a chat message for a given reciver.
    public static void addChatMessage(String recipient, String message) {
        for (Patient p : globalPatientDb) {
            if (p.getName().equalsIgnoreCase(recipient) || p.getUserId().equalsIgnoreCase(recipient)) {
                p.addChatMessage(message);
            }
        }
        for (Doctor d : globalDoctorDb) {
            if (d.getName().equalsIgnoreCase(recipient) || d.getUserId().equalsIgnoreCase(recipient)) {
                d.addChatMessage(message);
            }
        }
    }

    // Searches for a patient with the given ID in the provided list.
    private static Patient findPatient(List<Patient> patientDb, String id) {
        for (Patient p : patientDb) {
            if (p.getUserId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    // Searches for a doctor with the given ID in the provided list.
    private static Doctor findDoctor(List<Doctor> doctorDb, String id) {
        for (Doctor d : doctorDb) {
            if (d.getUserId().equals(id)) {
                return d;
            }
        }
        return null;
    }

    // -------------------------------
    // 1. User Management Classes
    // -------------------------------

    // Base User class that holds common details for patients and doctors.
    // Note: Administrator will also extend this class, but I didnt use its ID for ease of testing
    public static class User {
        // Private fields for encapsulation.
        private String name;
        private String userId;
        private String role;

        // Constructor initializes name, userId, and role.
        public User(String name, String userId, String role) {
            this.name = name;
            this.userId = userId;
            this.role = role;
        }

        // Getter and setter for name.
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

        // Getter and setter for userId.
        public String getUserId() {
            return userId;
        }
        public void setUserId(String userId) {
            this.userId = userId;
        }

        // Getter and setter for role.
        public String getRole() {
            return role;
        }
        public void setRole(String role) {
            this.role = role;
        }

        // Returns a string representation of the user.
        @Override
        public String toString() {
            //this cpapatilises the first letter for beautification purposes
            return role.substring(0, 1).toUpperCase() + role.substring(1) + ": " + name + ", ID: " + userId;
        }
    }

    // Patient class extends User and does: vital uploads, viewing doctor feedback, scheduling appointments.
    public static class Patient extends User {
        // Private fields
        private MedicalHistory medicalHistory;
        private List<Feedback> feedbacks;
        private List<Prescription> prescriptions;
        private List<Appointment> appointments;

        // New fields: Inbox for alerts/reminders and chat history.
        private List<String> inboxMessages = new ArrayList<>();
        private List<String> chatMessages = new ArrayList<>();

        // Constructor for Patient; it calls the super constructor of user base class using role "patient".
        public Patient(String name, String userId) {
            super(name, userId, "patient");
            this.medicalHistory = new MedicalHistory(userId);
            this.feedbacks = new ArrayList<>();
            this.prescriptions = new ArrayList<>();
            this.appointments = new ArrayList<>();
        }

        // New methods for notifications and chat.
        public void addInboxMessage(String message) {
            inboxMessages.add(message);
        }
        public void viewInbox() {
            System.out.println("Inbox for " + this.getName() + ":");
            if (inboxMessages.isEmpty()) {
                System.out.println("No messages.");
            } else {
                for (String msg : inboxMessages) {
                    System.out.println(msg);
                }
            }
        }
        public void addChatMessage(String message) {
            chatMessages.add(message);
        }
        public void viewChat() {
            System.out.println("Chat messages for " + this.getName() + ":");
            if (chatMessages.isEmpty()) {
                System.out.println("No chat messages.");
            } else {
                for (String msg : chatMessages) {
                    System.out.println(msg);
                }
            }
        }

        // Getters and setters for patient-specific fields.
        public MedicalHistory getMedicalHistory() {
            return medicalHistory;
        }
        public void setMedicalHistory(MedicalHistory medicalHistory) {
            this.medicalHistory = medicalHistory;
        }

        public List<Feedback> getFeedbacks() {
            return feedbacks;
        }
        public void setFeedbacks(List<Feedback> feedbacks) {
            this.feedbacks = feedbacks;
        }

        public List<Prescription> getPrescriptions() {
            return prescriptions;
        }
        public void setPrescriptions(List<Prescription> prescriptions) {
            this.prescriptions = prescriptions;
        }

        public List<Appointment> getAppointments() {
            return appointments;
        }
        public void setAppointments(List<Appointment> appointments) {
            this.appointments = appointments;
        }

        // VITAL UPLOADS
        // Allows the patient to enter vital signs.
        // The vitals are stored in the VitalsDatabase and also added to the patient's medical history.
        public void enterVitals(VitalSign vitals, VitalsDatabase vitalsDb) {
            if (vitals == null) {
                System.out.println("Invalid vitals provided.");
                return;
            }
            // Save the vitals under the patient's ID.
            vitalsDb.storeVitals(this.getUserId(), vitals);
            // Add a record to the medical history for reference.
            this.medicalHistory.addRecord("Vitals recorded: " + vitals);
            System.out.println("Vitals successfully recorded.");
        }

        // SCHEDULING APPOINTMENTS
        // Lets the patient manage appointments.
        // They can schedule a new appointment or view the status of existing ones.
        public void manageAppointments(AppointmentManager appointmentManager, Scanner scanner) {
            boolean continueAppointments = true;
            while (continueAppointments) {
                System.out.println("\nPatient - Manage Appointments:");
                System.out.println("1. Schedule Appointment");
                System.out.println("2. View Appointment Status");
                System.out.println("3. Back to Patient Menu");
                System.out.print("Enter choice: ");
                int subChoice = 0;
                //comvertinf our input to an integer
                try {
                    subChoice = Integer.parseInt(scanner.nextLine());
                }
                //if it isnt an int, NumberFormatException error will be throw so we need to deal wd it using catch
                catch (NumberFormatException e) {
                    System.out.println("Invalid input.");
                    continue;
                }
                switch (subChoice) {
                    case 1:
                        // Ask the patient for appointment details.
                        System.out.print("Enter appointment date (YYYY-MM-DD): ");
                        String date = scanner.nextLine();
                        System.out.print("Enter doctor name: ");
                        String dName = scanner.nextLine();
                        // Request a new appointment from the AppointmentManager.
                        Appointment app = appointmentManager.requestAppointment(this.getUserId(), dName, date);
                        if (app != null) {
                            // Add the appointment to the patient's list and log it in medical history.
                            this.appointments.add(app);
                            this.medicalHistory.addRecord("Appointment requested: " + app);
                            System.out.println("Appointment requested successfully.");
                        }
                        break;
                    case 2:
                        // Display the status of all appointments.
                        if (appointments.isEmpty()) {
                            System.out.println("No appointments scheduled.");
                        } else {
                            System.out.println("Your Appointments:");
                            for (Appointment a : appointments) {
                                System.out.println(a);
                            }
                        }
                        break;
                    case 3:
                        // Exit the appointment management menu.
                        continueAppointments = false;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            }
        }

        // VIEWING DOC FEEDBACK
        // Displays feedback and prescription details from doctors.
        // It shows which doctor provided feedback and what prescription was given.
        public void viewFeedbackAndPrescription() {
            System.out.println("\nFeedback and Prescriptions:");
            if (feedbacks.isEmpty() && prescriptions.isEmpty()) {
                System.out.println("No feedback or prescriptions available.");
            } else {
                if (!feedbacks.isEmpty()) {
                    System.out.println("Feedback:");
                    for (Feedback fb : feedbacks) {
                        System.out.println("Doctor " + fb.getDoctorId() + " says: " + fb.getFeedbackText());
                    }
                }
                if (!prescriptions.isEmpty()) {
                    System.out.println("Prescriptions:");
                    for (Prescription p : prescriptions) {
                        System.out.println("From Doctor " + p.getDoctorName() + ": " +
                                "Medication: " + p.getMedicationName() + ", Dosage: " + p.getDosage() +
                                ", Schedule: " + p.getSchedule());
                    }
                }
            }
        }
    }

    // Doctor class extends User. Does: view patient data, write feedback, prescribe medications, and manage appointments.
    public static class Doctor extends User {
        // Private list of appointments
        private List<Appointment> appointments;

        // New fields: Alerts received and chat history.
        private List<String> alertsReceived = new ArrayList<>();
        private List<String> chatMessages = new ArrayList<>();

        // Constructor initializes a doctor with name and ID.
        public Doctor(String name, String userId) {
            super(name, userId, "doctor");
            this.appointments = new ArrayList<>();
        }

        // New methods for alerts and chat.
        public void addAlert(String alert) {
            alertsReceived.add(alert);
        }
        public void viewAlerts() {
            System.out.println("Alerts for Dr. " + this.getName() + ":");
            if (alertsReceived.isEmpty()) {
                System.out.println("No alerts.");
            } else {
                for (String alert : alertsReceived) {
                    System.out.println(alert);
                }
            }
        }
        public void addChatMessage(String message) {
            chatMessages.add(message);
        }
        public void viewChat() {
            System.out.println("Chat messages for Dr. " + this.getName() + ":");
            if (chatMessages.isEmpty()) {
                System.out.println("No chat messages.");
            } else {
                for (String msg : chatMessages) {
                    System.out.println(msg);
                }
            }
        }

        // Getter and setter for the appointments list.
        public List<Appointment> getAppointments() {
            return appointments;
        }
        public void setAppointments(List<Appointment> appointments) {
            this.appointments = appointments;
        }

        // VIEWINF PATIENT DATA
        // Lets the doctor view a patient's vital signs and medical history.
        public void viewPatientData(String patientId, VitalsDatabase vitalsDb, List<Patient> patientDb) {
            // Retrieve the vitals data from the database.
            String vitals = vitalsDb.retrieveVitals(patientId);
            if (vitals != null) {
                System.out.println("Vitals for Patient " + patientId + ": ");
                System.out.println(vitals);
            } else {
                System.out.println("No vitals data available for this patient.");
            }
            // If the patient exists, display their medical history.
            Patient patient = findPatient(patientDb, patientId);
            if (patient != null) {
                System.out.println("Medical History:");
                patient.getMedicalHistory().viewHistory();
            } else {
                System.out.println("Patient not found.");
            }
        }

        // WRITING FEEDBACK
        // Allows the doctor to write feedback for a patient.
        public void writeFeedback(String feedbackText, Patient patient) {
            Feedback feedback = new Feedback(feedbackText, patient.getUserId(), this.getUserId());
            // Add the feedback to the patient's list.
            patient.getFeedbacks().add(feedback);
            // Also log the feedback in the patient's medical history.
            patient.getMedicalHistory().addRecord("Feedback given: " + feedbackText);
            System.out.println("Feedback successfully added.");
        }

        // Allows the doctor to provide a prescription for a patient.
        public void providePrescription(String medicationName, String dosage, String schedule, Patient patient) {
            // Create a new prescription with the doctor's name.
            Prescription prescription = new Prescription(this.getName(), medicationName, dosage, schedule);
            // Add the prescription to the patient's list.
            patient.getPrescriptions().add(prescription);
            // Log the prescription in the patient's medical history.
            patient.getMedicalHistory().addRecord("Prescription provided: " + prescription);
            System.out.println("Prescription provided successfully.");
        }

        // MANAGE APPOITNMENTS
        // Displays the IDs of patients who have an approved appointment with this doctor.
        public void viewPatientIDs() {
            // Instead of using HashSet, we use an ArrayList with a check to avoid duplicates.
            ArrayList<String> patientIds = new ArrayList<>();
            // Loop through the global appointment list.
            for (Appointment a : AppointmentManager.getAppointmentsGlobal()) {
                // Check if the appointment belongs to this doctor and is approved.
                if (a.getDoctorName().equalsIgnoreCase(this.getName()) && a.getStatus().equalsIgnoreCase("approved")) {
                    if (!patientIds.contains(a.getPatientId())) { // ensure uniqueness
                        patientIds.add(a.getPatientId());
                    }
                }
            }
            if (patientIds.isEmpty()) {
                System.out.println("No approved appointments found.");
            } else {
                System.out.println("Patient IDs with approved appointments:");
                for (String id : patientIds) {
                    System.out.println(id);
                }
            }
        }

        // Allows the doctor to manage a specific patient's data.
        // The doctor can view patient data, write feedback, or provide a prescription.
        public void managePatient(List<Patient> patientDb, VitalsDatabase vitalsDb, Scanner scanner) {
            boolean continueManage = true;
            System.out.print("Enter Patient ID to manage: ");
            String patId = scanner.nextLine();
            Patient patient = findPatient(patientDb, patId);
            if (patient == null) {
                System.out.println("Patient not found.");
                return;
            }
            while (continueManage) {
                System.out.println("\nManage Patient " + patId + " Menu:");
                System.out.println("1. View Patient Data");
                System.out.println("2. Write Feedback");
                System.out.println("3. Provide Prescription");
                System.out.println("4. Back");
                System.out.print("Enter choice: ");
                int choice = 0;
                try {
                    choice = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input.");
                    continue;
                }
                switch (choice) {
                    case 1:
                        viewPatientData(patId, vitalsDb, patientDb);
                        break;
                    case 2:
                        System.out.print("Enter feedback: ");
                        String feedback = scanner.nextLine();
                        writeFeedback(feedback, patient);
                        break;
                    case 3:
                        System.out.print("Enter medication name: ");
                        String medName = scanner.nextLine();
                        System.out.print("Enter dosage: ");
                        String dosage = scanner.nextLine();
                        System.out.print("Enter schedule: ");
                        String schedule = scanner.nextLine();
                        providePrescription(medName, dosage, schedule, patient);
                        break;
                    case 4:
                        continueManage = false;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            }
        }

        // Allows the doctor to manage pending appointments using an index-based input system.
        // For example, the doctor can input "1A 2C" to approve the first appointment and cancel the second.
        // A would mean accpet and C cancel
        public void manageAppointments(AppointmentManager appointmentManager, Scanner scanner) {
            List<Appointment> pendingApps = new ArrayList<>();
            // Loop through the global appointment list to find pending appointments for this doctor.
            for (Appointment a : AppointmentManager.getAppointmentsGlobal()) {
                if (a.getDoctorName().equalsIgnoreCase(this.getName()) && a.getStatus().equalsIgnoreCase("pending")) {
                    pendingApps.add(a);
                }
            }
            if (pendingApps.isEmpty()) {
                System.out.println("No pending appointments.");
                return;
            }
            // Display each pending appointment with an index.
            System.out.println("Pending Appointments:");
            for (int i = 0; i < pendingApps.size(); i++) {
                System.out.println((i + 1) + ". " + pendingApps.get(i));
            }
            System.out.println("Enter your decisions using index and action (e.g., '1A 2C 3A' where A=Approve, C=Cancel):");
            String decisions = scanner.nextLine();
            String[] tokens = decisions.split("\\s+");
            // Process each token: the number is the index and the letter is the action.
            for (String token : tokens) {
                if (token.length() < 2)
                    continue;
                try {
                    int index = Integer.parseInt(token.substring(0, token.length() - 1));
                    String action = token.substring(token.length() - 1);
                    // Check if the index is within valid range.
                    if (index < 1 || index > pendingApps.size()) {
                        System.out.println("Invalid index: " + index);
                        continue;
                    }
                    Appointment app = pendingApps.get(index - 1);
                    // Based on the action letter, update the appointment's status.
                    if (action.equalsIgnoreCase("A")) {
                        app.setStatus("approved");
                        SystemLogs.addLog("Appointment approved by Doctor " + this.getName() + ": " + app);
                    } else if (action.equalsIgnoreCase("C")) {
                        app.setStatus("cancelled");
                        SystemLogs.addLog("Appointment cancelled by Doctor " + this.getName() + ": " + app);
                    } else {
                        System.out.println("Invalid action for appointment index " + index);
                    }
                } catch (Exception e) {
                    System.out.println("Error processing token: " + token);
                }
            }
            System.out.println("Appointment statuses updated.");
        }
    }

    // -------------------------------
    // System Logs Class
    // This class acts like a simple diary that logs events in our system.
    // We can add events (logs) and display them later to see what actions have occurred.
    // -------------------------------
    public static class SystemLogs {
        private static List<String> logs = new ArrayList<>();

        // Adds a log entry with the current date and time.
        public static void addLog(String log) {
            logs.add(new Date() + ": " + log);
        }

        // Displays all log entries, or a message if there are none.
        public static void displayLogs() {
            if (logs.isEmpty()) {
                System.out.println("No logs available.");
            } else {
                for (String log : logs) {
                    System.out.println(log);
                }
            }
        }
    }

    // -------------------------------
    // Administrator Class
    // This class now extends User, but we do not use an ID for administrators.
    // We simply pass an empty string for the ID when calling the super constructor.
    // The admin can manage doctors, manage patients, and view all user IDs.
    // -------------------------------
    public static class Administrator extends User {
        // Constructor for Administrator. We pass an empty string for the ID.
        public Administrator(String name) {
            super(name, "", "admin");
        }

        // MANAGE DOCTOR
        // Manage doctors: add, remove, or update a doctor.
        public void manageDoctors(List<Doctor> doctorDb, Doctor doctorObj, String action) {
            // Check if a doctor with the same ID exists.
            Doctor existing = findDoctor(doctorDb, doctorObj.getUserId());
            if (action.equalsIgnoreCase("add")) {
                if (existing != null) {
                    System.out.println("Doctor already exists.");
                } else {
                    doctorDb.add(doctorObj);
                    SystemLogs.addLog("Doctor added: " + doctorObj);
                    System.out.println("Doctor added successfully.");
                }
            } else if (action.equalsIgnoreCase("remove")) {
                if (existing != null) {
                    doctorDb.remove(existing);
                    SystemLogs.addLog("Doctor removed: " + doctorObj);
                    System.out.println("Doctor removed successfully.");
                } else {
                    System.out.println("Doctor not found.");
                }
            } else if (action.equalsIgnoreCase("update")) {
                if (existing != null) {
                    doctorDb.remove(existing);
                    doctorDb.add(doctorObj);
                    SystemLogs.addLog("Doctor updated: " + doctorObj);
                    System.out.println("Doctor updated successfully.");
                } else {
                    System.out.println("Doctor not found.");
                }
            } else {
                System.out.println("Invalid action for managing doctors.");
            }
        }

        // MANAGE PATIENTS: add, remove, or update a patient.
        public void managePatients(List<Patient> patientDb, Patient patientObj, String action) {
            Patient existing = findPatient(patientDb, patientObj.getUserId());
            if (action.equalsIgnoreCase("add")) {
                if (existing != null) {
                    System.out.println("Patient already exists.");
                } else {
                    patientDb.add(patientObj);
                    SystemLogs.addLog("Patient added: " + patientObj);
                    System.out.println("Patient added successfully.");
                }
            } else if (action.equalsIgnoreCase("remove")) {
                if (existing != null) {
                    patientDb.remove(existing);
                    SystemLogs.addLog("Patient removed: " + patientObj);
                    System.out.println("Patient removed successfully.");
                } else {
                    System.out.println("Patient not found.");
                }
            } else if (action.equalsIgnoreCase("update")) {
                if (existing != null) {
                    patientDb.remove(existing);
                    patientDb.add(patientObj);
                    SystemLogs.addLog("Patient updated: " + patientObj);
                    System.out.println("Patient updated successfully.");
                } else {
                    System.out.println("Patient not found.");
                }
            } else {
                System.out.println("Invalid action for managing patients.");
            }
        }

        // Displays all registered Patient IDs and Doctor IDs.
        public void viewAllIDs(List<Patient> patientDb, List<Doctor> doctorDb) {
            System.out.println("\n--- Registered Patient IDs ---");
            if (patientDb.isEmpty()) {
                System.out.println("No patients registered.");
            } else {
                for (Patient p : patientDb) {
                    System.out.println(p.getUserId());
                }
            }
            System.out.println("\n--- Registered Doctor IDs ---");
            if (doctorDb.isEmpty()) {
                System.out.println("No doctors registered.");
            } else {
                for (Doctor d : doctorDb) {
                    System.out.println(d.getUserId());
                }
            }
        }
    }

    // -------------------------------
    // 2. Health Data Handling Classes
    // -------------------------------

    // Class to store a patient's vital signs.
    public static class VitalSign {
        private int heartRate;
        private int oxygenLevel;
        private String bloodPressure;
        private double temperature;

        // Constructor initializes vital sign values.
        public VitalSign(int heartRate, int oxygenLevel, String bloodPressure, double temperature) {
            this.heartRate = heartRate;
            this.oxygenLevel = oxygenLevel;
            this.bloodPressure = bloodPressure;
            this.temperature = temperature;
        }

        // Getters and setters for each vital sign.
        public int getHeartRate() {
            return heartRate;
        }
        public void setHeartRate(int heartRate) {
            this.heartRate = heartRate;
        }

        public int getOxygenLevel() {
            return oxygenLevel;
        }
        public void setOxygenLevel(int oxygenLevel) {
            this.oxygenLevel = oxygenLevel;
        }

        public String getBloodPressure() {
            return bloodPressure;
        }
        public void setBloodPressure(String bloodPressure) {
            this.bloodPressure = bloodPressure;
        }

        public double getTemperature() {
            return temperature;
        }
        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        // Returns a formatted string representing the vital signs.
        @Override
        public String toString() {
            return "Heart Rate: " + heartRate + " bpm, Oxygen Level: " + oxygenLevel +
                    "%, Blood Pressure: " + bloodPressure + " mmHg, Temperature: " + temperature + "°C";
        }
    }

    // Class to manage storage and retrieval of vital signs for patients.
    public static class VitalsDatabase {
        // we use a list to store entries.
        // Each entry is a pair represented by an array of two elements: [patientId, List<VitalSign>]
        private List<Object[]> vitalsData;

        // Constructor initializes the list.
        public VitalsDatabase() {
            vitalsData = new ArrayList<>();
        }

        // Stores a new set of vitals for a patient.
        public void storeVitals(String patientId, VitalSign vitals) {
            List<VitalSign> records = null;
            // Search for existing record.
            for (Object[] entry : vitalsData) {
                if (entry[0].equals(patientId)) {
                    records = (List<VitalSign>) entry[1];
                    break;
                }
            }
            if (records == null) {
                records = new ArrayList<>();
                vitalsData.add(new Object[]{patientId, records});
            }
            records.add(vitals);
        }

        // Retrieves all vital records for a given patient.
        public String retrieveVitals(String patientId) {
            for (Object[] entry : vitalsData) {
                if (entry[0].equals(patientId)) {
                    List<VitalSign> records = (List<VitalSign>) entry[1];
                    //stringbuilder helps us modify a string whch is immutable without creaitng new string obj
                    StringBuilder sb = new StringBuilder();
                    for (VitalSign v : records) {
                        sb.append(v.toString()).append("\n");
                    }
                    return sb.toString();
                }
            }
            return null;
        }
    }

    // -------------------------------
    // 3. Appointment Scheduling Classes
    // -------------------------------

    // Class representing an appointment with its details.
    public static class Appointment {
        private String appointmentDate;
        private String doctorName;
        private String patientId;
        private String status;

        // Constructor initializes appointment details.
        public Appointment(String appointmentDate, String doctorName, String patientId, String status) {
            this.appointmentDate = appointmentDate;
            this.doctorName = doctorName;
            this.patientId = patientId;
            this.status = status;
        }

        // Getters and setters for appointment details.
        public String getAppointmentDate() {
            return appointmentDate;
        }
        public void setAppointmentDate(String appointmentDate) {
            this.appointmentDate = appointmentDate;
        }

        public String getDoctorName() {
            return doctorName;
        }
        public void setDoctorName(String doctorName) {
            this.doctorName = doctorName;
        }

        public String getPatientId() {
            return patientId;
        }
        public void setPatientId(String patientId) {
            this.patientId = patientId;
        }

        public String getStatus() {
            return status;
        }
        public void setStatus(String status) {
            this.status = status;
        }

        // Returns a readable string for the appointment.
        @Override
        public String toString() {
            return "Appointment with Dr. " + doctorName + " on " + appointmentDate + ", Status: " + status;
        }
    }

    // Class to handle appointment scheduling, approval, and cancellation.
    public static class AppointmentManager {
        // Global list to store all appointments so that changes are seen by both patients and doctors.
        private static List<Appointment> appointmentsGlobal = new ArrayList<>();

        // Getter and setter for the global appointments list.
        public static List<Appointment> getAppointmentsGlobal() {
            return appointmentsGlobal;
        }
        public static void setAppointmentsGlobal(List<Appointment> appointmentsGlobal) {
            AppointmentManager.appointmentsGlobal = appointmentsGlobal;
        }

        // Creates a new appointment and adds it to the global list.
        public Appointment requestAppointment(String patientId, String doctorName, String date) {
            Appointment app = new Appointment(date, doctorName, patientId, "pending");
            appointmentsGlobal.add(app);

            // Schedule a reminder 1 day before the appointment
            try {
                LocalDateTime apptDateTime = LocalDate.parse(date).atStartOfDay();
                long delay = ChronoUnit.MILLIS.between(LocalDateTime.now(), apptDateTime.minusDays(1));
                if (delay > 0) {
                    String recipient = findPatient(globalPatientDb, patientId).getName();
                    String msg = "You have an appointment with Dr. " + doctorName + " on " + date;
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            new ReminderService().scheduleAppointmentReminder(recipient, msg, 0);
                        }
                    }, delay);
                }
            } catch (Exception e) {
                System.out.println("Failed to schedule appointment reminder: " + e.getMessage());
            }

            return app;
        }

        // Finds an appointment matching the details and sets its status to approved.
        public void approveAppointment(String patientId, String doctorName, String date) {
            for (Appointment app : appointmentsGlobal) {
                if (app.getPatientId().equals(patientId) && app.getDoctorName().equals(doctorName)
                        && app.getAppointmentDate().equals(date)) {
                    app.setStatus("approved");
                    return;
                }
            }
            System.out.println("Appointment not found to approve.");
        }

        // Finds an appointment matching the details and sets its status to cancelled.
        public void cancelAppointment(String patientId, String doctorName, String date) {
            boolean found = false;
            for (Appointment app : appointmentsGlobal) {
                if (app.getPatientId().equals(patientId) && app.getDoctorName().equals(doctorName)
                        && app.getAppointmentDate().equals(date)) {
                    app.setStatus("cancelled");
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("Appointment not found to cancel.");
            }
        }
    }

    // -------------------------------
    // 4. Doctor-Patient Interaction Classes
    // -------------------------------
    // Class to store feedback from a doctor for a patient.
    public static class Feedback {
        private String feedbackText;
        private String patientId;
        private String doctorId;

        // Constructor initializes the feedback details.
        public Feedback(String feedbackText, String patientId, String doctorId) {
            this.feedbackText = feedbackText;
            this.patientId = patientId;
            this.doctorId = doctorId;
        }

        // Getters and setters for feedback details.
        public String getFeedbackText() {
            return feedbackText;
        }
        public void setFeedbackText(String feedbackText) {
            this.feedbackText = feedbackText;
        }

        public String getPatientId() {
            return patientId;
        }
        public void setPatientId(String patientId) {
            this.patientId = patientId;
        }

        public String getDoctorId() {
            return doctorId;
        }
        public void setDoctorId(String doctorId) {
            this.doctorId = doctorId;
        }

        // Returns a simple string representation of the feedback.
        @Override
        public String toString() {
            return "Feedback from Doctor " + doctorId + ": " + feedbackText;
        }
    }

    // Class to store prescription details provided by a doctor.
    public static class Prescription {
        private String doctorName;
        private String medicationName;
        private String dosage;
        private String schedule;

        // Constructor initializes the prescription details.
        public Prescription(String doctorName, String medicationName, String dosage, String schedule) {
            this.doctorName = doctorName;
            this.medicationName = medicationName;
            this.dosage = dosage;
            this.schedule = schedule;
        }

        // Getters and setters for prescription details.
        public String getDoctorName() {
            return doctorName;
        }
        public void setDoctorName(String doctorName) {
            this.doctorName = doctorName;
        }

        public String getMedicationName() {
            return medicationName;
        }
        public void setMedicationName(String medicationName) {
            this.medicationName = medicationName;
        }

        public String getDosage() {
            return dosage;
        }
        public void setDosage(String dosage) {
            this.dosage = dosage;
        }

        public String getSchedule() {
            return schedule;
        }
        public void setSchedule(String schedule) {
            this.schedule = schedule;
        }

        // Returns a formatted string with the prescription details.
        @Override
        public String toString() {
            return "From Doctor " + doctorName + " - Medication: " + medicationName + ", Dosage: " + dosage + ", Schedule: " + schedule;
        }
    }

    // Class to maintain a patient's medical history.
    public static class MedicalHistory {
        private String patientId;
        private List<String> historyRecords;

        // Constructor creates an empty history for a patient.
        public MedicalHistory(String patientId) {
            this.patientId = patientId;
            this.historyRecords = new ArrayList<>();
        }

        // Getters and setters for medical history fields.
        public String getPatientId() {
            return patientId;
        }
        public void setPatientId(String patientId) {
            this.patientId = patientId;
        }

        public List<String> getHistoryRecords() {
            return historyRecords;
        }
        public void setHistoryRecords(List<String> historyRecords) {
            this.historyRecords = historyRecords;
        }

        // Adds a new record to the patient's history.
        public void addRecord(String record) {
            historyRecords.add(new Date() + ": " + record);
        }

        // Prints out the entire medical history.
        public void viewHistory() {
            if (historyRecords.isEmpty()) {
                System.out.println("No medical history available.");
            } else {
                System.out.println("Medical History for Patient " + patientId + ":");
                for (String record : historyRecords) {
                    System.out.println(record);
                }
            }
        }
    }

    // -------------------------------
    // 5. Main Program Menu
    // -------------------------------
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        VitalsDatabase vitalsDb = new VitalsDatabase();
        AppointmentManager appointmentManager = new AppointmentManager();
        List<Patient> patientDb = new ArrayList<>();
        List<Doctor> doctorDb = new ArrayList<>();

        // Add a fake patient and doctor for testing.
        Patient patient1 = new Patient("Gulwarina", "P001");
        Doctor doctor1 = new Doctor("Muska Saleem", "D001");
        patientDb.add(patient1);
        doctorDb.add(doctor1);

        // Populate global databases used by the new notification/chat enhancements.
        globalPatientDb = patientDb;
        globalDoctorDb = doctorDb;

        boolean mainLoop = true;
        while (mainLoop) {
            System.out.println("\n=== Healthcare Management System ===");
            System.out.println("Select user role:");
            System.out.println("1. Patient");
            System.out.println("2. Doctor");
            System.out.println("3. Administrator");
            System.out.println("4. Extra Features");
            System.out.println("5. Exit");
            System.out.print("Enter choice: ");
            int choice = 0;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    System.out.print("Enter Patient ID: ");
                    String pId = scanner.nextLine();
                    Patient currentPatient = findPatient(patientDb, pId);
                    if (currentPatient == null) {
                        System.out.println("Patient not found.");
                        break;
                    }
                    boolean patientMenu = true;
                    while (patientMenu) {
                        System.out.println("\nWelcome, " + currentPatient.getName());
                        System.out.println("Patient Menu:");
                        System.out.println("1. Enter Vitals");
                        System.out.println("2. Manage Appointments");
                        System.out.println("3. View Feedback and Prescription");
                        System.out.println("4. View Medical History");
                        System.out.println("5. View Inbox (Alerts/Reminders)");
                        System.out.println("6. View Chat Messages");
                        System.out.println("7. Back to Main Menu");
                        System.out.print("Enter choice: ");
                        int pChoice = 0;
                        try {
                            pChoice = Integer.parseInt(scanner.nextLine());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input.");
                            continue;
                        }
                        switch (pChoice) {
                            case 1:
                                try {
                                    System.out.print("Enter heart rate: ");
                                    int hr = Integer.parseInt(scanner.nextLine());
                                    System.out.print("Enter oxygen level: ");
                                    int ox = Integer.parseInt(scanner.nextLine());
                                    System.out.print("Enter blood pressure (e.g., 120/80): ");
                                    String bp = scanner.nextLine();
                                    System.out.print("Enter temperature: ");
                                    double temp = Double.parseDouble(scanner.nextLine());
                                    VitalSign vitals = new VitalSign(hr, ox, bp, temp);
                                    currentPatient.enterVitals(vitals, vitalsDb);
                                } catch (Exception e) {
                                    System.out.println("Error in input: " + e.getMessage());
                                }
                                break;
                            case 2:
                                currentPatient.manageAppointments(appointmentManager, scanner);
                                break;
                            case 3:
                                currentPatient.viewFeedbackAndPrescription();
                                break;
                            case 4:
                                currentPatient.getMedicalHistory().viewHistory();
                                break;
                            case 5:
                                currentPatient.viewInbox();
                                break;
                            case 6:
                                currentPatient.viewChat();
                                break;
                            case 7:
                                patientMenu = false;
                                break;
                            default:
                                System.out.println("Invalid choice.");
                        }
                    }
                    break;

                case 2:
                    System.out.print("Enter Doctor ID: ");
                    String dId = scanner.nextLine();
                    Doctor currentDoctor = findDoctor(doctorDb, dId);
                    if (currentDoctor == null) {
                        System.out.println("Doctor not found.");
                        break;
                    }
                    boolean doctorMenu = true;
                    while (doctorMenu) {
                        System.out.println("\nWelcome, " + currentDoctor.getName());
                        System.out.println("Doctor Menu:");
                        System.out.println("1. View Patient IDs (approved appointments)");
                        System.out.println("2. Manage Patient");
                        System.out.println("3. Manage Appointments");
                        System.out.println("4. View Alerts");
                        System.out.println("5. View Chat Messages");
                        System.out.println("6. Back to Main Menu");
                        System.out.print("Enter choice: ");
                        int dChoice = 0;
                        try {
                            dChoice = Integer.parseInt(scanner.nextLine());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input.");
                            continue;
                        }
                        switch (dChoice) {
                            case 1:
                                currentDoctor.viewPatientIDs();
                                break;
                            case 2:
                                currentDoctor.managePatient(patientDb, vitalsDb, scanner);
                                break;
                            case 3:
                                currentDoctor.manageAppointments(appointmentManager, scanner);
                                break;
                            case 4:
                                currentDoctor.viewAlerts();
                                break;
                            case 5:
                                currentDoctor.viewChat();
                                break;
                            case 6:
                                doctorMenu = false;
                                break;
                            default:
                                System.out.println("Invalid choice.");
                        }
                    }
                    break;

                case 3:
                    Administrator admin = new Administrator("Admin");
                    boolean adminMenu = true;
                    while (adminMenu) {
                        System.out.println("\nAdministrator Menu:");
                        System.out.println("1. Manage Doctors");
                        System.out.println("2. Manage Patients");
                        System.out.println("3. View System Logs");
                        System.out.println("4. View All IDs");
                        System.out.println("5. Back to Main Menu");
                        System.out.print("Enter choice: ");
                        int aChoice = 0;
                        try {
                            aChoice = Integer.parseInt(scanner.nextLine());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input.");
                            continue;
                        }
                        switch (aChoice) {
                            case 1:
                                System.out.print("Enter action (add/remove/update): ");
                                String actionD = scanner.nextLine();
                                System.out.print("Enter Doctor ID: ");
                                String docId = scanner.nextLine();
                                System.out.print("Enter Doctor Name: ");
                                String docName = scanner.nextLine();
                                Doctor doc = new Doctor(docName, docId);
                                admin.manageDoctors(doctorDb, doc, actionD);
                                break;
                            case 2:
                                System.out.print("Enter action (add/remove/update): ");
                                String actionP = scanner.nextLine();
                                System.out.print("Enter Patient ID: ");
                                String patId = scanner.nextLine();
                                System.out.print("Enter Patient Name: ");
                                String patName = scanner.nextLine();
                                Patient pat = new Patient(patName, patId);
                                admin.managePatients(patientDb, pat, actionP);
                                break;
                            case 3:
                                SystemLogs.displayLogs();
                                break;
                            case 4:
                                admin.viewAllIDs(patientDb, doctorDb);
                                break;
                            case 5:
                                adminMenu = false;
                                break;
                            default:
                                System.out.println("Invalid choice.");
                        }
                    }
                    break;

                case 4:
                    runExtraFeatures(scanner);
                    break;

                case 5:
                    System.out.println("Exiting system. Goodbye!");
                    mainLoop = false;
                    break;

                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
        scanner.close();
    }

    // -------------------------------
    // New Extra Features Menu for added modules
    // -------------------------------
    private static void runExtraFeatures(Scanner scanner) {
        EmergencyAlert emergencyAlert = new EmergencyAlert();
        PanicButton panicButton = new PanicButton();
        ChatServer chatServer = new ChatServer();
        ReminderService reminderService = new ReminderService();
        VideoCall videoCall = new VideoCall();

        boolean extraLoop = true;
        while (extraLoop) {
            System.out.println("\n--- Extra Features Menu ---");
            System.out.println("1. Emergency Alert (Check Vitals)");
            System.out.println("2. Press Panic Button");
            System.out.println("3. Chat");
            System.out.println("4. Start Video Call");
            System.out.println("5. Send Appointment Reminders");
            System.out.println("6. Send Medicine Notifications ");
            System.out.println("7. Back to Main Menu");
            System.out.print("Enter choice: ");
            int extraChoice = 0;
            try {
                extraChoice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (extraChoice) {
                case 1:
                    System.out.print("Enter heart rate: ");
                    int hr = Integer.parseInt(scanner.nextLine());
                    System.out.print("Enter blood pressure (as an integer value): ");
                    int bp = Integer.parseInt(scanner.nextLine());
                    System.out.print("Enter oxygen level: ");
                    int ox = Integer.parseInt(scanner.nextLine());
                    System.out.print("Enter patient name: ");
                    String patName = scanner.nextLine();
                    emergencyAlert.checkVitals(patName, hr, bp, ox);
                    break;
                case 2:
                    System.out.print("Enter Doctor name for this patient to alert the doctor : ");
                    String doctorName = scanner.nextLine();
                    System.out.print("Enter patient name: ");
                    String patientName = scanner.nextLine();
                    panicButton.pressButton(patientName, doctorName);
                    break;
                case 3:
                    ChatClient chatClient = new ChatClient(chatServer);
                    System.out.print("Enter your name (sender): ");
                    String sender = scanner.nextLine();
                    System.out.print("Enter receiver name: ");
                    String receiver = scanner.nextLine();
                    System.out.print("Type your message: ");
                    String message = scanner.nextLine();
                    chatClient.sendMessage(sender, receiver, message);
                    chatServer.showMessages();
                    break;
                case 4:
                    System.out.print("Enter patient name: ");
                    String vcPatient = scanner.nextLine();
                    System.out.print("Enter doctor name: ");
                    String vcDoctor = scanner.nextLine();
                    videoCall.startCall(vcPatient, vcDoctor);
                    break;
                case 5:
                    System.out.print("Enter recipient email for appointment reminders: ");
                    String recipient = scanner.nextLine();
                    System.out.print("Enter appointment time (e.g., 3 PM): ");
                    String time = scanner.nextLine();
                    reminderService.sendAppointmentReminder(recipient, time);
                    break;
                case 6:
                    System.out.print("Enter recipient name for notification reminders: ");
                    String recipient1 = scanner.nextLine();
                    System.out.print("Reminder is sent ");
                    reminderService.sendMedicationReminder(recipient1);
                    break;
                case 7:
                    extraLoop = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
        }
    }
}

// -------------------------------
// New Modules and Classes to be Integrated
// -------------------------------

// Notifiable interface for consistent notification behavior.
interface Notifiable {
    void sendNotification(String recipient, String message);
}

// EmailNotification class implementing Notifiable with JavaMail API.
class EmailNotification implements Notifiable {
    @Override
    public void sendNotification(String recipient, String message) {
        try {
            JavaMailUtil.sendMail(recipient);
            main.addNotification(recipient, "Email Alert: " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


// SMSNotification class implementing Notifiable.
class SMSNotification implements Notifiable {
    @Override
    public void sendNotification(String recipient, String message) {
        System.out.println("SMS sent to " + recipient + ": " + message);
    }
}

// NotificationService uses Notifiable to dispatch notifications.
class NotificationService {
    Notifiable emailNotifier = new EmailNotification();
    Notifiable smsNotifier = new SMSNotification();

    public void sendEmailAlert(String recipient, String message) {
        emailNotifier.sendNotification(recipient, message);
        main.addNotification(recipient, "Email Alert: " + message);
    }

    public void sendSMSAlert(String recipient, String message) {
        smsNotifier.sendNotification(recipient, message);
        main.addNotification(recipient, "SMS Alert: " + message);
    }
}

// EmergencyAlert monitors vitals and triggers alerts if abnormal values are found.
class EmergencyAlert {
    private static final int HEART_RATE_WARNING = 100;
    private static final int HEART_RATE_CRITICAL = 120;
    private static final int BLOOD_PRESSURE_CRITICAL = 180;
    private static final int OXYGEN_LOW = 90;

    NotificationService notificationService = new NotificationService();

    public void checkVitals(String patientName, int heartRate, int bloodPressure, int oxygenLevel) {
        if (heartRate > HEART_RATE_CRITICAL || bloodPressure > BLOOD_PRESSURE_CRITICAL || oxygenLevel < OXYGEN_LOW) {
            System.out.println("!!! EMERGENCY ALERT !!!");
            System.out.println("Patient " + patientName + " has abnormal vitals.");
            notificationService.sendSMSAlert(patientName, "Immediate attention needed.");
        } else if (heartRate > HEART_RATE_WARNING) {
            System.out.println("Warning: Increased heart rate detected for " + patientName);
        } else {
            System.out.println("Vitals are normal for " + patientName + ":)");
        }
    }
}

// PanicButton class triggers an immediate alert when pressed.
class PanicButton {
    NotificationService notificationService = new NotificationService();

    public void pressButton(String patientName, String doctorName) {
        System.out.println("Panic button pressed for " + patientName);
        notificationService.sendSMSAlert(patientName, "You triggered the panic button, please wait for the doctor.");
        notificationService.sendSMSAlert(doctorName, "Immediate response needed for " + patientName);
    }
}

// ChatServer handles message transfer between patient and doctor.
class ChatServer {
    ArrayList<String> messages = new ArrayList<>();

    public void receiveMessage(String message) {
        messages.add(message);
        System.out.println("Server received: " + message);
    }

    public void showMessages() {
        System.out.println("Chat History:");
        for (String msg : messages) {
            System.out.println(msg);
        }
    }
}

// ChatClient handles input/output from console and communicates with ChatServer.
class ChatClient {
    ChatServer server;

    public ChatClient(ChatServer server) {
        this.server = server;
    }

    // Existing method for one-way message sending.
    public void sendMessage(String user, String message) {
        String fullMessage = user + ": " + message;
        server.receiveMessage(fullMessage);
    }

    // Overloaded method to send a message from sender to receiver.
    public void sendMessage(String sender, String receiver, String message) {
        String fullMessage = sender + " to " + receiver + ": " + message;
        server.receiveMessage(fullMessage);
        main.addChatMessage(sender, fullMessage);
        main.addChatMessage(receiver, fullMessage);
    }

    public void receiveMessage(String message) {
        System.out.println("New message: " + message);
    }
}

// VideoCall simulates a video call by printing a join link.
class VideoCall {
    public void startCall(String patientName, String doctorName) {
        System.out.println("Starting video call between " + patientName + " and Dr. " + doctorName);
        System.out.println("Join link: https://zoom.fakemeeting.com/" + patientName + "-to-" + doctorName);
    }
}

// ReminderService sends reminders for medication and appointments.
class ReminderService {
    Notifiable emailNotifier = new EmailNotification();
    Notifiable smsNotifier = new SMSNotification();

    public void sendMedicationReminder(String recipient) {
        String message = "Take your meds";
        smsNotifier.sendNotification(recipient, message);
        main.addNotification(recipient, "Medication Reminder: " + message);
    }

    public void sendAppointmentReminder(String recipient, String time) {
        String message = "You have an appointment at " + time;
        emailNotifier.sendNotification(recipient, message);
        smsNotifier.sendNotification(recipient, message);
        main.addNotification(recipient, "Appointment Reminder: " + message);
    }

    // Scheduling method used internally for automated reminders
    public void scheduleAppointmentReminder(String recipient, String message, long delayMillis) {
        new Timer().schedule(new TimerTask() {
            @Override public void run() {
                emailNotifier.sendNotification(recipient, message);
                smsNotifier.sendNotification(recipient, message);
                main.addNotification(recipient, "Scheduled Appointment Reminder: " + message);
            }
        }, delayMillis);
    }
}

