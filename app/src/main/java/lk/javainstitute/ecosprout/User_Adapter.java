package lk.javainstitute.ecosprout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class User_Adapter extends RecyclerView.Adapter<User_Adapter.UserHolder> {
    ArrayList<User> users;

    OnitemClickListener onitemClickListener;

    public User_Adapter(ArrayList<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_template, parent, false);
        return new UserHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {
        User user = users.get(position);
        holder.textView.setText(user.getId());
        holder.textView4.setText(user.getFname()+" "+user.getLname());
        holder.textView3.setText(user.getStatus());
        holder.textView2.setText(user.getMobile());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onitemClickListener.onClick(user);

            }
        });




    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserHolder extends RecyclerView.ViewHolder {

        TextView textView;
        TextView textView2;
        TextView textView3;
        TextView textView4;

        public UserHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView62);
            textView2 = itemView.findViewById(R.id.textView63);
            textView3 = itemView.findViewById(R.id.textView64);
            textView4 = itemView.findViewById(R.id.textView65);


        }



    }
    public void setOnitemClickListener(OnitemClickListener onitemClickListener) {
        this.onitemClickListener = onitemClickListener;
    }

    public interface OnitemClickListener{
        void onClick(User u);
    }
}
