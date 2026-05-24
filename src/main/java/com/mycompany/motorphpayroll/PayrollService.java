package com.mycompany.motorphpayroll;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * This class handles all the calculations for the MotorPH employee payroll system.
 * It reads files containing employee information and time logs, calculates their total earnings, 
 * subtracts government deductions, and prints the result to the screen.
 */
public class PayrollService {

    // File path pointing to where the spreadsheet of employee personal information is saved
    private static final String EMPLOYEE_DETAILS_FILE =
            "src/main/java/com/mycompany/motorphpayroll/Employee Details.csv";

    // File path pointing to where the spreadsheet of daily login/logout records is saved
    private static final String ATTENDANCE_RECORD_FILE =
            "src/main/java/com/mycompany/motorphpayroll/Employee Attendance Record.csv";

    // Standard business shift hours (8:00 AM to 5:00 PM)
    private static final double START_WORK = 8.0;
    private static final double END_WORK = 17.0;

    // ================= PROCESS ONE =================
    /**
     * Calculates and displays payroll for a single employee based on their ID.
     */
    public static void processOne(String employeeId, JTextField nameField, JTextArea outputArea) {
        outputArea.setText(""); // Clear the text screen before displaying the new results
        calculate(employeeId, nameField, outputArea); // Run the payroll calculations
    }

    // ================= PROCESS ALL =================
    /**
     * Loops through a specific batch of employee IDs (from 10001 to 10034) 
     * and calculates payroll for everyone all at once.
     */
    public static void processAll(JTextArea outputArea) {
        outputArea.setText(""); // Clear the text screen before starting

        // Go through each employee ID sequentially in a loop
        for (int id = 10001; id <= 10034; id++) {
            calculate(String.valueOf(id), null, outputArea); // Run calculation for the current ID
        }

        // Print a final message to the screen when everyone has been processed
        outputArea.append("\n====================================================\n");
        outputArea.append("ALL PAYROLL PROCESSING COMPLETE\n");
        outputArea.append("====================================================\n");
    }

    // ================= MAIN CALCULATION =================
    /**
     * This is the core engine that fetches data, loops through the dates, 
     * computes gross/net amounts, and prints out the final paycheck stub.
     */
    private static boolean calculate(String employeeId, JTextField nameField, JTextArea outputArea) {

        try {
            // Temporary holding slots for the employee data we are about to find
            String name = "";
            String birthday = "";
            double basicSalary = 0;
            double hourlyRate = 0;

            boolean found = false; // Kept as "false" until we successfully locate the employee in the file

            // ================= READ EMPLOYEE FILE =================
            // Open the Employee Details spreadsheet
            try (BufferedReader br = new BufferedReader(new FileReader(EMPLOYEE_DETAILS_FILE))) {

                String line;
                br.readLine(); // Skip the very first header line (e.g., "ID, Name, Salary")

                // Read the file line-by-line until the end
                while ((line = br.readLine()) != null) {

                    if (line.trim().isEmpty()) continue; // Skip empty rows

                    String[] data = manualSplit(line); // Break the row up into separate text columns

                    // Check if the ID in this column matches the ID we are searching for
                    if (data[0].trim().equals(employeeId.trim())) {

                        // Combine First Name and Last Name into one full name
                        name = data[2].trim() + " " + data[1].trim();
                        birthday = data[3].trim();

                        // Grab the basic monthly salary (column index 13) and hourly rate (column index 18)
                        basicSalary = parseDoubleSafe(data[13]);
                        hourlyRate = parseDoubleSafe(data[18]);

                        found = true; // Mark that we found the person successfully

                        // If a designated text box on the visual screen is provided, update it with their name
                        if (nameField != null) {
                            nameField.setText(name);
                        }
                        break; // Stop looking through the file since we found the match
                    }
                }
            }

            // If the loop finished and the employee wasn't found, print an error and stop
            if (!found) {
                outputArea.append("\nEmployee not found: " + employeeId);
                return false;
            }

            // ================= LOAD ATTENDANCE =================
            // Create an empty memory list to load the attendance text file into
            List<String[]> records = new ArrayList<>();

            // Open the attendance record spreadsheet
            try (BufferedReader br = new BufferedReader(new FileReader(ATTENDANCE_RECORD_FILE))) {

                String line;
                br.readLine(); // Skip the column headers line

                // Read every single log-in/log-out row and save it into our memory list
                while ((line = br.readLine()) != null) {
                    records.add(line.split(","));
                }
            }

            boolean hasData = false; // Remains "false" until we discover matching attendance data

            // ================= MONTH LOOP (JUNE–DECEMBER ONLY) =================
            // Cycle through each month starting from June (6) up to December (12)
            for (int month = 6; month <= 12; month++) {

                // Containers to track hours worked and hours late for both cutoff periods
                double cutoff1 = 0;      // Days 1-15 total hours worked
                double cutoff2 = 0;      // Days 16-end total hours worked
                double cutoff1Late = 0;  // Days 1-15 total late occurrences
                double cutoff2Late = 0;  // Days 16-end total late occurrences

                // Scan through every attendance row loaded earlier
                for (String[] r : records) {

                    // Skip the row if it belongs to a different employee
                    if (!r[0].trim().equals(employeeId.trim())) continue;

                    // Parse out the month and the day from the date format (MM/DD/YYYY)
                    String[] date = r[3].split("/");
                    int m = Integer.parseInt(date[0]);
                    int day = Integer.parseInt(date[1]);

                    // Skip if the row's month doesn't match the specific month loop we are on
                    if (m != month) continue;

                    // Calculate the daily hours worked [0] and late penalty status [1] from time-in and time-out
                    double[] work = computeDailyWork(r[4], r[5]);

                    // Sort the calculated daily metrics into Cutoff 1 or Cutoff 2 based on the day of the month
                    if (day <= 15) {
                        cutoff1 += work[0];
                        cutoff1Late += work[1];
                    } else {
                        cutoff2 += work[0];
                        cutoff2Late += work[1];
                    }
                }

                // If the employee didn't work a single hour in this specific month, skip calculation for this month
                if (cutoff1 == 0 && cutoff2 == 0) continue;

                hasData = true; // Employee has real historical data for this month

                // ================= COMPUTATION =================

                // Multiply total hours worked by hourly rate to get Gross Salary for each period
                double firstCutoffSalary = cutoff1 * hourlyRate;
                double secondCutoffSalary = cutoff2 * hourlyRate;

                // Total earnings combined before government deductions
                double totalGrossSalary = firstCutoffSalary + secondCutoffSalary;

                // DEDUCTIONS BASED ON BASIC SALARY
                // Find SSS deduction from tables using basic monthly salary
                double sss = getSSS(basicSalary);
                // PhilHealth deduction is 3% of basic salary, divided between the two cutoff periods
                double phil = (basicSalary * 0.03) / 2;

                // Pag-IBIG deduction rules: 1% if basic salary is under 1,500; 2% if it is over 1,500
                double pagibig =
                        (basicSalary <= 1500)
                                ? basicSalary * 0.01
                                : basicSalary * 0.02;

                // Deduct basic government contributions from the gross salary to determine taxable income
                double taxable = Math.max(0,
                        totalGrossSalary - (sss + phil + pagibig));

                // Run taxable income through tax brackets to compute standard withholding income tax
                double tax = getTax(taxable);

                // Add up all deductions together
                double totalDeductions = sss + phil + pagibig + tax;

                // Final net take-home salary applied strictly onto the second cutoff paycheck
                double netSalarySecondCutoff = secondCutoffSalary - totalDeductions;

                // ================= OUTPUT =================
                // Print a neat, formatted payslip summary layout directly onto the software window screen

                outputArea.append("\n========================================\n");
                outputArea.append("Employee number: " + employeeId + "\n");
                outputArea.append("Employee name: " + name + "\n");
                outputArea.append("Birthday: " + birthday + "\n");

                // Print First Cutoff Breakdown
                outputArea.append("\nCutoff date: 1 - 15 (" + getMonthName(month) + ")\n");
                outputArea.append("Total hours worked: " + cutoff1 + "\n");
                outputArea.append("Late hours: " + cutoff1Late + "\n");
                outputArea.append("Gross salary: " + firstCutoffSalary + "\n");
                outputArea.append("Net salary: " + firstCutoffSalary + "\n"); // Gross and Net are identical here

                // Print Second Cutoff Breakdown (where total monthly deductions are subtracted)
                outputArea.append("\nCutoff date: 16 - End (" + getMonthName(month) + ")\n");
                outputArea.append("Total hours worked: " + cutoff2 + "\n");
                outputArea.append("Late hours: " + cutoff2Late + "\n");
                outputArea.append("Gross salary: " + secondCutoffSalary + "\n");

                // Print Individual Deductions List
                outputArea.append("\nSSS: " + sss + "\n");
                outputArea.append("Phil-health: " + phil + "\n");
                outputArea.append("Pag-ibig: " + pagibig + "\n");
                outputArea.append("Tax: " + tax + "\n");

                // Print Summaries
                outputArea.append("Total deductions: " + totalDeductions + "\n");
                outputArea.append("Net salary: " + netSalarySecondCutoff + "\n");

                outputArea.append("========================================\n");
            }

            // Fallback screen printout if the employee has zero logs across June-December
            if (!hasData) {
                outputArea.append("\nNo records found (June–December only).");
            }

            return true;

        } catch (Exception e) {
            // Error handling fallback block: prevents the software from completely crashing if a file goes missing
            e.printStackTrace();
            outputArea.append("\nERROR: " + e.getMessage());
            return false;
        }
    }

    // ================= TIME COMPUTATION =================
    /**
     * Converts a single day's text timestamp (like "08:15" and "17:00") 
     * into countable math hours, accounts for late penalties, and subtracts lunch.
     */
    private static double[] computeDailyWork(String in, String out) {

        // Separate the time strings into hours and minutes
        String[] i = in.split(":");
        String[] o = out.split(":");

        // Convert timestamps to decimals (e.g., "8:30" becomes 8.5)
        double start = Integer.parseInt(i[0]) + Integer.parseInt(i[1]) / 60.0;
        double end = Integer.parseInt(o[0]) + Integer.parseInt(o[1]) / 60.0;

        // Cap work boundaries: cannot start tracking before 8AM and cannot track beyond 5PM
        start = Math.max(8.0, start);
        end = Math.min(17.0, end);

        double late = 0;

        int h = Integer.parseInt(i[0]);
        int m = Integer.parseInt(i[1]);

        // LATE RULE: If you log in after 8AM, or precisely at 8AM but your minutes are at 11 or higher,
        // you receive a fixed 0.5 hour (30 minutes) penalty value recorded.
        if (h > 8 || (h == 8 && m >= 11)) {
            late = 0.5;
        }

        // Subtract clock-in time from clock-out time to calculate standard hours
        double hours = end - start;
        hours -= 1; // Deduct exactly 1 hour automatically for the unpaid lunch break

        // Return a package containing [Total hours worked today, Late deduction category value]
        return new double[]{
                Math.max(0, hours),
                late
        };
    }

    // ================= HELPERS =================
    /**
     * Utility tool to clean up currency formatting (removes commas and quotation marks)
     * so that text strings can be securely processed as mathematical values.
     */
    private static double parseDoubleSafe(String v) {
        try {
            return Double.parseDouble(v.replace(",", "").replace("\"", ""));
        } catch (Exception e) {
            return 0; // Return zero if data is blank or completely garbled to prevent crashes
        }
    }

    /**
     * Converts calendar tracking numbers to real text month titles.
     */
    private static String getMonthName(int m) {
        switch (m) {
            case 6: return "June";
            case 7: return "July";
            case 8: return "August";
            case 9: return "September";
            case 10: return "October";
            case 11: return "November";
            case 12: return "December";
            default: return "";
        }
    }

    // ================= DEDUCTIONS =================
    /**
     * Chart indicating monthly SSS contribution values corresponding to designated basic salary ranges.
     */
    private static double getSSS(double salary) {
        if (salary <= 3250) return 135;
        if (salary <= 3750) return 157.5;
        if (salary <= 4250) return 180;
        if (salary <= 10000) return 450;
        if (salary <= 20000) return 900;
        return 1125; // Default value if the basic monthly salary is above 20,000
    }

    /**
     * Computes progressive BIR monthly withholding tax based on Philippine tax tables.
     */
    private static double getTax(double income) {
        if (income <= 20833) return 0; // Tax-exempt tier
        if (income <= 33333) return (income - 20833) * 0.20;
        if (income <= 66667) return 2500 + (income - 33333) * 0.25;
        if (income <= 166667) return 10833.33 + (income - 66667) * 0.30;
        return 40833.33 + (income - 166667) * 0.35; // Maximum progressive tax bracket
    }

    // ================= CSV PARSER =================
    /**
     * Specialized custom parser designed to break down a comma-separated row line into 19 individual columns.
     * It handles quotation marks elegantly, making sure commas inside name blocks don't mess up the parsing columns.
     */
    public static String[] manualSplit(String line) {

        String[] result = new String[19]; // Prepare an array with 19 distinct data compartments
        String current = "";
        int idx = 0;
        boolean inQuotes = false;

        // Loop character by character through the entire single string row line
        for (char c : line.toCharArray()) {

            if (c == '"') {
                inQuotes = !inQuotes; // Toggle tracking status if we encounter text encapsulated inside quotes
            } else if (c == ',' && !inQuotes) {
                result[idx++] = current; // Save column data and jump to the next compartment when we find an open comma
                current = "";
            } else {
                current += c; // Build the column value text systematically letter by letter
            }
        }

        result[idx] = current; // Save whatever remaining information is left on the tail end of the row line
        return result;
    }
}