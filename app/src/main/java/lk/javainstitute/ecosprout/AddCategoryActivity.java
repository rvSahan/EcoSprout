package lk.javainstitute.ecosprout;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class AddCategoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_category);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText editText = findViewById(R.id.editTextText10);
        editText.requestFocus();

        Button button = findViewById(R.id.button30);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().isEmpty()){
                    editText.setError("Field can't be empty");
                }else{
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("categories").whereEqualTo("name",editText.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.getResult().isEmpty()){
                                HashMap<String,Object> category = new HashMap<>();
                                category.put("name",editText.getText().toString());

                                db.collection("categories").add(category).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        editText.setText("");
                                        Toast.makeText(AddCategoryActivity.this, "Category Added Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }else{
                                Toast.makeText(AddCategoryActivity.this, "Category Already Exists", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            }
        });

    }
}