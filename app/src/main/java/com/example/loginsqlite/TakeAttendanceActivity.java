package com.example.loginsqlite;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.loginsqlite.databinding.ActivityTakeAttendanceBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TakeAttendanceActivity extends AppCompatActivity {

    private ActivityTakeAttendanceBinding binding;
    private AttendanceAdapter adapter;
    private DatabaseHelper dbHelper;
    private String selectedClass;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTakeAttendanceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);

        selectedClass = getIntent().getStringExtra("CLASS_NAME");
        if (selectedClass == null) {
            Toast.makeText(this, "No class selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.attendanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadStudents();

        binding.selectDateButton.setOnClickListener(v -> showDatePickerDialog());

        String text = "Date: " + (selectedDate != null ? selectedDate : "Not Selected") + " | Class: " + selectedClass;
        binding.classDateTextView.setText(text);

        binding.saveAttendanceButton.setOnClickListener(v -> saveAttendance());
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime());
                    updateDateDisplay();
                    String text = "Date: " + selectedDate + " | Class: " + selectedClass;
                    binding.classDateTextView.setText(text);
                    loadSavedAttendanceForDate();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        if (selectedDate == null) {
            binding.selectedDate.setText(R.string.no_date_selected);
        } else {
            binding.selectedDate.setText(selectedDate);
        }
    }

    private void loadStudents() {
        List<DatabaseHelper.Student> students = dbHelper.getStudentsByClass(selectedClass);
        if (students.isEmpty()) {
            binding.attendanceRecyclerView.setVisibility(View.GONE);
            binding.attendanceEmptyView.setVisibility(View.VISIBLE);
        } else {
            binding.attendanceRecyclerView.setVisibility(View.VISIBLE);
            binding.attendanceEmptyView.setVisibility(View.GONE);
            adapter = new AttendanceAdapter(students);
            binding.attendanceRecyclerView.setAdapter(adapter);
        }
    }

    private void loadSavedAttendanceForDate() {
        if (adapter != null && selectedDate != null) {
            Map<Integer, Boolean> savedAttendance = dbHelper.getAttendanceForDate(selectedClass, selectedDate);
            adapter.setAttendance(savedAttendance);
        }
    }

    private void saveAttendance() {
        if (selectedDate == null || selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        List<AttendanceAdapter.AttendanceItem> items = adapter.getItems();
        if (dbHelper.addAttendanceInBatch(items, selectedDate, selectedClass)) {
            Toast.makeText(this, "Attendance saved successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error saving attendance", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}
