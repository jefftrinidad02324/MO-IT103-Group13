package com.mycompany.motorphpayroll;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/** 
 * This is a collection of helper tools used across the payroll system.
 * It handles reading files, verifying correct data entry, calculating time, 
 * and figuring out payroll deductions like taxes and social security.
 */
public class Utility {

    // Defines standard working hours: Starts at 8:00 AM (8.0) and ends at 5:00 PM (17.0)
    public static final double START_WORK = 8.0;
    public static final double END_WORK = 17.0;

    /**
     * Splits a single line of text from a CSV spreadsheet file into 19 individual pieces of data.
     * It is smart enough to ignore commas that are inside quotation marks (like in a street address).
     */
    public static String[] manualSplit(String line) {
        String[] values = new String[19];
        StringBuilder current = new StringBuilder();
        int index = 0;
        boolean insideQuotes = false;

        // If the line is completely empty, return 19 blank items
        if (line == null) {
            fillMissing(values);
            return values;
        }

        // Looks at the line letter by letter to break it apart properly
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                // If it sees double quotes, it keeps track of whether it is inside or outside a text block
                if (insideQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (ch == ',' && !insideQuotes) {
                // Splits the data whenever it hits a comma, as long as that comma isn't inside quotes
                if (index < values.length) {
                    values[index++] = current.toString();
                }
                current.setLength(0);
            } else {
                // Adds the current letter/number to the current piece of data
                current.append(ch);
            }
        }
        if (index < values.length) {
            values[index] = current.toString();
        }
        fillMissing(values);
        return values;
    }

    /**
     * Ensures there are no completely empty spaces in our data array. 
     * If a space is completely blank, it fills it with a safe, empty text string ("").
     */
    private static void fillMissing(String[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                values[i] = "";
            }
        }
    }

    /**
     * Calculates two things: Total hours worked and any penalty points for being late.
     * It limits working hours between 8:00 AM and 5:00 PM and automatically deducts a 1-hour lunch break.
     */
    public static double[] computeHours(String timeIn, String timeOut) {
        // If the time format is written incorrectly, return 0 hours worked and 0 late penalty
        if (!isValidTime(timeIn) || !isValidTime(timeOut)) {
            return new double[]{0, 0};
        }

        // Breaks down time (e.g., "08:30") into numbers: Hour (8) and Minute (30)
        String[] inParts = timeIn.trim().split(":");
        String[] outParts = timeOut.trim().split(":");
        int inHour = Integer.parseInt(inParts[0]);
        int inMinute = Integer.parseInt(inParts[1]);
        int outHour = Integer.parseInt(outParts[0]);
        int outMinute = Integer.parseInt(outParts[1]);

        // Converts hours and minutes into a decimal format (e.g., 8:30 becomes 8.5)
        double start = inHour + inMinute / 60.0;
        double end = outHour + outMinute / 60.0;
        
        // Grace period rule: If an employee clocks in at or after 8:11 AM, they get a 0.5 late deduction
        double lateDeduction = (inHour > 8 || (inHour == 8 && inMinute >= 11)) ? 0.5 : 0;

        // Adjusts time to fit official shift boundaries (ignores early arrivals or staying past 5:00 PM)
        start = Math.max(START_WORK, start);
        end = Math.min(END_WORK, end);
        
        // Subtracts start time from end time, then subtracts 1 hour for the unpaid lunch break
        double workedHours = Math.max(0, end - start - 1);
        return new double[]{workedHours, lateDeduction};
    }

    /**
     * Checks if a written time is valid and follows a 24-hour clock structure (like 00:00 to 23:59).
     */
    public static boolean isValidTime(String value) {
        if (value == null || !value.trim().matches("(?:[0-9]|1[0-9]|2[0-3]):[0-5]\\d")) {
            return false;
        }
        return true;
    }

    /**
     * Checks if a calendar date is real, written as Month/Day/Year (MM/DD/YYYY), 
     * and makes sure the date is not set in the future.
     */
    public static boolean isValidDate(String value) {
        if (value == null || !value.trim().matches("\\d{2}/\\d{2}/\\d{4}")) {
            return false;
        }
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        format.setLenient(false); // Prevents fake dates like 02/31/2026
        try {
            Date date = format.parse(value.trim());
            return date.before(new Date()); // Returns true only if the date has already passed
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Confirms that a phone number contains only numbers and is between 10 to 13 digits long.
     */
    public static boolean isValidPhone(String value) {
        return value != null && value.trim().matches("\\+?\\d{10,13}");
    }

    /**
     * Confirms a valid SSS Social Security number layout (Example: 00-0000000-0).
     */
    public static boolean isValidSSS(String value) {
        return value != null && value.trim().matches("\\d{2}-\\d{7}-\\d");
    }

    /**
     * Confirms a valid PhilHealth insurance number layout (Example: 00-000000000-0).
     */
    public static boolean isValidPhilHealth(String value) {
        return value != null && value.trim().matches("\\d{2}-\\d{9}-\\d");
    }

    /**
     * Confirms a valid Tax Identification Number (TIN) layout (Example: 000-000-000 or 000-000-000-000).
     */
    public static boolean isValidTIN(String value) {
        return value != null && value.trim().matches("\\d{3}-\\d{3}-\\d{3}(?:-\\d{3})?");
    }

    /**
     * Confirms a valid Pag-IBIG HDMF fund number layout (Example: 0000-0000-0000).
     */
    public static boolean isValidPagIbig(String value) {
        return value != null && value.trim().matches("\\d{4}-\\d{4}-\\d{4}");
    }

    /**
     * Safely converts text numbers (like "2,500.50") into actual mathematical numbers. 
     * It cleans up text by stripping out commas or quotes. If the text is broken, it returns 0 instead of crashing.
     */
    public static double parseDoubleSafe(String value) {
        try {
            if (value == null) {
                return 0;
            }
            return Double.parseDouble(value.replace(",", "").replace("\"", "").trim());
        } catch (NumberFormatException e) {
            return 0; // Safe fallback if conversion fails
        }
    }

    /**
     * Verifies if a given piece of text is a clean number that can be used in calculations.
     */
    public static boolean isNumeric(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(value.replace(",", "").replace("\"", "").trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Verifies if a given text is a number AND is zero or positive (no negative values allowed).
     */
    public static boolean isNonNegativeNumber(String value) {
        return isNumeric(value) && parseDoubleSafe(value) >= 0;
    }

    /**
     * Converts a month's number (1 through 12) into its real text name (1 becomes "January").
     */
    public static String getMonthName(int month) {
        String[] months = {"", "January", "February", "March", "April", "May",
                "June", "July", "August", "September", "October", "November", "December"};
        return month >= 1 && month <= 12 ? months[month] : "";
    }

    /**
     * Looks up the exact monthly SSS contribution fee based on an employee's salary range bracket.
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
        return 1125; // Maximum cap amount for salaries above 24,750
    }

    /**
     * Calculates the withholding tax amount based on current graduated tax income brackets.
     */
    public static double getTax(double income) {
        if (income <= 20833) return 0; // No tax for income under 20,833
        if (income <= 33333) return (income - 20833) * 0.20;
        if (income <= 66667) return 2500 + (income - 33333) * 0.25;
        if (income <= 166667) return 10833.33 + (income - 66667) * 0.30;
        if (income <= 666667) return 40833.33 + (income - 166667) * 0.32;
        return 200833.33 + (income - 666667) * 0.35;
    }

    /**
     * Converts a mathematical decimal number into normal text format so it can be cleanly displayed.
     */
    public static String formatNumber(double value) {
        return String.valueOf(value);
    }
}