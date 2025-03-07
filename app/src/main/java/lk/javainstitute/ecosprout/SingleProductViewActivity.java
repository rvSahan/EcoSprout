package lk.javainstitute.ecosprout;

import static android.widget.Toast.makeText;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

public class SingleProductViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_single_product_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String product = getIntent().getStringExtra("product");
        Gson gson = new Gson();
        ProductClass productClass = gson.fromJson(product, ProductClass.class);
        ImageView imageView = findViewById(R.id.imageView4);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("product").document(productClass.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                    DocumentSnapshot document = task.getResult();
                    productClass.setImage(document.getString("image"));



                }

            }
        });
        TextView textView = findViewById(R.id.textView15);
        textView.setText(productClass.getName());
        TextView textView1 = findViewById(R.id.textView17);
        textView1.setText("Rs." + productClass.getPrice() + ".00");
        TextView textView2 = findViewById(R.id.textView19);
        textView2.setText(productClass.getDescription());

        String imageName = productClass.getImage();

        if (productClass.getImage() != null && !productClass.getImage().isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(productClass.getImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                imageView.setImageResource(R.drawable.picker);
            }
        } else {
            imageView.setImageResource(R.drawable.picker);
        }



        Button cancel = findViewById(R.id.button10);
        cancel.setOnClickListener(view -> {
            startActivity(new Intent(SingleProductViewActivity.this, ActivityHome.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });

        Button addToCart = findViewById(R.id.button9);
        EditText qtyInput = findViewById(R.id.editTextNumberDecimal);

        SharedPreferences sp = getSharedPreferences("lk.javainstitute.ecosprout", Context.MODE_PRIVATE);
        String userId = sp.getString("user", null);

        addToCart.setOnClickListener(view -> {
            String enteredQtyStr = qtyInput.getText().toString().trim();

            if (enteredQtyStr.isEmpty()) {
                Toast.makeText(SingleProductViewActivity.this, "Please enter a quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            float enteredQty;
            try {
                enteredQty = Float.parseFloat(enteredQtyStr);
                if (enteredQty <= 0) {
                    Toast.makeText(SingleProductViewActivity.this, "Quantity must be greater than zero", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(SingleProductViewActivity.this, "Invalid quantity entered", Toast.LENGTH_SHORT).show();
                return;
            }

            // Fetch available quantity from Firestore
            db.collection("product").document(productClass.getId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String availableQtyStr = documentSnapshot.getString("qty");

                            if (availableQtyStr != null && !availableQtyStr.isEmpty()) {
                                float availableQty;
                                try {
                                    availableQty = Float.parseFloat(availableQtyStr);
                                } catch (NumberFormatException e) {
                                    Toast.makeText(SingleProductViewActivity.this, "Invalid product quantity in database", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Ensure entered qty does not exceed available stock
                                if (enteredQty > availableQty) {
                                    qtyInput.setText(String.valueOf(availableQty));
                                    Toast.makeText(SingleProductViewActivity.this, "Max available quantity is " + availableQty, Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Retrieve user object
                                String u = sp.getString("user", null);
                                User user = gson.fromJson(u, User.class);

                                // Reference to cart item in Firestore
                                DocumentReference cartItemRef = db.collection("cart")
                                        .document(String.valueOf(user.getId()))
                                        .collection("items")
                                        .document(productClass.getId());

                                cartItemRef.get().addOnSuccessListener(cartSnapshot -> {
                                    int newQty = Math.round(enteredQty); // Convert to whole number

                                    if (cartSnapshot.exists()) {
                                        // Get existing quantity and add new qty
                                        String existingQtyStr = cartSnapshot.getString("qty");
                                        int existingQty = (existingQtyStr != null) ? Integer.parseInt(existingQtyStr) : 0;
                                        newQty += existingQty; // Sum up the qty

                                        // Ensure new quantity does not exceed stock
                                        if (newQty > availableQty) {
                                            newQty = (int) availableQty; // Store as integer
                                            Toast.makeText(SingleProductViewActivity.this, "Cart updated to max available: " + newQty, Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    // **Use CartItem class and store as an integer string**
                                    CartItem cartItem = new CartItem(
                                            productClass.getId(),
                                            productClass.getName(),
                                            productClass.getPrice(),
                                            productClass.getImage(),
                                            String.valueOf(newQty) // Convert integer to string before saving
                                    );

                                    cartItemRef.set(cartItem)
                                            .addOnSuccessListener(aVoid -> {

                                                Toast.makeText(SingleProductViewActivity.this, "Added to cart!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(SingleProductViewActivity.this, ActivityHome.class)
                                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(SingleProductViewActivity.this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                                                Log.e("FirestoreError", "Error adding to cart", e);
                                            });

                                });

                            } else {
                                Toast.makeText(SingleProductViewActivity.this, "Invalid product quantity", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(SingleProductViewActivity.this, "Product not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SingleProductViewActivity.this, "Failed to fetch product data", Toast.LENGTH_SHORT).show();
                        Log.e("FirestoreError", "Error fetching product data", e);
                    });
        });
    }
}
