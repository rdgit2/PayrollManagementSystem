/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author alfre
 */
public class AttendanceFrame extends JPanel{
    
     private static final LocalTime WORK_START = LocalTime.of(8, 0);
    private static final int GRACE_MINUTES = 15;      // later than 08:15 = LATE
    private static final double LUNCH_HOURS = 1.0;    // deducted from time in -> time out
    private static final String NONE = "—";
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
 
    private final JTextField txtDate = new JTextField(LocalDate.now().toString(), 10);
 
    // Time in / Time out are editable; Hours and Status are computed.
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Employee", "Time in", "Time out", "Hours", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return c == 1 || c == 2; }
    };
    private final JTable table = new JTable(model);
    private boolean updating;   // stops recompute() from re-triggering itself
 
    public AttendanceFrame() {
        setLayout(new BorderLayout());
        add(buildPanel(), BorderLayout.CENTER);
        loadAttendance();
 
        model.addTableModelListener(e -> {
            if (!updating && e.getFirstRow() >= 0 && (e.getColumn() == 1 || e.getColumn() == 2)) {
                recompute(e.getFirstRow());
            }
        });
    }
 
    // ---------- layout pieces ----------
 
    private JPanel buildPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(16, 20, 16, 20));
 
        JLabel h = new JLabel("Attendance");
        h.setFont(h.getFont().deriveFont(Font.BOLD, 20f));
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(h);
        p.add(Box.createVerticalStrut(10));
 
        // date on the left, buttons on the right
        JPanel top = new JPanel(new BorderLayout());
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.add(new JLabel("Date"));
        left.add(txtDate);
 
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton markAll = new JButton("Mark all present");
        JButton save = new JButton("Save");
        markAll.addActionListener(e -> markAllPresent());
        save.addActionListener(e -> save());
        right.add(markAll);
        right.add(save);
 
        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        top.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.add(top);
        p.add(Box.createVerticalStrut(10));
 
        // table
        table.setRowHeight(26);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
 
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(sp);
        p.add(Box.createVerticalStrut(8));
 
        JLabel note = new JLabel("Hours are computed from time in and time out (1 hour lunch deducted).");
        note.setForeground(Color.GRAY);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(note);
        return p;
    }
 
    /** Colours the Status column: green = PRESENT, orange = LATE, red = ABSENT. */
    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                       boolean foc, int row, int c) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, c);
            setFont(getFont().deriveFont(Font.BOLD, 11f));
            if (!sel) {
                String s = String.valueOf(v);
                if (s.equals("PRESENT"))   setForeground(new Color(30, 140, 60));
                else if (s.equals("LATE")) setForeground(new Color(210, 130, 0));
                else                       setForeground(new Color(200, 50, 50));
            }
            return this;
        }
    }
 
    // ---------- behavior ----------
 
    /** Replace the sample rows with DAO calls for the selected date. */
    private void loadAttendance() {
        // TODO: new AttendanceDAO().findByDate(LocalDate.parse(txtDate.getText()))
        model.setRowCount(0);
        model.addRow(new Object[]{"Santos, Maria", "08:00", "17:00", "", ""});
        model.addRow(new Object[]{"Reyes, Juan",   "08:25", "17:00", "", ""});
        model.addRow(new Object[]{"Cruz, Ana",     NONE,    NONE,    "", ""});
        for (int i = 0; i < model.getRowCount(); i++) {
            recompute(i);
        }
    }
 
    private void markAllPresent() {
        updating = true;
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt("08:00", i, 1);
            model.setValueAt("17:00", i, 2);
        }
        updating = false;
        for (int i = 0; i < model.getRowCount(); i++) {
            recompute(i);
        }
    }
 
    private void save() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        // TODO: loop the rows and call new AttendanceDAO().save(date, employee, in, out)
        JOptionPane.showMessageDialog(this, "Attendance for " + txtDate.getText() + " saved.");
    }
 
    private void recompute(int row) {
        updating = true;
        try {
            LocalTime in = parse(model.getValueAt(row, 1));
            LocalTime out = parse(model.getValueAt(row, 2));
 
            double hours = 0.0;
            String status;
            if (in == null) {
                status = "ABSENT";
            } else {
                status = in.isAfter(WORK_START.plusMinutes(GRACE_MINUTES)) ? "LATE" : "PRESENT";
                if (out != null) {
                    double h = Duration.between(in, out).toMinutes() / 60.0 - LUNCH_HOURS;
                    hours = Math.max(0, Math.round(h * 10) / 10.0);
                }
            }
            model.setValueAt(String.format("%.1f", hours), row, 3);
            model.setValueAt(status, row, 4);
        } finally {
            updating = false;
        }
    }
 
    private LocalTime parse(Object v) {
        if (v == null) return null;
        String s = v.toString().trim();
        if (s.isEmpty() || s.equals(NONE)) return null;
        try {
            return LocalTime.parse(s, HM);
        } catch (DateTimeParseException ex) {
            return null;   // treat bad input as "no time"
        }
    }
}
