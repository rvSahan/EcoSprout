package lk.javainstitute.ecosprout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;

public class AdminFragment extends Fragment {

    private int activeUsers = 0;
    private int inactiveUsers = 0;
    private int pendingOrders = 0;
    private int completedOrders = 0;
    private int activeProducts = 0;
    private int inactiveProducts = 0;

    public static AdminFragment newInstance(String param1, String param2) {
        AdminFragment fragment = new AdminFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin, container, false);
        SharedPreferences sp = getActivity().getSharedPreferences("lk.javainstitute.ecosprout", Context.MODE_PRIVATE);
        String admin1 = sp.getString("admin", null);
        Gson gson = new Gson();
        Admin admin = gson.fromJson(admin1, Admin.class);

        TextView textView43 = view.findViewById(R.id.textView44);
        textView43.setText(admin.getFname() + " " + admin.getLname());

        BarChart barChart1 = view.findViewById(R.id.barChart1);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch and display active users count
        db.collection("user").whereEqualTo("status", "Active").get().addOnCompleteListener(task -> {
            activeUsers = task.getResult().size();
            TextView textView48 = view.findViewById(R.id.textView48);
            textView48.setText(String.valueOf("Active User Count: " + activeUsers));
            updateBarChart(barChart1);
        });



        // Fetch and display pending orders count
        db.collection("order").whereEqualTo("status", "Pending").get().addOnCompleteListener(task -> {
            pendingOrders = task.getResult().size();
            TextView textView51 = view.findViewById(R.id.textView49);
            textView51.setText(String.valueOf("Pending Orders: " + pendingOrders));
            updateBarChart(barChart1);
        });

        // Fetch and display inactive users count
        db.collection("user").whereEqualTo("status", "Inactive").get().addOnCompleteListener(task -> {
            inactiveUsers = task.getResult().size();
            TextView textView49 = view.findViewById(R.id.textView50);
            textView49.setText(String.valueOf("Inactive User Count: " + inactiveUsers));
            updateBarChart(barChart1);
        });

        // Fetch and display completed orders count
        db.collection("order").whereEqualTo("status", "Completed").get().addOnCompleteListener(task -> {
            completedOrders = task.getResult().size();
            TextView textView53 = view.findViewById(R.id.textView51);
            textView53.setText(String.valueOf("Completed Orders: " + completedOrders));
            updateBarChart(barChart1);
        });

        // Fetch and display active products count
        db.collection("product").whereEqualTo("status", "Active").get().addOnCompleteListener(task -> {
            activeProducts = task.getResult().size();
            TextView textView52 = view.findViewById(R.id.textView52);
            textView52.setText(String.valueOf("Active Products: " + activeProducts));
            updateBarChart(barChart1);
        });

        // Fetch and display inactive products count
        db.collection("product").whereEqualTo("status", "Inactive").get().addOnCompleteListener(task -> {
            inactiveProducts = task.getResult().size();
            TextView textView53 = view.findViewById(R.id.textView53);
            textView53.setText(String.valueOf("Inactive Products: " + inactiveProducts));
            updateBarChart(barChart1);
        });

        // Bar chart setup

        updateBarChart(barChart1);

        Button logout = view.findViewById(R.id.button18);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sp.edit();
                editor.remove("admin"); // Or use editor.clear() if you want to clear everything
                editor.apply();


                Intent intent = new Intent(getActivity(), LogIn.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getActivity().startActivity(intent);

                Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    // Method to update the bar chart
    private void updateBarChart(BarChart barChart) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, activeUsers));
        entries.add(new BarEntry(1, pendingOrders));
        entries.add(new BarEntry(2, inactiveUsers));
        entries.add(new BarEntry(3, completedOrders));
        entries.add(new BarEntry(4, activeProducts));
        entries.add(new BarEntry(5, inactiveProducts));

        BarDataSet dataSet = new BarDataSet(entries, "Admin Dashboard Stats");
        ArrayList<Integer> colorArrayList = new ArrayList<>();
        colorArrayList.add(getActivity().getColor(R.color.orange));
        colorArrayList.add(getActivity().getColor(R.color.dgreen));
        colorArrayList.add(getActivity().getColor(R.color.grey));
        colorArrayList.add(getActivity().getColor(R.color.lgreen));
        colorArrayList.add(getActivity().getColor(R.color.blue));
        colorArrayList.add(getActivity().getColor(R.color.red));
        dataSet.setColors(colorArrayList);
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        barChart.setData(data);
        barChart.invalidate(); // Refresh the chart

        // Set chart description
        Description description = new Description();
        description.setText("EcoSprout Admin Overview");
        barChart.setDescription(description);
        barChart.animateY(1000);
    }
}
