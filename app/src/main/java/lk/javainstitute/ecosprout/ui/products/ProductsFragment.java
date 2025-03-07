package lk.javainstitute.ecosprout.ui.products;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;

import lk.javainstitute.ecosprout.AddProductActivity;
import lk.javainstitute.ecosprout.CityAdapter;
import lk.javainstitute.ecosprout.ProductAdapter;
import lk.javainstitute.ecosprout.ProductClass;
import lk.javainstitute.ecosprout.R;
import lk.javainstitute.ecosprout.SingleProductViewActivity;
import lk.javainstitute.ecosprout.databinding.FragmentProductsBinding;

public class ProductsFragment extends Fragment {

    private FragmentProductsBinding binding;
    private RecyclerView recyclerView2;
    private FirebaseFirestore bd;

    String city,category;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProductsViewModel productsViewModel =
                new ViewModelProvider(this).get(ProductsViewModel.class);

        binding = FragmentProductsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ArrayList<String> items = new ArrayList<>();
        items.add("Select");
        Spinner spinner = root.findViewById(R.id.spinner4);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("city").orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            items.add(document.getString("name"));
                        }
                        CityAdapter adapter = new CityAdapter(getActivity(),R.layout.city_template, items);
                        spinner.setAdapter(adapter);


                    }
                });

        ArrayList<String> item = new ArrayList<>();
        item.add("Select");
        Spinner spinner1 = root.findViewById(R.id.spinner3);
        FirebaseFirestore db1 = FirebaseFirestore.getInstance();
        db1.collection("categories").orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            item.add(document.getString("name"));
                        }
                        CityAdapter adapter = new CityAdapter(getActivity(),R.layout.city_template, item);
                        spinner1.setAdapter(adapter);


                    }

                });

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                category = item.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                city = items.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        EditText maxPrice = root.findViewById(R.id.editTextText9);
        EditText minPrice = root.findViewById(R.id.editTextText8);
        Button search = root.findViewById(R.id.button26);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Query query = db.collection("product");

                if (!category.isEmpty() && !category.equals("Select")) {
                    query = query.whereEqualTo("category", category);
                }
                if (!city.isEmpty() && !city.equals("Select")) {
                    query = query.whereEqualTo("city", city);
                }

                // Always filter by active status
                query = query.whereEqualTo("status", "Active");

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<ProductClass> productArrayList = new ArrayList<>();
                            double min = minPrice.getText().toString().isEmpty() ? 0 : Double.parseDouble(minPrice.getText().toString());
                            double max = maxPrice.getText().toString().isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxPrice.getText().toString());

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String priceStr = document.getString("price");

                                if (priceStr != null && !priceStr.isEmpty()) {
                                    try {
                                        double price = Double.parseDouble(priceStr);

                                        // Apply manual price range filtering
                                        if (price >= min && price <= max) {
                                            ProductClass productClass = new ProductClass();
                                            productClass.setName(document.getString("name"));
                                            productClass.setDescription(document.getString("description"));
                                            productClass.setPrice(priceStr); // Keep price as string for display
                                            productClass.setImage(document.getString("image"));
                                            productClass.setId(document.getId());
                                            productClass.setQty(document.getString("qty"));
                                            productClass.setCategory(document.getString("category"));
                                            productClass.setCity(document.getString("city"));
                                            productClass.setStatus(document.getString("status"));

                                            productArrayList.add(productClass);
                                        }
                                    } catch (NumberFormatException e) {
                                        Log.e("PriceError", "Invalid price format: " + priceStr);
                                    }
                                }
                            }

                            ProductAdapter productAdapter = new ProductAdapter(productArrayList);
                            productAdapter.setOnitemClickListener(c -> {
                                Gson gson = new Gson();
                                String p = gson.toJson(c);
                                startActivity(new Intent(getActivity(), SingleProductViewActivity.class)
                                        .putExtra("product", p));
                            });
                            recyclerView2.setAdapter(productAdapter);
                        } else {
                            Log.e("FirestoreError", "Error getting documents: ", task.getException());
                        }
                    }
                });
            }
        });



        recyclerView2 = binding.recyclerview2;
        recyclerView2.setLayoutManager(new GridLayoutManager(getContext(), 2));

        bd = FirebaseFirestore.getInstance();

        // Load all products initially
        loadAllProducts();

        // SearchView setup
        SearchView searchView = binding.searchView;
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

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
                .whereEqualTo("status", "Active")
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
                            productArrayList.add(productClass);
                        }

                        ProductAdapter productAdapter = new ProductAdapter(productArrayList);
                        productAdapter.setOnitemClickListener(c -> {
                            Gson gson = new Gson();
                            String p = gson.toJson(c);
                            startActivity(new Intent(getActivity(), SingleProductViewActivity.class)
                                    .putExtra("product", p));
                            Toast.makeText(getActivity(), c.getName(), Toast.LENGTH_SHORT).show();
                        });

                        recyclerView2.setAdapter(productAdapter);
                    } else {
                        Log.e("FirestoreError", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void searchProducts(String query) {
        bd.collection("product")
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
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
                            productClass.setQty(document.getString("qty"));
                            filteredList.add(productClass);
                        }

                        ProductAdapter productAdapter = new ProductAdapter(filteredList);
                        productAdapter.setOnitemClickListener(c -> {
                            Gson gson = new Gson();
                            String p = gson.toJson(c);
                            startActivity(new Intent(getActivity(), SingleProductViewActivity.class)
                                    .putExtra("product", p));
                            Toast.makeText(getActivity(), c.getName(), Toast.LENGTH_SHORT).show();
                        });

                        recyclerView2.setAdapter(productAdapter);
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
