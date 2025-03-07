package lk.javainstitute.ecosprout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.utils.widget.ImageFilterButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartHolder> {
    private final ArrayList<CartItem> cartItems;
    private final FirebaseFirestore db;
    private final SharedPreferences sp;
    private final User user;
    private final CartUpdateListener cartUpdateListener;
    private final ArrayList<Integer> stockQuantities = new ArrayList<>();

    public CartAdapter(Context context, ArrayList<CartItem> cartItems, CartUpdateListener cartUpdateListener) {
        this.cartItems = cartItems;
        this.db = FirebaseFirestore.getInstance();
        this.sp = context.getSharedPreferences("lk.javainstitute.ecosprout", Context.MODE_PRIVATE);
        this.cartUpdateListener = cartUpdateListener;

        String u = sp.getString("user", null);
        Gson gson = new Gson();
        this.user = gson.fromJson(u, User.class);

        fetchStockQuantities();
    }

    @NonNull
    @Override
    public CartHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item_view, parent, false);
        return new CartHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartHolder holder, @SuppressLint("RecyclerView") int position) {
        CartItem cartItem = cartItems.get(position);
        holder.textView.setText(cartItem.getName());
        holder.textView2.setText(String.valueOf(cartItem.getQty()));
        holder.textView3.setText("Rs. " + cartItem.getPrice() + ".00");



        // Load image if available
        if (cartItem.getImage() != null && !cartItem.getImage().isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(cartItem.getImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                holder.imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                holder.imageView.setImageResource(R.drawable.picker);
            }
        } else {
            holder.imageView.setImageResource(R.drawable.picker);
        }

        // Handle increase button
        holder.imageFilterButton.setOnClickListener(view -> {
            int currentQty = Integer.parseInt(cartItem.getQty());
            int stockQty = stockQuantities.get(position);

            if (currentQty < stockQty) {
                cartItem.setQty(String.valueOf(currentQty + 1));
                db.collection("cart").document(String.valueOf(user.getId()))
                        .collection("items").document(cartItem.getProductId())
                        .update("qty", cartItem.getQty());

                notifyItemChanged(position);
                cartUpdateListener.onCartUpdated();
            }
        });

        // Handle decrease button
        holder.imageFilterButton2.setOnClickListener(view -> {
            int currentQty = Integer.parseInt(cartItem.getQty());

            if (currentQty > 1) {
                cartItem.setQty(String.valueOf(currentQty - 1));
                db.collection("cart").document(String.valueOf(user.getId()))
                        .collection("items").document(cartItem.getProductId())
                        .update("qty", cartItem.getQty());

                notifyItemChanged(position);
                cartUpdateListener.onCartUpdated();
            }
        });

        // Handle remove button
        holder.imageFilterButton3.setOnClickListener(view -> {
            db.collection("cart").document(String.valueOf(user.getId()))
                    .collection("items").document(cartItem.getProductId())
                    .delete();

            cartItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartItems.size());
            cartUpdateListener.onCartUpdated();
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    private void fetchStockQuantities() {
        for (int i = 0; i < cartItems.size(); i++) {
            final int index = i;
            db.collection("product").document(cartItems.get(i).getProductId())
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String qtyString = document.getString("qty");
                                int stockQty = parseInteger(qtyString, 0);

                                while (stockQuantities.size() <= index) stockQuantities.add(0);
                                stockQuantities.set(index, stockQty);
                            }
                        }
                    });
        }
    }

    private int parseInteger(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static class CartHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView, textView2, textView3;
        ImageFilterButton imageFilterButton, imageFilterButton2, imageFilterButton3;

        public CartHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView5);
            textView = itemView.findViewById(R.id.textView21);
            textView2 = itemView.findViewById(R.id.textView27);
            textView3 = itemView.findViewById(R.id.textView28);
            imageFilterButton = itemView.findViewById(R.id.imageFilterButton);
            imageFilterButton2 = itemView.findViewById(R.id.imageFilterButton2);
            imageFilterButton3 = itemView.findViewById(R.id.imageFilterButton3);
        }
    }

    public interface CartUpdateListener {
        void onCartUpdated();
    }
}
