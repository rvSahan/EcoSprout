package lk.javainstitute.ecosprout.ui.home;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;

import lk.javainstitute.ecosprout.ContactUsActivity;
import lk.javainstitute.ecosprout.ImageActivity;
import lk.javainstitute.ecosprout.ImageAdapter;
import lk.javainstitute.ecosprout.LogIn;
import lk.javainstitute.ecosprout.ProductAdapter;
import lk.javainstitute.ecosprout.ProductClass;
import lk.javainstitute.ecosprout.R;
import lk.javainstitute.ecosprout.SingleProductViewActivity;
import lk.javainstitute.ecosprout.User;
import lk.javainstitute.ecosprout.databinding.FragmentHomeBinding;
import lk.javainstitute.ecosprout.ui.products.ProductsFragment;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedPreferences sp = getActivity().getSharedPreferences("lk.javainstitute.ecosprout", Context.MODE_PRIVATE);
        String u = sp.getString("user",null);
        Gson gson = new Gson();
        User user = gson.fromJson(u,User.class);
        TextView textView = binding.textView7;
        textView.setText(user.getFname()+" "+user.getLname());

        RecyclerView recyclerView = binding.RecycleView;

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("https://images.unsplash.com/photo-1457530378978-8bac673b8062?q=80&w=1470&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D");
        arrayList.add("https://images.unsplash.com/photo-1495908333425-29a1e0918c5f?q=80&w=1480&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D");
        arrayList.add("https://plus.unsplash.com/premium_photo-1680282695137-c57784ac2208?q=80&w=1470&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D");
        arrayList.add("https://plus.unsplash.com/premium_photo-1678652878787-e3d721cb50f5?q=80&w=1374&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D");

        ImageAdapter adapter = new ImageAdapter(getActivity(),arrayList);
        adapter.setOnitemClickListener(new ImageAdapter.OnitemClickListener() {
            @Override
            public void onClick(ImageView imageView, String url) {




                startActivity(new Intent(getActivity(), ImageActivity.class).putExtra("image", url), ActivityOptions.makeSceneTransitionAnimation(getActivity(), imageView, "image").toBundle());
            }
        });

        recyclerView.setAdapter(adapter);

        RecyclerView recyclerView2 = binding.RecycleView2;
        recyclerView2.setLayoutManager(new GridLayoutManager(getContext(), 2));
        ArrayList<ProductClass> productArrayList = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("product")
                .whereEqualTo("status", "Active") // Only fetch active products
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            productArrayList.clear();

                            ArrayList<ProductClass> tempList = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ProductClass productClass = new ProductClass();
                                productClass.setName(document.getString("name"));
                                productClass.setDescription(document.getString("description"));
                                productClass.setPrice(document.getString("price"));
                                productClass.setImage(document.getString("image"));
                                productClass.setId(document.getId());
                                productClass.setQty(document.getString("qty"));
                                productClass.setDate_time(document.getTimestamp("date_time")); // Assuming you have this field
                                tempList.add(productClass);
                            }

                            // Sort manually by date_time descending
                            Collections.sort(tempList, (a, b) -> b.getDate_time().compareTo(a.getDate_time()));

                            // Limit to the latest 4 products
                            productArrayList.addAll(tempList.subList(0, Math.min(tempList.size(), 4)));

                            ProductAdapter productAdapter = new ProductAdapter(productArrayList);
                            productAdapter.setOnitemClickListener(new ProductAdapter.OnitemClickListener() {
                                @Override
                                public void onClick(ProductClass c) {
                                    Gson gson = new Gson();
                                    String p = gson.toJson(c);
                                    startActivity(new Intent(getActivity(), SingleProductViewActivity.class).putExtra("product", p));
                                    Toast.makeText(getActivity(), c.getName(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            recyclerView2.setAdapter(productAdapter);
                        } else {
                            Log.e("FirestoreError", "Error getting documents: ", task.getException());
                        }
                    }
                });



        Button b6 = binding.button6;
        b6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_homeFragment_to_productsFragment);

            }
        });

       Button logout = binding.button13;
       logout.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {

               LayoutInflater inflater = LayoutInflater.from(getActivity());
               View layout = inflater.inflate(R.layout.logout_alert, null);
               AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
               alertDialog.setView(layout);
               AlertDialog alert = alertDialog.create();
               alert.show();

               Button cancel = layout.findViewById(R.id.button17);
               cancel.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       alert.dismiss();

                   }
               });

                Button logout = layout.findViewById(R.id.button16);
                logout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.remove("user"); // Or use editor.clear() if you want to clear everything
                        editor.apply();


                        Intent intent = new Intent(getActivity(), LogIn.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        getActivity().startActivity(intent);

                        Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();

                    }
                });






           }
       });

        Button contactUs = binding.button27;
        contactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), ContactUsActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });





        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}