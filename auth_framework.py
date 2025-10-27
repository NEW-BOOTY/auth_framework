"""
AuthFramework.py
Copyright © 2025 Devin B. Royal. All rights reserved.

🔐 WHAT THIS PROGRAM DOES:
- Provides a secure authentication system with hashed PINs, biometric simulation, and OTP-based MFA
- Supports role-based access control for Admin, Analyst, and Guest users
- Simulates forensic evidence tagging and chain-of-custody logging
- Logs all activity to a persistent audit file (audit_log.txt)
- Routes users to simulated SPA endpoints based on role

🔮 WHAT IT CAN DO:
- Be extended to use real biometric APIs and OTP services
- Connect to databases (e.g., SQLite, PostgreSQL) for persistent user and evidence storage
- Integrate with forensic platforms for evidence tracking and audit trails
- Serve as a backend for SPA frontends via REST or WebSocket

🚀 WHAT IT'S DESIGNED TO SUPPORT:
- Enterprise-grade authentication workflows
- Modular expansion into microservices or GUI dashboards
- Secure forensic automation and compliance-ready logging
"""

import hashlib
import uuid
import time
import random
from datetime import datetime

# Simulated user database with SHA-256 hashed PINs
USER_DB = {
    "admin": hashlib.sha256("4269".encode()).hexdigest(),
    "analyst": hashlib.sha256("3141".encode()).hexdigest(),
    "guest": hashlib.sha256("1234".encode()).hexdigest()
}

# Role mapping for access control
USER_ROLES = {
    "admin": "Admin",
    "analyst": "Analyst",
    "guest": "Guest"
}

MAX_ATTEMPTS = 3
LOCKOUT_DURATION = 30  # seconds
LOG_FILE = "audit_log.txt"

def hash_pin(pin):
    """Hashes a PIN using SHA-256."""
    return hashlib.sha256(pin.encode()).hexdigest()

def log_to_file(entry):
    """Appends an audit entry to the persistent log file."""
    with open(LOG_FILE, "a") as f:
        f.write(entry + "\n")

def verify_pin(username):
    """Verifies user PIN with rate limiting and lockout logic."""
    attempts = 0
    lockout_start = None

    while attempts < MAX_ATTEMPTS:
        if lockout_start and time.time() - lockout_start < LOCKOUT_DURATION:
            print("⏳ Account locked. Try again later.")
            return False

        pin = input("Enter PIN: ")
        if hash_pin(pin) == USER_DB[username]:
            return True
        else:
            attempts += 1
            print(f"❌ Incorrect PIN. Attempts left: {MAX_ATTEMPTS - attempts}")

        if attempts >= MAX_ATTEMPTS:
            lockout_start = time.time()
            print("🚫 Too many failed attempts. Account locked.")
            return False
    return False

def simulate_biometric():
    """Simulates biometric scan via keyword prompt and delay."""
    print("🔄 Contacting biometric API...")
    time.sleep(1.5)
    scan = input("Simulate biometric scan (type 'scan'): ")
    if scan.lower() != "scan":
        print("❌ Biometric scan failed.")
        return False
    print("✅ Biometric scan accepted.")
    return True

def simulate_otp():
    """Simulates OTP generation and verification."""
    otp = str(random.randint(100000, 999999))
    print("📡 Sending OTP...")
    time.sleep(1)
    print(f"Your OTP is: {otp}")
    entered = input("Enter OTP: ")
    if entered != otp:
        print("❌ MFA failed.")
        return False
    print("✅ MFA verified.")
    return True

def forensic_module():
    """Simulates forensic evidence tagging and logging."""
    print("\n🧪 Forensic Evidence System")
    evidence_id = input("Enter evidence ID to tag: ")
    note = input("Enter chain-of-custody note: ")
    print("✅ Evidence tagged and logged.")
    log_to_file(f"[FORENSIC] Evidence ID: {evidence_id}, Note: {note}")

def route_to_module(role):
    """Routes user to appropriate module based on role."""
    if role == "Admin":
        print("🔧 Routing to Admin Control Panel...")
        print("Simulated SPA endpoint: /admin/dashboard")
    elif role == "Analyst":
        print("🔍 Routing to Forensic Evidence Dashboard...")
        forensic_module()
    elif role == "Guest":
        print("📄 Routing to Read-Only SPA Toolkit...")
        print("Simulated SPA endpoint: /guest/view")
    else:
        print("⚠️ Unknown role.")

def log_audit(username, role, success, token):
    """Logs login attempt with metadata to file and console."""
    status = "SUCCESS" if success else "FAILURE"
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    entry = f"[AUDIT] {status} login for '{username}' ({role}) at {timestamp} | Token: {token}"
    print(entry)
    log_to_file(entry)

def main():
    """Main entry point for authentication workflow."""
    print("===============================================")
    print("  Enterprise Auth Framework by Devin B. Royal   ")
    print("  Forensic + SPA Toolkit Integration Ready      ")
    print("===============================================\n")

    username = input("Enter username: ")
    if username not in USER_DB:
        print("🚫 Unknown user.")
        return

    if not verify_pin(username):
        return
    if not simulate_biometric():
        return
    if not simulate_otp():
        return

    role = USER_ROLES[username]
    token = str(uuid.uuid4())
    log_audit(username, role, True, token)

    print("✅ Access granted.")
    print(f"Session Token: {token}")
    print(f"Role: {role}")

    route_to_module(role)

if __name__ == "__main__":
    main()
