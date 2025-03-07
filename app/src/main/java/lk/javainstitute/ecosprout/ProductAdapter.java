package lk.javainstitute.ecosprout;

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
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProductAdapter  extends RecyclerView.Adapter<ProductAdapter.ProductHolder>  {

    ArrayList<ProductClass> arrayList;

    OnitemClickListener onitemClickListener;

    public ProductAdapter(ArrayList<ProductClass> arrayList){
        this.arrayList = arrayList;


    }

    @NonNull
    @Override
    public ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.product_template,parent,false);
        return new ProductHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ProductHolder holder, int position) {
        ProductClass productClass = arrayList.get(position);
        holder.textView.setText(productClass.getName());
        holder.textView2.setText("Rs."+productClass.getPrice()+".00");

        String imageName = productClass.getImage();
        if (productClass.getImage() != null && !productClass.getImage().isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(productClass.getImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                holder.imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                holder.imageView.setImageResource(R.drawable.picker);
            }
        } else {
            holder.imageView.setImageResource(R.drawable.picker);
        }

//        if (imageName != null && !imageName.isEmpty()) {
//            int imageResId = holder.imageView.getContext().getResources()
//                    .getIdentifier(imageName, "drawable", holder.imageView.getContext().getPackageName());
//
//            // Debug: Print resource ID to check if it's valid
//            Log.d("ImageDebug", "Image Name: " + imageName + " | Resource ID: " + imageResId);
//
//            if (imageResId != 0) {
//                holder.imageView.setImageResource(imageResId); // Set image if found
//            } else {
//                holder.imageView.setImageResource(R.drawable.ic_notifications_black_24dp); // Default image
//            }
//        } else {
//            holder.imageView.setImageResource(R.drawable.menu); // Default image if name is null/empty
//        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onitemClickListener.onClick(productClass);

            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class ProductHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView textView;
        TextView textView2;

        public ProductHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView3);
            textView = itemView.findViewById(R.id.textView10);
            textView2 = itemView.findViewById(R.id.textView13);

        }
    }
    public void setOnitemClickListener(OnitemClickListener onitemClickListener) {
        this.onitemClickListener = onitemClickListener;
    }

    public interface OnitemClickListener{
        void onClick(ProductClass c);
    }
}
