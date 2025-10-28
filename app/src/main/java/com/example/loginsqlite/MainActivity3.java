package com.example.loginsqlite;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity3 extends AppCompatActivity {

    private EditText nameEditText, classEditText, rollNoEditText;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        nameEditText = findViewById(R.id.student_name);
        classEditText = findViewById(R.id.student_class);
        rollNoEditText = findViewById(R.id.student_roll_no);
        Button addStudentButton = findViewById(R.id.add_student);
        dbHelper = new DatabaseHelper(this);

        // Get the class name from Intent and pre-fill it
        String className = getIntent().getStringExtra("CLASS_NAME");
        if (className != null && !className.isEmpty()) {
            classEditText.setText(className);
            classEditText.setEnabled(false); // Disable editing to enforce the selected class
        } else {
            Toast.makeText(this, "No class selected", Toast.LENGTH_SHORT).show();
            finish(); // Close if no class is provided
            return;
        }

        addStudentButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String studentClass = classEditText.getText().toString().trim();
            String rollNo = rollNoEditText.getText().toString().trim();

            if (name.isEmpty() || studentClass.isEmpty() || rollNo.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.addStudent(name, studentClass, rollNo)) {
                Toast.makeText(this, "Student added successfully", Toast.LENGTH_SHORT).show();
                nameEditText.setText("");
                rollNoEditText.setText("");
            } else {
                Toast.makeText(this, "Failed to add student (duplicate roll number?)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) dbHelper.close();
    }
}