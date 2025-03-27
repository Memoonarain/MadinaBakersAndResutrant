package com.example.madinabakersresutrant;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.madinabakersresutrant.OrderModel;
import com.example.madinabakersresutrant.CartItem;
import com.example.madinabakersresutrant.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private ArrayList<OrderModel> orderList;

    public OrderAdapter(Context context, ArrayList<OrderModel> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderModel order = orderList.get(position);

        holder.orderId.setText("Order ID: #" + order.getOrderId());
        holder.items.setText("Items: " + getItemSummary(order));
        holder.total.setText("Total: Rs. " + order.getTotalAmount());
        holder.status.setText("Status: " + order.getStatus());
        holder.time.setText("Ordered at: " + convertTimestamp(order.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView orderId, items, total, status, time;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.order_id_text);
            items = itemView.findViewById(R.id.order_items_text);
            total = itemView.findViewById(R.id.order_total_text);
            status = itemView.findViewById(R.id.order_status_text);
            time = itemView.findViewById(R.id.order_time_text);
        }
    }

    private String getItemSummary(OrderModel order) {
        StringBuilder summary = new StringBuilder();
        for (CartItem item : order.getItems()) {
            summary.append(item.getQuantity()).append("x ").append(item.getName()).append(", ");
        }

        // Remove last comma
        if (summary.length() > 2) {
            summary.setLength(summary.length() - 2);
        }

        return summary.toString();
    }

    private String convertTimestamp(long timestamp) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
