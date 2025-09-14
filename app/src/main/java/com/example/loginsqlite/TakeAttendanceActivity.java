package com.example.loginsqlite;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TakeAttendanceActivity extends AppCompatActivity {
    private RecyclerView attendanceRecyclerView;
    private TextView emptyView;
    private TextView selectedDateView;
    private Button selectDateButton;
    private DatabaseHelper dbHelper;
    private List<DatabaseHelper.Student> students;
    private Map<Integer, Boolean> attendanceStatus;
    private String selectedDate;
    private AttendanceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_attendance);

        // Hide ActionBar title (fallback)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Initialize views and database helper
        attendanceRecyclerView = findViewById(R.id.attendance_list_view);
        emptyView = findViewById(R.id.attendance_empty_view);
        selectedDateView = findViewById(R.id.selected_date);
        selectDateButton = findViewById(R.id.select_date_button);
        dbHelper = new DatabaseHelper(this);
        attendanceStatus = new HashMap<>();

        try {
            // Validate views
            if (attendanceRecyclerView == null || emptyView == null || selectedDateView == null || selectDateButton == null) {
                Toast.makeText(this, "Error: Views not found", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // Set up RecyclerView
            attendanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            attendanceRecyclerView.setHasFixedSize(true);

            // Fetch students
            students = dbHelper.getAllStudents();
            if (students == null || students.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                attendanceRecyclerView.setVisibility(View.GONE);
                Toast.makeText(this, R.string.no_students_found, Toast.LENGTH_SHORT).show();
                return;
            }

            // Set up adapter
            adapter = new AttendanceAdapter(students);
            attendanceRecyclerView.setAdapter(adapter);

            // Set up date picker
            selectDateButton.setOnClickListener(v -> showDatePickerDialog());

            // Handle save attendance button
            Button saveButton = findViewById(R.id.save_attendance_button);
            if (saveButton != null) {
                saveButton.setOnClickListener(v -> {
                    try {
                        if (selectedDate == null) {
                            Toast.makeText(this, R.string.select_date_first, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        boolean success = true;
                        for (DatabaseHelper.Student student : students) {
                            boolean isPresent = attendanceStatus.getOrDefault(student.getId(), false);
                            if (!dbHelper.addAttendance(student.getId(), selectedDate, isPresent)) {
                                success = false;
                            }
                        }
                        Toast.makeText(this,
                                success ? R.string.attendance_saved : R.string.attendance_save_failed,
                                Toast.LENGTH_SHORT).show();
                        if (success) {
                            finish();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error saving attendance: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(this, "Error: Save button not found", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading attendance: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                    selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    selectedDateView.setText(selectedDate);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {
        private final List<DatabaseHelper.Student> students;

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView;
            CheckBox checkBox;

            public ViewHolder(View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.student_name);
                checkBox = itemView.findViewById(R.id.student_checkbox);
            }
        }

        public AttendanceAdapter(List<DatabaseHelper.Student> students) {
            this.students = students;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_student, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            DatabaseHelper.Student student = students.get(position);
            if (student != null && holder.nameTextView != null && holder.checkBox != null) {
                holder.nameTextView.setText(student.toString());

                // Clear old listener
                holder.checkBox.setOnCheckedChangeListener(null);

                // Restore checkbox state
                boolean isChecked = attendanceStatus.getOrDefault(student.getId(), false);
                holder.checkBox.setChecked(isChecked);

                // Set new listener
                holder.checkBox.setOnCheckedChangeListener((buttonView, checked) -> {
                    attendanceStatus.put(student.getId(), checked);
                    notifyDataSetChanged(); // Refresh all items
                });
            } else {
                Toast.makeText(TakeAttendanceActivity.this, "Error: Invalid student data or views", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public int getItemCount() {
            return students.size();
        }
    }
}