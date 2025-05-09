package library; // Base package

import javax.swing.*;
import library.frontend.LoginFrame;

public class LoginPage {

    public static void main(String[] args) {

        // Set the look and feel to the system's default
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception e) {
//            e.printStackTrace(); // Print the stack trace for debugging
//        }


        JFrame.setDefaultLookAndFeelDecorated(true);

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