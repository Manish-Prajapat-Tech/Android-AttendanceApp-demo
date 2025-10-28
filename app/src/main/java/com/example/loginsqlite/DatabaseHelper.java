package com.example.loginsqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import androidx.annotation.NonNull;

import java.util.*;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 10; 
    // Database Name
    private static final String DATABASE_NAME = "StudentAttendance.db";

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_STUDENTS = "students";
    private static final String TABLE_CLASSES = "classes";
    private static final String TABLE_ATTENDANCE = "attendance";

    // Users Table Columns
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // Students Table Columns
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CLASS = "class_id";
    private static final String COLUMN_ROLL_NO = "rollNo";

    // Classes Table Columns
    private static final String COLUMN_CLASS_ID = "id";
    private static final String COLUMN_CLASS_NAME = "class_name";

    // Attendance Table Columns
    private static final String COLUMN_ATTENDANCE_ID = "id";
    private static final String COLUMN_STUDENT_ID = "student_id";
    private static final String COLUMN_ATTENDANCE_DATE = "date";
    private static final String COLUMN_ATTENDANCE_CLASS = "class_id";
    private static final String COLUMN_ATTENDANCE_PRESENT = "present";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_USERNAME + " TEXT NOT NULL UNIQUE," + COLUMN_PASSWORD + " TEXT NOT NULL)";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_CLASSES_TABLE = "CREATE TABLE " + TABLE_CLASSES + " (" + COLUMN_CLASS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_CLASS_NAME + " TEXT NOT NULL UNIQUE)";
        db.execSQL(CREATE_CLASSES_TABLE);

        String CREATE_STUDENTS_TABLE = "CREATE TABLE " + TABLE_STUDENTS + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NAME + " TEXT NOT NULL," + COLUMN_CLASS + " INTEGER NOT NULL," + COLUMN_ROLL_NO + " TEXT NOT NULL," + "UNIQUE (" + COLUMN_CLASS + ", " + COLUMN_ROLL_NO + ")," + "FOREIGN KEY (" + COLUMN_CLASS + ") REFERENCES " + TABLE_CLASSES + "(" + COLUMN_CLASS_ID + "))";
        db.execSQL(CREATE_STUDENTS_TABLE);

        String CREATE_ATTENDANCE_TABLE = "CREATE TABLE " + TABLE_ATTENDANCE + " (" + COLUMN_ATTENDANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_STUDENT_ID + " INTEGER NOT NULL," + COLUMN_ATTENDANCE_DATE + " TEXT NOT NULL," + COLUMN_ATTENDANCE_CLASS + " INTEGER NOT NULL," + COLUMN_ATTENDANCE_PRESENT + " INTEGER NOT NULL," + "UNIQUE (" + COLUMN_STUDENT_ID + ", " + COLUMN_ATTENDANCE_DATE + ")," + "FOREIGN KEY (" + COLUMN_STUDENT_ID + ") REFERENCES " + TABLE_STUDENTS + "(" + COLUMN_ID + ")," + "FOREIGN KEY (" + COLUMN_ATTENDANCE_CLASS + ") REFERENCES " + TABLE_CLASSES + "(" + COLUMN_CLASS_ID + "))";
        db.execSQL(CREATE_ATTENDANCE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTENDANCE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        long result = db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};
        try (Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null)) {
            return cursor.getCount() > 0;
        }
    }

    public boolean checkUserExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};
        try (Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null)) {
            return cursor.getCount() > 0;
        }
    }

    public int getUsersCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, TABLE_USERS);
    }

    public boolean addClass(String className) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CLASS_NAME, className);
        long result = db.insertWithOnConflict(TABLE_CLASSES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        return result != -1;
    }

    public List<String> getAllClasses() {
        List<String> classes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_CLASSES, new String[]{COLUMN_CLASS_NAME}, null, null, null, null, null)) {
            if (cursor.moveToFirst()) {
                do {
                    classes.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_NAME)));
                } while (cursor.moveToNext());
            }
        }
        return classes;
    }

    public boolean addStudent(String name, String className, String rollNo) {
        SQLiteDatabase db = this.getWritableDatabase();
        int classId = getClassId(className, db);
        if (classId == -1) {
            ContentValues classValues = new ContentValues();
            classValues.put(COLUMN_CLASS_NAME, className);
            if (db.insert(TABLE_CLASSES, null, classValues) == -1) return false;
            classId = getClassId(className, db);
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_CLASS, classId);
        values.put(COLUMN_ROLL_NO, rollNo);
        long result = db.insertWithOnConflict(TABLE_STUDENTS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        return result != -1;
    }

    public boolean deleteStudent(String className, String rollNo) {
        SQLiteDatabase db = this.getWritableDatabase();
        int classId = getClassId(className, db);
        if (classId == -1) return false;

        String whereClause = COLUMN_CLASS + " = ? AND " + COLUMN_ROLL_NO + " = ?";
        String[] whereArgs = {String.valueOf(classId), rollNo};
        int result = db.delete(TABLE_STUDENTS, whereClause, whereArgs);
        return result > 0;
    }

    public List<Student> getStudentsByClass(String className) {
        List<Student> studentList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        int classId = getClassId(className, db);
        if (classId == -1) return studentList;

        try (Cursor cursor = db.query(TABLE_STUDENTS, null, COLUMN_CLASS + " = ?", new String[]{String.valueOf(classId)}, null, null, "CAST(" + COLUMN_ROLL_NO + " AS INTEGER) ASC")) {
            if (cursor.moveToFirst()) {
                do {
                    studentList.add(new Student(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                            className,
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLL_NO))
                    ));
                } while (cursor.moveToNext());
            }
        }
        return studentList;
    }

    public boolean addAttendanceInBatch(List<AttendanceAdapter.AttendanceItem> items, String date, String className) {
        SQLiteDatabase db = this.getWritableDatabase();
        int classId = getClassId(className, db);
        if (classId == -1) return false;

        db.beginTransaction();
        try {
            for (AttendanceAdapter.AttendanceItem item : items) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_STUDENT_ID, item.getStudent().getId());
                values.put(COLUMN_ATTENDANCE_DATE, date);
                values.put(COLUMN_ATTENDANCE_CLASS, classId);
                values.put(COLUMN_ATTENDANCE_PRESENT, item.isPresent() ? 1 : 0);
                db.insertWithOnConflict(TABLE_ATTENDANCE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error adding attendance in batch", e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public Map<Integer, Boolean> getAttendanceForDate(String className, String date) {
        Map<Integer, Boolean> attendanceMap = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        int classId = getClassId(className, db);
        if (classId == -1) return attendanceMap;

        String query = "SELECT s." + COLUMN_ID + ", a." + COLUMN_ATTENDANCE_PRESENT +
                " FROM " + TABLE_STUDENTS + " s" +
                " LEFT JOIN " + TABLE_ATTENDANCE + " a ON s." + COLUMN_ID + " = a." + COLUMN_STUDENT_ID +
                " AND a." + COLUMN_ATTENDANCE_DATE + " = ?" +
                " WHERE s." + COLUMN_CLASS + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{date, String.valueOf(classId)})) {
            if (cursor.moveToFirst()) {
                int studentIdCol = cursor.getColumnIndexOrThrow(COLUMN_ID);
                int presentCol = cursor.getColumnIndex(COLUMN_ATTENDANCE_PRESENT);

                do {
                    int studentId = cursor.getInt(studentIdCol);
                    if (presentCol != -1 && !cursor.isNull(presentCol)) {
                        boolean isPresent = cursor.getInt(presentCol) == 1;
                        attendanceMap.put(studentId, isPresent);
                    }
                } while (cursor.moveToNext());
            }
        }
        return attendanceMap;
    }

    public List<StudentAttendance> getAttendanceByDateAndClass(String date, String className) {
        List<StudentAttendance> attendanceList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        int classId = getClassId(className, db);
        if (classId == -1) return attendanceList;

        String selectQuery = "SELECT a." + COLUMN_ATTENDANCE_DATE + ", a." + COLUMN_ATTENDANCE_PRESENT + ", s." + COLUMN_NAME + ", s." + COLUMN_ROLL_NO + ", s." + COLUMN_ID + " as student_id " +
                " FROM " + TABLE_ATTENDANCE + " a" +
                " JOIN " + TABLE_STUDENTS + " s ON a." + COLUMN_STUDENT_ID + " = s." + COLUMN_ID +
                " WHERE a." + COLUMN_ATTENDANCE_DATE + " = ? AND a." + COLUMN_ATTENDANCE_CLASS + " = ?";
        try (Cursor cursor = db.rawQuery(selectQuery, new String[]{date, String.valueOf(classId)})) {
            if (cursor.moveToFirst()) {
                do {
                    Student student = new Student(
                            cursor.getInt(cursor.getColumnIndexOrThrow("student_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                            className,
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLL_NO))
                    );
                    boolean isPresent = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ATTENDANCE_PRESENT)) == 1;
                    String attendanceDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ATTENDANCE_DATE));
                    attendanceList.add(new StudentAttendance(student, isPresent, attendanceDate));
                } while (cursor.moveToNext());
            }
        }
        return attendanceList;
    }

    public Map<Integer, Integer> getAttendanceCountByStudent(String className, String startDate, String endDate) {
        Map<Integer, Integer> studentAttendanceCount = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        int classId = getClassId(className, db);
        if (classId == -1) return studentAttendanceCount;

        String query = "SELECT " + COLUMN_STUDENT_ID + ", COUNT(" + COLUMN_ATTENDANCE_ID + ") as present_count " +
                "FROM " + TABLE_ATTENDANCE + " " +
                "WHERE " + COLUMN_ATTENDANCE_CLASS + " = ? AND " +
                COLUMN_ATTENDANCE_PRESENT + " = 1 AND " +
                COLUMN_ATTENDANCE_DATE + " BETWEEN ? AND ? " +
                "GROUP BY " + COLUMN_STUDENT_ID;

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(classId), startDate, endDate})) {
            if (cursor.moveToFirst()) {
                do {
                    int studentId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_ID));
                    int presentCount = cursor.getInt(cursor.getColumnIndexOrThrow("present_count"));
                    studentAttendanceCount.put(studentId, presentCount);
                } while (cursor.moveToNext());
            }
        }
        return studentAttendanceCount;
    }

    public int getWorkingDaysCount(String className, String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        int classId = getClassId(className, db);
        if (classId == -1) return 0;

        String query = "SELECT COUNT(DISTINCT " + COLUMN_ATTENDANCE_DATE + ") FROM " + TABLE_ATTENDANCE + " " +
                "WHERE " + COLUMN_ATTENDANCE_CLASS + " = ? AND " +
                COLUMN_ATTENDANCE_DATE + " BETWEEN ? AND ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(classId), startDate, endDate})) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }

    private int getClassId(String className, @NonNull SQLiteDatabase db) {
        String[] columns = {COLUMN_CLASS_ID};
        String selection = COLUMN_CLASS_NAME + " = ?";
        String[] selectionArgs = {className};
        try (Cursor cursor = db.query(TABLE_CLASSES, columns, selection, selectionArgs, null, null, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CLASS_ID));
            }
        }
        return -1;
    }

    public static final class StudentAttendance {
        private final Student student;
        private final boolean isPresent;
        private final String date;

        public StudentAttendance(Student student, boolean isPresent, String date) {
            this.student = student;
            this.isPresent = isPresent;
            this.date = date;
        }

        public Student getStudent() {
            return student;
        }

        public boolean isPresent() {
            return isPresent;
        }

        public String getDate() {
            return date;
        }
    }

    public static final class Student {
        private final int id;
        private final String name;
        private final String className;
        private final String rollNo;

        public Student(int id, String name, String className, String rollNo) {
            this.id = id;
            this.name = name;
            this.className = className;
            this.rollNo = rollNo;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getClassName() {
            return className;
        }

        public String getRollNo() {
            return rollNo;
        }

        @NonNull
        @Override
        public String toString() {
            return this.name;
        }
    }
}
