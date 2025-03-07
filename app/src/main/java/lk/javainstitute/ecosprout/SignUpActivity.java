package lk.javainstitute.ecosprout;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ktx.Firebase;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        EditText mobile = findViewById(R.id.editTextPhone5);
        EditText firstName = findViewById(R.id.editTextText);
        EditText lastName = findViewById(R.id.editTextText2);
        EditText password = findViewById(R.id.editTextTextPassword2);
        EditText confirmPassword = findViewById(R.id.editTextTextPassword4);
        EditText email = findViewById(R.id.editTextTextEmailAddress);

        Button b1 = findViewById(R.id.button2);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    if(mobile.getText().toString().isEmpty()){
                        CharSequence text = "Please Enter Mobile";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                        toast.show();
                    }else if(!Validation.validateMobileNumber(mobile.getText().toString())){
                        CharSequence text = "Please Enter Valid Mobile";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                        toast.show();
                    }else if(firstName.getText().toString().isEmpty()){
                        CharSequence text = "Please Enter First Name";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                        toast.show();

                    }else if(!Validation.validateName(firstName.getText().toString())){
                        CharSequence text = "Please Enter Valid First Name";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                        toast.show();
                    } else if (lastName.getText().toString().isEmpty()) {
                        CharSequence text = "Please Enter Last Name";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                        toast.show();

                    }else if(!Validation.validateName(lastName.getText().toString())){
                        CharSequence text = "Please Enter Valid Last Name";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                        toast.show();
                    }else if(email.getText().toString().isEmpty()){
                        CharSequence text = "Please Enter Email";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                        toast.show();

                    }else if(!Validation.validateEmail(email.getText().toString())){
                        CharSequence text = "Please Enter Valid Email";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                        toast.show();
                    }else if(password.getText().toString().isEmpty()){
                        CharSequence text = "Please Enter Password";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                        toast.show();

                    }else if(!Validation.validatePassword(password.getText().toString())){
                        CharSequence text = "Please Enter Valid Password";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                        toast.show();

                    }else if(confirmPassword.getText().toString().isEmpty()){
                        CharSequence text = "Please Enter Confirm Password";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                        toast.show();

                    }else if(!password.getText().toString().equals(confirmPassword.getText().toString())){
                        CharSequence text = "Please Enter Same Password";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                        toast.show();

                    }else{

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("user").whereEqualTo("mobile", mobile.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.getResult().isEmpty()){
                                    //Log.i("TAG", "onComplete:");
                                    HashMap<String, Object> user = new HashMap<>();
                                    user.put("fname", firstName.getText().toString());
                                    user.put("lname", lastName.getText().toString());
                                    user.put("mobile", mobile.getText().toString());
                                    user.put("password",password.getText().toString());
                                    user.put("email",email.getText().toString());
                                    user.put("status","Active");
                                    db.collection("user").add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {

                                            CharSequence text = "User Registered Successfully";
                                            int duration = Toast.LENGTH_SHORT;

                                            Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                                            toast.show();

                                            Intent intent = new Intent(SignUpActivity.this, LogIn.class);
                                            startActivity(intent);



                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            CharSequence text = "User Registration Failed";
                                            int duration = Toast.LENGTH_SHORT;

                                            Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                                            toast.show();
                                        }
                                    });
                                }else{
                                    CharSequence text = "This Mobile Has Already Registered";
                                    int duration = Toast.LENGTH_SHORT;

                                    Toast toast = Toast.makeText(SignUpActivity.this, text, duration);
                                    toast.show();
                                }
                            }
                        });
                    }

            }
        });


        Button b4 = findViewById(R.id.button4);
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, LogIn.class);
                startActivity(intent);
            }
        });
    }
}