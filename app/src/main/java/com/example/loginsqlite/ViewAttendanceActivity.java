package com.example.loginsqlite;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ViewAttendanceActivity extends AppCompatActivity {
    private ListView presentListView;
    private ListView absentListView;
    private TextView presentEmptyView;
    private TextView absentEmptyView;
    private TextView selectedDateView;
    private Button selectDateButton;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        // Hide ActionBar title (fallback)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        try {
            // Initialize views and database helper
            presentListView = findViewById(R.id.present_list_view);
            absentListView = findViewById(R.id.absent_list_view);
            presentEmptyView = findViewById(R.id.present_empty_view);
            absentEmptyView = findViewById(R.id.absent_empty_view);
            selectedDateView = findViewById(R.id.selected_date);
            selectDateButton = findViewById(R.id.select_date_button);
            dbHelper = new DatabaseHelper(this);

            if (presentListView == null || absentListView == null ||
                    presentEmptyView == null || absentEmptyView == null ||
                    selectedDateView == null || selectDateButton == null) {
                Toast.makeText(this, "Error: Views not found", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // Set empty views
            presentListView.setEmptyView(presentEmptyView);
            absentListView.setEmptyView(absentEmptyView);

            // Set up date picker
            selectDateButton.setOnClickListener(v -> showDatePickerDialog());

            // Handle back button
            Button backButton = findViewById(R.id.back_button); // Fixed ID
            if (backButton != null) {
                backButton.setOnClickListener(v -> finish());
            } else {
                Toast.makeText(this, "Error: Back button not found", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // Load attendance for today by default
            Calendar calendar = Calendar.getInstance();
            String defaultDate = String.format(Locale.getDefault(), "%d-%02d-%02d",
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
            selectedDateView.setText(defaultDate);
            loadAttendance(defaultDate);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing view attendance: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    selectedDateView.setText(selectedDate);
                    loadAttendance(selectedDate);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void loadAttendance(String date) {
        try {
            List<DatabaseHelper.StudentAttendance> attendanceList = dbHelper.getAttendanceByDate(date);
            List<DatabaseHelper.Student> presentStudents = new ArrayList<>();
            List<DatabaseHelper.Student> absentStudents = new ArrayList<>();

            if (attendanceList == null) {
                Toast.makeText(this, R.string.no_attendance_found, Toast.LENGTH_SHORT).show();
                presentListView.setAdapter(null);
                absentListView.setAdapter(null);
                return;
            }

            for (DatabaseHelper.StudentAttendance attendance : attendanceList) {
                if (attendance != null && attendance.getStudent() != null) {
                    if (attendance.isPresent()) {
                        presentStudents.add(attendance.getStudent());
                    } else {
                        absentStudents.add(attendance.getStudent());
                    }
                }
            }

            ArrayAdapter<DatabaseHelper.Student> presentAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_list_item_1, presentStudents);
            presentListView.setAdapter(presentAdapter);

            ArrayAdapter<DatabaseHelper.Student> absentAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_list_item_1, absentStudents);
            absentListView.setAdapter(absentAdapter);

            if (attendanceList.isEmpty()) {
                Toast.makeText(this, R.string.no_attendance_found, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading attendance: " + e.getMessage(), Toast.LENGTH_LONG).show();
            presentListView.setAdapter(null);
            absentListView.setAdapter(null);
        }
    }
}