package com.mycompany.motorphpayroll;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/** Procedural payroll module using static methods and arrays only. */
public class PayrollService {

    private static final String EMPLOYEE_DETAILS_FILE =
            "src/main/java/com/mycompany/motorphpayroll/Employee Details.csv";
    private static final String ATTENDANCE_RECORD_FILE =
            "src/main/java/com/mycompany/motorphpayroll/Employee Attendance Record.csv";

    public static void processOne(String employeeId, JTextField nameField, JTextArea outputArea) {
        outputArea.setText("");
        if (employeeId == null || employeeId.trim().isEmpty()) {
            outputArea.setText("Please enter an employee number before processing payroll.");
            return;
        }
        if (!EmployeeService.isEmployeeNumberInSeries(employeeId)) {
            if (nameField != null) nameField.setText("");
            outputArea.setText("Invalid employee number. Please enter a valid number starting from 10001.");
            return;
        }

        try {
            List<String[]> records = loadAttendance();
            if (calculate(employeeId.trim(), nameField, outputArea, records)) {
                JOptionPane.showMessageDialog(null,
                        "Payroll report has been generated successfully.");
            }
        } catch (Exception e) {
            outputArea.setText("The payroll report could not be processed. Please verify the CSV files.");
        }
    }

    public static void processAll(JTextArea outputArea) {
        outputArea.setText("");
        int processedCount = 0;
        try {
            List<String[]> employees = loadEmployees();
            List<String[]> records = loadAttendance();
            if (employees.isEmpty()) {
                outputArea.setText("No employee records are available for payroll processing.");
                return;
            }

            for (int i = 0; i < employees.size(); i++) {
                String id = employees.get(i)[0].trim();
                if (EmployeeService.isEmployeeNumberInSeries(id) &&
                        calculate(id, null, outputArea, records)) {
                    processedCount++;
                }
            }

            outputArea.append("\n====================================================\n");
            outputArea.append("ALL PAYROLL PROCESSING COMPLETE\n");
            outputArea.append("Employees processed: " + processedCount + "\n");
            outputArea.append("====================================================\n");
            JOptionPane.showMessageDialog(null,
                    "Payroll processing completed for " + processedCount + " employee(s).");
        } catch (Exception e) {
            outputArea.setText("All payroll reports could not be processed. Please verify the CSV files.");
        }
    }

    /** Feature 5: employee count, total gross, total deductions, and average net pay. */
    public static String generateSummary() {
        StringBuilder summary = new StringBuilder();
        try {
            List<String[]> employees = loadEmployees();
            List<String[]> records = loadAttendance();
            if (employees.isEmpty()) {
                return "No employee records are loaded. The payroll summary cannot be generated.";
            }

            int employeesWithPayroll = 0;
            double totalGross = 0;
            double totalDeductions = 0;
            double totalNet = 0;

            for (int i = 0; i < employees.size(); i++) {
                String[] employee = employees.get(i);
                String employeeId = employee[0].trim();
                if (!EmployeeService.isEmployeeNumberInSeries(employeeId)) continue;

                double basicSalary = Utility.parseDoubleSafe(employee[13]);
                double hourlyRate = Utility.parseDoubleSafe(employee[18]);
                double employeeGross = 0;
                double employeeDeductions = 0;
                double employeeNet = 0;
                boolean hasPayroll = false;

                for (int month = 6; month <= 12; month++) {
                    double[] cutoffHours = getMonthlyHours(employeeId, month, records);
                    double gross = (cutoffHours[0] + cutoffHours[1]) * hourlyRate;
                    if (gross <= 0) continue;

                    double deductions = computeTotalDeductions(basicSalary, gross);
                    employeeGross += gross;
                    employeeDeductions += deductions;
                    employeeNet += gross - deductions;
                    hasPayroll = true;
                }

                if (hasPayroll) {
                    employeesWithPayroll++;
                    totalGross += employeeGross;
                    totalDeductions += employeeDeductions;
                    totalNet += employeeNet;
                }
            }

            if (employeesWithPayroll == 0) {
                return "No payroll data was found from June to December.";
            }

            double averageNetPay = totalNet / employeesWithPayroll;
            summary.append("PAYROLL SUMMARY REPORT\n");
            summary.append("====================================================\n");
            summary.append("Payroll coverage: June to December\n");
            summary.append("Total number of employees: ").append(employees.size()).append("\n");
            summary.append("Employees with payroll data: ").append(employeesWithPayroll).append("\n");
            summary.append("Total gross pay: ").append(Utility.formatNumber(totalGross)).append("\n");
            summary.append("Total deductions: ").append(Utility.formatNumber(totalDeductions)).append("\n");
            summary.append("Average net pay: ").append(Utility.formatNumber(averageNetPay)).append("\n");
            summary.append("====================================================\n");
            summary.append("Summary generated successfully.\n");
        } catch (Exception e) {
            summary.append("The payroll summary could not be generated. Please verify the CSV files.");
        }
        return summary.toString();
    }

    private static boolean calculate(String employeeId, JTextField nameField,
                                     JTextArea outputArea, List<String[]> records) {
        String[] employee = EmployeeService.findEmployee(employeeId);
        if (employee == null) {
            outputArea.append("\nEmployee number " + employeeId + " was not found.\n");
            return false;
        }

        String name = employee[2].trim() + " " + employee[1].trim();
        String birthday = employee[3].trim();
        double basicSalary = Utility.parseDoubleSafe(employee[13]);
        double hourlyRate = Utility.parseDoubleSafe(employee[18]);
        if (hourlyRate <= 0 || basicSalary <= 0) {
            outputArea.append("\nEmployee " + employeeId + " has invalid salary data.\n");
            return false;
        }
        if (nameField != null) nameField.setText(name);

        boolean hasData = false;
        for (int month = 6; month <= 12; month++) {
            double[] hours = getMonthlyHours(employeeId, month, records);
            double firstHours = hours[0];
            double secondHours = hours[1];
            double firstLate = hours[2];
            double secondLate = hours[3];
            if (firstHours == 0 && secondHours == 0) continue;

            hasData = true;
            String monthName = Utility.getMonthName(month);
            double firstPayableHours = Math.max(0, firstHours - firstLate);
            double secondPayableHours = Math.max(0, secondHours - secondLate);
            double firstGross = firstPayableHours * hourlyRate;
            double secondGross = secondPayableHours * hourlyRate;
            double totalGross = firstGross + secondGross;

            double sss = Utility.getSSS(basicSalary);
            double philHealth = (basicSalary * 0.03) / 2;
            double pagIbig = basicSalary <= 1500 ? basicSalary * 0.01 : basicSalary * 0.02;
            double taxableIncome = Math.max(0, totalGross - sss - philHealth - pagIbig);
            double tax = Utility.getTax(taxableIncome);
            double totalDeductions = sss + philHealth + pagIbig + tax;
            double secondNet = secondGross - totalDeductions;
            double monthlyNet = firstGross + secondNet;

            outputArea.append("\n====================================================\n");
            outputArea.append("MONTH: " + monthName.toUpperCase() + "\n");
            outputArea.append("Employee number: " + employeeId + "\n");
            outputArea.append("Employee name: " + name + "\n");
            outputArea.append("Birthday: " + birthday + "\n");

            outputArea.append("\nFIRST CUTOFF: " + monthName + " 1 to " + monthName + " 15\n");
            outputArea.append("Hours worked: " + Utility.formatNumber(firstHours) + "\n");
            outputArea.append("Late deduction hours: " + Utility.formatNumber(firstLate) + "\n");
            outputArea.append("Gross salary: " + Utility.formatNumber(firstGross) + "\n");
            outputArea.append("Net salary: " + Utility.formatNumber(firstGross) + "\n");

            outputArea.append("\nSECOND CUTOFF: " + monthName + " 16 to end of month\n");
            outputArea.append("Hours worked: " + Utility.formatNumber(secondHours) + "\n");
            outputArea.append("Late deduction hours: " + Utility.formatNumber(secondLate) + "\n");
            outputArea.append("Gross salary: " + Utility.formatNumber(secondGross) + "\n");
            outputArea.append("SSS: " + Utility.formatNumber(sss) + "\n");
            outputArea.append("PhilHealth: " + Utility.formatNumber(philHealth) + "\n");
            outputArea.append("Pag-IBIG: " + Utility.formatNumber(pagIbig) + "\n");
            outputArea.append("Tax: " + Utility.formatNumber(tax) + "\n");
            outputArea.append("Total deductions: " + Utility.formatNumber(totalDeductions) + "\n");
            outputArea.append("Second cutoff net salary: " + Utility.formatNumber(secondNet) + "\n");
            outputArea.append("Monthly net salary: " + Utility.formatNumber(monthlyNet) + "\n");
            outputArea.append("====================================================\n");
        }

        if (!hasData) {
            outputArea.append("\nNo payroll records were found for employee " + employeeId +
                    " from June to December.\n");
        }
        return hasData;
    }

    /** [first hours, second hours, first late deductions, second late deductions]. */
    private static double[] getMonthlyHours(String employeeId, int month,
                                            List<String[]> records) {
        double firstHours = 0;
        double secondHours = 0;
        double firstLate = 0;
        double secondLate = 0;

        for (int i = 0; i < records.size(); i++) {
            String[] record = records.get(i);
            if (record.length < 6 || !record[0].trim().equals(employeeId)) continue;

            String[] dateParts = record[3].trim().split("/");
            if (dateParts.length != 3) continue;
            int recordMonth;
            int day;
            try {
                recordMonth = Integer.parseInt(dateParts[0]);
                day = Integer.parseInt(dateParts[1]);
            } catch (NumberFormatException e) {
                continue;
            }
            if (recordMonth != month || !Utility.isValidTime(record[4]) ||
                    !Utility.isValidTime(record[5])) continue;

            double[] work = Utility.computeHours(record[4], record[5]);
            if (day <= 15) {
                firstHours += work[0];
                firstLate += work[1];
            } else {
                secondHours += work[0];
                secondLate += work[1];
            }
        }
        return new double[]{firstHours, secondHours, firstLate, secondLate};
    }

    private static double computeTotalDeductions(double basicSalary, double grossPay) {
        double sss = Utility.getSSS(basicSalary);
        double philHealth = (basicSalary * 0.03) / 2;
        double pagIbig = basicSalary <= 1500 ? basicSalary * 0.01 : basicSalary * 0.02;
        double taxable = Math.max(0, grossPay - sss - philHealth - pagIbig);
        return sss + philHealth + pagIbig + Utility.getTax(taxable);
    }

    private static List<String[]> loadEmployees() throws Exception {
        List<String[]> employees = new ArrayList<String[]>();
        try (BufferedReader br = new BufferedReader(new FileReader(EMPLOYEE_DETAILS_FILE))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) employees.add(Utility.manualSplit(line));
            }
        }
        return employees;
    }

    private static List<String[]> loadAttendance() throws Exception {
        List<String[]> records = new ArrayList<String[]>();
        try (BufferedReader br = new BufferedReader(new FileReader(ATTENDANCE_RECORD_FILE))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) records.add(line.split(",", -1));
            }
        }
        return records;
    }
}
