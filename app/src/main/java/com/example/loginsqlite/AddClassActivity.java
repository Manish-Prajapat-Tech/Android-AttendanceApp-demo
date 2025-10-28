package com.example.loginsqlite;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddClassActivity extends AppCompatActivity {

    private EditText etClassName;
    private Button btnAddClass;
    private ListView listClasses;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> classList;
    private DatabaseHelper dbHelper;
    private final Executor executor = Executors.newSingleThreadExecutor(); // Executor for background tasks
    private final Handler handler = new Handler(Looper.getMainLooper()); // Handler to post results back to the main thread

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);

        etClassName = findViewById(R.id.etClassName);
        btnAddClass = findViewById(R.id.btnAddClass);
        listClasses = findViewById(R.id.listClasses);

        dbHelper = new DatabaseHelper(this);
        classList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, classList);
        listClasses.setAdapter(adapter);

        loadClasses();

        btnAddClass.setOnClickListener(v -> addClass());

        listClasses.setOnItemClickListener((parent, view, position, id) -> {
            String selectedClass = classList.get(position);
            Intent intent = new Intent(AddClassActivity.this, MainActivity2.class);
            intent.putExtra("CLASS_NAME", selectedClass);
            startActivity(intent);
        });
    }

    private void addClass() {
        String className = etClassName.getText().toString().trim();

        if (className.isEmpty()) {
            Toast.makeText(this, "Please enter class name", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            // Background database operation
            boolean added = dbHelper.addClass(className);

            // Post result back to the main thread
            handler.post(() -> {
                if (added) {
                    Toast.makeText(AddClassActivity.this, "Class added successfully!", Toast.LENGTH_SHORT).show();
                    etClassName.setText("");
                    loadClasses(); // Refresh list
                } else {
                    Toast.makeText(AddClassActivity.this, "Class already exists or error occurred.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void loadClasses() {
        executor.execute(() -> {
            // Background database operation
            List<String> loadedClasses = dbHelper.getAllClasses();

            // Post result back to the main thread
            handler.post(() -> {
                classList.clear();
                classList.addAll(loadedClasses);
                adapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) dbHelper.close();
    }
}
