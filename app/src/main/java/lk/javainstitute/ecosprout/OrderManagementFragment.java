package lk.javainstitute.ecosprout;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SearchView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrderManagementFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderManagementFragment extends Fragment {
    FirebaseFirestore db;

    ArrayList<Order> orders;

    RecyclerView recyclerView;




    public static OrderManagementFragment newInstance(String param1, String param2) {
        OrderManagementFragment fragment = new OrderManagementFragment();
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

        View view = inflater.inflate(R.layout.fragment_order_management, container, false);

         recyclerView = view.findViewById(R.id.recyclerView4);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));



         orders = new ArrayList<>();

         db = FirebaseFirestore.getInstance();

         loadAllOrders();

         SearchView searchView = view.findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    searchUsers(query);
                } else {
                    loadAllOrders(); // Load all if search is empty
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadAllOrders(); // Also load all products when clearing the text
                }
                return false; // Optional: live search while typing
            }
        });







        // Inflate the layout for this fragment
        return view;
    }

    public void loadAllOrders(){
        db.collection("order")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            orders.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                orders.add(document.toObject(Order.class));
                            }

                            OrderManagementAdapter orderAdapter = new OrderManagementAdapter(orders);
                            orderAdapter.setOnitemClickListener(new OrderManagementAdapter.OnitemClickListener() {
                                @Override
                                public void onClick(Order o) {
                                    Gson gson = new Gson();
                                    String p = gson.toJson(o);
                                    LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                                    View userStatus = layoutInflater.inflate(R.layout.order_status, null);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setView(userStatus);
                                    AlertDialog orderStatusAlertDialog = builder.create();
                                    orderStatusAlertDialog.show();

                                    RadioButton pending = userStatus.findViewById(R.id.radioButton5);
                                    RadioButton delivering = userStatus.findViewById(R.id.radioButton6);
                                    RadioButton completed = userStatus.findViewById(R.id.radioButton7);

                                    if(o.getStatus().equals("Pending")){
                                        pending.setChecked(true);
                                    }
                                    if(o.getStatus().equals("Delivering")){
                                        delivering.setChecked(true);
                                    }
                                    if(o.getStatus().equals("Completed")) {
                                        completed.setChecked(true);
                                    }

                                    Button b1 = userStatus.findViewById(R.id.button24);
                                    b1.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            orderStatusAlertDialog.dismiss();
                                        }
                                    });

                                    Button b2 = userStatus.findViewById(R.id.button25);
                                    b2.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (pending.isChecked()) {
                                                db.collection("order").document(o.getOrderId()).update("status", "Pending");
                                                orderStatusAlertDialog.dismiss();

                                            }
                                            if (delivering.isChecked()) {
                                                db.collection("order").document(o.getOrderId()).update("status", "Delivering");
                                                orderStatusAlertDialog.dismiss();
                                            }
                                            if (completed.isChecked()) {
                                                db.collection("order").document(o.getOrderId()).update("status", "Completed");

                                                orderStatusAlertDialog.dismiss();
                                            }
                                            loadAllOrders();

                                        }
                                    });



                                }
                            });
                            recyclerView.setAdapter(orderAdapter);





                        }
                    }
                });

    }
    private void searchUsers(String query) {
        db.collection("order")
                .orderBy("orderId")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Order> orderArrayList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            orderArrayList.add(document.toObject(Order.class));
                        }

                        OrderManagementAdapter orderAdapter = new OrderManagementAdapter(orderArrayList);
                        orderAdapter.setOnitemClickListener(new OrderManagementAdapter.OnitemClickListener() {
                            @Override
                            public void onClick(Order o) {
                                Gson gson = new Gson();
                                String p = gson.toJson(o);
                                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                                View userStatus = layoutInflater.inflate(R.layout.order_status, null);
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setView(userStatus);
                                AlertDialog orderStatusAlertDialog = builder.create();
                                orderStatusAlertDialog.show();

                                RadioButton pending = userStatus.findViewById(R.id.radioButton5);
                                RadioButton delivering = userStatus.findViewById(R.id.radioButton6);
                                RadioButton completed = userStatus.findViewById(R.id.radioButton7);

                                if(o.getStatus().equals("Pending")){
                                    pending.setChecked(true);
                                }
                                if(o.getStatus().equals("Delivering")){
                                    delivering.setChecked(true);
                                }
                                if(o.getStatus().equals("Completed")) {
                                    completed.setChecked(true);
                                }

                                Button b1 = userStatus.findViewById(R.id.button24);
                                b1.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        orderStatusAlertDialog.dismiss();
                                    }
                                });

                                Button b2 = userStatus.findViewById(R.id.button25);
                                b2.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (pending.isChecked()) {
                                            db.collection("order").document(o.getOrderId()).update("status", "Pending");
                                            orderStatusAlertDialog.dismiss();

                                        }
                                        if (delivering.isChecked()) {
                                            db.collection("order").document(o.getOrderId()).update("status", "Delivering");
                                            orderStatusAlertDialog.dismiss();
                                        }
                                        if (completed.isChecked()) {
                                            db.collection("order").document(o.getOrderId()).update("status", "Completed");
                                            orderStatusAlertDialog.dismiss();
                                        }
                                        loadAllOrders();

                                    }
                                });



                            }
                        });
                        recyclerView.setAdapter(orderAdapter);
                    } else {
                        Log.e("FirestoreError", "Error getting documents: ", task.getException());
                    }
                });
    }
}