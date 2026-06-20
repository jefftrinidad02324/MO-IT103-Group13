/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.motorphpayroll;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Main user interface class of the MotorPH Payroll System.
 * Handles login, employee services, payroll processing,
 * and employee record management.
 */
public class MotorPHPayroll {

   
    static JFrame loginFrame;
    static JFrame employeeFrame;
    static JFrame payrollFrame;

    static JTextField employeeNumberField;
    static JTextField employeeNameField;
    static JTextArea employeeOutputArea;

    static JTextField payrollEmployeeNumberField;
    static JTextField payrollEmployeeNameField;
    static JTextArea payrollOutputArea;

    static JTable employeeTable;
    static DefaultTableModel employeeTableModel;
    static JTextField[] formFields;

    static String loggedInEmployeeNumber = "";

    static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 24);
    static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 16);
    static final Font BUTTON_FONT = new Font("Arial", Font.PLAIN, 15);
    static final Font FIELD_FONT = new Font("Arial", Font.PLAIN, 15);
    static final Font OUTPUT_FONT = new Font("Monospaced", Font.PLAIN, 14);

    /**
 * Entry point of the application.
 * Launches the login page on the Swing Event Dispatch Thread.
 */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showLoginPage();
            }
        });
    }

     /**
 * Creates and displays the login window.
 * Validates employee and payroll staff credentials
 * before granting access to the system.
 */
    public static void showLoginPage() {
        loginFrame = new JFrame("MotorPH Login");
        loginFrame.setSize(760, 420);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 55, 30, 55));

        JLabel titleLabel = new JLabel("MOTORPH PAYROLL SYSTEM");
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 25));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel usernameLabel = createLoginLabel("Employee Number / Staff Username:");
        JLabel passwordLabel = createLoginLabel("Password:");
        final JTextField usernameField = createLoginTextField(24);
        final JPasswordField passwordField = createLoginPasswordField(24);

        addFormRow(formPanel, gbc, 0, usernameLabel, usernameField);
        addFormRow(formPanel, gbc, 1, passwordLabel, passwordField);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        JButton loginButton = createButton("Login", 160, 40);
        JButton exitButton = createButton("Exit program", 170, 40);
        buttonPanel.add(loginButton);
        buttonPanel.add(exitButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());

                if (username.isEmpty()) {
                    JOptionPane.showMessageDialog(loginFrame,
                            "Please enter your employee number or staff username to continue.");
                    return;
                }

                if (password.isEmpty()) {
                    JOptionPane.showMessageDialog(loginFrame,
                            "Please enter your password to continue.");
                    return;
                }

                if (username.equals("payroll_staff")) {
                    if (!password.equals("12345")) {
                        JOptionPane.showMessageDialog(loginFrame,
                                "Wrong password. Please enter the correct password.");
                        return;
                    }

                    loggedInEmployeeNumber = "";
                    loginFrame.dispose();
                    showPayrollPage();
                    return;
                }

                if (!EmployeeService.isEmployeeNumberInSeries(username)) {
                    JOptionPane.showMessageDialog(loginFrame,
                            "Wrong username or employee number. Employee numbers must be in sequence starting from 10001.");
                    return;
                }

                if (!isEmployeeLogin(username)) {
                    JOptionPane.showMessageDialog(loginFrame,
                            "Wrong username or employee number. Please enter the correct credentials.");
                    return;
                }

                if (!password.equals("12345")) {
                    JOptionPane.showMessageDialog(loginFrame,
                            "Wrong password. Please enter the correct password.");
                    return;
                }

                loggedInEmployeeNumber = getEmployeeNumberFromLogin(username);
                loginFrame.dispose();
                showEmployeePage();
            }
        });

        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });

        loginFrame.add(mainPanel);
        loginFrame.setVisible(true);
    }
/**
 * Checks whether the entered employee number
 * exists in the employee records.
 *
 * @param username Employee number entered by the user
 * @return true if employee exists; otherwise false
 */
    private static boolean isEmployeeLogin(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        return EmployeeService.findEmployee(username.trim()) != null;
    }
/**
 * Retrieves and returns the employee number
 * used during login.
 *
 * @param username Employee login identifier
 * @return Employee number
 */
    private static String getEmployeeNumberFromLogin(String username) {
        return username.trim();
    }
/**
 * Displays the Employee Self-Service Portal.
 * Allows employees to view personal information
 * and generate individual payslips.
 */
    public static void showEmployeePage() {
        employeeFrame = new JFrame("Employee Portal");
        employeeFrame.setSize(950, 700);
        employeeFrame.setLocationRelativeTo(null);
        employeeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel titleLabel = new JLabel("EMPLOYEE SELF-SERVICE PORTAL");
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        employeeNumberField = createTextField(20);
        employeeNameField = createTextField(28);
        employeeNumberField.setEditable(false);
        employeeNameField.setEditable(false);

        addFormRow(formPanel, gbc, 0, createLabel("Employee Number:"), employeeNumberField);
        addFormRow(formPanel, gbc, 1, createLabel("Employee Name:"), employeeNameField);
        centerPanel.add(formPanel, BorderLayout.NORTH);

        employeeOutputArea = new JTextArea();
        employeeOutputArea.setEditable(false);
        employeeOutputArea.setFont(OUTPUT_FONT);
        employeeOutputArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(employeeOutputArea);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton infoButton = createButton("View My Information", 190, 38);
        JButton payslipButton = createButton("View My Payslip", 180, 38);
        JButton backButton = createButton("Back to login", 160, 38);
        JButton exitButton = createButton("Exit program", 160, 38);

        buttonPanel.add(infoButton);
        buttonPanel.add(payslipButton);
        buttonPanel.add(backButton);
        buttonPanel.add(exitButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        employeeNumberField.setText(loggedInEmployeeNumber);
        EmployeeService.displayEmployee(loggedInEmployeeNumber, employeeNameField, employeeOutputArea);

        infoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                EmployeeService.displayEmployee(loggedInEmployeeNumber, employeeNameField, employeeOutputArea);
            }
        });

        payslipButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                PayrollService.processOne(loggedInEmployeeNumber, employeeNameField, employeeOutputArea);
            }
        });

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                loggedInEmployeeNumber = "";
                employeeFrame.dispose();
                showLoginPage();
            }
        });

        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });

        employeeFrame.add(mainPanel);
        employeeFrame.setVisible(true);
    }
/**
 * Displays the Payroll Staff Portal.
 * Allows payroll staff to process payroll,
 * generate summaries, and manage employee records.
 */
    public static void showPayrollPage() {
        payrollFrame = new JFrame("Payroll Staff Portal");
        payrollFrame.setSize(1160, 780);
        payrollFrame.setLocationRelativeTo(null);
        payrollFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel titleLabel = new JLabel("PAYROLL STAFF PORTAL");
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        payrollEmployeeNumberField = createTextField(20);
        payrollEmployeeNameField = createTextField(28);
        payrollEmployeeNameField.setEditable(false);

        addFormRow(inputPanel, gbc, 0, createLabel("Employee Number:"), payrollEmployeeNumberField);
        addFormRow(inputPanel, gbc, 1, createLabel("Employee Name:"), payrollEmployeeNameField);

        payrollOutputArea = new JTextArea();
        payrollOutputArea.setEditable(false);
        payrollOutputArea.setFont(OUTPUT_FONT);
        payrollOutputArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(payrollOutputArea);

        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.insets = new Insets(5, 7, 5, 7);

        JButton processOneButton = createButton("Process One Employee", 230, 38);
        JButton processAllButton = createButton("Process All Employees", 230, 38);
        JButton summaryButton = createButton("Generate Summary", 200, 38);
        JButton recordsButton = createButton("Manage Records", 190, 38);
        JButton backButton = createButton("Back to login", 170, 38);
        JButton exitButton = createButton("Exit program", 170, 38);

        addButtonToGrid(buttonPanel, buttonGbc, processOneButton, 0, 0);
        addButtonToGrid(buttonPanel, buttonGbc, processAllButton, 1, 0);
        addButtonToGrid(buttonPanel, buttonGbc, summaryButton, 2, 0);
        addButtonToGrid(buttonPanel, buttonGbc, recordsButton, 0, 1);
        addButtonToGrid(buttonPanel, buttonGbc, backButton, 1, 1);
        addButtonToGrid(buttonPanel, buttonGbc, exitButton, 2, 1);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        processOneButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String employeeNumber = payrollEmployeeNumberField.getText().trim();

                if (!EmployeeService.isEmployeeNumberInSeries(employeeNumber)) {
                    JOptionPane.showMessageDialog(payrollFrame,
                            "Invalid employee number. Employee numbers must be in sequence starting from 10001.");
                    payrollEmployeeNameField.setText("");
                    payrollOutputArea.setText("");
                    return;
                }

                PayrollService.processOne(
                        employeeNumber,
                        payrollEmployeeNameField,
                        payrollOutputArea
                );
            }
        });

        processAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                payrollEmployeeNameField.setText("");
                PayrollService.processAll(payrollOutputArea);
            }
        });

        summaryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                payrollOutputArea.setText(PayrollService.generateSummary());
                JOptionPane.showMessageDialog(payrollFrame,
                        "Payroll summary has been generated successfully.");
            }
        });

        recordsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                payrollFrame.setVisible(false);
                showEmployeeRecordsPage();
            }
        });

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                payrollFrame.dispose();
                showLoginPage();
            }
        });

        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });

        payrollFrame.add(mainPanel);
        payrollFrame.setVisible(true);
    }
/**
 * Displays the Employee Record Management window.
 * Allows payroll staff to add, update, delete,
 * search, and view employee records.
 */
    public static void showEmployeeRecordsPage() {
        final JFrame recordsFrame = new JFrame("Employee Record Management");
        recordsFrame.setSize(1500, 850);
        recordsFrame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));

        JLabel titleLabel = new JLabel("EMPLOYEE RECORD MANAGEMENT");
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setFont(TITLE_FONT);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        String[] visibleColumns = {
                "Employee Number", "Last Name", "First Name", "Birthday", "SSS #",
                "PhilHealth #", "TIN #", "Pag-IBIG #", "Basic Salary", "Hourly Rate"
        };

        employeeTableModel = new DefaultTableModel(visibleColumns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        employeeTable = new JTable(employeeTableModel);
        employeeTable.setAutoCreateRowSorter(true);
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeTable.setFont(new Font("Arial", Font.PLAIN, 13));
        employeeTable.setRowHeight(24);
        employeeTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        employeeTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        EmployeeService.loadEmployeesToTable(employeeTableModel);

        JScrollPane tableScrollPane = new JScrollPane(employeeTable);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        JPanel lowerPanel = new JPanel(new BorderLayout(10, 10));
        lowerPanel.add(createEmployeeFormPanel(), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 4, 12, 10));
        JButton loadButton = createButton("Load Selected", 190, 40);
        JButton viewButton = createButton("View Details", 190, 40);
        JButton addButton = createButton("Add Record", 190, 40);
        JButton updateButton = createButton("Update Record", 190, 40);
        JButton deleteButton = createButton("Delete Record", 190, 40);
        JButton clearButton = createButton("Clear Form", 190, 40);
        JButton refreshButton = createButton("Refresh Table", 190, 40);
        JButton backButton = createButton("Back", 190, 40);

        buttonPanel.add(loadButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);

        lowerPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(lowerPanel, BorderLayout.SOUTH);

        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                loadSelectedEmployeeToForm();
            }
        });

        viewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                int selectedRow = employeeTable.getSelectedRow();
                if (selectedRow < 0) {
                    JOptionPane.showMessageDialog(recordsFrame,
                            "Please select an employee record before continuing.");
                    return;
                }

                int modelRow = employeeTable.convertRowIndexToModel(selectedRow);
                String employeeNumber = employeeTableModel.getValueAt(modelRow, 0).toString();
                String[] employee = EmployeeService.findEmployee(employeeNumber);

                if (employee == null) {
                    JOptionPane.showMessageDialog(recordsFrame,
                            "The selected employee record is no longer available. Please refresh the table and try again.");
                    return;
                }

                JTextArea detailsArea = new JTextArea(18, 45);
                detailsArea.setEditable(false);
                detailsArea.setFont(OUTPUT_FONT);
                detailsArea.setText(buildEmployeeDetails(employee));
                JOptionPane.showMessageDialog(recordsFrame, new JScrollPane(detailsArea),
                        "Employee Details", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (EmployeeService.addEmployee(getFormValues())) {
                    EmployeeService.loadEmployeesToTable(employeeTableModel);
                    clearForm();
                }
            }
        });

        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (formFields[0].getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(recordsFrame,
                            "Please select or enter an employee number before updating the record.");
                    return;
                }

                if (EmployeeService.updateEmployee(formFields[0].getText().trim(), getFormValues())) {
                    EmployeeService.loadEmployeesToTable(employeeTableModel);
                    clearForm();
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String employeeNumber = formFields[0].getText().trim();

                if (employeeNumber.isEmpty()) {
                    int selectedRow = employeeTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        int modelRow = employeeTable.convertRowIndexToModel(selectedRow);
                        employeeNumber = employeeTableModel.getValueAt(modelRow, 0).toString();
                    }
                }

                if (employeeNumber.isEmpty()) {
                    JOptionPane.showMessageDialog(recordsFrame,
                            "Please select an employee record before attempting to delete it.");
                    return;
                }

                int answer = JOptionPane.showConfirmDialog(recordsFrame,
                        "Are you sure you would like to delete Employee No. " + employeeNumber + "? This action cannot be undone.",
                        "Delete Employee Record", JOptionPane.YES_NO_OPTION);

                if (answer == JOptionPane.YES_OPTION) {
                    if (EmployeeService.deleteEmployee(employeeNumber)) {
                        EmployeeService.loadEmployeesToTable(employeeTableModel);
                        clearForm();
                    }
                }
            }
        });

        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                clearForm();
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                EmployeeService.loadEmployeesToTable(employeeTableModel);
                JOptionPane.showMessageDialog(recordsFrame,
                        "Employee records have been refreshed successfully.");
            }
        });

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                recordsFrame.dispose();
                if (payrollFrame != null) {
                    payrollFrame.setVisible(true);
                    payrollFrame.toFront();
                }
            }
        });

        recordsFrame.add(mainPanel);
        recordsFrame.setVisible(true);
    }
/**
 * Creates the employee information input form.
 *
 * @return JPanel containing all employee fields
 */
    
    private static JPanel createEmployeeFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Employee Information Form"));

        String[] labels = EmployeeService.EMPLOYEE_COLUMNS;
        formFields = new JTextField[labels.length];
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        for (int index = 0; index < labels.length; index++) {
            int row = index / 2;
            int pairColumn = index % 2;
            int labelColumn = pairColumn * 2;
            int fieldColumn = labelColumn + 1;

            JLabel label = createSmallLabel(labels[index] + ":");
            formFields[index] = createSmallTextField(18);

            gbc.gridx = labelColumn;
            gbc.gridy = row;
            gbc.fill = GridBagConstraints.NONE;
            formPanel.add(label, gbc);

            gbc.gridx = fieldColumn;
            gbc.gridy = row;
            gbc.fill = GridBagConstraints.NONE;
            formPanel.add(formFields[index], gbc);
        }

        return formPanel;
    }
/**
 * Loads the selected employee record from the table
 * into the input form for viewing or editing.
 */
    private static void loadSelectedEmployeeToForm() {
        int selectedRow = employeeTable.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(null,
                    "Please select an employee record before continuing.");
            return;
        }

        int modelRow = employeeTable.convertRowIndexToModel(selectedRow);
        String employeeNumber = employeeTableModel.getValueAt(modelRow, 0).toString();
        String[] employee = EmployeeService.findEmployee(employeeNumber);

        if (employee == null) {
            JOptionPane.showMessageDialog(null,
                    "The selected employee record is no longer available. Please refresh the table and try again.");
            return;
        }

        for (int index = 0; index < formFields.length; index++) {
            formFields[index].setText(employee[index]);
        }
    }
/**
 * Retrieves all values entered in the employee form.
 *
 * @return Array containing employee information
 */
    private static String[] getFormValues() {
        String[] values = new String[EmployeeService.EMPLOYEE_COLUMNS.length];

        for (int index = 0; index < values.length; index++) {
            values[index] = formFields[index].getText().trim();
        }

        return values;
    }
/**
 * Clears all employee input fields in the form.
 */
    private static void clearForm() {
        for (int index = 0; index < formFields.length; index++) {
            formFields[index].setText("");
        }
    }
/**
 * Formats employee information into a readable
 * text display.
 *
 * @param employee Employee data array
 * @return Formatted employee details
 */
    
    private static String buildEmployeeDetails(String[] employee) {
        StringBuilder details = new StringBuilder();

        for (int index = 0; index < EmployeeService.EMPLOYEE_COLUMNS.length; index++) {
            details.append(EmployeeService.EMPLOYEE_COLUMNS[index]);
            details.append(": ");
            details.append(employee[index]);
            details.append("\n");
        }

        return details.toString();
    }
/**
 * Creates a formatted label for the login page.
 */
    private static JLabel createLoginLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setPreferredSize(new Dimension(270, 36));
        return label;
    }
/**
 * Creates a formatted text field for login input.
 */
    private static JTextField createLoginTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(FIELD_FONT);
        field.setPreferredSize(new Dimension(320, 36));
        return field;
    }
/**
 * Creates a password field for secure password entry.
 */
    private static JPasswordField createLoginPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        field.setFont(FIELD_FONT);
        field.setPreferredSize(new Dimension(320, 36));
        return field;
    }
/**
 * Creates a standard form label.
 */
    private static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setPreferredSize(new Dimension(165, 32));
        return label;
    }
/**
 * Creates a compact label used in employee forms.
 */
    private static JLabel createSmallLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setPreferredSize(new Dimension(150, 27));
        return label;
    }
/**
 * Creates a standard text field component.
 */
    private static JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(FIELD_FONT);
        field.setPreferredSize(new Dimension(280, 34));
        return field;
    }
/**
 * Creates a compact text field for employee forms.
 */
    private static JTextField createSmallTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setPreferredSize(new Dimension(200, 27));
        return field;
    }
/**
 * Creates a formatted button with a specified size.
 */
    private static JButton createButton(String text, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setPreferredSize(new Dimension(width, height));
        button.setMinimumSize(new Dimension(width, height));
        return button;
    }
/**
 * Adds a label and input field to a form row.
 */
    private static void addFormRow(JPanel panel, GridBagConstraints gbc, int row,
                                   JLabel label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(field, gbc);
    }
/**
 * Adds a button to a specified grid position.
 */
    private static void addButtonToGrid(JPanel panel, GridBagConstraints gbc,
                                        JButton button, int column, int row) {
        gbc.gridx = column;
        gbc.gridy = row;
        panel.add(button, gbc);
    }
}
