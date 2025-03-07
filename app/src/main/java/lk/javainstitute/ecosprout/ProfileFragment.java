package lk.javainstitute.ecosprout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.io.InputStream;
import java.util.ArrayList;

import android.util.Base64;
import java.io.ByteArrayOutputStream;

public class ProfileFragment extends Fragment {

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView imageView;
    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Registering the image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            imageView.setImageURI(uri);
                            saveImageToFirestore(uri);
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imageView = view.findViewById(R.id.imageView);
        imageView.setOnClickListener(v -> {
            ImagePicker.Companion.with(ProfileFragment.this)
                    .crop()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .galleryOnly()
                    .createIntent(intent -> {
                        imagePickerLauncher.launch(intent);
                        return null;
                    });
        });

        SharedPreferences sp = getActivity().getSharedPreferences("lk.javainstitute.ecosprout", Context.MODE_PRIVATE);
        String u = sp.getString("user", null);
        Gson gson = new Gson();
        user = gson.fromJson(u, User.class);

        EditText editTextPhone2 = view.findViewById(R.id.editTextPhone2);
        EditText editTextText4 = view.findViewById(R.id.editTextText4);
        editTextText4.setEnabled(false);
        EditText editTextText5 = view.findViewById(R.id.editTextText5);
        editTextText5.setEnabled(false);
        EditText editTextTextEmailAddress2 = view.findViewById(R.id.editTextTextEmailAddress2);

        editTextPhone2.setText(user.getMobile());
        editTextText4.setText(user.getFname());
        editTextText5.setText(user.getLname());
        editTextTextEmailAddress2.setText(user.getEmail());

        loadProfileImage();

        EditText editTextTextPostalAddress = view.findViewById(R.id.editTextTextPostalAddress);
        EditText editTextTextPostalAddress2 = view.findViewById(R.id.editTextTextPostalAddress2);

        if (user.getLine1() != null) {
            editTextTextPostalAddress.setText(user.getLine1());
        }
        if (user.getLine2() != null) {
            editTextTextPostalAddress2.setText(user.getLine2());
        }

        ArrayList<String> items = new ArrayList<>();
        Spinner spinner = view.findViewById(R.id.spinner1);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("city").orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            items.add(document.getString("name"));
                        }
                        CityAdapter adapter = new CityAdapter(getActivity(), R.layout.city_template, items);
                        spinner.setAdapter(adapter);

                        String city = user.getCity();
                        if (city != null) {
                            int position = adapter.getPosition(city);
                            spinner.setSelection(position);
                        }
                    }
                });

        Button save = view.findViewById(R.id.button11);
        save.setOnClickListener(v -> {
            if (editTextPhone2.getText().toString().isEmpty()) {
                editTextPhone2.setError("Enter mobile number");
            } else if (!Validation.validateMobileNumber(editTextPhone2.getText().toString())) {
                editTextPhone2.setError("Invalid mobile number");
            } else if (editTextTextEmailAddress2.getText().toString().isEmpty()) {
                editTextTextEmailAddress2.setError("Enter email");
            } else if (!Validation.validateEmail(editTextTextEmailAddress2.getText().toString())) {
                editTextTextEmailAddress2.setError("Invalid email");
            } else if (editTextTextPostalAddress.getText().toString().isEmpty()) {
                editTextTextPostalAddress.setError("Enter address line 1");
            } else if (editTextTextPostalAddress2.getText().toString().isEmpty()) {
                editTextTextPostalAddress2.setError("Enter address line 2");
            } else {
                SharedPreferences sp1 = getActivity().getSharedPreferences("lk.javainstitute.ecosprout", Context.MODE_PRIVATE);

                if(user.getMobile().equals(editTextPhone2.getText().toString())){

                    user.setEmail(editTextTextEmailAddress2.getText().toString());
                    user.setLine1(editTextTextPostalAddress.getText().toString());
                    user.setLine2(editTextTextPostalAddress2.getText().toString());
                    user.setCity(spinner.getSelectedItem().toString());


                    sp1.edit().putString("user", new Gson().toJson(user)).apply();

                    db.collection("user").document(user.getId())
                            .update("mobile", user.getMobile(),
                                    "email", user.getEmail(),
                                    "line1", user.getLine1(),
                                    "line2", user.getLine2(),
                                    "city", user.getCity(),
                                    "image", user.getImage());

                    CharSequence text = "Profile Updated";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(getActivity(), text, duration);
                    toast.show();

                }else{
                    db.collection("user").whereEqualTo("mobile",editTextPhone2.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.getResult().isEmpty()){

                                user.setMobile(editTextPhone2.getText().toString());
                                user.setEmail(editTextTextEmailAddress2.getText().toString());
                                user.setLine1(editTextTextPostalAddress.getText().toString());
                                user.setLine2(editTextTextPostalAddress2.getText().toString());
                                user.setCity(spinner.getSelectedItem().toString());

                                sp1.edit().putString("user", new Gson().toJson(user)).apply();

                                db.collection("user").document(user.getId())
                                        .update("mobile", user.getMobile(),
                                                "email", user.getEmail(),
                                                "line1", user.getLine1(),
                                                "line2", user.getLine2(),
                                                "city", user.getCity(),
                                                "image", user.getImage());

                                CharSequence text = "Profile Updated";
                                int duration = Toast.LENGTH_SHORT;
                                Toast toast = Toast.makeText(getActivity(), text, duration);
                                toast.show();


                            }else{
                                editTextPhone2.setError("mobile already registered");
                            }
                        }
                    });
                }







            }
        });

        return view;
    }

    private void saveImageToFirestore(Uri uri) {
        try (InputStream inputStream = getActivity().getContentResolver().openInputStream(uri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Convert to Base64
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            user.setImage(base64Image);

            // Save user data including image to SharedPreferences
            SharedPreferences sp = getActivity().getSharedPreferences("lk.javainstitute.ecosprout", Context.MODE_PRIVATE);
            sp.edit().putString("user", new Gson().toJson(user)).apply();

            // Update image in Firestore
            FirebaseFirestore.getInstance().collection("user").document(user.getId())
                    .update("image", base64Image)
                    .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "Image uploaded", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "Upload failed", Toast.LENGTH_SHORT).show());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error saving image", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProfileImage() {
        if (user.getImage() != null && !user.getImage().isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(user.getImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                imageView.setImageResource(R.drawable.picker);
            }
        } else {
            imageView.setImageResource(R.drawable.picker);
        }
    }
}
