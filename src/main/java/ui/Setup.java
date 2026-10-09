/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
/**
 *
 * @author alfre
 */
public class Setup extends JPanel{
  private final CardLayout cards = new CardLayout();
    private final JPanel holder = new JPanel(cards);
    private final JPanel crudHost = new JPanel(new BorderLayout());
 
    public Setup() {
        setLayout(new BorderLayout());
        holder.add(buildMenu(), "menu");
        holder.add(crudHost, "crud");
        add(holder, BorderLayout.CENTER);
        cards.show(holder, "menu");
    }
 
    // ---------- layout pieces ----------
 
    private JPanel buildMenu() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(16, 20, 16, 20));
 
        JLabel h = new JLabel("Setup");
        h.setFont(h.getFont().deriveFont(Font.BOLD, 20f));
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(h);
        p.add(Box.createVerticalStrut(6));
 
        JLabel note = new JLabel("Lookup tables managed with one reusable CRUD panel each.");
        note.setForeground(Color.GRAY);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(note);
        p.add(Box.createVerticalStrut(12));
 
        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        grid.add(tile("Departments", "DEPARTMENTS"));
        grid.add(tile("Job positions and salary", "JOB_POSITIONS"));
        grid.add(tile("Deduction types", "DEDUCTION_TYPES"));
        grid.add(tile("System users and roles", "USERS"));
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        p.add(grid);
 
        p.add(Box.createVerticalGlue());
        return p;
    }
 
    private JPanel tile(String title, String tableName) {
        JPanel t = new JPanel();
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        Color normal = new Color(248, 248, 248);
        Color hover = new Color(230, 240, 252);
        t.setBackground(normal);
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(10, 12, 10, 12)));
        t.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 
        JLabel name = new JLabel(title);
        name.setFont(name.getFont().deriveFont(Font.BOLD, 13f));
        JLabel tbl = new JLabel(tableName);
        tbl.setForeground(Color.GRAY);
        t.add(name);
        t.add(Box.createVerticalStrut(2));
        t.add(tbl);
 
        t.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { openTable(title, tableName); }
            @Override public void mouseEntered(MouseEvent e) { t.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { t.setBackground(normal); }
        });
        return t;
    }
 
    // ---------- behavior ----------
 
    private void openTable(String title, String tableName) {
        crudHost.removeAll();
 
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(16, 20, 16, 20));
 
        JButton back = new JButton("← Back to Setup");
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.addActionListener(e -> cards.show(holder, "menu"));
        p.add(back);
        p.add(Box.createVerticalStrut(10));
 
        JLabel h = new JLabel(title);
        h.setFont(h.getFont().deriveFont(Font.BOLD, 20f));
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(h);
        p.add(Box.createVerticalStrut(6));
 
        // TODO: replace this with  new LookupCrudPanel(tableName)  - the one reusable CRUD panel
        JLabel todo = new JLabel("CRUD panel for " + tableName + " goes here.");
        todo.setForeground(Color.GRAY);
        todo.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(todo);
        p.add(Box.createVerticalGlue());
 
        crudHost.add(p, BorderLayout.CENTER);
        crudHost.revalidate();
        crudHost.repaint();
        cards.show(holder, "crud");
    }
}
