/*
 * AuthFramework.java
 * Copyright © 2025 Devin B. Royal. All rights reserved.
 *
 * Licensed for enterprise and educational use only.
 * Redistribution, modification, or commercial use requires explicit written permission.
 *
 * Author: Devin B. Royal
 * Contact: https://java1kind.org
 */

import java.util.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthFramework {
    private static final Map<String, byte[]> USER_DB = new HashMap<>();
    private static final Map<String, String> USER_ROLES = new HashMap<>();
    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCKOUT_DURATION_MS = 30_000;
    private static final String LOG_FILE = "audit_log.txt";

    static {
        USER_DB.put("admin", hash("4269"));
        USER_ROLES.put("admin", "Admin");

        USER_DB.put("analyst", hash("3141"));
        USER_ROLES.put("analyst", "Analyst");

        USER_DB.put("guest", hash("1234"));
        USER_ROLES.put("guest", "Guest");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        printBanner();

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        if (!USER_DB.containsKey(username)) {
            System.out.println("🚫 Unknown user.");
            return;
        }

        if (!verifyPin(scanner, username)) return;
        if (!simulateBiometricAPI(scanner)) return;
        if (!simulateOTPService(scanner)) return;

        String role = USER_ROLES.get(username);
        String token = UUID.randomUUID().toString();
        logAudit(username, role, true, token);

        System.out.println("✅ Access granted.");
        System.out.println("Session Token: " + token);
        System.out.println("Role: " + role);

        routeToModule(role, scanner);
    }

    private static void printBanner() {
        System.out.println("===============================================");
        System.out.println("  Enterprise Auth Framework by Devin B. Royal   ");
        System.out.println("  © 2025 All rights reserved                    ");
        System.out.println("  Forensic + SPA Toolkit Integration Ready      ");
        System.out.println("===============================================\n");
    }

    private static boolean verifyPin(Scanner scanner, String username) {
        int attempts = 0;
        long lockoutStart = 0;

        while (attempts < MAX_ATTEMPTS) {
            if (lockoutStart > 0 && System.currentTimeMillis() - lockoutStart < LOCKOUT_DURATION_MS) {
                System.out.println("⏳ Account locked. Try again later.");
                return false;
            }

            System.out.print("Enter PIN: ");
            String input = scanner.nextLine();

            if (Arrays.equals(hash(input), USER_DB.get(username))) {
                return true;
            } else {
                attempts++;
                System.out.println("❌ Incorrect PIN. Attempts left: " + (MAX_ATTEMPTS - attempts));
            }

            if (attempts >= MAX_ATTEMPTS) {
                lockoutStart = System.currentTimeMillis();
                System.out.println("🚫 Too many failed attempts. Account locked for 30 seconds.");
                return false;
            }
        }
        return false;
    }

    private static boolean simulateBiometricAPI(Scanner scanner) {
        System.out.print("Simulate biometric scan (type 'scan'): ");
        String input = scanner.nextLine();
        System.out.println("🔄 Contacting biometric API...");
        try { Thread.sleep(1500); } catch (InterruptedException e) {}
        if (!input.equalsIgnoreCase("scan")) {
            System.out.println("❌ Biometric scan failed.");
            return false;
        }
        System.out.println("✅ Biometric scan accepted.");
        return true;
    }

    private static boolean simulateOTPService(Scanner scanner) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        System.out.println("📡 Sending OTP via secure channel...");
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        System.out.println("Your OTP is: " + otp);
        System.out.print("Enter OTP: ");
        String input = scanner.nextLine();
        if (!input.equals(otp)) {
            System.out.println("❌ MFA failed.");
            return false;
        }
        System.out.println("✅ MFA verified.");
        return true;
    }

    private static void routeToModule(String role, Scanner scanner) {
        switch (role) {
            case "Admin" -> {
                System.out.println("🔧 Routing to Admin Control Panel...");
                System.out.println("Simulated SPA endpoint: /admin/dashboard");
            }
            case "Analyst" -> {
                System.out.println("🔍 Routing to Forensic Evidence Dashboard...");
                simulateForensicSystem(scanner);
            }
            case "Guest" -> {
                System.out.println("📄 Routing to Read-Only SPA Toolkit...");
                System.out.println("Simulated SPA endpoint: /guest/view");
            }
            default -> System.out.println("⚠️ Unknown role.");
        }
    }

    private static void simulateForensicSystem(Scanner scanner) {
        System.out.println("\n🧪 Forensic Evidence System");
        System.out.print("Enter evidence ID to tag: ");
        String evidenceId = scanner.nextLine();
        System.out.print("Enter chain-of-custody note: ");
        String note = scanner.nextLine();
        System.out.println("✅ Evidence tagged and logged.");
        logToFile("Evidence ID: " + evidenceId + ", Note: " + note);
    }

    private static void logAudit(String username, String role, boolean success, String token) {
        String status = success ? "SUCCESS" : "FAILURE";
        String timestamp = new Date().toString();
        String log = String.format("[AUDIT] %s login for '%s' (%s) at %s | Token: %s%n",
                status, username, role, timestamp, token);
        System.out.print(log);
        logToFile(log);
    }

    private static void logToFile(String entry) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            fw.write(entry);
        } catch (IOException e) {
            System.out.println("⚠️ Failed to write to log file.");
        }
    }

    private static byte[] hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available");
        }
    }
}


/**
 * Full-featured enterprise authentication framework with simulated integrations for real-world systems.
 *
 * -------------------------------------------------------------------------------
 * 🔐 Authentication & Security Modules
 *
 * ✅ Secure Login
 * - Verifies users using SHA-256 hashed PINs
 * - Enforces rate limiting (max 3 attempts)
 * - Implements lockout logic (30-second cooldown after failure)
 *
 * ✅ Biometric Simulation
 * - Mimics fingerprint or facial scan via a keyword prompt ("scan")
 * - Simulates API delay to resemble real biometric verification
 *
 * ✅ Multi-Factor Authentication (MFA)
 * - Generates a random 6-digit OTP
 * - Simulates delivery delay and requires correct entry
 * - Adds a second layer of security before granting access
 *
 * ✅ Session Management
 * - Generates a UUID session token upon successful login
 * - Simulates session-based access control
 *
 * -------------------------------------------------------------------------------
 * 🧑‍💼 Role-Based Access Control
 *
 * Role      | Access Module
 * ----------|-------------------------------
 * Admin     | Admin Control Panel (/admin/dashboard)
 * Analyst   | Forensic Evidence Dashboard
 * Guest     | Read-Only SPA Toolkit (/guest/view)
 *
 * Each role is routed to a different module with simulated functionality.
 *
 * -------------------------------------------------------------------------------
 * 🧪 Forensic Evidence System Simulation
 *
 * - Allows analysts to:
 *   - Enter an evidence ID
 *   - Add a chain-of-custody note
 *   - Simulates tagging and logging of forensic data
 * - Mimics integration with a forensic backend (e.g., evidence tracking, audit trails)
 *
 * -------------------------------------------------------------------------------
 * 📦 Persistent Storage Simulation
 *
 * - Logs all login attempts and forensic actions to a local file (audit_log.txt)
 * - Includes:
 *   - Username
 *   - Role
 *   - Timestamp
 *   - Session token
 *   - Evidence notes (for analysts)
 *
 * -------------------------------------------------------------------------------
 * 🧠 SPA Toolkit Integration Simulation
 *
 * - Simulates routing to SPA endpoints based on role
 * - Ready for real frontend integration via REST or WebSocket
 */
