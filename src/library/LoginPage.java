package library; // Base package

import javax.swing.*;
import library.frontend.LoginFrame; // Import LoginFrame from the frontend sub-package

/**
 * Main entry point for the Library Management System application.
 * Sets the look and feel and launches the LoginFrame GUI.
 */
public class LoginPage {

    public static void main(String[] args) {

        // Run the GUI creation on the Event Dispatch Thread (EDT)
        // This is crucial for Swing applications
        SwingUtilities.invokeLater(() -> {
            // Create and show the LoginFrame
            // The LoginFrame constructor now handles making itself visible
            new LoginFrame();
            System.out.println("LoginFrame invoked.");
        });
    }
}