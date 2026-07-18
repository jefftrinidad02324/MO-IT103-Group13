# MotorPH Payroll System

## MO-IT103 -- Computer Programming 2

### Terminal Assessment Project

------------------------------------------------------------------------

# Group Information

**Course:** MO-IT103 -- Computer Programming 2\
**Project:** MotorPH Payroll System\
**Programming Language:** Java\
**Development Environment:** Apache NetBeans IDE




------------------------------------------------------------------------

# Team Members

  Jeff Trinidad         
  Aileen Rodriguez        
  Jenise Marienne Burce  
  Lovely Rose Yu    
  Patricia Kaye Red      

------------------------------------------------------------------------
# User Guide

The MotorPH Payroll System was designed to be simple and easy to use,
even for users without an information technology background. This
section provides step-by-step instructions for operating each feature of
the application.

------------------------------------------------------------------------

## Starting the Application

1.  Open the project in **Apache NetBeans IDE**.
2.  Verify that the following files are located in:

``` text
src/main/java/com/mycompany/motorphpayroll/
```

-   Employee Details.csv
-   Employee Attendance Record.csv

3.  Run **MotorPHPayroll.java**.
4.  Wait for the Login window to appear.

------------------------------------------------------------------------

# Employee User Guide

Employees can only access their own records and payroll information.

## Logging In

1.  Enter your **Employee Number**.
2.  Enter your password.
3.  Click **Login**.

If the credentials are incorrect, a friendly message will appear
explaining the error.

------------------------------------------------------------------------

## Viewing Personal Information

After logging in:

1.  Click **View My Information**.
2.  The application displays:
    -   Employee Number
    -   Full Name
    -   Birthday
    -   Position
    -   Employment Status
    -   Supervisor
    -   Basic Salary
    -   Hourly Rate
    -   Government Identification Numbers

------------------------------------------------------------------------

## Viewing Your Payslip

1.  Click **View My Payslip**.
2.  The system automatically calculates:
    -   Hours Worked
    -   Gross Salary
    -   First Cutoff Salary
    -   Second Cutoff Salary
    -   SSS Contribution
    -   PhilHealth Contribution
    -   Pag-IBIG Contribution
    -   Withholding Tax
    -   Total Deductions
    -   Monthly Net Salary

The payroll report is displayed directly on the screen.

------------------------------------------------------------------------

## Logging Out

Click **Back to Login** to return to the login page or **Exit Program**
to close the application.

------------------------------------------------------------------------

# Payroll Staff User Guide

Payroll staff members have access to payroll processing and employee
management functions.

## Logging In

Username:

``` text
payroll_staff
```

Password:

``` text
12345
```

Click **Login**.

------------------------------------------------------------------------

## Processing Payroll for One Employee

1.  Enter the employee number.
2.  Click **Process One Employee**.
3.  Review the generated payroll report.

------------------------------------------------------------------------

## Processing Payroll for All Employees

1.  Click **Process All Employees**.
2.  Wait while the application reads all attendance records.
3.  Payroll reports for every employee are generated automatically.

------------------------------------------------------------------------

## Generating the Payroll Summary

Click **Generate Payroll Summary** to display: - Total employees
processed - Total gross salary - Total deductions - Total net salary

------------------------------------------------------------------------

## Managing Employee Records

Open **Manage Employee Records**.

### Adding an Employee

1.  Click **Add Employee**.
2.  Complete all required fields.
3.  Click **Save**.
4.  The application validates the information and assigns the next
    available employee number automatically.

### Updating an Employee

1.  Search for an employee.
2.  Modify the necessary information.
3.  Click **Update**.

### Deleting an Employee

1.  Search for the employee.
2.  Click **Delete**.
3.  Confirm the deletion.

------------------------------------------------------------------------

# Validation Messages

The application automatically checks the information entered by the user
before saving it.

Examples include: - Invalid employee number - Duplicate employee
number - Missing required information - Incorrect government
identification numbers - Invalid phone number - Invalid birthday
format - Invalid salary values

Whenever an error is detected, the system displays a clear message
explaining how to correct the problem.

------------------------------------------------------------------------

# Troubleshooting

### Employee cannot log in

-   Verify the employee number.
-   Verify the password.
-   Ensure the employee exists in the Employee Details.csv file.

### Payroll report is empty

-   Verify attendance records exist.
-   Check that the attendance CSV file has not been modified
    incorrectly.

### Employee records do not appear

-   Confirm the CSV files are in the correct project folder.
-   Restart the application after updating the CSV files.


# Test Case Document from Group 24

https://docs.google.com/spreadsheets/d/1CxdpepFMvJeOUwAi0cxdSNAqnbC4F97LxRmvxwaa_CM/edit?usp=sharing
------------------------------------------------------------------------

# Conclusion

The **MotorPH Payroll System** is a comprehensive desktop payroll
management application developed as the final project for **MO-IT103 --
Computer Programming 2**. The system demonstrates how procedural
programming can be applied to solve real-world payroll management
challenges through a reliable, modular, and easy-to-use desktop
application.

The application automates employee record management, attendance
processing, payroll computation, statutory deductions, and payroll
reporting using CSV files as its data source. By replacing manual
payroll computation with an automated process, the system improves
accuracy, consistency, and efficiency while reducing the possibility of
human error.

The graphical user interface was designed to be simple enough for users
with little or no technical background. Employees can securely access
their personal information and payslips, while payroll staff can process
payroll, generate payroll summaries, and manage employee records using
clearly labeled menus and guided workflows. Validation messages and
prompts help users understand errors and complete each task correctly
without requiring programming knowledge.

From a software development perspective, the project demonstrates the
practical application of modular procedural programming, Java Swing
interface development, file handling, input validation, payroll
computation, and reusable utility functions. Each module has a dedicated
responsibility, making the application easier to maintain, understand,
test, and extend.

Although this version uses CSV files instead of a relational database,
the overall architecture provides an excellent foundation for future
enhancements such as MySQL integration, cloud-based storage, PDF payslip
generation, advanced reporting, multi-user authentication, role-based
access control, audit trails, backup and recovery, and dashboard
analytics.

Overall, the MotorPH Payroll System successfully achieves the objectives
of the MO-IT103 Terminal Assessment by delivering a practical,
maintainable, and user-friendly payroll solution. It demonstrates
professional software development practices while providing a useful
business application that can be operated confidently by both employees
and payroll administrators.
