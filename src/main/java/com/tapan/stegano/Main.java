package com.tapan.stegano;

import com.tapan.stegano.gui.MainWindow;
import javax.swing.*;

/**
 * Entry point — launches the Stegano GUI.
 */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}