package com.example.madinabakersresutrant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {
    private Context context;
    private List<AddressModel> addressModelList;
    private CartActivity.OnAddressClickListener listener;
    public AddressAdapter(Context context, List<AddressModel> addressModelList, CartActivity.OnAddressClickListener listener){
        this.context = context;
        this.addressModelList = addressModelList;
        this.listener = listener;
    }
    public AddressAdapter(Context context, List<AddressModel> addressModelList){
        this.context = context;
        this.addressModelList = addressModelList;
    }
    @NonNull
    @Override
    public AddressAdapter.AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.address_item, parent, false);

        return new AddressViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull AddressAdapter.AddressViewHolder holder, int position) {
        AddressModel  addressModel = addressModelList.get(position);
        holder.txtCityName.setText(addressModel.getCity());
        holder.txtAddress.setText(addressModel.getAddress());
        holder.txtAddressName.setText(addressModel.getName());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddressClick(addressModel);
            }
        });

    }

    @Override
    public int getItemCount() {
        return addressModelList.size();
    }

    public class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView txtCityName, txtAddressName, txtAddress;
        Button btnDelete,btnEdit;
        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            txtAddress = itemView.findViewById(R.id.txtAddress);
            txtAddressName = itemView.findViewById(R.id.txtAddressName);
            txtCityName= itemView.findViewById(R.id.txtCityName);
            btnEdit= itemView.findViewById(R.id.btnEdt);
            btnDelete = itemView.findViewById(R.id.btnDelete);

        }
    }
}
