package com.mycompany.motorphpayroll;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Procedural reusable helper functions for CSV, validation, time, and payroll. */
public class Utility {

    public static final double START_WORK = 8.0;
    public static final double END_WORK = 17.0;

    public static String[] manualSplit(String line) {
        String[] values = new String[19];
        StringBuilder current = new StringBuilder();
        int index = 0;
        boolean insideQuotes = false;

        if (line == null) {
            fillMissing(values);
            return values;
        }

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (insideQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (ch == ',' && !insideQuotes) {
                if (index < values.length) {
                    values[index++] = current.toString();
                }
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        if (index < values.length) {
            values[index] = current.toString();
        }
        fillMissing(values);
        return values;
    }

    private static void fillMissing(String[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                values[i] = "";
            }
        }
    }

    public static double[] computeHours(String timeIn, String timeOut) {
        if (!isValidTime(timeIn) || !isValidTime(timeOut)) {
            return new double[]{0, 0};
        }

        String[] inParts = timeIn.trim().split(":");
        String[] outParts = timeOut.trim().split(":");
        int inHour = Integer.parseInt(inParts[0]);
        int inMinute = Integer.parseInt(inParts[1]);
        int outHour = Integer.parseInt(outParts[0]);
        int outMinute = Integer.parseInt(outParts[1]);

        double start = inHour + inMinute / 60.0;
        double end = outHour + outMinute / 60.0;
        double lateDeduction = (inHour > 8 || (inHour == 8 && inMinute >= 11)) ? 0.5 : 0;

        start = Math.max(START_WORK, start);
        end = Math.min(END_WORK, end);
        double workedHours = Math.max(0, end - start - 1);
        return new double[]{workedHours, lateDeduction};
    }

    public static boolean isValidTime(String value) {
        if (value == null || !value.trim().matches("(?:[0-9]|1[0-9]|2[0-3]):[0-5]\\d")) {
            return false;
        }
        return true;
    }

    public static boolean isValidDate(String value) {
        if (value == null || !value.trim().matches("\\d{2}/\\d{2}/\\d{4}")) {
            return false;
        }
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        format.setLenient(false);
        try {
            Date date = format.parse(value.trim());
            return date.before(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean isValidPhone(String value) {
        return value != null && value.trim().matches("\\+?\\d{10,13}");
    }

    public static boolean isValidSSS(String value) {
        return value != null && value.trim().matches("\\d{2}-\\d{7}-\\d");
    }

    public static boolean isValidPhilHealth(String value) {
        return value != null && value.trim().matches("\\d{2}-\\d{9}-\\d");
    }

    public static boolean isValidTIN(String value) {
        return value != null && value.trim().matches("\\d{3}-\\d{3}-\\d{3}(?:-\\d{3})?");
    }

    public static boolean isValidPagIbig(String value) {
        return value != null && value.trim().matches("\\d{4}-\\d{4}-\\d{4}");
    }

    public static double parseDoubleSafe(String value) {
        try {
            if (value == null) {
                return 0;
            }
            return Double.parseDouble(value.replace(",", "").replace("\"", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

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

    public static boolean isNonNegativeNumber(String value) {
        return isNumeric(value) && parseDoubleSafe(value) >= 0;
    }

    public static String getMonthName(int month) {
        String[] months = {"", "January", "February", "March", "April", "May",
                "June", "July", "August", "September", "October", "November", "December"};
        return month >= 1 && month <= 12 ? months[month] : "";
    }

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
        return 1125;
    }

    public static double getTax(double income) {
        if (income <= 20833) return 0;
        if (income <= 33333) return (income - 20833) * 0.20;
        if (income <= 66667) return 2500 + (income - 33333) * 0.25;
        if (income <= 166667) return 10833.33 + (income - 66667) * 0.30;
        if (income <= 666667) return 40833.33 + (income - 166667) * 0.32;
        return 200833.33 + (income - 666667) * 0.35;
    }

    public static String formatNumber(double value) {
        return String.valueOf(value);
    }
}
