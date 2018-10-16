package com.android.developer.feedingindia.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.developer.feedingindia.R;
import com.android.developer.feedingindia.pojos.DeliveryDetails;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashMap;


public class DeliveriesAdapter extends RecyclerView.Adapter<DeliveriesAdapter.ViewHolder>{

    private ArrayList<DeliveryDetails> deliveries;
    private OnClickListener mListener;

    public DeliveriesAdapter(ArrayList<DeliveryDetails> deliveries){

        this.deliveries = deliveries;

    }

    public interface  OnClickListener{

        void onClick(View view,int position);
        void onClickAddDeliveryImage(View view,int position);
        void onClickImage(ImageView view,int position);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView donorAddress,hungerSpotAddress,status;
        private TextView donorName,donorContactNumber;
        private TextView deliveredOn;
        private ImageView deliveryImage;
        private ImageView donationImage;

        public ViewHolder(View view, final OnClickListener mListener){

            super(view);

            donorName = view.findViewById(R.id.donor_name_text);
            donorContactNumber = view.findViewById(R.id.donor_contact_number_text);
            donorAddress = view.findViewById(R.id.donor_address);
            hungerSpotAddress = view.findViewById(R.id.hunger_spot_address);
            status = view.findViewById(R.id.status);
            deliveredOn = view.findViewById(R.id.delivered_on);
            donationImage = view.findViewById(R.id.donation_image);
            deliveryImage = view.findViewById(R.id.delivery_image);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(mListener!=null)
                        mListener.onClick(view,getAdapterPosition());

                }
            });

            deliveryImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if(mListener!=null)
                        mListener.onClickAddDeliveryImage(view,getAdapterPosition());

                    return  true;

                }
            });

            deliveryImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(mListener!=null)
                        mListener.onClickImage(deliveryImage,getAdapterPosition());

                }
            });

            donationImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(mListener!=null)
                        mListener.onClickImage(donationImage,getAdapterPosition());

                }
            });

        }

    }

    public void setOnItemClickListener(OnClickListener clickListener){

        mListener = clickListener;

    }

    @NonNull
    @Override
    public DeliveriesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.delivery_item, parent, false);
        return new ViewHolder(view,mListener);

    }

    @Override
    public void onBindViewHolder(@NonNull DeliveriesAdapter.ViewHolder viewHolder, int i) {

        DeliveryDetails mDeliveryDetails = deliveries.get(i);

        viewHolder.donorName.setText(mDeliveryDetails.getDonorName());
        viewHolder.donorContactNumber.setText(mDeliveryDetails.getDonorConactNumber());

        HashMap<String,String> donorAddressHashMap;
        donorAddressHashMap = mDeliveryDetails.getDonorAddress();
        String donorAddressString = donorAddressHashMap.get("address").trim() + "\n" +
                donorAddressHashMap.get("city").trim() + "\n" +
                donorAddressHashMap.get("state").trim() + " - " + donorAddressHashMap.get("pinCode").trim();
        viewHolder.donorAddress.setText(donorAddressString);

        HashMap<String,String> hungerSpotAddressHashMap;
        hungerSpotAddressHashMap = mDeliveryDetails.getHungerSpotAddress();
        String hungerSpotAddressString = hungerSpotAddressHashMap.get("address").trim() + "\n" +
                hungerSpotAddressHashMap.get("city").trim() + "\n" +
                hungerSpotAddressHashMap.get("state").trim() + "-" + hungerSpotAddressHashMap.get("pinCode").trim();
        viewHolder.hungerSpotAddress.setText(hungerSpotAddressString);

        viewHolder.status.setText(deliveries.get(i).getStatus().toUpperCase());

        String deliveredOnString = "Delivered On : ";

        if (!mDeliveryDetails.getDeliveredOn().isEmpty())
            deliveredOnString +=  mDeliveryDetails.getDeliveredOn() ;
        else
            deliveredOnString += "Nil";

        viewHolder.deliveredOn.setText(deliveredOnString);

        Log.i("Hello", "onBindViewHolder: " + deliveries.get(i).getDonationImgUrl());

        if(deliveries.get(i).getDonationImgUrl() != null)
            Picasso.get().load(deliveries.get(i).getDonationImgUrl()).into(viewHolder.donationImage);
        else
            viewHolder.donationImage.setImageResource(R.drawable.no_image);

        if(deliveries.get(i).getDeliveryImgUrl() != null)
            Picasso.get().load(deliveries.get(i).getDeliveryImgUrl()).into(viewHolder.deliveryImage);
        else
        if(mDeliveryDetails.getStatus().equals("delivered"))
            viewHolder.deliveryImage.setImageResource(R.drawable.no_image);
        else
            viewHolder.deliveryImage.setImageResource(R.drawable.add_image);

    }

    @Override
    public int getItemCount() {
        return deliveries.size();
    }
}
