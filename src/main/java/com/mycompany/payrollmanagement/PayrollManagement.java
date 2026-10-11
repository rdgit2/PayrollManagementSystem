package com.mycompany.payrollmanagement;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import ui.LoginFrame;

public class PayrollManagement {

    public static void main(String[] args) {
        // Blue payment-app theme (must run before any window is created)
        UIManager.put("Component.accentColor", new Color(0x1E5EFF));
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("Button.default.background", new Color(0x1E5EFF));
        UIManager.put("Button.default.foreground", Color.WHITE);
        UIManager.put("Table.selectionBackground", new Color(0xDCE8FF));
        UIManager.put("Table.selectionForeground", new Color(0x0B2A6F));
        UIManager.put("Table.alternateRowColor", new Color(0xF5F8FF));
        UIManager.put("TableHeader.background", new Color(0xE8F0FF));
        UIManager.put("TableHeader.foreground", new Color(0x0B2A6F));
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 10);
        FlatLightLaf.setup();

        // Start the app through the existing login window
        SwingUtilities.invokeLater(() -> LoginFrame.main(args));
    }
}