package com.example.loginsqlite;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {
    private ListView studentListView;
    private Button returnButton;
    private TextView emptyView;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        try {
            // Initialize views and database helper
            returnButton = findViewById(R.id.back2);
            studentListView = findViewById(R.id.student_list_view);
            emptyView = findViewById(R.id.empty_view);
            dbHelper = new DatabaseHelper(this);

            if (studentListView == null || emptyView == null) {
                Toast.makeText(this, "Error: Views not found", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            studentListView.setEmptyView(emptyView);

            // Fetch and display students
            List<DatabaseHelper.Student> students = dbHelper.getAllStudents();
            if (students == null || students.isEmpty()) {
                Toast.makeText(this, R.string.no_students_found, Toast.LENGTH_SHORT).show();
            } else {
                ArrayAdapter<DatabaseHelper.Student> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        students
                );
                studentListView.setAdapter(adapter);
            }

            // Handle take attendance button
            Button takeAttendanceButton = findViewById(R.id.take_attendance_button);
            if (takeAttendanceButton != null) {
                takeAttendanceButton.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(MainActivity2.this, TakeAttendanceActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(this, "Error opening take attendance: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            // Handle view attendance button
            Button viewAttendanceButton = findViewById(R.id.view_attendance_button);
            if (viewAttendanceButton != null) {
                viewAttendanceButton.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(MainActivity2.this, ViewAttendanceActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(this, "Error opening view attendance: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            // Handle back button

        } catch (Exception e) {
            Toast.makeText(this, "Error loading student list: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                startActivity(intent);
            }
        });
        Button btnTakeAttendance = findViewById(R.id.take_attendance_button);  // Your ID
        btnTakeAttendance.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity2.this, TakeAttendanceActivity.class);
                startActivity(intent);
            } catch (Exception e) {

                Toast.makeText(MainActivity2.this, "Error opening attendance", Toast.LENGTH_SHORT).show();
            }
        });

    }

}