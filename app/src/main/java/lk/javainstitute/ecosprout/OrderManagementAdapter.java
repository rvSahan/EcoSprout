package lk.javainstitute.ecosprout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class OrderManagementAdapter extends  RecyclerView.Adapter<OrderManagementAdapter.OrderManagementHolder> {
    ArrayList<Order> orders;

    OnitemClickListener onitemClickListener;

    public OrderManagementAdapter(ArrayList<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderManagementHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_template, parent, false);
        return new OrderManagementHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderManagementHolder holder, int position) {
        Order order = orders.get(position);
        holder.orderId.setText(order.getOrderId());

        Timestamp timestamp = order.getTimestamp();
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String formattedDate = sdf.format(date);

        holder.dateTime.setText(formattedDate);
        holder.total.setText("Rs."+String.valueOf(order.getTotalPrice()));
        holder.status.setText(order.getStatus());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onitemClickListener.onClick(order);

            }
        });

    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class OrderManagementHolder extends RecyclerView.ViewHolder {
        TextView orderId, dateTime, total, status;

        public OrderManagementHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.textView35);
            dateTime = itemView.findViewById(R.id.textView36);
            total = itemView.findViewById(R.id.textView37);
            status = itemView.findViewById(R.id.textView38);
        }
    }
    public void setOnitemClickListener(OnitemClickListener onitemClickListener) {
        this.onitemClickListener = onitemClickListener;
    }

    public interface OnitemClickListener{
        void onClick(Order o);
    }

}
