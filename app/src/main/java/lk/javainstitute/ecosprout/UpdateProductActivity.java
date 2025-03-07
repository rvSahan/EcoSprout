package lk.javainstitute.ecosprout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

public class UpdateProductActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        String product = getIntent().getStringExtra("product");
        Gson gson = new Gson();
        ProductClass productClass = gson.fromJson(product, ProductClass.class);

        TextView textView = findViewById(R.id.textView59);
        textView.setText(productClass.getName());

        EditText editText = findViewById(R.id.editTextNumber);
        editText.setText(productClass.getPrice());

        EditText editText2 = findViewById(R.id.editTextTextMultiLine);
        editText2.setText(productClass.getDescription());

        EditText editText3 = findViewById(R.id.editTextNumber2);
        editText3.setText(productClass.getQty());

        if(productClass.getStatus().equals("Active")){
            RadioButton radioButton = findViewById(R.id.radioButton2);
            radioButton.setChecked(true);
        }else{
           RadioButton radioButton = findViewById(R.id.radioButton);
           radioButton.setChecked(true);

        }

        Button button = findViewById(R.id.button19);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editText.getText().toString().isEmpty() || editText2.getText().toString().isEmpty() || editText3.getText().toString().isEmpty()){
                    textView.setText("Please fill all the fields");
                }else{
                    productClass.setPrice(editText.getText().toString());
                    productClass.setDescription(editText2.getText().toString());
                    productClass.setQty(editText3.getText().toString());
                    productClass.setName(textView.getText().toString());
                    RadioButton radioButton = findViewById(R.id.radioButton2);
                    if(radioButton.isChecked()){
                        productClass.setStatus("Active");
                    }else{
                        productClass.setStatus("Inactive");
                    }
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("product").document(productClass.getId()).set(productClass);

                    Toast.makeText(UpdateProductActivity.this, "Product Updated Successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(UpdateProductActivity.this, AdminActivity.class);
                    startActivity(intent);




                }
            }
        });


    }
}