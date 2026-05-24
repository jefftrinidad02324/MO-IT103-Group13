

//This utility class provides all necessary helper functions for the MotorPH Payroll System.It handles data processing, time conversion, work hours calculation, government contribution computation, payroll report generation
//payroll coverage validation, and all supporting calculations needed for accurate payroll processing and record management.


package com.mycompany.motorphpayroll;

// Manually splits a text/data line into separate string values using comma as separator.
// Specifically designed to ignore commas that are inside quotation marks, 
// ensuring correct separation of CSV/formatted data.
//Used for reading and processing employee and attendance records.


public class Utility {

    public static String[] manualSplit(String line) {

        String[] arr = new String[19];
        String temp = "";
        int i = 0;
        boolean q = false;

        for (char c : line.toCharArray()) {

            if (c == '"') q = !q;

            else if (c == ',' && !q) {
                arr[i++] = temp;
                temp = "";
            } else {
                temp += c;
            }
        }

        arr[i] = temp;
        return arr;
    }

// The computeHours method calculates the total number of hours worked by an employee using their Time In and Time Out. 
//It separates the hour and minute values from the time format and converts them into decimal numbers for easier computation. 
//The program then subtracts the start time from the end time and deducts one hour for the employee’s lunch break. 
//The Math.max(0, …) function prevents negative values from being returned if incorrect attendance data is entered. 
//Overall, the method automates payroll hour computation to make attendance and salary calculations faster and more accurate.
 
    public static double[] computeHours(String in, String out) {

        String[] i = in.split(":");
        String[] o = out.split(":");

        double start = Integer.parseInt(i[0]) + Integer.parseInt(i[1]) / 60.0;
        double end = Integer.parseInt(o[0]) + Integer.parseInt(o[1]) / 60.0;

        return new double[]{Math.max(0, end - start - 1)};
    }

// Computes SSS contribution based on salary bracket
    public static double getSSS(double salary) {
        if (salary <= 3250) return 135;
        if (salary <= 3750) return 157.5;
        if (salary <= 4250) return 180;
        return 1125;
    }

 //Computes withholding tax based on taxable income
     public static double getTax(double income) {
        if (income <= 20832) return 0;
        if (income <= 33333) return (income - 20833) * 0.2;
        return 2500 + (income - 33333) * 0.25;
    }
}