package com.apcsa.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import com.apcsa.data.PowerSchool;
import com.apcsa.model.Student;
import com.apcsa.model.Teacher;
import com.apcsa.model.User;

public class Application {

    private Scanner in;
    private User activeUser;
    
    public static boolean running;
    
    
    public static final int RTCHANGEPWD = 1;    // ROOT - reset user password
    public static final int RTRESETDB = 2;      // ROOT - factory reset database
    public static final int RTLOGOUT = 3;       // ROOT - logout
    public static final int RTSHUTDOWN = 4;     // ROOT - shut down
    public static final int ADBYFAC = 1;        // ADMIN - view faculty
    public static final int ADBYDEP = 2;        // ADMIN - view by department (#22)
    public static final int ADBYENROLL = 3;     // ADMIN - view student enrollment
    public static final int ADBYGRADE = 4;      // ADMIN - view by grade
    public static final int ADBYCOURSE = 5;     // ADMIN - view by course
    public static final int ADCHANGEPWD = 6;    // ADMIN - change password
    public static final int ADLOGOUT = 7;       // ADMIN - logout
    public static final int TCBYCOURSE = 1;     // TEACHER - view enrollment by course
    public static final int TCNEWASGN = 2;      // TEACHER - add assignment
    public static final int TCDLTASGN = 3;      // TEACHER - delete assignment
    public static final int TCNEWGRD = 4;       // TEACHER - enter grade
    public static final int TCCHANGEPWD = 5;    // TEACHER - change password
    public static final int TCLOGOUT = 6;       // TEACHER - logout
    public static final int STVIEWGRD = 1;      // STUDENT - view course grades
    public static final int STBYCOURSE = 2;     // STUDENT - view assignment grades by course
    public static final int STCHANGEPWD = 3;    // STUDENT - change password
    public static final int STLOGOUT = 4;       // STUDENT - logout
    /**
     * Creates an instance of the Application class, which is responsible for interacting
     * with the user via the command line interface.
     */
    enum RootAction { PASSWORD, DATABASE, LOGOUT, SHUTDOWN }
    
    enum AdministratorAction { FACULTY, DEPARTMENT, STUDENTS, GRADE, COURSE, PASSWORD, LOGOUT }
    
    private void showAdministratorUI() {
        while (activeUser != null) {
            switch (getAdministratorMenuSelection()) {
                case FACULTY: viewFaculty(); break;
                case DEPARTMENT: viewFacultyByDepartment(); break;
                case STUDENTS: viewStudents(); break;
                case GRADE: viewStudentsByGrade(); break;
                case COURSE: viewStudentsByCourse(); break;
                case PASSWORD: changePassword(false); break;
                case LOGOUT: logout(); break;
                default: System.out.println("\nInvalid selection."); break;
            }
        }
    }
    
    private AdministratorAction getAdministratorMenuSelection() {
        System.out.println();
        
        System.out.println("[1] View faculty.");
        System.out.println("[2] View faculty by department.");
        System.out.println("[3] View student enrollment.");
        System.out.println("[4] View student enrollment by grade.");
        System.out.println("[5] View student enrollment by course.");
        System.out.println("[6] Change password.");
        System.out.println("[7] Logout.");
        System.out.print("\n::: ");

        switch (Utils.getInt(in, -1)) {
            case 1: return AdministratorAction.FACULTY;
            case 2: return AdministratorAction.DEPARTMENT;
            case 3: return AdministratorAction.STUDENTS;
            case 4: return AdministratorAction.GRADE;
            case 5: return AdministratorAction.COURSE;
            case 6: return AdministratorAction.PASSWORD;
            case 7: return AdministratorAction.LOGOUT;
        }
        
        return null;
    }
    
    private void viewFaculty() {        
        ArrayList<Teacher> teachers = PowerSchool.getTeachers();
        
        if (teachers.isEmpty()) {
            System.out.println("\nNo teachers to display.");
        } else {
            System.out.println();
            
            int i = 1;
            for (Teacher teacher : teachers) {
                System.out.println(i++ + ". " + teacher.getName() + " / " + teacher.getDepartmentName());
            } 
        }
    }
    
    private void viewFacultyByDepartment() {
        //
        // get a list of teachers by department (this requires a database call)
        //      to do this, you'll need to prompt the user to choose a department (more on this later)
        //
        // if list of teachers is empty...
        //      print a message saying exactly that
        // otherwise...
        //      print the list of teachers by name an department (just like last time)
        //
    }
    
    private void viewStudents() {
        //
        // get a list of students
        //
        // if list of students is empty...
        //      print a message saying exactly that
        // otherwise...
        //      print the list of students by name and graduation year
        //
    }
    
    private void viewStudentsByGrade() {
        //
        // get list of students by grade
        //      to do this, you'll need to prompt the user to choose a grade level (more on this later)
        //
        // if the list of students is empty...
        //      print a message saying exactly that
        // otherwise...
        //      print the list of students by name and class rank
        //
    }
    
    private void viewStudentsByCourse() {
        //
        // get a list of students by course
        //      to do this, you'll need to prompt the user to choose a course (more on this later)
        //
        // if the list of students is empty...
        //      print a message saying exactly that
        // otherwise...
        //      print the list of students by name and grade point average
        //
    }
    
    private void changePassword(boolean firstLogin) {
        // if it isn't the user's first login...
        //      ask the user for his or her current password
        //
        // ask all users (first login or not) to enter a new password
        //
        // change the password (this will require a call to the database)
        //      this requires three pieces of information: the username, the old password, and the new password
        //      the old password will either be something the use entered (if it isn't his or her first login) or
        //      it'll be the same as their username
    }
    
    private int getDepartmentSelection() {
        int selection = -1;
        System.out.println("\nChoose a department.");
        
        while (selection < 1 || selection > 6) {
            System.out.println("\n[1] Computer Science.");
            System.out.println("[2] English.");
            System.out.println("[3] History.");
            System.out.println("[4] Mathematics.");
            System.out.println("[5] Physical Education.");
            System.out.println("[6] Science.");
            System.out.print("\n::: ");
            
            selection = Utils.getInt(in, -1);
        }
        
        return selection;
    }
    
    private int getGradeSelection() {
        int selection = -1;
        System.out.println("\nChoose a grade level.");
        
        while (selection < 1 || selection > 4) {
            System.out.println("\n[1] Freshman.");
            System.out.println("[2] Sophomore.");
            System.out.println("[3] Junior.");
            System.out.println("[4] Senior.");
            System.out.print("\n::: ");
            
            selection = Utils.getInt(in, -1);
        }
        
        return selection + 8;   // +8 because you want a value between 9 and 12
    }
    
    private String getCourseSelection() throws SQLException {
        boolean valid = false;
        String courseNo = null;

        while (!valid) {
            System.out.print("\nCourse No.: ");
            courseNo = in.next();

            if (isValidCourse(courseNo)) {
                valid = true;
            } else {
                System.out.println("\nCourse not found.");
            }
        }

        return courseNo;
    }
    
    public void createAndShowUI() {
        System.out.println("\nHello, again, " + activeUser.getFirstName() + "!");
        
        if (activeUser.isRoot()) {
            showRootUI();
        } else if (activeUser.isAdministrator()) {
            showAdministratorUI();
        } else {
            // TODO - add cases for teacher, student, and unknown
        }
    }
    
    public Application() {
        this.in = new Scanner(System.in);

        try {
            PowerSchool.initialize(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the PowerSchool application.
     */

    public void startup() {
        System.out.println("PowerSchool -- now for students, teachers, and school administrators!");

        // continuously prompt for login credentials and attempt to login

        while (true) {
            System.out.print("\nUsername: ");
            String username = in.next();

            System.out.print("Password: ");
            String password = in.next();

            // if login is successful, update generic user to administrator, teacher, or student

            if (login(username, password)) {
                activeUser = activeUser.isAdministrator()
                    ? PowerSchool.getAdministrator(activeUser) : activeUser.isTeacher()
                    ? PowerSchool.getTeacher(activeUser) : activeUser.isStudent()
                    ? PowerSchool.getStudent(activeUser) : activeUser.isRoot()
                    ? activeUser : null;

                if (isFirstLogin() && !activeUser.isRoot()) {
                    // first-time users need to change their passwords from the default provided
                    System.out.print("\nEnter new password: ");
                    String newPassword = in.next();
                    PowerSchool.changePassword(username, newPassword);
                    System.out.println("\nSuccessfully changed password.");
                ////////////////////////////// ROOT ////////////////////////////
                } else if (activeUser.isRoot()) {
                    boolean validLogin = true;
                    System.out.println("\nHello, again, ROOT!");
                    while (validLogin) {
                        switch (getSelectionRoot()) {
                            case RTCHANGEPWD:
                                resetPassword();
                                break;
                            case RTRESETDB:
                                System.out.print("\nAre you sure you want to reset all settings and data? (y/n) ");
                                String resetDecision = in.next();
                                if (resetDecision.equals("y")) {
                                    PowerSchool.reset();
                                    System.out.println("\nSuccessfully reset database.");
                                }
                                break;
                            case RTSHUTDOWN:
                                rootShutdown();
                                break;
                            case RTLOGOUT:
                                validLogin = logoutConfirm();
                                in.nextLine();
                                break;
                            default:
                                System.out.print("\nInvalid selection.\n");
                                break;
                        }
                    }
                ////////////////////////////// ADMINISTRATOR ////////////////////////////
                } else if (activeUser.isAdministrator()) {
                    boolean validLogin = true;
                    String firstName = activeUser.getFirstName();
                    System.out.printf("\nHello, again, %s!\n", firstName);
                    while (validLogin) {
                        switch (getSelectionAdministrator()) {
                            case ADBYFAC:
                                viewFaculty();
                                break;
                            case ADBYDEP:
                                viewDepartments();
                                break;
                            case ADBYENROLL:
                                viewStudents();
                                break;
                            case ADBYGRADE:
                                viewStudentsByGrade();
                                break;
                            case ADBYCOURSE:
                                viewStudentsByCourse();
                                break;
                            case ADCHANGEPWD:
                                resetUserPassword();
                                break;
                            case ADLOGOUT:
                                validLogin = logoutConfirm();
                                in.nextLine();
                            break;
                            default:
                                System.out.print("\nInvalid selection.\n");
                                break;
                        }
                    }
                ////////////////////////////// TEACHER ////////////////////////////
                } else if (activeUser.isTeacher()) {
                    boolean validLogin = true;
                    String firstName = activeUser.getFirstName();
                    System.out.printf("\nHello, again, %s!\n", firstName);
                    while (validLogin) {
                        switch (getSelectionTeacher()) {
                            case TCBYCOURSE:
                                enrollmentByCourse();
                                break;
                            case TCNEWASGN:
                                addAssignment();
                                break;
                            case TCDLTASGN:
                                deleteAssignment();
                                break;
                            case TCNEWGRD:
                                enterGrade();
                                break;
                            case TCCHANGEPWD:
                                resetUserPassword();
                                break;
                            case TCLOGOUT:
                                validLogin = logoutConfirm();
                                in.nextLine();
                                break;
                            default:
                                System.out.print("\nInvalid selection.\n");
                                break;
                        }
                    }
                ////////////////////////////// STUDENT ////////////////////////////
                } else if (activeUser.isStudent()) {
                    boolean validLogin = true;
                    String firstName = activeUser.getFirstName();
                    System.out.printf("\nHello, again, %s!\n", firstName);
                    while (validLogin) {
                        switch (getSelectionStudent()) {
                            case STVIEWGRD:
                                ((Student) activeUser).viewCourseGrades();
                                break;
                            case STBYCOURSE:
                                ((Student) activeUser).viewAssignmentGradesByCourse(in);
                                break;
                            case STCHANGEPWD:
                                resetUserPassword();
                                break;
                            case STLOGOUT:
                                validLogin = logoutConfirm();
                                in.nextLine();
                                break;
                            default:
                                System.out.print("\nInvalid selection.\n");
                                break;
                        }
                    }
                }
            } else {
                System.out.println("\nInvalid username and/or password.");
            }
        }
    }

    
    private void showRootUI() {
        while (activeUser != null) {
            switch (getRootMenuSelection()) {
                case PASSWORD: resetPassword(); break;
                case DATABASE: factoryReset(); break;
                case LOGOUT: logout(); break;
                case SHUTDOWN: shutdown(); break;
                default: System.out.println("\nInvalid selection."); break;
            }
        }
    }
    
    private RootAction getRootMenuSelection() {
        System.out.println();
        
        System.out.println("[1] Reset user password.");
        System.out.println("[2] Factory reset database.");
        System.out.println("[3] Logout.");
        System.out.println("[4] Shutdown.");
        System.out.print("\n::: ");
        
        switch (Utils.getInt(in, -1)) {
            case 1: return RootAction.PASSWORD;
            case 2: return RootAction.DATABASE;
            case 3: return RootAction.LOGOUT;
            case 4: return RootAction.SHUTDOWN;
            default: return null;
        }
     }
    
    public boolean logoutConfirm() {
        System.out.print("\nAre you sure you want to logout? (y/n) ");
        String logoutDecision = in.next();
        if (logoutDecision.equals("y")) {
            return false;
        } else {
            return true;
        }
    }
    
    private void resetPassword() {
    	System.out.print("\nUsername: ");
        String username = in.next();
        if (Utils.confirm(in, "\nAre you sure you want to reset the password for " + username + "?  (y/n) ")) {
            if (in != null) {
                if (PowerSchool.resetPassword(username)) {
                    PowerSchool.resetLastLogin(username);
                    System.out.println("\nSuccessfully reset password for " + username + ".");
                } else {
                    System.out.println("\nPassword reset failed");
                }
            }
        }
    }
    
    public void resetUserPassword() {
        System.out.print("\nEnter current password: ");
        String oldPassword = in.next();
        System.out.print("Enter new password: ");
        String newPassword = in.next();
        if (Utils.getHash(oldPassword).equals(activeUser.getPassword())) {
            PowerSchool.changePassword(activeUser.getUsername(), Utils.getHash(newPassword));
            System.out.println("\nSuccessfully changed password.");
        } else if (!(oldPassword.equals(activeUser.getPassword()))) {
            System.out.println("\nInvalid current password.");
        }
    }
    
    private void viewFaculty() {
        ArrayList<Teacher> teachers = PowerSchool.getTeachers();

        if (teachers.isEmpty()) {
            System.out.println("\nNo teachers to display.");
        } else {
            System.out.println();

            int i = 1;
            for (Teacher teacher : teachers) {
                System.out.println(i++ + ". " + teacher.getName() + " / " + teacher.getDepartmentName());
            }
        }
    }
    
    private void viewDepartments() {
        ArrayList<Teacher> teachers = PowerSchool.getTeachersByDept(getDepartmentSelection());

        if (teachers.isEmpty()) {
            System.out.println("\nNo teachers to display.");
        } else {
            System.out.println();

            int i = 1;
            for (Teacher teacher : teachers) {
                System.out.println(i++ + ". " + teacher.getName() + " / " + teacher.getDepartmentName());
            }
        }

    }
    
    private int getDepartmentSelection() {
        int selection = -1;
        
        System.out.println("\nChoose a department.");

        while (selection < 1 || selection > 6) {
            System.out.println("\n[1] Computer Science.");
            System.out.println("[2] English.");
            System.out.println("[3] History.");
            System.out.println("[4] Mathematics.");
            System.out.println("[5] Physical Education.");
            System.out.println("[6] Science.");
            System.out.print("\n::: ");

            selection = Utils.getInt(in, -1);
        }

        return selection;
    }
    
    private void viewStudents() {
        ArrayList<Student> students = PowerSchool.getStudents();

        if (students.isEmpty()) {
            System.out.println("\nNo students to display.");
        } else {
            System.out.println();

            int i = 1;
            for (Student student : students) {
                System.out.println(i++ + ". " + student.getName() + " / " + student.getGraduationYear());
            }
        }
    }
    
    private void viewStudentsByGrade() {
        ArrayList<Student> students = PowerSchool.getStudentsByGrade(getGradeSelection());

        if (students.isEmpty()) {
            System.out.println("\nNo students to display.");
        } else {
            System.out.println();

            int i = 1;
            for (Student student : students) {
                System.out.println(i++ + ". " + student.getName() + " / #" + student.getClassRank());
            }
        }
    }

    private void viewStudentsByCourse() {
        String courseNo = "";
        try {
        courseNo = getCourseSelection();
        } catch(SQLException e) {

        }
        ArrayList<Student> students = PowerSchool.getStudentsByCourse(courseNo);

        if (students.isEmpty()) {
            System.out.println("\nNo students to display.");
        } else {
            System.out.println();

            int i = 1;
            for (Student student : students) {
                System.out.println(i++ + ". " + student.getName() + " / " + fixGPA(student));
            }
        }
    }
    
    private String fixGPA(Student student) {
        double GPA = student.getGpa();
        if (GPA == -1) {
            return "--";
        } else {
            return String.valueOf(GPA);
        }
    }

    private boolean isValidCourse(String courseId) {
        boolean validCourse = false;
        for (int i=1; i <  PowerSchool.getNumberOfCourses(); i++) {
            if (PowerSchool.getCourseNumber(i).equals(courseId)) {
                validCourse = true;
            }
        }
        return validCourse;
    }
    
    private int getGradeSelection() {
        int selection = -1;
        System.out.println("\nChoose a grade level.");

        while (selection < 1 || selection > 4) {
            System.out.println("\n[1] Freshman.");
            System.out.println("[2] Sophomore.");
            System.out.println("[3] Junior.");
            System.out.println("[4] Senior.");
            System.out.print("\n::: ");

            selection = Utils.getInt(in, -1);
        }

        return selection + 8;
    }
    
    private void factoryReset() {
        //
        // ask root user to confirm intent to reset the database
        //
        // if confirmed...
        //      call database initialize method with parameter of true
        //      print success message
        //
    }
    
    private void logout() {
        //
        // ask user to confirm intent to logout
        //
        // if confirmed...
        //      set activeUser to null
        //
    }
    
    /*
     * Shuts down the application after encountering an error.
     * 
     * @param e the error that initiated the shutdown sequence
     */

    private void shutdown(Exception e) {
        if (in != null) {
            in.close();
        }
        
        System.out.println("Encountered unrecoverable error. Shutting down...\n");
        System.out.println(e.getMessage());
                
        System.out.println("\nGoodbye!");
        System.exit(0);
    }

    /*
     * Releases all resources and kills the application.
     */

    private void rootShutdown() {        
        System.out.println();
            
        if (Utils.confirm(in, "Are you sure? (y/n) ")) {
            if (in != null) {
                in.close();
            }
            
            System.out.println("\nGoodbye!");
            System.exit(0);
        }
    }
    /**
     * Logs in with the provided credentials.
     *
     * @param username the username for the requested account
     * @param password the password for the requested account
     * @return true if the credentials were valid; false otherwise
     */

    public boolean login(String username, String password) {
        activeUser = PowerSchool.login(username, password);

        return activeUser != null;
    }

    /**
     * Determines whether or not the user has logged in before.
     *
     * @return true if the user has never logged in; false otherwise
     */

    public boolean isFirstLogin() {
        return activeUser.getLastLogin().equals("0000-00-00 00:00:00.000");
    }

    /////// MAIN METHOD ///////////////////////////////////////////////////////////////////

    /*
     * Starts the PowerSchool application.
     *
     * @param args unused command line argument list
     */

    public static void main(String[] args) {
        Application app = new Application();

        app.startup();
    }
}