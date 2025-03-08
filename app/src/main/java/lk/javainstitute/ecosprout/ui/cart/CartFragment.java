package lk.javainstitute.ecosprout.ui.cart;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import lk.javainstitute.ecosprout.ActivityHome;
import lk.javainstitute.ecosprout.CartAdapter;
import lk.javainstitute.ecosprout.CartItem;
import lk.javainstitute.ecosprout.Order;
import lk.javainstitute.ecosprout.R;
import lk.javainstitute.ecosprout.User;
import lk.javainstitute.ecosprout.databinding.FragmentCartBinding;
import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;
import lk.payhere.androidsdk.model.StatusResponse;

import com.google.firebase.Timestamp;

public class CartFragment extends Fragment implements CartAdapter.CartUpdateListener {
    private FragmentCartBinding binding;
    private ArrayList<CartItem> cartItems;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private TextView totalTextView, itemCountTextView;

    private static final String TAG = "PayhereDomain";

    double totalPrice;

    User user;

    String orderId;

    private final ActivityResultLauncher payhereLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null)  {
                    Intent data = result.getData();
                    if (data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)){
                        Serializable serializable = data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
                        if (serializable instanceof PHResponse){
                            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) serializable;
                            if (response.isSuccess()) {
                                saveOrderToFirestore();
                                clearCart();
                                pushNotifi();
                                Toast.makeText(getActivity(), "Payment Successful!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Payment Failed: " + response.getData().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Toast toast = Toast.makeText(getActivity(), "Canceled", Toast.LENGTH_SHORT);
                }
            }
    );

    private void pushNotifi() {
        NotificationManager nm = getContext().getSystemService(NotificationManager.class);

        if (nm == null) {
            Log.e("NotificationError", "NotificationManager is null");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel nc = new NotificationChannel(
                    "1",
                    "Payment Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            nc.setDescription("Channel for payment success notifications");
            nm.createNotificationChannel(nc);
        }

        Notification notification = new Notification.Builder(getActivity().getApplicationContext(), "1")
                .setContentTitle("Payment")
                .setContentText("Payment Successful")
                .setSmallIcon(R.drawable.pngegg)
                .setAutoCancel(true)
                .build();

        // Add a unique notification ID
        nm.notify(1, notification); // '1' here is a unique ID for this notification
    }


    private void saveOrderToFirestore() {
         orderId = String.valueOf(System.currentTimeMillis());

        Order order = new Order(orderId, user.getId(), cartItems, totalPrice, Timestamp.now(), "Pending");
        db.collection("order").document(orderId)
                .set(order)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getActivity(), "Order placed successfully!", Toast.LENGTH_SHORT).show();
                        updateProductQuantities();
                        updateTotal();
                        startActivity(new Intent(getActivity(), ActivityHome.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        getActivity().finish();


                    }
                });

    }

    private void updateProductQuantities() {
        for (CartItem item : cartItems) {
            String productId = item.getProductId();
            int purchasedQty = Integer.parseInt(item.getQty());

            db.collection("product").document(productId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if (documentSnapshot.exists()) {
                                    int currentQty = Integer.parseInt(documentSnapshot.getString("qty"));
                                    int updatedQty = currentQty - purchasedQty;

                                    db.collection("product").document(productId)
                                            .update("qty", String.valueOf(updatedQty));

                                    Toast.makeText(getContext(), "Product quantity updated for " + productId, Toast.LENGTH_SHORT).show();

                                }

                            }


                        }
                    });
        }
    }


    private void clearCart() {
        db.collection("cart").document(user.getId()).collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                    cartItems.clear();
                    recyclerView.getAdapter().notifyDataSetChanged();
                    updateTotal();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to clear cart", e));
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.CartRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        totalTextView = binding.textView26;  // Total Price TextView
        itemCountTextView = binding.textView23; // New: Item Count TextView

        db = FirebaseFirestore.getInstance();
        cartItems = new ArrayList<>();

        loadCartData();

        Button checkoutButton = binding.button12;





        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences sp  = getActivity().getSharedPreferences("lk.javainstitute.ecosprout", Context.MODE_PRIVATE);
                String u = sp.getString("user", null);
                Gson gson = new Gson();
                user = gson.fromJson(u, User.class);

                if(user.getLine1() != null && user.getLine2() != null && user.getCity() != null){
                    initiatePayment();

                }else{
                    Toast.makeText(getContext(), "Please fill in your address on your profile", Toast.LENGTH_SHORT).show();
                }




            }
        });
        checkoutButton.setEnabled(false);

        return root;
    }

    private void initiatePayment() {




        InitRequest req = new InitRequest();
        req.setMerchantId("123456"); // Replace with actual Merchant ID
        req.setCurrency("LKR");
        req.setAmount(totalPrice);
        req.setOrderId(String.valueOf(orderId));
        req.setItemsDescription("EcoSprout Order");
        req.setCustom1("Order for " + user.getFname());
        req.getCustomer().setFirstName(user.getFname());
        req.getCustomer().setLastName(user.getLname());
        req.getCustomer().setEmail(user.getEmail());
        req.getCustomer().setPhone(user.getMobile());
        req.getCustomer().getAddress().setAddress(user.getLine1() + ", " + user.getLine2());
        req.getCustomer().getAddress().setCity(user.getCity());
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        // Log values for debugging
        Log.d(TAG, "Payment Initiated with Order ID: " + req.getOrderId());
        Log.d(TAG, "Amount: " + req.getAmount());
        Log.d(TAG, "Currency: " + req.getCurrency());

        // Start PayHere Payment Activity
        Intent intent = new Intent(getContext(), PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL); // Set the sandbox URL

        payhereLauncher.launch(intent);
    }


    private void loadCartData() {
        SharedPreferences sp = getActivity().getSharedPreferences("lk.javainstitute.ecosprout", Context.MODE_PRIVATE);
        String u = sp.getString("user", null);
        Gson gson = new Gson();
         user = gson.fromJson(u, User.class);

        db.collection("cart").document(String.valueOf(user.getId())).collection("items").get()
                .addOnCompleteListener(task -> {

                    cartItems.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        cartItems.add(document.toObject(CartItem.class));
                    }
                    recyclerView.setAdapter(new CartAdapter(getContext(), cartItems, this));
                    updateTotal();
                });
    }

    @Override
    public void onCartUpdated() {
        updateTotal();
    }

    private void updateTotal() {
        int totalItems = 0;
         totalPrice = 0;

        for (CartItem item : cartItems) {
            int qty = Integer.parseInt(item.getQty());
            totalItems += qty;
            totalPrice += Double.parseDouble(item.getPrice()) * qty;
        }

        // Update the UI
        itemCountTextView.setText(String.valueOf(totalItems));
        totalTextView.setText("Rs. " + totalPrice );

        Button checkoutButton = binding.button12;
        if (totalItems > 0) {
            checkoutButton.setEnabled(true);
        } else {
            checkoutButton.setEnabled(false);
        }
    }
}
