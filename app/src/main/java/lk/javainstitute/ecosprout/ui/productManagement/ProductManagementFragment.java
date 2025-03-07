package lk.javainstitute.ecosprout.ui.productManagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;

import lk.javainstitute.ecosprout.AddCategoryActivity;
import lk.javainstitute.ecosprout.AddProductActivity;
import lk.javainstitute.ecosprout.ProductAdapter;
import lk.javainstitute.ecosprout.ProductClass;
import lk.javainstitute.ecosprout.SingleProductViewActivity;
import lk.javainstitute.ecosprout.UpdateProductActivity;
import lk.javainstitute.ecosprout.databinding.FragmentProductmanagementBinding;

public class ProductManagementFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseFirestore bd;

    private FragmentProductmanagementBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProductManagementViewModel productManagementViewModel =
                new ViewModelProvider(this).get(ProductManagementViewModel.class);

        binding = FragmentProductmanagementBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerview;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        bd = FirebaseFirestore.getInstance();

        loadAllProducts();

        SearchView searchView = binding.searchView;
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        Button button = binding.button20;
        button.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddProductActivity.class));
        });
        Button addcategory = binding.button29;
        addcategory.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddCategoryActivity.class);
            startActivity(intent);



        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    searchProducts(query);
                } else {
                    loadAllProducts(); // Load all if search is empty
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadAllProducts(); // Also load all products when clearing the text
                }
                return false; // Optional: live search while typing
            }
        });






        return root;
    }
    private void loadAllProducts() {
        bd.collection("product")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<ProductClass> productArrayList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ProductClass productClass = new ProductClass();
                            productClass.setName(document.getString("name"));
                            productClass.setDescription(document.getString("description"));
                            productClass.setPrice(document.getString("price"));
                            productClass.setImage(document.getString("image"));
                            productClass.setId(document.getId());
                            productClass.setQty(document.getString("qty"));
                            productClass.setStatus(document.getString("status"));
                            productArrayList.add(productClass);
                        }

                        ProductAdapter productAdapter = new ProductAdapter(productArrayList);
                        productAdapter.setOnitemClickListener(c -> {
                            Gson gson = new Gson();
                            String p = gson.toJson(c);
                            startActivity(new Intent(getActivity(), UpdateProductActivity.class)
                                    .putExtra("product", p));
                            Toast.makeText(getActivity(), c.getName(), Toast.LENGTH_SHORT).show();
                        });

                        recyclerView.setAdapter(productAdapter);
                    } else {
                        Log.e("FirestoreError", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void searchProducts(String query) {
        bd.collection("product")
                .orderBy("name") // Make sure 'title' is indexed in Firestore
                .startAt(query)
                .endAt(query + "\uf8ff") // Firestore text search
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<ProductClass> filteredList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ProductClass productClass = new ProductClass();
                            productClass.setName(document.getString("name"));
                            productClass.setDescription(document.getString("description"));
                            productClass.setPrice(document.getString("price"));
                            productClass.setImage(document.getString("image"));
                            productClass.setId(document.getId());
                            productClass.setStatus(document.getString("status"));
                            productClass.setQty(document.getString("qty"));
                            filteredList.add(productClass);
                        }

                        ProductAdapter productAdapter = new ProductAdapter(filteredList);
                        productAdapter.setOnitemClickListener(c -> {
                            Gson gson = new Gson();
                            String p = gson.toJson(c);
                            startActivity(new Intent(getActivity(), UpdateProductActivity.class)
                                    .putExtra("product", p));
                            Toast.makeText(getActivity(), c.getName(), Toast.LENGTH_SHORT).show();
                        });

                        recyclerView.setAdapter(productAdapter);
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