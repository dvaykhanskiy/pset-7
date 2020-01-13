package com.apcsa.data;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import com.apcsa.controller.Application;
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
    
    public static void changePassword(String username, String password) {
        try (Connection conn = getConnection()) {
            int isChanged = updatePassword(conn, username, Utils.getHash(password));
            if (isChanged != 1) {
                System.err.println("Unable to successfully create password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean resetPassword(String username) {
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(QueryUtils.UPDATE_AUTH_SQL)) {

            conn.setAutoCommit(false);
            stmt.setString(1, Utils.getHash(username));
            stmt.setString(2, username);

            if (stmt.executeUpdate() == 1) {
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
    
    public static int resetTimestamp(String username) {
        try (Connection conn = getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(QueryUtils.UPDATE_LAST_LOGIN_SQL)) {
                // https://stackoverflow.com/questions/18915075/java-convert-string-to-timestamp
                conn.setAutoCommit(false);
                stmt.setString(1, "0000-00-00 00:00:00.000");
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
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    public static int resetLastLogin(String username) {
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(QueryUtils.UPDATE_LAST_LOGIN_SQL)){

            conn.setAutoCommit(false);
            stmt.setString(1,"0000-00-00 00:00:00.000");
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
    
    public static ArrayList<Student> getStudents() {
        ArrayList<Student> students = new ArrayList<Student>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery(QueryUtils.GET_ALL_STUDENTS_SQL)) {
                while (rs.next()) {
                    students.add(new Student(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }

    public static ArrayList<Student> getStudentsByGrade(int grade) {
        ArrayList<Student> students = new ArrayList<Student>();

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENTS_BY_GRADE_SQL)) {
            stmt.setString(1, String.valueOf(grade));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(new Student(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }

    public static ArrayList<Student> getStudentsByCourse(String courseNo) {
        ArrayList<Student> students = new ArrayList<Student>();

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENTS_BY_COURSE_SQL)) {
            stmt.setString(1, courseNo);
            try (ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    students.add(new Student(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }

    public static ArrayList<String> getCourses(int departmentId) {
        ArrayList<String> courses = new ArrayList<String>();

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_COURSES)) {
            stmt.setString(1, String.valueOf(departmentId));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(rs.getString("course_no"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courses;
    }
    
    public static ArrayList<String> getCoursesFromDepartment(int departmentId) {
        ArrayList<String> courses = new ArrayList<String>();
         try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_COURSES)) {
                 stmt.setInt(1, departmentId);

                 try (ResultSet rs = stmt.executeQuery()) {
                     while (rs.next()) {
                         String result = rs.getString("course_no");
                       courses.add(result);
                     }
                 }
             return courses;
        } catch (SQLException e) {
            e.printStackTrace();
        }
         return courses;
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
    
    private static int updatePassword(Connection conn, String username, String password) {
        try (PreparedStatement stmt = conn.prepareStatement(QueryUtils.UPDATE_PASSWORD_SQL)) {
            conn.setAutoCommit(false);
            stmt.setString(1, password);
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
     
     public static ArrayList<Teacher> getTeachersByDept(int department) {
         ArrayList<Teacher> teachers = new ArrayList<Teacher>();

         try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ALL_TEACHERS_BY_DEPT_SQL)) {
             stmt.setString(1, String.valueOf(department));
             try (ResultSet rs = stmt.executeQuery()) {

                     teachers.add(new Teacher(rs));
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }

         return teachers;
     }
     
     public static Connection getConnection() throws SQLException {
         return DriverManager.getConnection(PROTOCOL + DATABASE_URL);
     }
     
     public static int getNumberOfCourses() {
         try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_NUMBER_OF_COURSES)) {

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return 1;
     }
     
     public static String getCourseNumber(int i) {
         try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_COURSE_NUMBER)) {

                 stmt.setString(1, String.valueOf(i));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("course_no");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return "e";
     }
     
     public static ArrayList<String> getStudentId(String courseId) {
         ArrayList<String> studentIds = new ArrayList<String>();
         try (Connection conn = getConnection();
                  PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENT_ID_FROM_COURSE_ID)) {

                 stmt.setString(1, courseId);
                  try (ResultSet rs = stmt.executeQuery()) {
                      while (rs.next()) {
                          studentIds.add(rs.getString("student_id"));
                      }
                  }
              return studentIds;
         } catch (SQLException e) {
             e.printStackTrace();
         }
          return studentIds;
     }

     public static ArrayList<String> getStudentsByStudentId(String studentIds) {
         ArrayList<String> students = new ArrayList<String>();
         try (Connection conn = getConnection();
                  PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENTS_BY_STUDENT_ID)) {

                 stmt.setString(1,  studentIds);
                  try (ResultSet rs = stmt.executeQuery()) {
                      while (rs.next()) {
                          students.add(rs.getString("first_name"));
                          students.add(rs.getString("last_name"));
                          students.add(rs.getString("gpa"));
                      }
                  }
              return students;
         } catch (SQLException e) {
             e.printStackTrace();
         }
          return students;
     }

     public static String getStudentGrade(String courseId, String studentId) {
         try (Connection conn = getConnection();
                  PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENT_GRADE)) {

                 stmt.setString(1,  courseId);
                 stmt.setString(2,  studentId);
                  try (ResultSet rs = stmt.executeQuery()) {
                      while (rs.next()) {
                          return rs.getString("grade");
                      }
                  }
              return "no";
         } catch (SQLException e) {
             e.printStackTrace();
         }
          return "no";
     }
     
     public static int addAssignment(int courseId, int assignmentId, int markingPeriod, int isMidterm, int isFinal, String title, int pointValue) {
         try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(QueryUtils.ADD_ASSIGNMENT)) {

                conn.setAutoCommit(false);
                stmt.setInt(1, courseId);
                stmt.setInt(2, assignmentId);
                stmt.setInt(3, markingPeriod);
                stmt.setInt(4, isMidterm);
                stmt.setInt(5, isFinal);
                stmt.setString(6, title);
                stmt.setInt(7, pointValue);

                if (stmt.executeUpdate() == 1) {
                    conn.commit();
                    return 1;
                } else {
                    conn.rollback();
                    return -1;
                }
            } catch (SQLException e) {
                return -1;
            }
     }
     
     public static int getCourseIdFromCourseNo(String courseNo){
         try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_COURSE_ID_FROM_DEPARTMENT_ID)) {

                stmt.setString(1, courseNo);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("course_id");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return 1;
     }

     public static int getCourseId(String courseNumber) {
         try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_COURSE_ID)) {

                 stmt.setString(1, courseNumber);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("course_Id");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return 0;
     }

     public static ArrayList<String> getAssignmentIds() {
         ArrayList<String> assignmentIds = new ArrayList<String>();
         try (Connection conn = getConnection();
                  PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_IDS)) {

                  try (ResultSet rs = stmt.executeQuery()) {
                      while (rs.next()) {
                        assignmentIds.add(rs.getString("assignment_id"));
                      }
                  }
              return assignmentIds;
         } catch (SQLException e) {
             e.printStackTrace();
         }
          return assignmentIds;
     }
     
     public static int assignmentRows() {
         try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(QueryUtils.PREVIOUS_ASSIGNMENT_ID)) {

             try (ResultSet rs = stmt.executeQuery()) {
                 if (rs.next()) {
                     return rs.getInt("count(*)");
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }

         return 1;
     }

     public static int deleteAssignment(int courseId, int markingPeriod, String title) {
         try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(QueryUtils.DELETE_ASSIGNMENT)) {

                conn.setAutoCommit(false);
                stmt.setInt(1, courseId);
                stmt.setInt(2, markingPeriod);
                stmt.setString(3, title);

                if (stmt.executeUpdate() == 1) {
                    conn.commit();
                    return 1;
                } else {
                    conn.rollback();
                    return -1;
                }
            } catch (SQLException e) {
                return -1;
            }
     }
     
     public static ArrayList<String> getPointValues(int courseId, int markingPeriod) {
         ArrayList<String> pointValues = new ArrayList<String>();

         try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_POINT_VALUES)) {

             conn.setAutoCommit(false);
             stmt.setInt(1, courseId);
             stmt.setInt(2, markingPeriod);

             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                    pointValues.add(rs.getString("point_value"));
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }

         return pointValues;
     }

     public static ArrayList<String> getAssignments(int courseId, int markingPeriod) {
         ArrayList<String> assignments = new ArrayList<String>();

         try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENTS)) {

             conn.setAutoCommit(false);
             stmt.setInt(1, courseId);
             stmt.setInt(2, markingPeriod);

             try (ResultSet rs = stmt.executeQuery()) {
                 while (rs.next()) {
                    assignments.add(rs.getString("title"));
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }

         return assignments;
     }

     public static int getPointValue(String title) {
         try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_POINT_VALUE)) {

             stmt.setString(1, title);

             try (ResultSet rs = stmt.executeQuery()) {
                 if (rs.next()) {
                     return rs.getInt("point_value");
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }

         return 1;
     }

     public static String getCourseIdFromCourseNoTwo(String courseNo) {
         try (Connection conn = getConnection();
                  PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_COURSE_ID_FROM_COURSE_NO)) {
                  stmt.setString(1,  courseNo);
                  try (ResultSet rs = stmt.executeQuery()) {
                      while (rs.next()) {
                          return rs.getString("course_id");
                      }
                  }
              return "no";
         } catch (SQLException e) {
             e.printStackTrace();
         }
          return "no";
     }

     public static ArrayList<String> getStudentFirstNames(String studentIds) {
         ArrayList<String> students = new ArrayList<String>();
         try (Connection conn = getConnection();
                  PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENTS_FIRST_NAMES)) {

                 stmt.setString(1, studentIds);
                  try (ResultSet rs = stmt.executeQuery()) {
                      while (rs.next()) {
                          students.add(rs.getString("first_name"));
                      }
                  }
              return students;
         } catch (SQLException e) {
             e.printStackTrace();
         }
          return students;
     }

     public static Collection<? extends String> getStudentLastNames(String studentIds) {
         ArrayList<String> students = new ArrayList<String>();
         try (Connection conn = getConnection();
                  PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENTS_LAST_NAMES)) {

                 stmt.setString(1,  studentIds);
                  try (ResultSet rs = stmt.executeQuery()) {
                      while (rs.next()) {
                          students.add(rs.getString("last_name"));
                      }
                  }
              return students;
         } catch (SQLException e) {
             e.printStackTrace();
         }
          return students;
     }
     
     public static int getAssignmentIdFromTitle(String title, int courseId, int markingPeriod) {
         try (Connection conn = getConnection();
                  PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_ID_FROM_TITLE_PLUS)) {

                 stmt.setString(1,  title);
                 stmt.setInt(2,  courseId);
                 stmt.setInt(3,  markingPeriod);
                 try (ResultSet rs = stmt.executeQuery()) {
                      while (rs.next()) {
                          return rs.getInt("assignment_id");
                      }
                  }
              return -1;
         } catch (SQLException e) {
             e.printStackTrace();
         }
          return -1;
     }
     
     public static int assignmentGradesRows() {
         try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(QueryUtils.ROWS_IN_ASSIGNMENT_GRADES)) {

             try (ResultSet rs = stmt.executeQuery()) {
                 if (rs.next()) {
                     return rs.getInt("count(*)");
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }

         return 1;
     }
     
     public static int previousGrade(int courseId, int assignmentId, int studentId) {
         try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(QueryUtils.PREVIOUS_GRADE)) {

             stmt.setInt(1, courseId);
             stmt.setInt(2, assignmentId);
             stmt.setInt(3, studentId);
             try (ResultSet rs = stmt.executeQuery()) {
                 if (rs.next()) {
                     return rs.getInt("points_earned");
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }

         return -1;
     }

     public static int checkGrade(int courseId, int assignmentId, int studentId) {
         try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(QueryUtils.FIND_GRADE)) {

             stmt.setInt(1, courseId);
             stmt.setInt(2, assignmentId);
             stmt.setInt(3, studentId);
             try (ResultSet rs = stmt.executeQuery()) {
                 if (rs.next()) {
                     return rs.getInt("is_graded");
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }

         return -1;
     }

     public static int addAssignmentGrade(int courseId, int assignmentId, int studentId, double pointsEarned, int pointsPossible, int isGraded) {
         try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(QueryUtils.ADD_ASSIGNMENT_GRADE)) {

                conn.setAutoCommit(false);
                stmt.setInt(1, courseId);
                stmt.setInt(2, assignmentId);
                stmt.setInt(3, studentId);
                stmt.setDouble(4, pointsEarned);
                stmt.setInt(5, pointsPossible);
                stmt.setInt(6, isGraded);

                if (stmt.executeUpdate() == 1) {
                    conn.commit();
                    return 1;
                } else {
                    conn.rollback();
                    return -1;
                }
            } catch (SQLException e) {
                return -1;
            }
     }

     public static int updateAssignmentGrade(int courseId, int assignmentId, int studentId, double pointsEarned) {
         try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(QueryUtils.UPDATE_ASSIGNMENT_GRADE)) {

                conn.setAutoCommit(false);
                stmt.setDouble(1, pointsEarned);
                stmt.setInt(2, courseId);
                stmt.setInt(3, assignmentId);
                stmt.setInt(4, studentId);

                if (stmt.executeUpdate() == 1) {
                    conn.commit();
                    return 1;
                } else {
                    conn.rollback();
                    return -1;
                }
            } catch (SQLException e) {
                return -1;
            }
     }
     
     public static ArrayList<Integer> getAssignmentIdByMP(int markingPeriod) {
         ArrayList<Integer> assignments = new ArrayList<Integer>();
         try (Connection conn = getConnection();
                  PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_ASSIGNMENT_BY_MP)) {

                 stmt.setInt(1,  markingPeriod);
                  try (ResultSet rs = stmt.executeQuery()) {
                      while (rs.next()) {
                          assignments.add(rs.getInt("assignment_id"));
                      }
                  }
              return assignments;
         } catch (SQLException e) {
             e.printStackTrace();
         }
          return assignments;
     }
     
     /**
      * Returns an MD5 hash of the user's plaintext password.
      *
      * @param plaintext the password
      * @return an MD5 hash of the password
      */

     public static void shutdown(boolean error) {
         if (error) {
             System.out.println("\nA fatal error has occurred. Shutting down...");
             Application.running = false;
             return;
         }
     }
     
     
}
