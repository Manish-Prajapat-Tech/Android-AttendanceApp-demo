package com.example.loginsqlite;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ViewAttendanceActivity extends AppCompatActivity {

    private ListView presentListView, absentListView;
    private TextView selectedDateTextView, presentEmptyView, absentEmptyView;
    private Button selectDateButton, backButton;
    private DatabaseHelper dbHelper;
    private String selectedClass;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        // Initialize views
        presentListView = findViewById(R.id.present_list_view);
        absentListView = findViewById(R.id.absent_list_view);
        selectedDateTextView = findViewById(R.id.selected_date);
        presentEmptyView = findViewById(R.id.present_empty_view);
        absentEmptyView = findViewById(R.id.absent_empty_view);
        selectDateButton = findViewById(R.id.select_date_button);
        backButton = findViewById(R.id.back_button);
        dbHelper = new DatabaseHelper(this);

        // Retrieve class from Intent
        selectedClass = getIntent().getStringExtra("CLASS_NAME");
        if (selectedClass == null) {
            Toast.makeText(this, "No class selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up date selection
        selectDateButton.setOnClickListener(v -> showDatePickerDialog());
        backButton.setOnClickListener(v -> finish());
        updateDateDisplay(); // Set initial date
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    selectedDate = year1 + "-" + String.format("%02d", month1 + 1) + "-" + String.format("%02d", dayOfMonth);
                    updateDateDisplay();
                    loadAttendance();
                }, year, month, day);
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        if (selectedDate == null) {
            selectedDateTextView.setText(R.string.no_date_selected);
        } else {
            selectedDateTextView.setText(selectedDate);
        }
    }

    private void loadAttendance() {
        if (selectedDate == null) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        List<DatabaseHelper.StudentAttendance> attendanceList = dbHelper.getAttendanceByDateAndClass(selectedDate, selectedClass);
        List<String> presentStudents = new ArrayList<>();
        List<String> absentStudents = new ArrayList<>();

        for (DatabaseHelper.StudentAttendance attendance : attendanceList) {
            String studentName = attendance.getStudent().getName();
            String rollNo = attendance.getStudent().getRollNo();
            String item = studentName + " (" + rollNo + ")";
            if (attendance.isPresent()) {
                presentStudents.add(item);
            } else {
                absentStudents.add(item);
            }
        }

        // Update present list
        ArrayAdapter<String> presentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, presentStudents);
        presentListView.setAdapter(presentAdapter);
        presentEmptyView.setVisibility(presentStudents.isEmpty() ? View.VISIBLE : View.GONE);

        // Update absent list
        ArrayAdapter<String> absentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, absentStudents);
        absentListView.setAdapter(absentAdapter);
        absentEmptyView.setVisibility(absentStudents.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
