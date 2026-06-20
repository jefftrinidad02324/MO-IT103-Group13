/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.motorphpayroll;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * EmployeeService Class
 * Handles all business logic and file operations for employee records:
 * - Reading, searching, adding, updating, and deleting employee data
 * - Validating input before saving
 * - Converting data to/from CSV format
 * - Loading data into GUI tables
 * All data is stored in "Employee Details.csv"
 */
public class EmployeeService {
 // Path to the CSV file that stores all employee information
    static String file =
            "src/main/java/com/mycompany/motorphpayroll/Employee Details.csv";
 // Minimum allowed employee number (all IDs start from 10001)
    public static final int MIN_EMPLOYEE_NUMBER = 10001;
 // List of all column names matching the structure of the CSV file
    public static final String[] EMPLOYEE_COLUMNS = {
            "Employee Number",
            "Last Name",
            "First Name",
            "Birthday",
            "Address",
            "Phone Number",
            "SSS #",
            "PhilHealth #",
            "TIN #",
            "Pag-IBIG #",
            "Status",
            "Position",
            "Immediate Supervisor",
            "Basic Salary",
            "Rice Subsidy",
            "Phone Allowance",
            "Clothing Allowance",
            "Gross Semi-Monthly Rate",
            "Hourly Rate"
    };
// Validates if an employee number is numeric and within the allowed range (≥ 10001)
    public static boolean isEmployeeNumberInSeries(String employeeNumber) {
        if (employeeNumber == null || !employeeNumber.trim().matches("\\d+")) {
            return false;
        }

        int number = Integer.parseInt(employeeNumber.trim());
        return number >= MIN_EMPLOYEE_NUMBER;
    }
    // Finds and displays employee details in the GUI components
    // Shows error messages if ID is invalid or record not found
    public static void displayEmployee(String id, JTextField nameField, JTextArea output) {
         // Check if input is empty
        if (id == null || id.trim().isEmpty()) {
            nameField.setText("");
            output.setText("Please enter your employee number to continue.");
            return;
        }
        
        // Validate employee number format and range
        if (!isEmployeeNumberInSeries(id)) {
            nameField.setText("");
            output.setText("Invalid employee number. Employee numbers must be in sequence starting from 10001.");
            return;
        }

        // Search for employee in file
        String[] employee = findEmployee(id.trim());

        // If not found
        if (employee == null) {
            nameField.setText("");
            output.setText("We couldn't locate your employee record. Please check your employee number and try again.");
            return;
        }

        // Format and display employee data
        String name = employee[2] + " " + employee[1];
        nameField.setText(name);

        output.setText(
                "Employee Number: " + employee[0] + "\n" +
                "Employee Name: " + name + "\n" +
                "Birthday: " + employee[3] + "\n" +
                "Address: " + employee[4] + "\n" +
                "Phone Number: " + employee[5] + "\n" +
                "Status: " + employee[10] + "\n" +
                "Position: " + employee[11] + "\n" +
                "Immediate Supervisor: " + employee[12] + "\n" +
                "Basic Salary: " + employee[13] + "\n" +
                "Hourly Rate: " + employee[18]
        );
    }

    /**
     * Reads all employee records from CSV and loads them into a JTable model
     * Only displays selected key fields in the table
     */
    public static void loadEmployeesToTable(DefaultTableModel model) {
        // Clear existing rows before loading new data
        model.setRowCount(0);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // Skip header row

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }

                 // Split CSV line correctly (handles commas inside quotes)
                String[] data = Utility.manualSplit(line);

                 // Add only required columns to table
                model.addRow(new Object[]{
                        data[0],
                        data[1],
                        data[2],
                        data[3],
                        data[6],
                        data[7],
                        data[8],
                        data[9],
                        data[13],
                        data[18]
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "The system was unable to load the employee records. Please verify that the Employee Details.csv file exists and try again.");
        }
    }

    // Searches the CSV file for a specific employee by number
    public static String[] findEmployee(String employeeNumber) {
        if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
            return null;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // Skip header

            // Loop through each record
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = Utility.manualSplit(line);

                // Match employee number
                if (data[0].trim().equals(employeeNumber.trim())) {
                    return data; // Return full record
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "The system was unable to search the employee records. Please check the required file and try again.");
        }

        return null; // Not found
    }

     /**
     * Adds a new employee record to the CSV file
     * Validates data first and checks for duplicate ID
     */
    public static boolean addEmployee(String[] employee) {
         // Validate all fields
        if (!isValidEmployee(employee)) {
            return false;
        }

        // Check if employee number already exists
        if (findEmployee(employee[0]) != null) {
            JOptionPane.showMessageDialog(null,
                    "This employee number already exists. Please use a different employee number.");
            return false;
        }

         // Append new record to file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.newLine();
            bw.write(toCsvLine(employee));

            JOptionPane.showMessageDialog(null,
                    "Employee record has been added successfully.");
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "We were unable to add the employee record. Please review the information and try again.");
            return false;
        }
    }

      /**
     * Updates an existing employee record
     * Replaces old data with new validated data
     */
    public static boolean updateEmployee(String employeeNumber, String[] updatedEmployee) {
        // Check if ID is provided
        if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Please select or enter an employee number before updating the record.");
            return false;
        }

        // Validate ID format and range
        if (!isEmployeeNumberInSeries(employeeNumber)) {
            JOptionPane.showMessageDialog(null,
                    "Invalid employee number. Employee numbers must be in sequence starting from 10001.");
            return false;
        }

         // Validate new data
        if (!isValidEmployee(updatedEmployee)) {
            return false;
        }

       // ❗ Note: Original duplicate validation kept as in your code 
        if (!isEmployeeNumberInSeries(employeeNumber)) {
            JOptionPane.showMessageDialog(null,
                    "Invalid employee number. Employee numbers must be in sequence starting from 10001.");
            return false;
        }

        List<String[]> employees = new ArrayList<String[]>();
        String header;
        boolean found = false;

         // Read all records from file
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            header = br.readLine();
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = Utility.manualSplit(line);

                // Replace if match found
                if (data[0].trim().equals(employeeNumber.trim())) {
                    employees.add(updatedEmployee);
                    found = true;
                } else {
                    employees.add(data);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "We were unable to read the employee file. Please verify that the file is available and try again.");
            return false;
        }

         // If employee not found
        if (!found) {
            JOptionPane.showMessageDialog(null,
                    "The selected employee record is no longer available. Please refresh the table and try again.");
            return false;
        }

         // Save updated list back to file
        return saveEmployees(header, employees, "Employee record has been updated successfully.");
    }

    // Deletes an employee record from the CSV file
    public static boolean deleteEmployee(String employeeNumber) {
        // Check if ID is provided
        if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Please select an employee record before attempting to delete it.");
            return false;
        }

        List<String[]> employees = new ArrayList<String[]>();
        String header;
        boolean found = false;

        // Read all records, skip the one to delete
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            header = br.readLine(); // Save header
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = Utility.manualSplit(line);

                // Keep all except the one to delete
                if (data[0].trim().equals(employeeNumber.trim())) {
                    found = true;
                } else {
                    employees.add(data);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "We were unable to read the employee file. Please verify that the file is available and try again.");
            return false;
        }

         // If employee not found
        if (!found) {
            JOptionPane.showMessageDialog(null,
                    "The selected employee record is no longer available. Please refresh the table and try again.");
            return false;
        }

         // Save remaining records back to file
        return saveEmployees(header, employees, "Employee record has been deleted successfully.");
    }

    /**
     * Validates employee data before saving or updating
     * Checks completeness, required fields, numeric formats, and valid ID
     */
    private static boolean isValidEmployee(String[] employee) {
        // Check if array length matches required columns
        if (employee == null || employee.length != EMPLOYEE_COLUMNS.length) {
            JOptionPane.showMessageDialog(null,
                    "The employee record is incomplete. Please review all fields and try again.");
            return false;
        }

         // Check required fields are not empty
        if (employee[0].trim().isEmpty()
                || employee[1].trim().isEmpty()
                || employee[2].trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Please complete the required fields: employee number, last name, and first name.");
            return false;
        }

       // Check employee number is numeric 
        if (!employee[0].trim().matches("\\d+")) {
            JOptionPane.showMessageDialog(null,
                    "Please enter a valid numeric employee number.");
            return false;
        }

         // Check employee number range
        if (!isEmployeeNumberInSeries(employee[0])) {
            JOptionPane.showMessageDialog(null,
                    "Invalid employee number. Employee numbers must be in sequence starting from 10001.");
            return false;
        }

        // Check numeric fields contain valid numbers
        int[] numericFields = {13, 14, 15, 16, 17, 18};
        for (int i = 0; i < numericFields.length; i++) {
            int column = numericFields[i];
            if (!employee[column].trim().isEmpty()
                    && !Utility.isNumeric(employee[column])) {
                JOptionPane.showMessageDialog(null,
                        "Please enter a valid number for " + EMPLOYEE_COLUMNS[column] + ".");
                return false;
            }
        }

        return true; // All validations passed
    }

     /**
     * Saves the full list of employees back to the CSV file
     * Used by update and delete operations
     */
    private static boolean saveEmployees(String header, List<String[]> employees, String successMessage) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            // Write header first
            bw.write(header == null ? "" : header);
            bw.newLine();

            // Write each employee record
            for (int i = 0; i < employees.size(); i++) {
                bw.write(toCsvLine(employees.get(i)));
                if (i < employees.size() - 1) {
                    bw.newLine();
                }
            }

            JOptionPane.showMessageDialog(null, successMessage);
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "We were unable to save the employee file. Please try again.");
            return false;
        }
    }

     /**
     * Converts a string array into a properly formatted CSV line
     * Handles commas, quotes, and line breaks inside values
     */
    private static String toCsvLine(String[] values) {
        StringBuilder line = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            String value = values[i] == null ? "" : values[i];
            boolean needsQuotes = value.contains(",")
                    || value.contains("\"")
                    || value.contains("\n")
                    || value.contains("\r");

            // Escape double quotes by replacing " with ""
            value = value.replace("\"", "\"\"");

            // Wrap in quotes if special characters exist
            if (needsQuotes) {
                line.append('"').append(value).append('"');
            } else {
                line.append(value);
            }

             // Add comma separator except after last field
            if (i < values.length - 1) {
                line.append(',');
            }
        }

        return line.toString();
    }
}

