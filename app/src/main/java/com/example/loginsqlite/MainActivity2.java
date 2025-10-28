package com.example.loginsqlite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity2 extends AppCompatActivity {

    private Button returnButton, takeAttendanceButton, viewAttendanceButton, settingsButton, addStudentButton, reportAttendanceButton;
    private DatabaseHelper dbHelper;
    private String selectedClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        dbHelper = new DatabaseHelper(this);
        selectedClass = getIntent().getStringExtra("CLASS_NAME");

        if (selectedClass == null) {
            Toast.makeText(this, "No class selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        returnButton = findViewById(R.id.back2);
        takeAttendanceButton = findViewById(R.id.take_attendance_button);
        viewAttendanceButton = findViewById(R.id.view_attendance_button);
        settingsButton = findViewById(R.id.settings_button);
        addStudentButton = findViewById(R.id.add_student_button);
        reportAttendanceButton = findViewById(R.id.report_attendance_button);

        addStudentButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity3.class);
            intent.putExtra("CLASS_NAME", selectedClass);
            startActivity(intent);
        });

        takeAttendanceButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, TakeAttendanceActivity.class);
            intent.putExtra("CLASS_NAME", selectedClass);
            startActivity(intent);
        });

        viewAttendanceButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewAttendanceActivity.class);
            intent.putExtra("CLASS_NAME", selectedClass);
            startActivity(intent);
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        reportAttendanceButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReportActivity.class);
            intent.putExtra("CLASS_NAME", selectedClass);
            startActivity(intent);
        });

        returnButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddClassActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}