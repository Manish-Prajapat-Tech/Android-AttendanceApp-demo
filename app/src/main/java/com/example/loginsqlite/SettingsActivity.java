package com.example.loginsqlite;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private EditText classToDeleteEditText, rollNoToDeleteEditText;
    private Button deleteStudentButton, logOutButton;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dbHelper = new DatabaseHelper(this);

        classToDeleteEditText = findViewById(R.id.class_to_delete);
        rollNoToDeleteEditText = findViewById(R.id.roll_no_to_delete);
        deleteStudentButton = findViewById(R.id.delete_student_button);
        logOutButton = findViewById(R.id.log_out_button);

        deleteStudentButton.setOnClickListener(v -> {
            String studentClass = classToDeleteEditText.getText().toString().trim();
            String rollNo = rollNoToDeleteEditText.getText().toString().trim();

            if (studentClass.isEmpty() || rollNo.isEmpty()) {
                Toast.makeText(this, "Please enter class and roll number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.deleteStudent(studentClass, rollNo)) {
                Toast.makeText(this, "Student deleted successfully", Toast.LENGTH_SHORT).show();
                classToDeleteEditText.setText("");
                rollNoToDeleteEditText.setText("");
            } else {
                Toast.makeText(this, "Student not found or error occurred", Toast.LENGTH_SHORT).show();
            }
        });

        logOutButton.setOnClickListener(v -> {
            // Add your log out logic here in the future
            finish(); // For now, just closes the activity
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