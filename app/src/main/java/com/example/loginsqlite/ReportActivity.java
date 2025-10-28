package com.example.loginsqlite;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private DatabaseHelper dbHelper;
    private String selectedClass;
    private TextView startDateTextView, endDateTextView;
    private String startDate, endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        dbHelper = new DatabaseHelper(this);
        selectedClass = getIntent().getStringExtra("CLASS_NAME");

        if (selectedClass == null) {
            Toast.makeText(this, "No class selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        startDateTextView = findViewById(R.id.start_date_text_view);
        endDateTextView = findViewById(R.id.end_date_text_view);

        Button selectStartDateButton = findViewById(R.id.select_start_date_button);
        Button selectEndDateButton = findViewById(R.id.select_end_date_button);
        Button generateReportButton = findViewById(R.id.generate_report_button);

        selectStartDateButton.setOnClickListener(v -> showDatePickerDialog(true));
        selectEndDateButton.setOnClickListener(v -> showDatePickerDialog(false));

        generateReportButton.setOnClickListener(v -> {
            if (startDate == null || endDate == null) {
                Toast.makeText(this, "Please select both start and end dates", Toast.LENGTH_SHORT).show();
                return;
            }

            if (checkPermission()) {
                createPdf();
            } else {
                requestPermission();
            }
        });
    }

    private void showDatePickerDialog(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    calendar.set(year, month, dayOfMonth);
                    String selectedDate = sdf.format(calendar.getTime());
                    if (isStartDate) {
                        startDate = selectedDate;
                        String startDateText = "Start Date: " + startDate;
                        startDateTextView.setText(startDateText);
                    } else {
                        endDate = selectedDate;
                        String endDateText = "End Date: " + endDate;
                        endDateTextView.setText(endDateText);
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true; // No permission required for API 29+
        } else {
            int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Write external storage permission allows us to save files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createPdf();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createPdf() {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, "attendance_report.pdf");
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/");
            }

            Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
            if (uri != null) {
                try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                    PdfWriter writer = new PdfWriter(outputStream);
                    PdfDocument pdfDocument = new PdfDocument(writer);
                    Document document = new Document(pdfDocument, PageSize.A4);

                    // Header
                    document.add(new Paragraph("Subject_Attendance_Room")
                            .setTextAlignment(TextAlignment.CENTER)
                            .setBold()
                            .setFontColor(new DeviceRgb(0, 0, 170)));

                    document.add(new Paragraph("Attendance Report for " + selectedClass)
                            .setTextAlignment(TextAlignment.CENTER));

                    document.add(new Paragraph("From: " + startDate + " To: " + endDate)
                            .setTextAlignment(TextAlignment.CENTER));

                    // Table
                    List<DatabaseHelper.Student> students = dbHelper.getStudentsByClass(selectedClass);
                    Map<Integer, Integer> attendanceCount = dbHelper.getAttendanceCountByStudent(selectedClass, startDate, endDate);
                    int workingDays = dbHelper.getWorkingDaysCount(selectedClass, startDate, endDate);

                    Table table = new Table(UnitValue.createPercentArray(new float[]{4, 2, 2, 2}));
                    table.setWidth(UnitValue.createPercentValue(100));
                    table.setMarginTop(20);

                    table.addHeaderCell("Student Name");
                    table.addHeaderCell("Roll No");
                    table.addHeaderCell("Present Days");
                    table.addHeaderCell("Total Days");

                    for (DatabaseHelper.Student student : students) {
                        int presentCount = attendanceCount.getOrDefault(student.getId(), 0);
                        table.addCell(student.getName());
                        table.addCell(student.getRollNo());
                        table.addCell(String.valueOf(presentCount));
                        table.addCell(String.valueOf(workingDays));
                    }
                    document.add(table);

                    // Footer
                    document.add(new Paragraph("developed by manish prajapat")
                            .setTextAlignment(TextAlignment.CENTER)
                            .setItalic()
                            .setMarginTop(20));

                    document.close();
                    Toast.makeText(this, "PDF report saved to Downloads folder", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Failed to create PDF file", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
