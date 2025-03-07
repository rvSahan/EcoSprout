package lk.javainstitute.ecosprout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;



public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        FlingAnimation fling = new FlingAnimation(findViewById(R.id.imageView2), DynamicAnimation.TRANSLATION_Y);
        ImageView im =findViewById(R.id.imageView2);
        im.setVisibility(View.VISIBLE);
        fling.setStartVelocity(-1000f);
        fling.setFriction(0.2f);
        fling.start();

        new Handler().postDelayed(()->{

            if (!isNetworkAvailable()) {
                Toast.makeText(MainActivity.this, "No Internet. Please turn on mobile data.", Toast.LENGTH_SHORT).show();

                // Keep checking for internet connection
                checkInternetAndRestart();
            } else {
                proceedToNextActivity();
            }




        },2500);
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void checkInternetAndRestart() {
        new Handler().postDelayed(() -> {
            if (isNetworkAvailable()) {
                Toast.makeText(MainActivity.this, "Internet is back. Restarting app...", Toast.LENGTH_SHORT).show();
                recreate();
            } else {
                checkInternetAndRestart();
            }
        }, 3000);
    }
    private void proceedToNextActivity() {
                    SharedPreferences sp = getSharedPreferences("lk.javainstitute.ecosprout", Context.MODE_PRIVATE);
            if(sp.contains("user")){
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
                                Toast toast = Toast.makeText(MainActivity.this, text, duration);
                                toast.show();
                                Intent intent = new Intent(MainActivity.this, LogIn.class);
                                startActivity(intent);
                                finish();
                            }else {

                                CharSequence text = "Login Successful";
                                int duration = Toast.LENGTH_SHORT;
                                Toast toast = Toast.makeText(MainActivity.this, text, duration);
                                toast.show();

                                Intent intent = new Intent(MainActivity.this, ActivityHome.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                            }


                        }
                    }
                });
            }else{
                Intent intent = new Intent(MainActivity.this, LogIn.class);
                startActivity(intent);
                finish();
            }
    }
}

