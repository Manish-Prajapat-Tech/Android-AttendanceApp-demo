package com.example.loginsqlite;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity3 extends AppCompatActivity {
    private EditText studentNameEditText, classEditText, rollNoEditText;
    private Button AddStudent;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        // Initialize views (IDs must match activity_main3.xml)
        studentNameEditText = findViewById(R.id.student_name);
        classEditText = findViewById(R.id.student_class);
        rollNoEditText = findViewById(R.id.roll_no);
        AddStudent = findViewById(R.id.add_student_button);
        dbHelper = new DatabaseHelper(this);

        // Set click listener for add student button
        AddStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = studentNameEditText.getText().toString().trim();
                String studentClass = classEditText.getText().toString().trim();
                String rollNo = rollNoEditText.getText().toString().trim();

                // Validate inputs using TextUtils for robustness
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(studentClass) || TextUtils.isEmpty(rollNo)) {
                    Toast.makeText(MainActivity3.this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                } else {
                    // Add student to database
                    boolean added = dbHelper.addStudent(name, studentClass, rollNo);
                    if (added) {
                        Toast.makeText(MainActivity3.this, R.string.add_student_success, Toast.LENGTH_SHORT).show();
                        // Clear input fields
                        studentNameEditText.setText("");
                        classEditText.setText("");
                        rollNoEditText.setText("");
                    } else {
                        Toast.makeText(MainActivity3.this, R.string.add_student_failed, Toast.LENGTH_SHORT).show();
                    }
                    // Optional: Close database after operation
                    dbHelper.close();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();  // Ensure database is closed on activity destroy
        }
    }
}