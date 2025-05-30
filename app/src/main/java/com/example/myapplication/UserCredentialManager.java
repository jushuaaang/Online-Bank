package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UserCredentialManager {

    private static final String PREFS_NAME = "UserAuthPrefs";
    private static final String KEY_USERS = "registered_users";
    private static final String KEY_LOGGED_IN_USER = "logged_in_user";
    private static final String TAG = "UserCredentialsManager";

    private static String hashPassword(String password, String salt) {
        if (password == null || salt == null) return null;
        try {
            String combined = salt + password;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error hashing password", e);
            return null;
        }
    }

    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : saltBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static Map<String, JSONObject> getUsers(Context context) {
        SharedPreferences prefs = getPreferences(context);
        String usersJsonString = prefs.getString(KEY_USERS, "{}");
        Map<String, JSONObject> usersMap = new HashMap<>();
        try {
            JSONObject mainJson = new JSONObject(usersJsonString);
            Iterator<String> keys = mainJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject userObject = mainJson.optJSONObject(key);
                if (userObject != null) {
                    usersMap.put(key, userObject);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing users JSON", e);
        }
        return usersMap;
    }

    private static void saveUsers(Context context, Map<String, JSONObject> users) {
        SharedPreferences prefs = getPreferences(context);
        JSONObject mainJson = new JSONObject(users); // JSONObject can take a Map
        prefs.edit().putString(KEY_USERS, mainJson.toString()).apply();
    }

    public static boolean signUpUser(Context context, String primaryIdentifier, String password, String email, String securityQuestion, String securityAnswer) {
        if (primaryIdentifier == null || primaryIdentifier.isEmpty() || password == null || password.isEmpty() ||
                email == null || email.isEmpty() || securityQuestion == null || securityQuestion.isEmpty() ||
                securityAnswer == null || securityAnswer.isEmpty()) {
            Log.w(TAG, "Attempted to sign up with null or empty critical fields.");
            return false;
        }

        Map<String, JSONObject> users = getUsers(context);
        if (users.containsKey(primaryIdentifier)) {
            Log.w(TAG, "User already exists: " + primaryIdentifier);
            return false;
        }

        // Check if email already exists
        for (JSONObject userData : users.values()) {
            try {
                if (email.equalsIgnoreCase(userData.optString("email"))) {
                    Log.w(TAG, "Sign up attempt with existing email: " + email);
                    return false; // Email already exists
                }
            } catch (Exception e) {
                // Should not happen with optString, but good practice
                Log.e(TAG, "Error checking existing email during sign up", e);
            }
        }


        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);
        String hashedSecurityAnswer = hashPassword(securityAnswer.toLowerCase(), salt);

        if (hashedPassword == null || hashedSecurityAnswer == null) {
            Log.e(TAG, "Failed to hash password or security answer during sign up.");
            return false;
        }

        JSONObject newUser = new JSONObject();
        try {
            newUser.put("username", primaryIdentifier); // Store the username explicitly
            newUser.put("password_hash", hashedPassword);
            newUser.put("salt", salt);
            newUser.put("email", email);
            newUser.put("security_question", securityQuestion);
            newUser.put("security_answer_hash", hashedSecurityAnswer);

            users.put(primaryIdentifier, newUser);
            saveUsers(context, users);
            Log.i(TAG, "User signed up successfully: " + primaryIdentifier);
            return true;
        } catch (JSONException e) {
            Log.e(TAG, "Error creating new user JSON for: " + primaryIdentifier, e);
            return false;
        }
    }

    /**
     * Attempts to log in a user with the given identifier (username or email) and password.
     *
     * @param context         The application context.
     * @param identifierInput The username or email provided by the user.
     * @param passwordInput   The password provided by the user.
     * @return The primary identifier (username) of the logged-in user if successful,
     *         null otherwise.
     */
    public static String getLoggedInUserIdentifier(Context context, String identifierInput, String passwordInput) {
        if (identifierInput == null || identifierInput.isEmpty() || passwordInput == null || passwordInput.isEmpty()) {
            return null;
        }
        Map<String, JSONObject> users = getUsers(context);

        // Try direct lookup by primaryIdentifier (assuming it's username)
        JSONObject userData = users.get(identifierInput);
        if (userData != null) {
            try {
                String storedHash = userData.getString("password_hash");
                String salt = userData.getString("salt");
                String inputHash = hashPassword(passwordInput, salt);

                if (inputHash != null && inputHash.equals(storedHash)) {
                    setLoggedInUser(context, identifierInput); // identifierInput is the username here
                    Log.i(TAG, "User login successful (by username): " + identifierInput);
                    return identifierInput; // Return the username
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error reading user data for login (by username): " + identifierInput, e);
            }
        }

        // If direct lookup failed or identifierInput might be an email, search by email
        for (Map.Entry<String, JSONObject> entry : users.entrySet()) {
            String username = entry.getKey();
            JSONObject userDetails = entry.getValue();
            try {
                String storedEmail = userDetails.optString("email");
                if (identifierInput.equalsIgnoreCase(storedEmail)) {
                    String storedHash = userDetails.getString("password_hash");
                    String salt = userDetails.getString("salt");
                    String inputHash = hashPassword(passwordInput, salt);

                    if (inputHash != null && inputHash.equals(storedHash)) {
                        setLoggedInUser(context, username); // Set the actual username as logged-in user
                        Log.i(TAG, "User login successful (by email, actual username): " + username);
                        return username; // Return the actual username
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error reading user data for login (by email search): " + username, e);
            }
        }
        Log.w(TAG, "Invalid username/email or password for: " + identifierInput);
        return null; // Login failed
    }


    /**
     * Retrieves the stored username for a given user identifier.
     * In this model, the identifier from getLoggedInUserIdentifier IS the username.
     */
    public static String getUsernameByIdentifier(Context context, String identifier) {
        if (identifier == null || identifier.isEmpty()) return null;
        Map<String, JSONObject> users = getUsers(context);
        JSONObject userData = users.get(identifier);
        if (userData != null) {
            return userData.optString("username", identifier); // Return stored username or identifier as fallback
        }
        return null;
    }

    /**
     * Retrieves the stored email for a given user identifier (username).
     */
    public static String getEmailByIdentifier(Context context, String identifier) {
        if (identifier == null || identifier.isEmpty()) return null;
        Map<String, JSONObject> users = getUsers(context);
        JSONObject userData = users.get(identifier);
        if (userData != null) {
            try {
                return userData.getString("email");
            } catch (JSONException e) {
                Log.e(TAG, "Error getting email for: " + identifier, e);
            }
        }
        return null;
    }


    public static String getUserSecurityQuestion(Context context, String primaryIdentifier) {
        if (primaryIdentifier == null || primaryIdentifier.isEmpty()) return null;
        JSONObject userData = getUsers(context).get(primaryIdentifier);
        // Fallback: search by email if primaryIdentifier might be an email
        if (userData == null && primaryIdentifier.contains("@")) {
            userData = findUserByEmail(context, primaryIdentifier);
        }

        if (userData != null) {
            try {
                return userData.getString("security_question");
            } catch (JSONException e) {
                Log.e(TAG, "Error getting security question for: " + primaryIdentifier, e);
            }
        }
        return null;
    }

    public static boolean verifySecurityAnswer(Context context, String primaryIdentifier, String answer) {
        if (primaryIdentifier == null || primaryIdentifier.isEmpty() || answer == null || answer.isEmpty()) return false;
        JSONObject userData = getUsers(context).get(primaryIdentifier);
        // Fallback: search by email
        if (userData == null && primaryIdentifier.contains("@")) {
            userData = findUserByEmail(context, primaryIdentifier);
        }

        if (userData == null) return false;

        try {
            String storedAnswerHash = userData.getString("security_answer_hash");
            String salt = userData.getString("salt");
            String inputAnswerHash = hashPassword(answer.toLowerCase(), salt);
            return inputAnswerHash != null && inputAnswerHash.equals(storedAnswerHash);
        } catch (JSONException e) {
            Log.e(TAG, "Error verifying security answer for: " + primaryIdentifier, e);
            return false;
        }
    }

    public static boolean resetPassword(Context context, String primaryIdentifier, String newPassword) {
        if (primaryIdentifier == null || primaryIdentifier.isEmpty() || newPassword == null || newPassword.isEmpty()) return false;
        Map<String, JSONObject> users = getUsers(context);
        JSONObject userData = users.get(primaryIdentifier);
        String actualUsername = primaryIdentifier;

        // Fallback: search by email
        if (userData == null && primaryIdentifier.contains("@")) {
            for (Map.Entry<String, JSONObject> entry : users.entrySet()){
                if(primaryIdentifier.equalsIgnoreCase(entry.getValue().optString("email"))){
                    userData = entry.getValue();
                    actualUsername = entry.getKey(); // This is the username key in the map
                    break;
                }
            }
        }

        if (userData == null) {
            Log.w(TAG, "Cannot reset password. User not found: " + primaryIdentifier);
            return false;
        }

        try {
            String salt = userData.getString("salt");
            String newHashedPassword = hashPassword(newPassword, salt);
            if (newHashedPassword == null) {
                Log.e(TAG, "Failed to hash new password during reset for: " + actualUsername);
                return false;
            }

            userData.put("password_hash", newHashedPassword);
            users.put(actualUsername, userData); // Use actualUsername for saving
            saveUsers(context, users);
            Log.i(TAG, "Password reset successfully for: " + actualUsername);
            return true;
        } catch (JSONException e) {
            Log.e(TAG, "Error resetting password for: " + actualUsername, e);
            return false;
        }
    }


    /**
     * Helper to find user data by email.
     * @return JSONObject of the user if found, null otherwise.
     */
    private static JSONObject findUserByEmail(Context context, String email) {
        if (email == null || email.isEmpty()) return null;
        Map<String, JSONObject> users = getUsers(context);
        for (JSONObject userData : users.values()) {
            if (email.equalsIgnoreCase(userData.optString("email"))) {
                return userData;
            }
        }
        return null;
    }

    /**
     * Gets the username associated with a given email.
     * @return Username if found, null otherwise.
     */
    public static String getUsernameByEmail(Context context, String email) {
        if (email == null || email.isEmpty()) return null;
        Map<String, JSONObject> users = getUsers(context);
        for (Map.Entry<String, JSONObject> entry : users.entrySet()) {
            if (email.equalsIgnoreCase(entry.getValue().optString("email"))) {
                return entry.getKey(); // The key is the username
            }
        }
        return null;
    }


    public static boolean userExists(Context context, String primaryIdentifier) {
        if (primaryIdentifier == null || primaryIdentifier.isEmpty()) return false;
        Map<String, JSONObject> users = getUsers(context);
        if (users.containsKey(primaryIdentifier)) return true;

        // Also check if the primaryIdentifier might be an email that exists
        for (JSONObject userData : users.values()) {
            if (primaryIdentifier.equalsIgnoreCase(userData.optString("email"))) {
                return true;
            }
        }
        return false;
    }

    public static void setLoggedInUser(Context context, String primaryIdentifier) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        if (primaryIdentifier == null) {
            editor.remove(KEY_LOGGED_IN_USER);
            Log.i(TAG, "User logged out.");
        } else {
            editor.putString(KEY_LOGGED_IN_USER, primaryIdentifier);
            Log.i(TAG, "Set logged in user: " + primaryIdentifier);
        }
        editor.apply();
    }

    public static String getLoggedInUser(Context context) {
        return getPreferences(context).getString(KEY_LOGGED_IN_USER, null);
    }

    public static void logoutUser(Context context) {
        setLoggedInUser(context, null);
        // Also clear the UserPrefs used by SettingsFragment
        SharedPreferences userDisplayPrefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor displayEditor = userDisplayPrefs.edit();
        displayEditor.clear();
        displayEditor.apply();
    }
}