package lk.javainstitute.ecosprout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class AddProductActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView imageView;

    private ProductClass product;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                         uri = result.getData().getData();
                        if (uri != null) {
                            imageView.setImageURI(uri);
                            //saveImageToFirestore(uri);
                        }
                    }
                }
        );
        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(v -> {
            ImagePicker.Companion.with(AddProductActivity.this)
                    .crop()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .galleryOnly()
                    .createIntent(intent -> {
                        imagePickerLauncher.launch(intent);
                        return null;
                    });
        });

        EditText editTextText7 = findViewById(R.id.editTextText7);
        EditText editTextTextMultiLine2 = findViewById(R.id.editTextTextMultiLine2);
        EditText editTextNumber3 = findViewById(R.id.editTextNumber3);
        EditText editTextNumber4 = findViewById(R.id.editTextNumber4);

        ArrayList<String> items = new ArrayList<>();
        Spinner spinner = findViewById(R.id.spinner);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("city").orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            items.add(document.getString("name"));
                        }
                        CityAdapter adapter = new CityAdapter(AddProductActivity.this,R.layout.city_template, items);
                        spinner.setAdapter(adapter);


                    }
                });

        ArrayList<String> item = new ArrayList<>();
        Spinner spinner1 = findViewById(R.id.spinner2);
        FirebaseFirestore db1 = FirebaseFirestore.getInstance();
        db1.collection("categories").orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            item.add(document.getString("name"));
                        }
                        CityAdapter adapter = new CityAdapter(AddProductActivity.this,R.layout.city_template, item);
                        spinner1.setAdapter(adapter);


                    }
                });

        Button button2 = findViewById(R.id.button21);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editTextText7.getText().toString().isEmpty()){
                    editTextText7.setError("Enter Product Title");
                }else if(editTextTextMultiLine2.getText().toString().isEmpty()){
                    editTextTextMultiLine2.setError("Enter Product Description");

                } else if(editTextNumber3.getText().toString().isEmpty()){
                    editTextNumber3.setError("Enter Product Price");

                } else if(editTextNumber4.getText().toString().isEmpty()){
                    editTextNumber4.setError("Enter Product Qty");

                }else{
                    String productId = String.valueOf(System.currentTimeMillis());
                    product = new ProductClass();
                    product.setName(editTextText7.getText().toString());
                    product.setDescription(editTextTextMultiLine2.getText().toString());
                    product.setPrice(editTextNumber3.getText().toString());
                    product.setQty(editTextNumber4.getText().toString());
                    product.setId(productId);
                    product.setStatus("Active");
                    product.setCity(spinner.getSelectedItem().toString());
                    product.setCategory(spinner1.getSelectedItem().toString());
                    product.setDate_time(Timestamp.now());

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("product").document(productId).set(product);

                    try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();


                        String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                        product.setImage(base64Image);




                        FirebaseFirestore.getInstance().collection("product").document(productId)
                                .update("image", base64Image)
                                .addOnSuccessListener(aVoid -> Toast.makeText(AddProductActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(AddProductActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(AddProductActivity.this, "Error saving image", Toast.LENGTH_SHORT).show();
                    }

                    Intent intent = new Intent(AddProductActivity.this, AdminActivity.class);
                    startActivity(intent);



                }
            }
        });



    }
}