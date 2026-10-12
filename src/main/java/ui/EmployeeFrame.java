package ui;

import dao.DepartmentDAO;
import dao.EmployeeDAO;
import dao.JobPositionDAO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import model.Department;
import model.Employee;
import model.JobPosition;

public class EmployeeFrame extends JPanel {

    // ---------- palette (same as the other screens) ----------
    // NOTE: the red color is called ERROR_RED because a plain "ERROR" name
    // clashes with an int constant that Swing components inherit.
    private static final Color PRIMARY   = new Color(0x1D4ED8);
    private static final Color BG        = new Color(0xF8FAFC);
    private static final Color BORDER    = new Color(0xE2E8F0);
    private static final Color TEXT      = new Color(0x0F172A);
    private static final Color TEXT_SOFT = new Color(0x334155);
    private static final Color MUTED     = new Color(0x64748B);
    private static final Color ERROR_RED = new Color(0xDC2626);
    private static final Color OK        = new Color(0x15803D);

    private static final String ALL = "All departments";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private final EmployeeDAO dao = new EmployeeDAO();
    private List<Employee> employees = new ArrayList<>();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Employee no", "Name", "Department", "Position",
                         "Monthly salary", "Contact", "Date hired", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextField txtSearch = new JTextField();
    private final JComboBox<String> cboFilter = new JComboBox<>();
    private final JLabel lblCount = new JLabel(" ");
    private final JButton btnAdd = primaryButton("Add employee");
    private final JButton btnEdit = secondaryButton("Edit");
    private final JButton btnToggle = secondaryButton("Deactivate");

    public EmployeeFrame() {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(28, 32, 24, 32));

        loadDepartmentFilter();   // fill the dropdown before any listener is attached

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> openDialog(null));
        btnEdit.addActionListener(e -> {
            Employee emp = selected();
            if (emp != null) openDialog(emp);
        });
        btnToggle.addActionListener(e -> toggleStatus());

        reload();
    }

    // ---------- layout ----------

    private JPanel buildHeader() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JLabel h = new JLabel("Employees");
        h.setFont(h.getFont().deriveFont(Font.BOLD, 24f));
        h.setForeground(TEXT);
        h.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Manage employee records and their sign-in accounts");
        sub.setForeground(MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(h);
        top.add(Box.createVerticalStrut(4));
        top.add(sub);

        // toolbar: search + department filter on the left, add on the right
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(20, 0, 14, 0));
        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtSearch.setPreferredSize(new Dimension(300, 38));
        txtSearch.putClientProperty("JTextField.placeholderText", "Search by name or employee no");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { reload(); }
            @Override public void removeUpdate(DocumentEvent e) { reload(); }
            @Override public void changedUpdate(DocumentEvent e) { reload(); }
        });

        cboFilter.setPreferredSize(new Dimension(190, 38));
        cboFilter.addActionListener(e -> reload());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(txtSearch);
        left.add(cboFilter);

        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(btnAdd, BorderLayout.EAST);
        top.add(toolbar);

        return top;
    }

    private JScrollPane buildTable() {
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));
        table.setForeground(TEXT);

        int[] widths = {100, 170, 130, 120, 110, 110, 105, 80};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        rightAlignColumn(4); // monthly salary

        // status column: green = ACTIVE, red = INACTIVE
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                           boolean focus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                if (!sel) comp.setForeground("INACTIVE".equals(v) ? ERROR_RED : OK);
                return comp;
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> updateButtons());
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Employee emp = selected();
                    if (emp != null) openDialog(emp);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private void rightAlignColumn(int col) {
        DefaultTableCellRenderer cell = new DefaultTableCellRenderer();
        cell.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(col).setCellRenderer(cell);

        TableCellRenderer base = table.getTableHeader().getDefaultRenderer();
        table.getColumnModel().getColumn(col).setHeaderRenderer((t, v, sel, foc, row, c) -> {
            Component comp = base.getTableCellRendererComponent(t, v, sel, foc, row, c);
            if (comp instanceof JLabel) {
                ((JLabel) comp).setHorizontalAlignment(SwingConstants.RIGHT);
            }
            return comp;
        });
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(14, 0, 0, 0));

        lblCount.setForeground(MUTED);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(btnEdit);
        buttons.add(btnToggle);

        footer.add(lblCount, BorderLayout.WEST);
        footer.add(buttons, BorderLayout.EAST);
        return footer;
    }

    // ---------- behavior ----------

    private void loadDepartmentFilter() {
        cboFilter.addItem(ALL);
        try {
            for (Department d : new DepartmentDAO().findAll()) {
                cboFilter.addItem(d.getDepartmentName());
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void reload() {
        try {
            String keyword = txtSearch.getText().trim();
            List<Employee> found = keyword.isEmpty() ? dao.findAll() : dao.search(keyword);

            String dept = (String) cboFilter.getSelectedItem();
            if (dept != null && !dept.equals(ALL)) {
                found = found.stream()
                        .filter(e -> dept.equals(e.getDepartmentName()))
                        .toList();
            }
            employees = found;

            model.setRowCount(0);
            for (Employee e : employees) {
                model.addRow(new Object[]{
                    e.getEmployeeNo(),
                    e.getFullName(),
                    e.getDepartmentName(),
                    e.getPositionTitle(),
                    String.format("%,.2f", e.getMonthlySalary()),
                    e.getContactNo() == null ? "" : e.getContactNo(),
                    e.getDateHired().format(DATE_FMT),
                    e.getStatus()
                });
            }
            lblCount.setText(employees.size() + (employees.size() == 1 ? " employee" : " employees"));
            updateButtons();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private Employee selected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return employees.get(table.convertRowIndexToModel(row));
    }

    private void updateButtons() {
        Employee e = selected();
        btnEdit.setEnabled(e != null);
        btnToggle.setEnabled(e != null);
        btnToggle.setText(e != null && "INACTIVE".equals(e.getStatus()) ? "Activate" : "Deactivate");
    }

    private void toggleStatus() {
        Employee e = selected();
        if (e == null) return;

        boolean activate = "INACTIVE".equals(e.getStatus());
        String message = activate
                ? "Activate " + e.getFullName() + "? They will be able to sign in again."
                : "Deactivate " + e.getFullName() + "? They will no longer be able to sign in.\n"
                  + "Their attendance and payslip history is kept.";
        int choice = JOptionPane.showConfirmDialog(this, message,
                activate ? "Activate employee" : "Deactivate employee",
                JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        try {
            dao.setStatus(e.getEmployeeId(), activate ? "ACTIVE" : "INACTIVE");
            reload();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void openDialog(Employee existing) {
        try {
            List<Department> departments = new DepartmentDAO().findAll();
            List<JobPosition> positions = new JobPositionDAO().findAll();
            EmployeeDialog dialog = new EmployeeDialog(
                    SwingUtilities.getWindowAncestor(this), existing, departments, positions);
            dialog.setVisible(true);
            if (dialog.saved) reload();
        } catch (SQLException ex) {
            showError(ex);
        }
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

    // =====================================================================
    // Add / Edit form
    // =====================================================================

    private class EmployeeDialog extends JDialog {

        private final Employee existing;
        boolean saved = false;

        private final JTextField txtNo = new JTextField();
        private final JTextField txtFirst = new JTextField();
        private final JTextField txtLast = new JTextField();
        private final JTextField txtContact = new JTextField();
        private final JTextField txtEmail = new JTextField();
        private final JTextField txtAddress = new JTextField();
        private final JTextField txtDate = new JTextField();
        private final JComboBox<Department> cboDept;
        private final JComboBox<JobPosition> cboPos;
        private final JComboBox<String> cboStatus = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        private final JLabel lblError = new JLabel(" ");

        EmployeeDialog(Window owner, Employee existing,
                       List<Department> departments, List<JobPosition> positions) {
            super(owner, existing == null ? "Add employee" : "Edit employee",
                    Dialog.ModalityType.APPLICATION_MODAL);
            this.existing = existing;
            cboDept = new JComboBox<>(departments.toArray(new Department[0]));
            cboPos = new JComboBox<>(positions.toArray(new JobPosition[0]));

            setLayout(new BorderLayout());
            getContentPane().setBackground(Color.WHITE);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(Color.WHITE);
            form.setBorder(new EmptyBorder(24, 28, 8, 28));

            int row = 0;
            row = addRow(form, row, "Employee no", txtNo);
            row = addRow(form, row, "First name", txtFirst);
            row = addRow(form, row, "Last name", txtLast);
            row = addRow(form, row, "Contact no", txtContact);
            row = addRow(form, row, "Email", txtEmail);
            row = addRow(form, row, "Address", txtAddress);
            row = addRow(form, row, "Date hired", txtDate);
            row = addRow(form, row, "Department", cboDept);
            row = addRow(form, row, "Position", cboPos);
            if (existing != null) {
                addRow(form, row, "Status", cboStatus);
            }
            txtDate.putClientProperty("JTextField.placeholderText", "yyyy-mm-dd");

            fillForm();

            // bottom: error message on the left, buttons on the right
            lblError.setForeground(ERROR_RED);
            lblError.setFont(lblError.getFont().deriveFont(Font.PLAIN, 12f));

            JButton btnCancel = secondaryButton("Cancel");
            JButton btnSave = primaryButton("Save");
            btnCancel.addActionListener(e -> dispose());
            btnSave.addActionListener(e -> onSave());

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttons.setOpaque(false);
            buttons.add(btnCancel);
            buttons.add(btnSave);

            JPanel bottom = new JPanel(new BorderLayout());
            bottom.setBackground(Color.WHITE);
            bottom.setBorder(new EmptyBorder(8, 28, 20, 28));
            bottom.add(lblError, BorderLayout.WEST);
            bottom.add(buttons, BorderLayout.EAST);

            add(form, BorderLayout.CENTER);
            add(bottom, BorderLayout.SOUTH);

            getRootPane().setDefaultButton(btnSave);
            setResizable(false);
            pack();
            setLocationRelativeTo(owner);
        }

        private int addRow(JPanel p, int row, String label, JComponent field) {
            JLabel l = new JLabel(label);
            l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
            l.setForeground(TEXT_SOFT);

            GridBagConstraints c = new GridBagConstraints();
            c.gridy = row;
            c.insets = new Insets(7, 0, 7, 16);
            c.anchor = GridBagConstraints.WEST;

            c.gridx = 0;
            p.add(l, c);

            field.setPreferredSize(new Dimension(280, 36));
            c.gridx = 1;
            c.insets = new Insets(7, 0, 7, 0);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            p.add(field, c);
            return row + 1;
        }

        private void fillForm() {
            if (existing == null) {
                txtDate.setText(LocalDate.now().toString());
                return;
            }
            txtNo.setText(existing.getEmployeeNo());
            txtFirst.setText(existing.getFirstName());
            txtLast.setText(existing.getLastName());
            txtContact.setText(existing.getContactNo() == null ? "" : existing.getContactNo());
            txtEmail.setText(existing.getEmail() == null ? "" : existing.getEmail());
            txtAddress.setText(existing.getAddress() == null ? "" : existing.getAddress());
            txtDate.setText(existing.getDateHired().toString());
            cboStatus.setSelectedItem(existing.getStatus());
            for (int i = 0; i < cboDept.getItemCount(); i++) {
                if (cboDept.getItemAt(i).getDepartmentId() == existing.getDepartmentId()) {
                    cboDept.setSelectedIndex(i);
                }
            }
            for (int i = 0; i < cboPos.getItemCount(); i++) {
                if (cboPos.getItemAt(i).getPositionId() == existing.getPositionId()) {
                    cboPos.setSelectedIndex(i);
                }
            }
        }

        private void onSave() {
            String no = txtNo.getText().trim();
            String first = txtFirst.getText().trim();
            String last = txtLast.getText().trim();

            if (no.isEmpty() || first.isEmpty() || last.isEmpty()) {
                lblError.setText("Employee no, first name and last name are required.");
                return;
            }
            if (no.length() > 20) {
                lblError.setText("Employee no must be 20 characters or fewer.");
                return;
            }

            LocalDate hired;
            try {
                hired = LocalDate.parse(txtDate.getText().trim());
            } catch (DateTimeParseException ex) {
                lblError.setText("Date hired must look like 2026-10-12.");
                return;
            }

            Department dept = (Department) cboDept.getSelectedItem();
            JobPosition pos = (JobPosition) cboPos.getSelectedItem();
            if (dept == null || pos == null) {
                lblError.setText("Add a department and a job position in Setup first.");
                return;
            }

            Employee e = new Employee();
            if (existing != null) e.setEmployeeId(existing.getEmployeeId());
            e.setEmployeeNo(no);
            e.setFirstName(first);
            e.setLastName(last);
            e.setContactNo(txtContact.getText());
            e.setEmail(txtEmail.getText());
            e.setAddress(txtAddress.getText());
            e.setDateHired(hired);
            e.setDepartmentId(dept.getDepartmentId());
            e.setPositionId(pos.getPositionId());
            e.setStatus(existing == null ? "ACTIVE" : (String) cboStatus.getSelectedItem());

            try {
                if (existing == null) {
                    dao.insert(e);
                    JOptionPane.showMessageDialog(this,
                            "Employee added.\n\nSign-in account created:\n"
                            + "Username: " + no + "\n"
                            + "Password: " + EmployeeDAO.DEFAULT_PASSWORD,
                            "Employee added", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    dao.update(e);
                }
                saved = true;
                dispose();
            } catch (SQLException ex) {
                if ("23505".equals(ex.getSQLState())) {
                    lblError.setText("That employee no or email already exists.");
                } else {
                    ex.printStackTrace();
                    lblError.setText("Could not save: " + ex.getMessage());
                }
            }
        }
    }
}