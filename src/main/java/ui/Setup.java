package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Setup extends JPanel {

    public Setup() {
        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel h = new JLabel("Job positions and salary");
        h.setFont(h.getFont().deriveFont(Font.BOLD, 20f));
        h.setBorder(new EmptyBorder(0, 0, 6, 0));

        add(h, BorderLayout.NORTH);
        add(new JobPositionsPanel(), BorderLayout.CENTER);
    }
}