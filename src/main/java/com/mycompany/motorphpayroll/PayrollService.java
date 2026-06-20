package com.mycompany.motorphpayroll;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the core payroll processing logic for the system.
 * It reads employee data and attendance text files, calculates regular pay,
 * computes government deductions, and displays payroll summaries onto the screen.
 */
public class PayrollService {

    // File paths pointing to where the spreadsheet records (CSV files) are saved.
    private static final String EMPLOYEE_DETAILS_FILE =
            "src/main/java/com/mycompany/motorphpayroll/Employee Details.csv";

    private static final String ATTENDANCE_RECORD_FILE =
            "src/main/java/com/mycompany/motorphpayroll/Employee Attendance Record.csv";

    /**
     * Processes payroll for a single employee based on the ID entered into the system.
     * It handles entry verification errors and triggers a confirmation window when successful.
     */
    public static void processOne(String employeeId, JTextField nameField, JTextArea outputArea) {
        outputArea.setText(""); // Clear out old text on the screen

        // Check if the user left the employee number field empty.
        if (employeeId == null || employeeId.trim().isEmpty()) {
            outputArea.setText("Please enter an employee number before processing payroll.");
            return;
        }

        // Validate that the ID follows the correct sequence (must start at 10001).
        if (!EmployeeService.isEmployeeNumberInSeries(employeeId.trim())) {
            if (nameField != null) {
                nameField.setText("");
            }
            outputArea.setText("Invalid employee number. Employee numbers must be in sequence starting from 10001.");
            return;
        }

        // Run the payroll calculations for this single employee.
        boolean successful = calculate(employeeId.trim(), nameField, outputArea, null);

        // Show a popup notification if the report was built successfully.
        if (successful) {
            JOptionPane.showMessageDialog(null,
                    "Payroll report has been generated successfully.");
        }
    }

    /**
     * Processes payroll for every single employee found in the records file all at once.
     * Skips invalid entries and prints a complete grand total summary at the very end.
     */
    public static void processAll(JTextArea outputArea) {
        outputArea.setText(""); // Clear out old text on the screen

        int processedCount = 0; // Keeps track of how many records were successfully processed

        try {
            // Read and load all employees from the text database.
            List<String[]> employees = loadEmployees();

            // Loop through each employee in the list one by one.
            for (int i = 0; i < employees.size(); i++) {
                String[] employee = employees.get(i);
                String employeeId = employee[0].trim();

                // If an ID is invalid, skip this person and print a note on screen.
                if (!EmployeeService.isEmployeeNumberInSeries(employeeId)) {
                    outputArea.append("\nSkipped invalid employee number: " + employeeId + "\n");
                    continue;
                }

                // Run calculations for the valid employee.
                boolean successful = calculate(employeeId, null, outputArea, null);

                if (successful) {
                    processedCount++; // Increment our counter for successful runs
                }
            }

            // Print the final grand summary layout to the screen window.
            outputArea.append("\n====================================================\n");
            outputArea.append("ALL PAYROLL PROCESSING COMPLETE\n");
            outputArea.append("Employees processed: " + processedCount + "\n");
            outputArea.append("====================================================\n");

            // Pop up a system message confirming everything finished successfully.
            JOptionPane.showMessageDialog(null,
                    "Payroll reports for all employees have been generated successfully.");

        } catch (Exception e) {
            // Catching any file reading problems safely without crashing the system.
            outputArea.setText("We were unable to process all payroll reports. Please verify the employee and attendance CSV files and try again.");
        }
    }

    /**
     * Loops through every record to accumulate overall totals (hours, gross pay, deductions, net pay)
     * and compiles a clean text report summary for the system dashboard.
     */
    public static String generateSummary() {
        StringBuilder summary = new StringBuilder();

        // Variables used to sum up the company-wide grand totals
        int employeeCount = 0;
        int attendanceCount = 0;
        double totalHours = 0;
        double totalGrossPay = 0;
        double totalDeductions = 0;
        double totalNetPay = 0;

        try {
            // Fetch raw lists from the data files
            List<String[]> employees = loadEmployees();
            List<String[]> attendanceRecords = loadAttendance();

            employeeCount = employees.size();
            attendanceCount = attendanceRecords.size();

            // Analyze every single employee
            for (int i = 0; i < employees.size(); i++) {
                String[] employee = employees.get(i);
                String employeeId = employee[0].trim();

                if (!EmployeeService.isEmployeeNumberInSeries(employeeId)) {
                    continue; // Skip invalid IDs
                }

                // Read salary info from standard columns (Column 14 for basic salary, Column 19 for hourly rate).
                double basicSalary = Utility.parseDoubleSafe(employee[13]);
                double hourlyRate = Utility.parseDoubleSafe(employee[18]);

                double employeeHours = 0;

                // Match and sum up all attendance log hours belonging to this specific employee
                for (int j = 0; j < attendanceRecords.size(); j++) {
                    String[] record = attendanceRecords.get(j);

                    if (!record[0].trim().equals(employeeId)) {
                        continue; // Keep looking if the log belongs to someone else
                    }

                    // Extract the month number from the date string (Format assumed: Month/Day/Year).
                    String[] dateParts = record[3].split("/");
                    int month = Integer.parseInt(dateParts[0]);

                    // Only look at records falling within June (Month 6) through December (Month 12).
                    if (month < 6 || month > 12) {
                        continue;
                    }

                    // Compute the hours for this specific day shift and add them to the running total.
                    double[] work = Utility.computeHours(record[4], record[5]);
                    employeeHours += work[0];
                }

                // If the employee actually logged hours during this date scope, compute their financial totals.
                if (employeeHours > 0) {
                    double grossPay = employeeHours * hourlyRate;
                    
                    // Deductions Breakdown
                    double sss = Utility.getSSS(basicSalary);
                    double philHealth = (basicSalary * 0.03) / 2; // 3% split equally between company & employee
                    double pagIbig = basicSalary <= 1500 ? basicSalary * 0.01 : basicSalary * 0.02; // 1% or 2% rate based on basic salary limit
                    
                    double taxableIncome = Math.max(0, grossPay - (sss + philHealth + pagIbig));
                    double tax = Utility.getTax(taxableIncome);
                    double deductions = sss + philHealth + pagIbig + tax;
                    double netPay = grossPay - deductions;

                    // Add individual results to global corporate metrics
                    totalHours += employeeHours;
                    totalGrossPay += grossPay;
                    totalDeductions += deductions;
                    totalNetPay += netPay;
                }
            }

            // Build out the physical text string structure for the summary printout view
            summary.append("PAYROLL SUMMARY REPORT\n");
            summary.append("====================================================\n");
            summary.append("Total employee records: ").append(employeeCount).append("\n");
            summary.append("Total attendance records: ").append(attendanceCount).append("\n");
            summary.append("Payroll coverage: June to December\n");
            summary.append("First cutoff: 1st day to 15th day of the month\n");
            summary.append("Second cutoff: 16th day to end of the month\n");
            summary.append("\n");
            summary.append("Total hours worked: ").append(Utility.formatNumber(totalHours)).append("\n");
            summary.append("Total gross pay: ").append(Utility.formatNumber(totalGrossPay)).append("\n");
            summary.append("Total deductions: ").append(Utility.formatNumber(totalDeductions)).append("\n");
            summary.append("Total net pay: ").append(Utility.formatNumber(totalNetPay)).append("\n");
            summary.append("====================================================\n");
            summary.append("Summary has been generated successfully.\n");

        } catch (Exception e) {
            summary.append("We were unable to generate the payroll summary. Please verify the required CSV files and try again.");
        }

        return summary.toString();
    }

    /**
     * Core calculator method. It evaluates data for a target employee, splits work hours into 
     * two separate cutoffs (1st-15th and 16th-End), applies statutory taxes, and types out 
     * a detailed payslip line-by-line onto the user interface text field.
     */
    private static boolean calculate(String employeeId, JTextField nameField, JTextArea outputArea, double[] summaryTotals) {
        try {
            // Confirm the sequence rule
            if (!EmployeeService.isEmployeeNumberInSeries(employeeId)) {
                outputArea.append("\nInvalid employee number " + employeeId + ". Employee numbers must be in sequence starting from 10001.\n");
                return false;
            }

            // Fetch the individual data profile from the external employee service
            String[] employee = EmployeeService.findEmployee(employeeId);

            if (employee == null) {
                outputArea.append("\nWe couldn't locate employee number " + employeeId + ". Please check the employee number and try again.\n");
                return false;
            }

            // Pull name components and basic financial rates from profile columns
            String name = employee[2].trim() + " " + employee[1].trim(); // First Name + Last Name
            String birthday = employee[3].trim();
            double basicSalary = Utility.parseDoubleSafe(employee[13]);
            double hourlyRate = Utility.parseDoubleSafe(employee[18]);

            // Set the graphic interface text box to show the name instantly
            if (nameField != null) {
                nameField.setText(name);
            }

            List<String[]> records = loadAttendance();
            boolean hasData = false;

            // Process calculations independently for each month from June (6) to December (12).
            for (int month = 6; month <= 12; month++) {
                double firstCutoffHours = 0;
                double secondCutoffHours = 0;
                double firstCutoffLate = 0;
                double secondCutoffLate = 0;

                // Sort out hours from the full log database file matching this month and ID
                for (int i = 0; i < records.size(); i++) {
                    String[] record = records.get(i);

                    if (!record[0].trim().equals(employeeId)) {
                        continue;
                    }

                    String[] dateParts = record[3].split("/");
                    int monthNumber = Integer.parseInt(dateParts[0]);
                    int dayNumber = Integer.parseInt(dateParts[1]);

                    if (monthNumber != month) {
                        continue;
                    }

                    double[] work = Utility.computeHours(record[4], record[5]);

                    // Assign time metrics depending on the day of the month (Cutoff 1 vs Cutoff 2).
                    if (dayNumber <= 15) {
                        firstCutoffHours += work[0];
                        firstCutoffLate += work[1];
                    } else {
                        secondCutoffHours += work[0];
                        secondCutoffLate += work[1];
                    }
                }

                // If this person has absolutely no recorded active time in this month, skip creating a receipt.
                if (firstCutoffHours == 0 && secondCutoffHours == 0) {
                    continue;
                }

                hasData = true; // Confirms valid calculation records were found

                String monthName = Utility.getMonthName(month);
                double firstGross = firstCutoffHours * hourlyRate;
                double secondGross = secondCutoffHours * hourlyRate;
                double totalGross = firstGross + secondGross;

                // Government contribution values lookup
                double sss = Utility.getSSS(basicSalary);
                double philHealth = (basicSalary * 0.03) / 2;
                double pagIbig = basicSalary <= 1500 ? basicSalary * 0.01 : basicSalary * 0.02;
                
                // Subtract combined statutory benefits before applying tax rates
                double taxableIncome = Math.max(0, totalGross - (sss + philHealth + pagIbig));
                double tax = Utility.getTax(taxableIncome);
                double totalDeductions = sss + philHealth + pagIbig + tax;
                
                // Final Net distribution logic:
                // Note: System currently applies all monthly statutory deductions onto the 2nd cutoff net check.
                double secondNet = secondGross - totalDeductions;
                double monthlyNet = firstGross + secondNet;

                // Append and display the final printed breakdown details onto the on-screen report layout
                outputArea.append("\n====================================================\n");
                outputArea.append("MONTH: " + monthName + "\n");
                outputArea.append("====================================================\n");
                outputArea.append("Employee number: " + employeeId + "\n");
                outputArea.append("Employee name: " + name + "\n");
                outputArea.append("Birthday: " + birthday + "\n");

                outputArea.append("\nCutoff date: " + monthName + " 1 to " + monthName + " 15\n");
                outputArea.append("Total hours worked: " + Utility.formatNumber(firstCutoffHours) + "\n");
                outputArea.append("Late deduction hours: " + Utility.formatNumber(firstCutoffLate) + "\n");
                outputArea.append("Gross salary: " + Utility.formatNumber(firstGross) + "\n");
                outputArea.append("Net salary: " + Utility.formatNumber(firstGross) + "\n");

                outputArea.append("\nCutoff date: " + monthName + " 16 to end of the month\n");
                outputArea.append("Total hours worked: " + Utility.formatNumber(secondCutoffHours) + "\n");
                outputArea.append("Late deduction hours: " + Utility.formatNumber(secondCutoffLate) + "\n");
                outputArea.append("Gross salary: " + Utility.formatNumber(secondGross) + "\n");

                outputArea.append("\nDeductions\n");
                outputArea.append("SSS: " + Utility.formatNumber(sss) + "\n");
                outputArea.append("PhilHealth: " + Utility.formatNumber(philHealth) + "\n");
                outputArea.append("Pag-IBIG: " + Utility.formatNumber(pagIbig) + "\n");
                outputArea.append("Tax: " + Utility.formatNumber(tax) + "\n");
                outputArea.append("Total deductions: " + Utility.formatNumber(totalDeductions) + "\n");
                outputArea.append("Net salary: " + Utility.formatNumber(secondNet) + "\n");
                outputArea.append("Monthly net salary: " + Utility.formatNumber(monthlyNet) + "\n");
                outputArea.append("====================================================\n");
            }

            // Print error text if loop finished without seeing a single hour entry
            if (!hasData) {
                outputArea.append("\nNo payroll records were found for employee number " + employeeId + " from June to December.\n");
                return false;
            }

            return true;
        } catch (Exception e) {
            outputArea.append("\nWe were unable to process the payroll report. Please verify the CSV files and try again.\n");
            return false;
        }
    }

    /**
     * File reader engine that opens the Employee CSV spreadsheet, reads it line-by-line,
     * strips headers, splits individual data rows, and populates a dynamic system list.
     */
    private static List<String[]> loadEmployees() throws Exception {
        List<String[]> employees = new ArrayList<String[]>();

        try (BufferedReader br = new BufferedReader(new FileReader(EMPLOYEE_DETAILS_FILE))) {
            String line;
            br.readLine(); // Discard the first header row (column descriptors)

            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    // Turn raw text row into a split field list array using our custom split utility.
                    employees.add(Utility.manualSplit(line));
                }
            }
        }

        return employees;
    }

    /**
     * File reader engine that opens the Attendance Log CSV, skips the title headers,
     * splits rows by standard comma divisions, and aggregates them into memory.
     */
    private static List<String[]> loadAttendance() throws Exception {
        List<String[]> records = new ArrayList<String[]>();

        try (BufferedReader br = new BufferedReader(new FileReader(ATTENDANCE_RECORD_FILE))) {
            String line;
            br.readLine(); // Discard the first header row (column descriptors)

            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    // Split the row by standard commas. The "-1" handles empty missing entries safely.
                    records.add(line.split(",", -1));
                }
            }
        }

        return records;
    }
}