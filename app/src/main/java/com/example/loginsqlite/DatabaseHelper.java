package com.example.loginsqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "SchoolDB";
    private static final int DATABASE_VERSION = 1;

    // Users table (unchanged)
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // Students table (unchanged)
    private static final String TABLE_STUDENTS = "students";
    private static final String COLUMN_STUDENT_ID = "id";
    private static final String COLUMN_STUDENT_NAME = "name";
    private static final String COLUMN_STUDENT_CLASS = "student_class";
    private static final String COLUMN_ROLL_NO = "roll_no";

    // Attendance table (unchanged)
    private static final String TABLE_ATTENDANCE = "attendance";
    private static final String COLUMN_ATTENDANCE_ID = "id";  // Renamed for clarity
    private static final String COLUMN_ATTENDANCE_DATE = "date";
    private static final String COLUMN_ATTENDANCE_PRESENT = "present";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table (unchanged)
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
                COLUMN_PASSWORD + " TEXT NOT NULL)";
        db.execSQL(createUsersTable);

        // Create students table (unchanged)
        String createStudentsTable = "CREATE TABLE " + TABLE_STUDENTS + " (" +
                COLUMN_STUDENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_STUDENT_NAME + " TEXT NOT NULL, " +
                COLUMN_STUDENT_CLASS + " TEXT NOT NULL, " +
                COLUMN_ROLL_NO + " TEXT UNIQUE NOT NULL)";
        db.execSQL(createStudentsTable);

        // Create attendance table (minor rename for id column)
        String createAttendanceTable = "CREATE TABLE " + TABLE_ATTENDANCE + " (" +
                COLUMN_ATTENDANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "student_id INTEGER NOT NULL, " + COLUMN_ATTENDANCE_DATE + " TEXT NOT NULL, " +
                COLUMN_ATTENDANCE_PRESENT + " INTEGER NOT NULL, " +
                "FOREIGN KEY(student_id) REFERENCES " + TABLE_STUDENTS + "(" + COLUMN_STUDENT_ID + "))";
        db.execSQL(createAttendanceTable);

        // Insert defaults (unchanged)
        insertDefaultUser(db);
        insertSampleStudents(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTENDANCE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // **New: StudentAttendance class (data model for attendance records)**
    public static class StudentAttendance {
        private Student student;
        private boolean present;

        public StudentAttendance(Student student, boolean present) {
            this.student = student;
            this.present = present;
        }

        // **New: isPresent() method**
        public boolean isPresent() {
            return present;
        }

        // **New: getStudent() method**
        public Student getStudent() {
            return student;
        }

        // Optional: toString for display (e.g., in ListView)
        @Override
        public String toString() {
            String status = present ? "Present" : "Absent";
            return student.toString() + " - " + status;
        }
    }

    // **New: getAttendanceByDate(String date) method - fetches attendance for a specific date**
    public List<StudentAttendance> getAttendanceByDate(String date) {
        List<StudentAttendance> attendanceList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // JOIN query: Fetch all students and their attendance for the given date
        // If no attendance record exists for a student on that date, treat as Absent
        String query = "SELECT s." + COLUMN_STUDENT_ID + ", s." + COLUMN_STUDENT_NAME + ", s." + COLUMN_STUDENT_CLASS + ", s." + COLUMN_ROLL_NO + ", " +
                "COALESCE(a." + COLUMN_ATTENDANCE_PRESENT + ", 0) AS present " +
                "FROM " + TABLE_STUDENTS + " s " +
                "LEFT JOIN " + TABLE_ATTENDANCE + " a ON s." + COLUMN_STUDENT_ID + " = a.student_id AND a." + COLUMN_ATTENDANCE_DATE + " = ? " +
                "ORDER BY s." + COLUMN_ROLL_NO;
        Cursor cursor = db.rawQuery(query, new String[]{date});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String studentClass = cursor.getString(2);
                String rollNo = cursor.getString(3);
                boolean present = cursor.getInt(4) == 1;

                Student student = new Student(id, name, studentClass, rollNo);
                attendanceList.add(new StudentAttendance(student, present));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        Log.d(TAG, "Fetched " + attendanceList.size() + " attendance records for date: " + date);
        return attendanceList;
    }

    // **Previous methods (unchanged - addStudent, addUser, etc.)**
    public boolean addStudent(String name, String studentClass, String rollNo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STUDENT_NAME, name);
        values.put(COLUMN_STUDENT_CLASS, studentClass);
        values.put(COLUMN_ROLL_NO, rollNo);
        try {
            long result = db.insertOrThrow(TABLE_STUDENTS, null, values);
            db.close();
            Log.d(TAG, "Student added successfully: " + name + " (Roll: " + rollNo + ")");
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error adding student: " + e.getMessage());
            db.close();
            return false;
        }
    }

    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_STUDENTS, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String studentClass = cursor.getString(2);
                String rollNo = cursor.getString(3);
                students.add(new Student(id, name, studentClass, rollNo));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        Log.d(TAG, "Fetched " + students.size() + " students");
        return students;
    }

    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        try {
            long result = db.insertOrThrow(TABLE_USERS, null, values);
            db.close();
            Log.d(TAG, "User added successfully: " + username);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error adding user: " + e.getMessage());
            db.close();
            return false;
        }
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " +
                COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        Log.d(TAG, "User check for " + username + ": " + (exists ? "Valid" : "Invalid"));
        return exists;
    }

    private void insertDefaultUser(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, "teacher");
        values.put(COLUMN_PASSWORD, "password");
        db.insert(TABLE_USERS, null, values);
        Log.d(TAG, "Default user inserted: teacher/password");
    }

    private void insertSampleStudents(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_STUDENT_NAME, "John Doe");
        values.put(COLUMN_STUDENT_CLASS, "Class 10");
        values.put(COLUMN_ROLL_NO, "001");
        db.insert(TABLE_STUDENTS, null, values);

        values.clear();
        values.put(COLUMN_STUDENT_NAME, "Jane Smith");
        values.put(COLUMN_STUDENT_CLASS, "Class 10");
        values.put(COLUMN_ROLL_NO, "002");
        db.insert(TABLE_STUDENTS, null, values);

        Log.d(TAG, "Sample students inserted");
    }

    public boolean addAttendance(int studentId, String date, boolean isPresent) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("student_id", studentId);
        values.put(COLUMN_ATTENDANCE_DATE, date);
        values.put(COLUMN_ATTENDANCE_PRESENT, isPresent ? 1 : 0);
        long result = db.insert(TABLE_ATTENDANCE, null, values);
        db.close();
        return result != -1;
    }

    // **Student class (unchanged)**
    public static class Student {
        private int id;
        private String name;
        private String studentClass;
        private String rollNo;

        public Student(int id, String name, String studentClass, String rollNo) {
            this.id = id;
            this.name = name;
            this.studentClass = studentClass;
            this.rollNo = rollNo;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getStudentClass() { return studentClass; }
        public String getRollNo() { return rollNo; }

        @Override
        public String toString() {
            return name + " (" + rollNo + ", " + studentClass + ")";
        }
    }
}