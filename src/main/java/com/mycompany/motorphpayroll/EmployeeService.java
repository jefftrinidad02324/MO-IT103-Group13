package com.mycompany.motorphpayroll;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Procedural employee-record module.
 * All operations are static methods; no employee objects are created.
 */
public class EmployeeService {

    static final String FILE =
            "src/main/java/com/mycompany/motorphpayroll/Employee Details.csv";

    public static final int MIN_EMPLOYEE_NUMBER = 10001;

    public static final String[] EMPLOYEE_COLUMNS = {
            "Employee Number", "Last Name", "First Name", "Birthday",
            "Address", "Phone Number", "SSS #", "PhilHealth #", "TIN #",
            "Pag-IBIG #", "Status", "Position", "Immediate Supervisor",
            "Basic Salary", "Rice Subsidy", "Phone Allowance",
            "Clothing Allowance", "Gross Semi-Monthly Rate", "Hourly Rate"
    };

    public static boolean isEmployeeNumberInSeries(String employeeNumber) {
        if (employeeNumber == null || !employeeNumber.trim().matches("\\d+")) {
            return false;
        }
        try {
            return Integer.parseInt(employeeNumber.trim()) >= MIN_EMPLOYEE_NUMBER;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Returns the next sequential employee number, starting at 10001. */
    public static String getNextEmployeeNumber() {
        int highest = MIN_EMPLOYEE_NUMBER - 1;

        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] data = Utility.manualSplit(line);
                if (data[0].trim().matches("\\d+")) {
                    int number = Integer.parseInt(data[0].trim());
                    if (number > highest) {
                        highest = number;
                    }
                }
            }
        } catch (IOException e) {
            return String.valueOf(MIN_EMPLOYEE_NUMBER);
        }

        return String.valueOf(highest + 1);
    }

    public static void displayEmployee(String id, JTextField nameField, JTextArea output) {
        if (id == null || id.trim().isEmpty()) {
            nameField.setText("");
            output.setText("Please enter your employee number to continue.");
            return;
        }
        if (!isEmployeeNumberInSeries(id)) {
            nameField.setText("");
            output.setText("Invalid employee number. Please enter a valid number starting from 10001.");
            return;
        }

        String[] employee = findEmployee(id.trim());
        if (employee == null) {
            nameField.setText("");
            output.setText("We could not locate your employee record. Please check the employee number and try again.");
            return;
        }

        String name = employee[2] + " " + employee[1];
        nameField.setText(name);
        output.setText(
                "Employee Number: " + employee[0] + "\n" +
                "Employee Name: " + name + "\n" +
                "Birthday: " + employee[3] + "\n" +
                "Address: " + employee[4] + "\n" +
                "Phone Number: " + employee[5] + "\n" +
                "SSS Number: " + employee[6] + "\n" +
                "PhilHealth Number: " + employee[7] + "\n" +
                "TIN: " + employee[8] + "\n" +
                "Pag-IBIG Number: " + employee[9] + "\n" +
                "Status: " + employee[10] + "\n" +
                "Position: " + employee[11] + "\n" +
                "Immediate Supervisor: " + employee[12] + "\n" +
                "Basic Salary: " + employee[13] + "\n" +
                "Hourly Rate: " + employee[18]
        );
    }

    public static void loadEmployeesToTable(DefaultTableModel model) {
        model.setRowCount(0);
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] data = Utility.manualSplit(line);
                model.addRow(new Object[]{
                        data[0], data[1], data[2], data[3], data[6],
                        data[7], data[8], data[9], data[13], data[18]
                });
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "The employee records could not be loaded. Please verify that Employee Details.csv is available.");
        }
    }

    public static String[] findEmployee(String employeeNumber) {
        if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
            return null;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] data = Utility.manualSplit(line);
                if (data[0].trim().equals(employeeNumber.trim())) {
                    return data;
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "The employee file could not be searched. Please verify the required CSV file.");
        }
        return null;
    }

    public static boolean addEmployee(String[] employee) {
        employee[0] = getNextEmployeeNumber();
        if (!isValidEmployee(employee)) {
            return false;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE, true))) {
            if (new File(FILE).length() > 0) {
                bw.newLine();
            }
            bw.write(toCsvLine(employee));
            JOptionPane.showMessageDialog(null,
                    "Employee record " + employee[0] + " has been added successfully.");
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "The employee record could not be added. Please verify the file and try again.");
            return false;
        }
    }

    public static boolean updateEmployee(String originalEmployeeNumber, String[] updatedEmployee) {
        if (originalEmployeeNumber == null || originalEmployeeNumber.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Please select an employee record before updating.");
            return false;
        }

        updatedEmployee[0] = originalEmployeeNumber.trim();
        if (!isValidEmployee(updatedEmployee)) {
            return false;
        }

        List<String[]> employees = new ArrayList<String[]>();
        String header;
        boolean found = false;

        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            header = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] data = Utility.manualSplit(line);
                if (data[0].trim().equals(originalEmployeeNumber.trim())) {
                    employees.add(updatedEmployee);
                    found = true;
                } else {
                    employees.add(data);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "The employee file could not be read. Please verify the file and try again.");
            return false;
        }

        if (!found) {
            JOptionPane.showMessageDialog(null,
                    "The selected employee record is no longer available. Please refresh the table.");
            return false;
        }
        return saveEmployees(header, employees,
                "Employee record has been updated successfully.");
    }

    public static boolean deleteEmployee(String employeeNumber) {
        if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Please select an employee record before deleting.");
            return false;
        }

        List<String[]> employees = new ArrayList<String[]>();
        String header;
        boolean found = false;

        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            header = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] data = Utility.manualSplit(line);
                if (data[0].trim().equals(employeeNumber.trim())) {
                    found = true;
                } else {
                    employees.add(data);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "The employee file could not be read. Please verify the file and try again.");
            return false;
        }

        if (!found) {
            JOptionPane.showMessageDialog(null,
                    "The selected employee record was not found. Please refresh the table.");
            return false;
        }
        return saveEmployees(header, employees,
                "Employee record has been deleted successfully.");
    }

    private static boolean isValidEmployee(String[] employee) {
        if (employee == null || employee.length != EMPLOYEE_COLUMNS.length) {
            showValidation("The employee record is incomplete.");
            return false;
        }

        int[] required = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 17, 18};
        for (int i = 0; i < required.length; i++) {
            int column = required[i];
            if (employee[column] == null || employee[column].trim().isEmpty()) {
                showValidation("Please complete the required field: " + EMPLOYEE_COLUMNS[column] + ".");
                return false;
            }
        }

        if (!isEmployeeNumberInSeries(employee[0])) {
            showValidation("Employee number must be numeric and start from 10001.");
            return false;
        }
        if (!employee[1].matches("[A-Za-z .'-]{2,50}") ||
                !employee[2].matches("[A-Za-z .'-]{2,50}")) {
            showValidation("First name and last name may contain letters, spaces, apostrophes, periods, and hyphens only.");
            return false;
        }
        if (!Utility.isValidDate(employee[3])) {
            showValidation("Birthday must be a real date in MM/dd/yyyy format and cannot be today or a future date.");
            return false;
        }
        if (!Utility.isValidPhone(employee[5])) {
            showValidation("Phone number must contain 10 to 13 digits and may start with +.");
            return false;
        }
        if (!Utility.isValidSSS(employee[6])) {
            showValidation("SSS number must follow the format 00-0000000-0.");
            return false;
        }
        if (!Utility.isValidPhilHealth(employee[7])) {
            showValidation("PhilHealth number must follow the format 00-000000000-0.");
            return false;
        }
        if (!Utility.isValidTIN(employee[8])) {
            showValidation("TIN must follow the format 000-000-000 or 000-000-000-000.");
            return false;
        }
        if (!Utility.isValidPagIbig(employee[9])) {
            showValidation("Pag-IBIG number must follow the format 0000-0000-0000.");
            return false;
        }

        int[] numericFields = {13, 14, 15, 16, 17, 18};
        for (int i = 0; i < numericFields.length; i++) {
            int column = numericFields[i];
            if (!Utility.isNonNegativeNumber(employee[column])) {
                showValidation(EMPLOYEE_COLUMNS[column] + " must be a valid non-negative number.");
                return false;
            }
        }

        if (Utility.parseDoubleSafe(employee[13]) <= 0 ||
                Utility.parseDoubleSafe(employee[17]) <= 0 ||
                Utility.parseDoubleSafe(employee[18]) <= 0) {
            showValidation("Basic Salary, Gross Semi-Monthly Rate, and Hourly Rate must be greater than zero.");
            return false;
        }
        return true;
    }

    private static void showValidation(String message) {
        JOptionPane.showMessageDialog(null, message,
                "Input Validation", JOptionPane.WARNING_MESSAGE);
    }

    private static boolean saveEmployees(String header, List<String[]> employees,
                                         String successMessage) {
        File original = new File(FILE);
        File temporary = new File(FILE + ".tmp");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(temporary))) {
            bw.write(header == null ? "" : header);
            for (int i = 0; i < employees.size(); i++) {
                bw.newLine();
                bw.write(toCsvLine(employees.get(i)));
            }
        } catch (IOException e) {
            temporary.delete();
            JOptionPane.showMessageDialog(null,
                    "The employee file could not be prepared for saving.");
            return false;
        }

        if (original.exists() && !original.delete()) {
            temporary.delete();
            JOptionPane.showMessageDialog(null,
                    "The current employee file is in use. Please close it and try again.");
            return false;
        }
        if (!temporary.renameTo(original)) {
            JOptionPane.showMessageDialog(null,
                    "The updated employee file could not be finalized.");
            return false;
        }

        JOptionPane.showMessageDialog(null, successMessage);
        return true;
    }

    private static String toCsvLine(String[] values) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            String value = values[i] == null ? "" : values[i].trim();
            boolean quote = value.contains(",") || value.contains("\"") ||
                    value.contains("\n") || value.contains("\r");
            value = value.replace("\"", "\"\"");
            if (quote) {
                line.append('"').append(value).append('"');
            } else {
                line.append(value);
            }
            if (i < values.length - 1) {
                line.append(',');
            }
        }
        return line.toString();
    }
}
