package com.apcsa.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import com.apcsa.controller.Utils;
import com.apcsa.model.Administrator;
import com.apcsa.model.Student;
import com.apcsa.model.Teacher;
import com.apcsa.model.User;

public class PowerSchool {

    private final static String PROTOCOL = "jdbc:sqlite:";
    private final static String DATABASE_URL = "data/powerschool.db";

    /**
     * Initializes the database if needed (or if requested).
     *
     * @param force whether or not to force-reset the database
     * @throws Exception
     */

    public static void initialize(boolean force) {
        if (force) {
            reset();    // force reset
        } else {
            boolean required = false;

            // check if all tables have been created and loaded in database

            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(QueryUtils.SETUP_SQL)) {

                while (rs.next()) {
                    if (rs.getInt("names") != 9) {
                        required = true;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // build database if needed

            if (required) {
                reset();
            }
        }
    }

    /**
     * Retrieves the User object associated with the requested login.
     *
     * @param username the username of the requested User
     * @param password the password of the requested User
     * @return the User object for valid logins; null for invalid logins
     */

    public static User login(String username, String password) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(QueryUtils.LOGIN_SQL)) {

            stmt.setString(1, username);
            stmt.setString(2, Utils.getHash(password));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = new Timestamp(new Date().getTime());
                    int affected = PowerSchool.updateLastLogin(conn, username, ts);

                    if (affected != 1) {
                        System.err.println("Unable to update last login (affected rows: " + affected + ").");
                    }

                    return new User(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Returns the administrator account associated with the user.
     *
     * @param user the user
     * @return the administrator account if it exists
     */

    public static User getAdministrator(User user) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ADMIN_SQL)) {

            stmt.setInt(1, user.getUserId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Administrator(user, rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    /**
     * Returns the teacher account associated with the user.
     *
     * @param user the user
     * @return the teacher account if it exists
     */

    public static User getTeacher(User user) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_TEACHER_SQL)) {

            stmt.setInt(1, user.getUserId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Teacher(user, rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    /**
     * Returns the student account associated with the user.
     *
     * @param user the user
     * @return the student account if it exists
     */

    public static User getStudent(User user) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENT_SQL)) {

            stmt.setInt(1, user.getUserId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Student(user, rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    /*
     * Establishes a connection to the database.
     *
     * @return a database Connection object
     * @throws SQLException
     */

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(PROTOCOL + DATABASE_URL);
    }

    /*
     * Updates the last login time for the user.
     *
     * @param conn the current database connection
     * @param username the user's username
     * @param ts the current timestamp
     * @return the number of affected rows
     */

    private static int updateLastLogin(Connection conn, String username, Timestamp ts) {
        try (PreparedStatement stmt = conn.prepareStatement(QueryUtils.UPDATE_LAST_LOGIN_SQL)) {

            conn.setAutoCommit(false);
            stmt.setString(1, ts.toString());
            stmt.setString(2, username);

            if (stmt.executeUpdate() == 1) {
                conn.commit();

                return 1;
            } else {
                conn.rollback();

                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();

            return -1;
        }
    }
    
    private static int updateAuth(Connection conn, String username, String auth) {
        try (PreparedStatement stmt = conn.prepareStatement(QueryUtils.UPDATE_AUTH_SQL)) {

            conn.setAutoCommit(false);
            stmt.setString(1, auth);
            stmt.setString(2, username);

            if (stmt.executeUpdate() == 1) {
                conn.commit();

                return 1;
            } else {
                conn.rollback();

                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();

            return -1;
        }
    }

    /*
     * Builds the database. Executes a SQL script from a configuration file to
     * create the tables, setup the primary and foreign keys, and load sample data.
     */

    private static void reset() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             BufferedReader br = new BufferedReader(new FileReader(new File("config/setup.sql")))) {

            String line;
            StringBuffer sql = new StringBuffer();

            // read the configuration file line-by-line to get SQL commands

            while ((line = br.readLine()) != null) {
                sql.append(line);
            }

            // execute SQL commands one-by-one

            for (String command : sql.toString().split(";")) {
                if (!command.strip().isEmpty()) {
                    stmt.executeUpdate(command);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: Unable to load SQL configuration file.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error: Unable to open and/or read SQL configuration file.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error: Unable to execute SQL script from configuration file.");
            e.printStackTrace();
        }
    }
    
    public static boolean resetPassword(String username) {
    	try (Connection conn = getConnection();
                PreparedStatement statement = conn.prepareStatement(QueryUtils.UPDATE_AUTH_SQL)) {

    		conn.setAutoCommit(false);
            statement.setString(1, Utils.getHash(username));
            statement.setString(2, username);

            if (statement.executeUpdate() == 1) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
               return false;

            }
           } catch (SQLException e) {
               e.printStackTrace();
               return false;
           }
    }
    
    /**
     * Retrieves all faculty members.
     * 
     * @return a list of teachers
     */
     
     public static ArrayList<Teacher> getTeachers() {
        ArrayList<Teacher> teachers = new ArrayList<Teacher>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
                        
            try (ResultSet rs = stmt.executeQuery(QueryUtils.GET_ALL_TEACHERS_SQL)) {
                while (rs.next()) {
                    teachers.add(new Teacher(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return teachers;
     }
     
     /**
      * Returns an MD5 hash of the user's plaintext password.
      *
      * @param plaintext the password
      * @return an MD5 hash of the password
      */

     public static String getHash(String plaintext) {
    	    StringBuilder pwd = new StringBuilder();

    	    try {
    	        MessageDigest md = MessageDigest.getInstance("MD5");

    	        md.update(plaintext.getBytes());
    	        byte[] digest = md.digest(plaintext.getBytes());

    	        for (int i = 0; i < digest.length; i++) {
    	            pwd.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
    	        }
    	    } catch (NoSuchAlgorithmException e) {
    	        e.printStackTrace();
    	    }

    	    return pwd.toString();
    	}
     
     
     /**
      * Safely reads an integer from the user.
      * 
      * @param in the Scanner
      * @param invalid an invalid (but type-safe) default
      * @return the value entered by the user or the invalid default
      */
         
     public static int getInt(Scanner in, int invalid) {
         try {
             return in.nextInt();                // try to read and return user-provided value
         } catch (InputMismatchException e) {            
             return invalid;                     // return default in the even of an type mismatch
         } finally {
             in.nextLine();                      // always consume the dangling newline character
         }
     }
     
     /**
      * Confirms a user's intent to perform an action.
      * 
      * @param in the Scanner
      * @param message the confirmation prompt
      * @return true if the user confirms; false otherwise
      */

     public static boolean confirm(Scanner in, String message) {
         String response = "";
         
         // prompt user for explicit response of yes or no
         
         while (!response.equals("y") && !response.equals("n")) {
             System.out.print(message);
             response = in.next().toLowerCase();
         }
         
         return response.equals("y");
     }
     
     /**
      * Sorts the list of students by rank, using the index to update the underlying class rank.
      * 
      * @param students the list of students
      * @return the updated list of students
      */

     @SuppressWarnings({ "unchecked", "rawtypes" })
     public static ArrayList<Student> updateRanks(ArrayList<Student> students) {
         Collections.sort(students, new Comparator() {

             // compares each student based on gpa to aid sorting
             
             @Override
             public int compare(Object student1, Object student2) {
                 if (((Student) student1).getGpa() > ((Student) student2).getGpa()) {
                     return -1;
                 } else if (((Student) student1).getGpa() == ((Student) student2).getGpa()) {
                     return 0;
                 } else {
                     return 1;
                 }
             }
             
         });
         
         // applies a class rank (provided the student has a measurable gpa)
         
         int rank = 1;
         for (int i = 0; i < students.size(); i++) {
             Student student = students.get(i);
             
             student.setClassRank(student.getGpa() != -1 ? rank++ : 0);
         }
                 
         return students;
     }
     
     
}
