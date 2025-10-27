/**
 * Copyright © 2024, Devin B. Royal. All rights reserved.
 */

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.eatthepath.pushy.apns.util.TokenUtil;
import com.eatthepath.pushy.apns.ApnsSigningKey;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.google.gson.Gson;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApnsCommandHandler {

    private static final Logger logger = Logger.getLogger(ApnsCommandHandler.class.getName());

    // Load sensitive data from environment variables or a secure configuration file.
    private static final String AUTH_KEY_PATH = System.getenv("AUTH_KEY_PATH");
    private static final String TEAM_ID = System.getenv("TEAM_ID");
    private static final String KEY_ID = System.getenv("KEY_ID");
    private static final String DEVICE_TOKEN = System.getenv("DEVICE_TOKEN");
    private static final String TOPIC = System.getenv("TOPIC");

    private static ApnsClient apnsClient;
    private static final int MAX_RETRIES = 3; // Retry mechanism for client initialization

    static {
        initializeApnsClient();
    }

    // Retry mechanism for APNs client initialization
    private static void initializeApnsClient() {
        if (AUTH_KEY_PATH == null || TEAM_ID == null || KEY_ID == null) {
            logger.log(Level.SEVERE, "Missing APNs credentials. Ensure environment variables are set correctly.");
            throw new IllegalArgumentException("APNs credentials are missing.");
        }

        int attempt = 0;
        boolean initialized = false;

        while (attempt < MAX_RETRIES && !initialized) {
            try {
                apnsClient = new ApnsClientBuilder()
                        .setApnsServer(ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
                        .setSigningKey(ApnsSigningKey.loadFromPkcs8File(new File(AUTH_KEY_PATH), TEAM_ID, KEY_ID))
                        .build();
                initialized = true;
                logger.log(Level.INFO, "APNs client successfully initialized.");
            } catch (Exception e) {
                attempt++;
                logger.log(Level.WARNING, "Attempt {0}: Failed to initialize APNs client.", attempt);
                if (attempt == MAX_RETRIES) {
                    logger.log(Level.SEVERE, "Failed to initialize APNs client after {0} attempts.", MAX_RETRIES);
                    throw new RuntimeException("Unable to initialize APNs client.", e);
                }
                try {
                    // Adding a small delay before retrying
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    // Commands for enabling and disabling Lost Mode, retrieving device information, etc.
    public static void enableLostMode() {
        Map<String, Object> payload = Map.of(
                "MessageType", "EnableLostMode",
                "Message", "Lost mode enabled",
                "PhoneNumber", "123-456-7890",
                "Footnote", "Return to IT department"
        );
        sendCommand("EnableLostMode", payload);
    }

    public static void disableLostMode() {
        Map<String, Object> payload = Map.of(
                "MessageType", "DisableLostMode"
        );
        sendCommand("DisableLostMode", payload);
    }

    public static void deviceInformation() {
        Map<String, Object> payload = Map.of(
                "MessageType", "DeviceInformation",
                "Queries", Map.of(
                        "UDID", true,
                        "DeviceName", true,
                        "OSVersion", true,
                        "ModelName", true
                )
        );
        sendCommand("DeviceInformation", payload);
    }

    public static void installApplication() {
        Map<String, Object> payload = Map.of(
                "MessageType", "InstallApplication",
                "iTunesStoreID", "app-itunes-store-id"
        );
        sendCommand("InstallApplication", payload);
    }

    public static void removeApplication() {
        Map<String, Object> payload = Map.of(
                "MessageType", "RemoveApplication",
                "Identifier", "app-identifier"
        );
        sendCommand("RemoveApplication", payload);
    }

    // Refactored asynchronous push notification sending method
    private static void sendCommand(String commandType, Map<String, Object> payload) {
        try {
            String jsonPayload = new Gson().toJson(payload);

            SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(
                    TokenUtil.sanitizeTokenString(DEVICE_TOKEN),
                    TOPIC,
                    jsonPayload.getBytes(StandardCharsets.UTF_8)
            );

            CompletableFuture<Void> future = apnsClient.sendNotification(pushNotification)
                    .thenAccept(response -> handlePushNotificationResponse(commandType, response))
                    .exceptionally(throwable -> {
                        logger.log(Level.SEVERE, "Error sending push notification for " + commandType, throwable);
                        return null;
                    });

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error while sending command: " + commandType, e);
        }
    }

    // Handles the response after a push notification is sent.
    private static void handlePushNotificationResponse(String commandType, PushNotificationResponse<SimpleApnsPushNotification> response) {
        if (response.isAccepted()) {
            logger.log(Level.INFO, "Command '{0}' was successfully accepted by APNs.", commandType);
        } else {
            logger.log(Level.WARNING, "Command '{0}' was rejected by APNs: {1} - {2}", new Object[]{
                    commandType, response.getRejectionReason(), response.getTokenInvalidationTimestamp()
            });

            // Handle specific rejection reasons
            if ("BadDeviceToken".equals(response.getRejectionReason())) {
                logger.log(Level.SEVERE, "The device token for command '{0}' is invalid. Consider re-registering the device.", commandType);
            } else if ("Unregistered".equals(response.getRejectionReason())) {
                logger.log(Level.SEVERE, "The device has been unregistered from APNs for command '{0}'.", commandType);
            } else {
                logger.log(Level.SEVERE, "APNs rejected the command '{0}' with reason: {1}", new Object[]{
                        commandType, response.getRejectionReason()
                });
            }
        }
    }

    public static void main(String[] args) {
        // Test commands
        enableLostMode();
        disableLostMode();
        deviceInformation();
        installApplication();
        removeApplication();
    }
}

/**
 * Copyright © 2024, Devin B. Royal. All rights reserved.
 */