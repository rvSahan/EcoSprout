package lk.javainstitute.ecosprout.ui.userManagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;

import lk.javainstitute.ecosprout.ProductAdapter;
import lk.javainstitute.ecosprout.ProductClass;
import lk.javainstitute.ecosprout.R;
import lk.javainstitute.ecosprout.UpdateProductActivity;
import lk.javainstitute.ecosprout.User;
import lk.javainstitute.ecosprout.User_Adapter;
import lk.javainstitute.ecosprout.databinding.FragmentUsermanagementBinding;

public class UserManagementFragment extends Fragment {

    private FirebaseFirestore db;

    RecyclerView recyclerView;

    private FragmentUsermanagementBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        UserManagementViewModel userManagementViewModel =
                new ViewModelProvider(this).get(UserManagementViewModel.class);

        binding = FragmentUsermanagementBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerView3;
       recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

       db = FirebaseFirestore.getInstance();

       loadAllUsers();

        SearchView searchView = binding.searchView;
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    searchUsers(query);
                } else {
                    loadAllUsers(); // Load all if search is empty
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadAllUsers(); // Also load all products when clearing the text
                }
                return false; // Optional: live search while typing
            }
        });





        return root;
    }

    private void loadAllUsers() {
        db.collection("user")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<User> userArrayList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User userClass = new User();
                            userClass.setFname(document.getString("fname"));
                            userClass.setLname(document.getString("lname"));
                            userClass.setStatus(document.getString("status"));
                            userClass.setMobile(document.getString("mobile"));
                            userClass.setId(document.getId());
                            userArrayList.add(userClass);
                        }
                        User_Adapter userAdapter = new User_Adapter(userArrayList);
                        userAdapter.setOnitemClickListener(new User_Adapter.OnitemClickListener() {
                            @Override
                            public void onClick(User u) {
                                Gson gson = new Gson();
                                String p = gson.toJson(u);
                                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                                View userStatus = layoutInflater.inflate(R.layout.user_status, null);
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setView(userStatus);
                                AlertDialog userStatusAlertDialog = builder.create();
                                userStatusAlertDialog.show();

                                RadioButton active = userStatus.findViewById(R.id.radioButton3);
                                RadioButton inactive = userStatus.findViewById(R.id.radioButton4);

                                if(u.getStatus().equals("Active")){
                                    active.setChecked(true);
                                }else{
                                    inactive.setChecked(true);

                                }
                                Button b1 = userStatus.findViewById(R.id.button22);
                                b1.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        userStatusAlertDialog.dismiss();
                                    }
                                });
                                Button b2 = userStatus.findViewById(R.id.button23);
                                b2.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (active.isChecked()) {
                                            db.collection("user").document(u.getId()).update("status", "Active");
                                            Toast.makeText(getActivity(), "User Status Updated", Toast.LENGTH_SHORT).show();
                                            userStatusAlertDialog.dismiss();
                                        }
                                        if (inactive.isChecked()) {
                                            db.collection("user").document(u.getId()).update("status", "Inactive");
                                            Toast.makeText(getActivity(), "User Status Updated", Toast.LENGTH_SHORT).show();
                                            userStatusAlertDialog.dismiss();
                                        }
                                        loadAllUsers();
                                    }



                                });

                            }
                        });
                        recyclerView.setAdapter(userAdapter);
                    } else {
                        Log.e("FirestoreError", "Error getting documents: ", task.getException());
                    }
                });

    }

    private void searchUsers(String query) {
        db.collection("user")
                .orderBy("mobile") // Make sure 'title' is indexed in Firestore
                .startAt(query)
                .endAt(query + "\uf8ff") // Firestore text search
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<User> userArrayList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User userClass = new User();
                            userClass.setFname(document.getString("fname"));
                            userClass.setLname(document.getString("lname"));
                            userClass.setStatus(document.getString("status"));
                            userClass.setMobile(document.getString("mobile"));
                            userClass.setId(document.getId());
                            userArrayList.add(userClass);
                        }

                        User_Adapter userAdapter = new User_Adapter(userArrayList);
                        userAdapter.setOnitemClickListener(new User_Adapter.OnitemClickListener() {
                            @Override
                            public void onClick(User u) {
                                Gson gson = new Gson();
                                String p = gson.toJson(u);
                                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                                View userStatus = layoutInflater.inflate(R.layout.user_status, null);
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setView(userStatus);
                                AlertDialog userStatusAlertDialog = builder.create();
                                userStatusAlertDialog.show();

                                RadioButton active = userStatus.findViewById(R.id.radioButton3);
                                RadioButton inactive = userStatus.findViewById(R.id.radioButton4);

                                if(u.getStatus().equals("Active")){
                                    active.setChecked(true);
                                }else{
                                    inactive.setChecked(true);

                                }
                                Button b1 = userStatus.findViewById(R.id.button22);
                                b1.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        userStatusAlertDialog.dismiss();
                                    }
                                });
                                Button b2 = userStatus.findViewById(R.id.button23);
                                b2.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (active.isChecked()) {
                                            db.collection("user").document(u.getId()).update("status", "Active");
                                            Toast.makeText(getActivity(), "User Status Updated", Toast.LENGTH_SHORT).show();
                                            userStatusAlertDialog.dismiss();
                                        }
                                        if (inactive.isChecked()) {
                                            db.collection("user").document(u.getId()).update("status", "Inactive");
                                            Toast.makeText(getActivity(), "User Status Updated", Toast.LENGTH_SHORT).show();
                                            userStatusAlertDialog.dismiss();
                                        }
                                        loadAllUsers();
                                    }



                                });

                            }
                        });
                        recyclerView.setAdapter(userAdapter);
                    } else {
                        Log.e("FirestoreError", "Error getting documents: ", task.getException());
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}