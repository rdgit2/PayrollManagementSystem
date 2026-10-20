package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Setup extends JPanel {

    private static final Color BG   = new Color(0xF8FAFC);
    private static final Color TEXT = new Color(0x0F172A);

    public Setup() {
        setLayout(new BorderLayout(0, 14));
        setBackground(BG);
        setBorder(new EmptyBorder(28, 32, 24, 32));

        JLabel h = new JLabel("Job positions and salary");
        h.setFont(h.getFont().deriveFont(Font.BOLD, 24f));
        h.setForeground(TEXT);

        add(h, BorderLayout.NORTH);
        add(new JobPositionsPanel(), BorderLayout.CENTER);
    }
}