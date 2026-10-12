package ui;

import dao.AttendanceDAO;
import java.awt.*;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Attendance;

public class AttendanceFrame extends JPanel {

    // ---------- palette (same as the other screens) ----------
    private static final Color PRIMARY   = new Color(0x1D4ED8);
    private static final Color BG        = new Color(0xF8FAFC);
    private static final Color BORDER    = new Color(0xE2E8F0);
    private static final Color TEXT      = new Color(0x0F172A);
    private static final Color TEXT_SOFT = new Color(0x334155);
    private static final Color MUTED     = new Color(0x64748B);
    private static final Color ERROR_RED = new Color(0xDC2626);
    private static final Color OK         = new Color(0x15803D);
    private static final Color ORANGE    = new Color(0xD97706);

    // ---------- attendance rules ----------
    private static final LocalTime WORK_START = LocalTime.of(8, 0);
    private static final int GRACE_MINUTES = 15;       // time in after 08:15 = LATE
    private static final double LUNCH_HOURS = 1.0;     // taken off the time in -> time out total
    private static final String UNMARKED = "NOT MARKED";
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    // table columns
    private static final int COL_NO = 0;
    private static final int COL_NAME = 1;
    private static final int COL_IN = 2;
    private static final int COL_OUT = 3;
    private static final int COL_HOURS = 4;
    private static final int COL_STATUS = 5;

    private final AttendanceDAO dao = new AttendanceDAO();

    // rows.get(i) is the Attendance record shown in table row i (the table is never sorted)
    private List<Attendance> rows = new ArrayList<>();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Employee no", "Name", "Time in", "Time out", "Hours", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int col) {
            return col == COL_IN || col == COL_OUT || col == COL_STATUS;
        }
    };
    private final JTable table = new JTable(model);
    private final JTextField txtDate = new JTextField(LocalDate.now().toString());
    private final JLabel lblSummary = new JLabel(" ");
    private final JButton btnLoad = secondaryButton("Load");
    private final JButton btnMarkAll = secondaryButton("Mark all present");
    private final JButton btnSave = primaryButton("Save attendance");

    // stops the table listener from reacting to changes that the code itself makes
    private boolean updating = false;

    public AttendanceFrame() {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(28, 32, 24, 32));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        btnLoad.addActionListener(e -> loadAttendance());
        txtDate.addActionListener(e -> loadAttendance());   // pressing Enter in the date box
        btnMarkAll.addActionListener(e -> markAllPresent());
        btnSave.addActionListener(e -> save());

        // react when the user edits a time or picks a status
        model.addTableModelListener(e -> {
            if (updating) return;
            if (e.getType() != TableModelEvent.UPDATE || e.getFirstRow() < 0) return;

            int row = e.getFirstRow();
            if (e.getColumn() == COL_IN || e.getColumn() == COL_OUT) {
                onTimeEdited(row);
            } else if (e.getColumn() == COL_STATUS) {
                onStatusEdited(row);
            }
        });

        loadAttendance();
    }

    // ---------- layout ----------

    private JPanel buildHeader() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JLabel h = new JLabel("Attendance");
        h.setFont(h.getFont().deriveFont(Font.BOLD, 24f));
        h.setForeground(TEXT);
        h.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Mark each active employee for a day, then save");
        sub.setForeground(MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(h);
        top.add(Box.createVerticalStrut(4));
        top.add(sub);

        // toolbar: date + Load on the left, Mark all present + Save on the right
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(20, 0, 14, 0));
        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDate = new JLabel("Date");
        lblDate.setFont(lblDate.getFont().deriveFont(Font.BOLD, 12f));
        lblDate.setForeground(TEXT_SOFT);

        txtDate.setPreferredSize(new Dimension(130, 38));
        txtDate.putClientProperty("JTextField.placeholderText", "yyyy-mm-dd");
        btnLoad.setPreferredSize(new Dimension(90, 38));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(lblDate);
        left.add(txtDate);
        left.add(btnLoad);

        btnMarkAll.setPreferredSize(new Dimension(150, 38));
        btnSave.setPreferredSize(new Dimension(160, 38));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(btnMarkAll);
        right.add(btnSave);

        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);
        top.add(toolbar);

        return top;
    }

    private JScrollPane buildTable() {
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));
        table.setForeground(TEXT);

        int[] widths = {110, 220, 90, 90, 80, 130};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // center the time in, time out and hours columns
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(COL_IN).setCellRenderer(center);
        table.getColumnModel().getColumn(COL_OUT).setCellRenderer(center);
        table.getColumnModel().getColumn(COL_HOURS).setCellRenderer(center);

        // status column: colored text, and a dropdown when you click it
        table.getColumnModel().getColumn(COL_STATUS).setCellRenderer(new StatusRenderer());
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"PRESENT", "LATE", "ABSENT", "ON_LEAVE"});
        table.getColumnModel().getColumn(COL_STATUS).setCellEditor(new DefaultCellEditor(statusBox));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(14, 0, 0, 0));

        lblSummary.setForeground(MUTED);

        JLabel note = new JLabel("Type times as HH:mm (for example 08:00). Hours = time in to time out, minus 1 hour lunch.");
        note.setForeground(MUTED);
        note.setFont(note.getFont().deriveFont(Font.PLAIN, 12f));

        footer.add(lblSummary, BorderLayout.WEST);
        footer.add(note, BorderLayout.EAST);
        return footer;
    }

    /** Colors the Status column: green = PRESENT, orange = LATE, red = ABSENT, blue = ON_LEAVE. */
    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                       boolean focus, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, focus, r, c);
            setFont(getFont().deriveFont(Font.BOLD, 12f));
            if (!sel) {
                String s = String.valueOf(v);
                if (s.equals("PRESENT"))       setForeground(OK);
                else if (s.equals("LATE"))     setForeground(ORANGE);
                else if (s.equals("ABSENT"))   setForeground(ERROR_RED);
                else if (s.equals("ON_LEAVE")) setForeground(PRIMARY);
                else                           setForeground(MUTED);
            }
            return this;
        }
    }

    // ---------- loading ----------

    /** Reads the date box, then fills the table with every active employee for that date. */
    private void loadAttendance() {
        LocalDate date = readDate();
        if (date == null) return;

        try {
            rows = dao.findForDate(date);

            updating = true;
            try {
                model.setRowCount(0);
                for (Attendance a : rows) {
                    String status = (a.getStatus() == null) ? UNMARKED : a.getStatus();
                    model.addRow(new Object[]{
                        a.getEmployeeNo(),
                        a.getEmployeeName(),
                        timeText(a.getTimeIn()),
                        timeText(a.getTimeOut()),
                        hoursText(a.getTimeIn(), a.getTimeOut()),
                        status
                    });
                }
            } finally {
                updating = false;
            }
            updateSummary();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // ---------- reacting to edits ----------

    /** The user typed a time. Work out the status and the hours again. */
    private void onTimeEdited(int row) {
        String status = statusAt(row);
        LocalTime in = parseTime(textAt(row, COL_IN));
        LocalTime out = parseTime(textAt(row, COL_OUT));

        updating = true;
        try {
            // an employee on leave stays on leave, whatever times are typed
            if (!status.equals("ON_LEAVE")) {
                if (in != null) {
                    boolean late = in.isAfter(WORK_START.plusMinutes(GRACE_MINUTES));
                    status = late ? "LATE" : "PRESENT";
                } else if (status.equals("PRESENT") || status.equals("LATE")) {
                    status = "ABSENT";   // the time in was cleared
                }
                model.setValueAt(status, row, COL_STATUS);
            }
            model.setValueAt(hoursText(in, out), row, COL_HOURS);
        } finally {
            updating = false;
        }
        updateSummary();
    }

    /** The user picked a status from the dropdown. Fill or clear the times to match. */
    private void onStatusEdited(int row) {
        String status = statusAt(row);

        updating = true;
        try {
            if (status.equals("ABSENT") || status.equals("ON_LEAVE")) {
                // no working time on these days
                model.setValueAt("", row, COL_IN);
                model.setValueAt("", row, COL_OUT);
            } else if (status.equals("PRESENT")) {
                if (textAt(row, COL_IN).isEmpty())  model.setValueAt("08:00", row, COL_IN);
                if (textAt(row, COL_OUT).isEmpty()) model.setValueAt("17:00", row, COL_OUT);
            } else if (status.equals("LATE")) {
                if (textAt(row, COL_IN).isEmpty())  model.setValueAt("08:30", row, COL_IN);
                if (textAt(row, COL_OUT).isEmpty()) model.setValueAt("17:00", row, COL_OUT);
            }

            LocalTime in = parseTime(textAt(row, COL_IN));
            LocalTime out = parseTime(textAt(row, COL_OUT));
            model.setValueAt(hoursText(in, out), row, COL_HOURS);
        } finally {
            updating = false;
        }
        updateSummary();
    }

    /** Sets every row to PRESENT 08:00 - 17:00, except employees marked ON_LEAVE. */
    private void markAllPresent() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        updating = true;
        try {
            for (int i = 0; i < model.getRowCount(); i++) {
                if (statusAt(i).equals("ON_LEAVE")) continue;
                model.setValueAt("08:00", i, COL_IN);
                model.setValueAt("17:00", i, COL_OUT);
                model.setValueAt(hoursText(LocalTime.of(8, 0), LocalTime.of(17, 0)), i, COL_HOURS);
                model.setValueAt("PRESENT", i, COL_STATUS);
            }
        } finally {
            updating = false;
        }
        updateSummary();
    }

    // ---------- saving ----------

    private void save() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        LocalDate date = readDate();
        if (date == null) return;

        List<Attendance> toSave = new ArrayList<>();

        for (int i = 0; i < model.getRowCount(); i++) {
            String status = statusAt(i);
            if (status.equals(UNMARKED)) continue;   // not marked, so nothing to save

            String name = textAt(i, COL_NAME);
            String inText = textAt(i, COL_IN);
            String outText = textAt(i, COL_OUT);
            LocalTime in = parseTime(inText);
            LocalTime out = parseTime(outText);

            if (!inText.isEmpty() && in == null) {
                showMessage(name + ": time in must look like 08:00.");
                return;
            }
            if (!outText.isEmpty() && out == null) {
                showMessage(name + ": time out must look like 17:00.");
                return;
            }
            if ((status.equals("PRESENT") || status.equals("LATE")) && in == null) {
                showMessage(name + ": enter a time in, or set the status to ABSENT.");
                return;
            }
            if (in != null && out != null && out.isBefore(in)) {
                showMessage(name + ": time out cannot be earlier than time in.");
                return;
            }

            Attendance a = new Attendance();
            a.setEmployeeId(rows.get(i).getEmployeeId());
            a.setAttendanceDate(date);
            a.setStatus(status);
            if (status.equals("ABSENT") || status.equals("ON_LEAVE")) {
                a.setTimeIn(null);
                a.setTimeOut(null);
            } else {
                a.setTimeIn(in);
                a.setTimeOut(out);
            }
            toSave.add(a);
        }

        if (toSave.isEmpty()) {
            showMessage("Nothing to save yet. Mark at least one employee.");
            return;
        }

        try {
            dao.saveAll(date, toSave);
            JOptionPane.showMessageDialog(this,
                    "Attendance for " + date + " saved (" + toSave.size() + " employees).",
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
            loadAttendance();   // reload so the screen shows exactly what is in the database
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // ---------- small helpers ----------

    private LocalDate readDate() {
        try {
            return LocalDate.parse(txtDate.getText().trim());
        } catch (DateTimeParseException ex) {
            showMessage("Date must look like 2026-10-12.");
            return null;
        }
    }

    /** Text of a table cell, never null. */
    private String textAt(int row, int col) {
        Object v = model.getValueAt(row, col);
        return v == null ? "" : v.toString().trim();
    }

    private String statusAt(int row) {
        String s = textAt(row, COL_STATUS);
        return s.isEmpty() ? UNMARKED : s;
    }

    /** Turns "08:30" into a LocalTime. Returns null for blank or invalid text. */
    private LocalTime parseTime(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return LocalTime.parse(text.trim(), HM);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private String timeText(LocalTime t) {
        return t == null ? "" : t.format(HM);
    }

    /** Hours worked = (time out - time in) minus lunch. Shows 0.0 if either time is missing. */
    private String hoursText(LocalTime in, LocalTime out) {
        double hours = 0.0;
        if (in != null && out != null) {
            double worked = Duration.between(in, out).toMinutes() / 60.0 - LUNCH_HOURS;
            hours = Math.max(0, Math.round(worked * 10) / 10.0);
        }
        return String.format("%.1f", hours);
    }

    /** Updates the count line at the bottom left. */
    private void updateSummary() {
        int present = 0, late = 0, absent = 0, onLeave = 0, unmarked = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            String s = statusAt(i);
            if (s.equals("PRESENT")) present++;
            else if (s.equals("LATE")) late++;
            else if (s.equals("ABSENT")) absent++;
            else if (s.equals("ON_LEAVE")) onLeave++;
            else unmarked++;
        }
        lblSummary.setText(model.getRowCount() + " employees   |   "
                + present + " present, " + late + " late, " + absent + " absent, "
                + onLeave + " on leave, " + unmarked + " not marked");
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Attendance", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ---------- button styles ----------

    private static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
        b.setForeground(Color.WHITE);
        b.setBackground(PRIMARY);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(150, 38));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty("JButton.buttonType", "roundRect");
        return b;
    }

    private static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 13f));
        b.setForeground(TEXT_SOFT);
        b.setBackground(Color.WHITE);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(120, 38));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty("JButton.buttonType", "roundRect");
        return b;
    }
}