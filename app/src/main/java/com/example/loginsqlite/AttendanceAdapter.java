package com.example.loginsqlite;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    public AttendanceAdapter(List<DatabaseHelper.Student> students, Object updateAttendance, TakeAttendanceActivity takeAttendanceActivity, List<AttendanceItem> items) {

        this.items = items;
    }

    public static class AttendanceItem {
        private final DatabaseHelper.Student student;
        private boolean isPresent;

        public AttendanceItem(DatabaseHelper.Student student, boolean isPresent) {
            this.student = student;
            this.isPresent = isPresent;
        }

        public DatabaseHelper.Student getStudent() {
            return student;
        }

        public boolean isPresent() {
            return isPresent;
        }

        public void setPresent(boolean present) {
            isPresent = present;
        }
    }
    private final List<AttendanceItem> items;

    public AttendanceAdapter(List<DatabaseHelper.Student> students) {
        this.items = new ArrayList<>();
        for (DatabaseHelper.Student student : students) {
            items.add(new AttendanceItem(student, false));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final AttendanceItem item = items.get(position);
        holder.studentNameTextView.setText(String.format("%s (%s)", item.getStudent().getName(), item.getStudent().getRollNo()));
        holder.presentCheckBox.setOnCheckedChangeListener(null);
        holder.presentCheckBox.setChecked(item.isPresent());
        holder.presentCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> item.setPresent(isChecked));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<AttendanceItem> getItems() {
        return items;
    }

    public void setAttendance(Map<Integer, Boolean> savedAttendance) {
        if (savedAttendance == null) {
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            AttendanceItem item = items.get(i);
            int studentId = item.getStudent().getId();
            if (savedAttendance.containsKey(studentId)) {
                Boolean isPresent = savedAttendance.get(studentId);
                if (isPresent != null && item.isPresent() != isPresent) {
                    item.setPresent(isPresent);
                    notifyItemChanged(i);
                }
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView studentNameTextView;
        final CheckBox presentCheckBox;

        public ViewHolder(View itemView) {
            super(itemView);
            studentNameTextView = itemView.findViewById(R.id.student_name);
            presentCheckBox = itemView.findViewById(R.id.present_checkbox);
        }
    }
}
