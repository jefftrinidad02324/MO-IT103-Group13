package com.mycompany.motorphpayroll;

/**
 * This is a Utility helper class designed for a payroll system.
 * It contains standard tools to handle text data, calculate working hours,
 * look up months, and figure out government deductions like SSS and taxes.
 */
public class Utility {

    // Defines the standard company working hours: 8:00 AM (8.0) to 5:00 PM (17.0).
    public static final double START_WORK = 8.0;
    public static final double END_WORK = 17.0;

    /**
     * Splits a row of text (like a line from a CSV spreadsheet) into 19 separate pieces of data.
     * It safely ignores commas if they are hidden inside quotation marks (e.g., "Smith, John").
     */
    public static String[] manualSplit(String line) {
        String[] values = new String[19];
        String current = "";
        int index = 0;
        boolean insideQuotes = false;

        // If there is no line to read, fill all 19 positions with empty text and stop.
        if (line == null) {
            for (int i = 0; i < values.length; i++) {
                values[i] = "";
            }
            return values;
        }

        // Read the line character by character to split it by commas.
        for (int i = 0; i < line.length(); i++) {
            char currentChar = line.charAt(i);

            if (currentChar == '"') {
                // If we see a quotation mark, toggle whether we are inside or outside quotes.
                insideQuotes = !insideQuotes;
            } else if (currentChar == ',' && !insideQuotes) {
                // If we hit a comma and we are NOT inside quotes, save the word we built and move to the next slot.
                if (index < values.length) {
                    values[index] = current;
                    index++;
                }
                current = ""; // Reset the temporary word builder
            } else {
                // Otherwise, keep adding characters to the current word.
                current += currentChar;
            }
        }

        // Save the very last word of the line into the remaining slot.
        if (index < values.length) {
            values[index] = current;
        }

        // Replace any missing/null slots with completely empty text to avoid system errors.
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                values[i] = "";
            }
        }

        return values;
    }

    /**
     * Calculates the actual number of hours worked and determines if there is a deduction for being late.
     * Takes time inputs in a 24-hour format (e.g., "08:30" and "17:00").
     */
    public static double[] computeHours(String timeIn, String timeOut) {
        // Separate the hours and minutes using the colon character.
        String[] inParts = timeIn.split(":");
        String[] outParts = timeOut.split(":");

        // Convert the times into decimal numbers (e.g., 8:30 becomes 8.5).
        double start = Integer.parseInt(inParts[0]) + Integer.parseInt(inParts[1]) / 60.0;
        double end = Integer.parseInt(outParts[0]) + Integer.parseInt(outParts[1]) / 60.0;

        int hour = Integer.parseInt(inParts[0]);
        int minute = Integer.parseInt(inParts[1]);

        // If an employee arrives after 8:00 AM, or specifically at 8:11 AM or later, apply a 0.5-hour late deduction.
        double lateDeduction = 0;
        if (hour > 8 || (hour == 8 && minute >= 11)) {
            lateDeduction = 0.5;
        }

        // Ensure calculations don't start before official hours (8 AM) or end after official hours (5 PM).
        start = Math.max(START_WORK, start);
        end = Math.min(END_WORK, end);

        // Calculate hours worked by subtracting start from end time, minus a 1-hour unpaid lunch break.
        double workedHours = Math.max(0, end - start - 1);
        // Apply the late arrival penalty, if any.
        workedHours = Math.max(0, workedHours - lateDeduction);

        // Returns two answers: [Hours Worked, Late Deduction Amount]
        return new double[]{workedHours, lateDeduction};
    }

    /**
     * Safely converts text into a decimal number.
     * It clears out formatting symbols like commas and quotes, and returns 0 if the text isn't a valid number.
     */
    public static double parseDoubleSafe(String value) {
        try {
            if (value == null) {
                return 0;
            }
            // Strip out commas and quotation marks, clean up spaces, and convert to a decimal number.
            return Double.parseDouble(value.replace(",", "").replace("\"", "").trim());
        } catch (Exception e) {
            // If anything goes wrong (like trying to convert "hello" to a number), safely return 0.
            return 0;
        }
    }

    /**
     * Checks if a piece of text can be successfully converted into a valid number.
     * Returns true if it is a number, and false if it is not.
     */
    public static boolean isNumeric(String value) {
        try {
            // Attempt to clean and convert the text to a decimal number.
            Double.parseDouble(value.replace(",", "").replace("\"", "").trim());
            return true; // Conversion succeeded
        } catch (Exception e) {
            return false; // Conversion failed
        }
    }

    /**
     * Converts a month's number (1 through 12) into its actual written word name.
     */
    public static String getMonthName(int month) {
        switch (month) {
            case 1: return "January";
            case 2: return "February";
            case 3: return "March";
            case 4: return "April";
            case 5: return "May";
            case 6: return "June";
            case 7: return "July";
            case 8: return "August";
            case 9: return "September";
            case 10: return "October";
            case 11: return "November";
            case 12: return "December";
            default: return ""; // Returns nothing if the number is invalid (e.g., 15)
        }
    }

    /**
     * Looks up the standard Philippine SSS (Social Security System) contribution bracket
     * based on the employee's monthly gross salary.
     */
    public static double getSSS(double salary) {
        if (salary <= 3250) return 135;
        if (salary <= 3750) return 157.5;
        if (salary <= 4250) return 180;
        if (salary <= 4750) return 202.5;
        if (salary <= 5250) return 225;
        if (salary <= 5750) return 247.5;
        if (salary <= 6250) return 270;
        if (salary <= 6750) return 292.5;
        if (salary <= 7250) return 315;
        if (salary <= 7750) return 337.5;
        if (salary <= 8250) return 360;
        if (salary <= 8750) return 382.5;
        if (salary <= 9250) return 405;
        if (salary <= 9750) return 427.5;
        if (salary <= 10250) return 450;
        if (salary <= 10750) return 472.5;
        if (salary <= 11250) return 495;
        if (salary <= 11750) return 517.5;
        if (salary <= 12250) return 540;
        if (salary <= 12750) return 562.5;
        if (salary <= 13250) return 585;
        if (salary <= 13750) return 607.5;
        if (salary <= 14250) return 630;
        if (salary <= 14750) return 652.5;
        if (salary <= 15250) return 675;
        if (salary <= 15750) return 697.5;
        if (salary <= 16250) return 720;
        if (salary <= 16750) return 742.5;
        if (salary <= 17250) return 765;
        if (salary <= 17750) return 787.5;
        if (salary <= 18250) return 810;
        if (salary <= 18750) return 832.5;
        if (salary <= 19250) return 855;
        if (salary <= 19750) return 877.5;
        if (salary <= 20250) return 900;
        if (salary <= 20750) return 922.5;
        if (salary <= 21250) return 945;
        if (salary <= 21750) return 967.5;
        if (salary <= 22250) return 990;
        if (salary <= 22750) return 1012.5;
        if (salary <= 23250) return 1035;
        if (salary <= 23750) return 1057.5;
        if (salary <= 24250) return 1080;
        if (salary <= 24750) return 1102.5;
        return 1125; // Maximum SSS contribution amount for salaries above 24,750
    }

    /**
     * Calculates the progressive withholding tax deduction based on the employee's taxable income bracket
     * (Following the Philippine TRAIN Law tax table format).
     */
    public static double getTax(double income) {
        if (income <= 20833) return 0; // Income below 20,833 is tax-exempt
        if (income <= 33333) return (income - 20833) * 0.20;
        if (income <= 66667) return 2500 + (income - 33333) * 0.25;
        if (income <= 166667) return 10833.33 + (income - 66667) * 0.30;
        if (income <= 666667) return 40833.33 + (income - 166667) * 0.32;
        return 200833.33 + (income - 666667) * 0.35; // Maximum tax rate bracket
    }

    /**
     * Converts a decimal number into plain text format so it can be safely displayed on screen.
     */
    public static String formatNumber(double value) {
        return String.valueOf(value);
    }
}