package lk.javainstitute.ecosprout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;




import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import java.util.concurrent.Executor;


public class LogIn extends AppCompatActivity {





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sp = getSharedPreferences("lk.javainstitute.ecosprout", Context.MODE_PRIVATE);

        if(!sp.contains("user")){



            EditText mobile = findViewById(R.id.editTextPhone);
            EditText password = findViewById(R.id.editTextTextPassword);

            Button b1 = findViewById(R.id.button);
            b1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mobile.getText().toString().isEmpty()) {
                        CharSequence text = "Please Enter Mobile";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(LogIn.this, text, duration);
                        toast.show();
                    } else if (!Validation.validateMobileNumber(mobile.getText().toString())) {
                        CharSequence text = "Please Enter Valid Mobile";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(LogIn.this, text, duration);
                        toast.show();
                    } else if (password.getText().toString().isEmpty()) {
                        CharSequence text = "Please Enter Password";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(LogIn.this, text, duration);
                        toast.show();

                    } else if (!Validation.validatePassword(password.getText().toString())) {
                        CharSequence text = "Please Enter Valid Password";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(LogIn.this, text, duration);
                        toast.show();

                    } else {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("admin").where(Filter.equalTo("mobile", mobile.getText().toString())).where(Filter.equalTo("password", password.getText().toString())).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.getResult().isEmpty()){

                                    db.collection("user").where(Filter.equalTo("mobile", mobile.getText().toString())).where(Filter.equalTo("password", password.getText().toString())).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                            if (task.getResult().isEmpty()) {
                                                CharSequence text = "Invalid Credentials";
                                                int duration = Toast.LENGTH_SHORT;
                                                Toast toast = Toast.makeText(LogIn.this, text, duration);
                                                toast.show();
                                            } else {

                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    if(document.getString("status").equals("Inactive")){
                                                        CharSequence text = "Your Account Is Inactive";
                                                        int duration = Toast.LENGTH_SHORT;
                                                        Toast toast = Toast.makeText(LogIn.this, text, duration);
                                                        toast.show();

                                                    }else{
                                                        CharSequence text = "Login Successful";
                                                        int duration = Toast.LENGTH_SHORT;
                                                        Toast toast = Toast.makeText(LogIn.this, text, duration);
                                                        toast.show();
                                                        FirebaseMessaging.getInstance().getToken()
                                                                .addOnCompleteListener(new OnCompleteListener<String>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<String> task) {
                                                                        if (!task.isSuccessful()) {
                                                                            Log.w("FCM", "Fetching FCM token failed", task.getException());
                                                                            return;
                                                                        }
                                                                        String token = task.getResult();
                                                                        db.collection("user").document(document.getId())
                                                                                .update("fcmToken", token);
                                                                    }
                                                                });

                                                        User user = new User();
                                                        user.setId(document.getId());
                                                        user.setFname(document.getString("fname"));
                                                        user.setLname(document.getString("lname"));
                                                        user.setMobile(document.getString("mobile"));
                                                        user.setEmail(document.getString("email"));
                                                        user.setCity(document.getString("city"));
                                                        user.setLine1(document.getString("line1"));
                                                        user.setLine2(document.getString("line2"));
                                                        user.setImage(document.getString("image"));


                                                        Gson gson = new Gson();
                                                        String userBean = gson.toJson(user);

                                                        SharedPreferences sp = getSharedPreferences("lk.javainstitute.ecosprout", Context.MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = sp.edit();
                                                        editor.putString("user", userBean);
                                                        editor.apply();

                                                        Intent intent = new Intent(LogIn.this, ActivityHome.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);

                                                    }

                                                }
                                            }

                                        }
                                    });




                                }else{

                                    for(QueryDocumentSnapshot document : task.getResult()) {

//                                        Admin admin = new Admin();
//                                        admin.setId(document.getId());
//                                        admin.setFname(document.getString("fname"));
//                                        admin.setLname(document.getString("lname"));
//                                        admin.setMobile(document.getString("mobile"));
//                                        admin.setEmail(document.getString("email"));
//
//                                        Gson gson = new Gson();
//                                        String adminBean = gson.toJson(admin);
//
//                                        SharedPreferences sp = getSharedPreferences("lk.javainstitute.ecosprout", Context.MODE_PRIVATE);
//                                        SharedPreferences.Editor editor = sp.edit();
//                                        editor.putString("admin",adminBean);
//                                        editor.apply();
//
//                                        Intent intent = new Intent(LogIn.this, AdminActivity.class);
//                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                        startActivity(intent);







                                        CharSequence text = "Admin Login Successful";
                                        int duration = Toast.LENGTH_SHORT;
                                        Toast toast = Toast.makeText(LogIn.this, text, duration);
                                        toast.show();

                                        LayoutInflater layoutInflater = LayoutInflater.from(LogIn.this);
                                        View adminAlertView = layoutInflater.inflate(R.layout.admin_verfication, null);
                                        AlertDialog.Builder builder = new AlertDialog.Builder(LogIn.this);
                                        builder.setView(adminAlertView);
                                        AlertDialog adminAlertDialog = builder.create();
                                        adminAlertDialog.show();

                                        int code1 = (int) (Math.random() * 1000000);


                                        JavaMailAPI javaMailAPI = new JavaMailAPI(document.getString("email"), "Admin Verification Code", "Your Verification Code : " + code1 + "");
                                        javaMailAPI.execute();

                                        EditText validationCode = adminAlertView.findViewById(R.id.editTextText6);

                                        Button b3 = adminAlertView.findViewById(R.id.button15);
                                        b3.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                adminAlertDialog.dismiss();
                                            }
                                        });

                                        Button b4 = adminAlertView.findViewById(R.id.button14);
                                        b4.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if(validationCode.getText().toString().equals(String.valueOf(code1))){

                                                    Admin admin = new Admin();
                                                    admin.setId(document.getId());
                                                    admin.setFname(document.getString("fname"));
                                                    admin.setLname(document.getString("lname"));
                                                    admin.setMobile(document.getString("mobile"));
                                                    admin.setEmail(document.getString("email"));

                                                    Gson gson = new Gson();
                                                    String adminBean = gson.toJson(admin);

                                                    SharedPreferences sp = getSharedPreferences("lk.javainstitute.ecosprout", Context.MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = sp.edit();
                                                    editor.putString("admin",adminBean);
                                                    editor.apply();

                                                    Intent intent = new Intent(LogIn.this, AdminActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);







                                                    CharSequence text = "Login Successful";
                                                    int duration = Toast.LENGTH_SHORT;
                                                    Toast toast = Toast.makeText(LogIn.this, text, duration);
                                                    toast.show();
                                                }else {
                                                    validationCode.setError("Invalid Code");
                                                }


                                            }
                                        });

                                    }




                                }


                            }
                        });


                    }
                }
            });

            Button b2 = findViewById(R.id.button3);
            b2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Runs in the background

                    Intent intent = new Intent(LogIn.this, SignUpActivity.class);
                    startActivity(intent);
                }
            });

            Button b5 = findViewById(R.id.button5);
            b5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (mobile.getText().toString().isEmpty()) {
                        CharSequence text = "Please Enter Mobile";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(LogIn.this, text, duration);
                        toast.show();
                    } else if (!Validation.validateMobileNumber(mobile.getText().toString())) {
                        CharSequence text = "Please Enter Valid Mobile";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(LogIn.this, text, duration);
                        toast.show();
                    } else {




                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("user").whereEqualTo("mobile", mobile.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (!task.getResult().isEmpty()) {
                                    CharSequence text = "Please Check Your Email and Enter the validation code. Then reset the password";
                                    int duration = Toast.LENGTH_SHORT;
                                    Toast toast = Toast.makeText(LogIn.this, text, duration);
                                    toast.show();

                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        LayoutInflater layoutInflater = LayoutInflater.from(LogIn.this);
                                        View forgotPasswordAlertView = layoutInflater.inflate(R.layout.forget_password, null);

                                        AlertDialog.Builder builder = new AlertDialog.Builder(LogIn.this);
                                        builder.setView(forgotPasswordAlertView);
                                        AlertDialog resetPasswordAlertDialog = builder.create();
                                        resetPasswordAlertDialog.show();

                                        int code = (int) (Math.random() * 1000000);



                                        JavaMailAPI javaMailAPI = new JavaMailAPI( document.getString("email"), "Password Reset Validation Code", "Your Verification Code : " + code + "");
                                        javaMailAPI.execute();

                                        EditText validationCode = forgotPasswordAlertView.findViewById(R.id.editTextText3);
                                        EditText newPassword = forgotPasswordAlertView.findViewById(R.id.editTextTextPassword3);
                                        EditText confirmPassword = forgotPasswordAlertView.findViewById(R.id.editTextTextPassword5);
                                        Button b6 = forgotPasswordAlertView.findViewById(R.id.button7);
                                        b6.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if (validationCode.getText().toString().isEmpty()) {

                                                    CharSequence text = "Please Enter Validation Code";
                                                    int duration = Toast.LENGTH_SHORT;
                                                    Toast toast = Toast.makeText(LogIn.this, text, duration);
                                                    toast.show();
                                                }else if(validationCode.getText().toString().equals(String.valueOf(code))){
                                                    CharSequence text = "Invalid Validation Code";
                                                    int duration = Toast.LENGTH_SHORT;
                                                    Toast toast = Toast.makeText(LogIn.this, text, duration);
                                                    toast.show();

                                                } else if (newPassword.getText().toString().isEmpty()) {

                                                    CharSequence text = "Please Enter New Password";
                                                    int duration = Toast.LENGTH_SHORT;
                                                    Toast toast = Toast.makeText(LogIn.this, text, duration);
                                                    toast.show();

                                                } else if(!Validation.validatePassword(newPassword.getText().toString())){

                                                    CharSequence text = "Please Enter Valid Password";
                                                    int duration = Toast.LENGTH_SHORT;
                                                    Toast toast = Toast.makeText(LogIn.this, text, duration);
                                                    toast.show();
                                                } else if (confirmPassword.getText().toString().isEmpty()) {

                                                    CharSequence text = "Please Enter Confirm Password";
                                                    int duration = Toast.LENGTH_SHORT;
                                                    Toast toast = Toast.makeText(LogIn.this, text, duration);
                                                    toast.show();
                                                } else if(!newPassword.getText().toString().equals(confirmPassword.getText().toString())){

                                                    CharSequence text = "Password Does Not Match";
                                                    int duration = Toast.LENGTH_SHORT;
                                                    Toast toast = Toast.makeText(LogIn.this, text, duration);
                                                    toast.show();
                                                } else {

                                                    if(validationCode.getText().toString().equals(String.valueOf(code))){

                                                        db.collection("user").document(document.getId()).update("password", newPassword.getText().toString());
                                                        CharSequence text = "Password Reset Successful";
                                                        int duration = Toast.LENGTH_SHORT;
                                                        Toast toast = Toast.makeText(LogIn.this, text, duration);
                                                        toast.show();
                                                        resetPasswordAlertDialog.dismiss();


                                                    }else{

                                                        CharSequence text = "Invalid Validation Code";
                                                        int duration = Toast.LENGTH_SHORT;
                                                        Toast toast = Toast.makeText(LogIn.this, text, duration);
                                                        toast.show();
                                                    }

                                                }


                                            }
                                        });
                                        Button b7 = forgotPasswordAlertView.findViewById(R.id.button8);
                                        b7.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                resetPasswordAlertDialog.dismiss();
                                            }
                                        });

                                    }




                                }else{
                                    CharSequence text = "Invalid Credentials";
                                    int duration = Toast.LENGTH_SHORT;
                                    Toast toast = Toast.makeText(LogIn.this, text, duration);
                                    toast.show();
                                }



                            }
                        });

                    }
                }
            });

        }else{

            String u = sp.getString("user",null);
            Gson gson = new Gson();
            User user = gson.fromJson(u,User.class);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("user").whereEqualTo("mobile",user.getMobile()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                   for (QueryDocumentSnapshot document : task.getResult()) {
                       if(document.getString("status").equals("Inactive")){
                       CharSequence text = "Your Account Is Inactive";
                       int duration = Toast.LENGTH_SHORT;
                       Toast toast = Toast.makeText(LogIn.this, text, duration);
                       toast.show();
                       }else {

                           CharSequence text = "Login Successful";
                           int duration = Toast.LENGTH_SHORT;
                           Toast toast = Toast.makeText(LogIn.this, text, duration);
                           toast.show();

                           Intent intent = new Intent(LogIn.this, ActivityHome.class);
                           intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                           startActivity(intent);

                       }


                   }
                }
            });

        }



    }

}