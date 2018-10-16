package com.android.developer.feedingindia.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.developer.feedingindia.R;
import com.android.developer.feedingindia.pojos.DonationDetails;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.DonationViewHolder> {

    private ArrayList<DonationDetails> userDonations;
    public DonationAdapter.OnClickListener mListener;

    public DonationAdapter(ArrayList<DonationDetails> userDonations){

        this.userDonations = userDonations;

    }

    public interface OnClickListener{

        void onClick(int position);
        void onClickImage(ImageView view,int position);

    }


    public static class DonationViewHolder extends  RecyclerView.ViewHolder{

        private ImageView donationImageView,deliveryImageView;
        private TextView foodType,deliveryStatus,deliveredOn,foodDescription,delivererName,delivererContactNumber;

        public DonationViewHolder(@NonNull View view, final OnClickListener listener) {

            super(view);

            donationImageView = view.findViewById(R.id.donation_image);
            deliveryImageView = view.findViewById(R.id.delivery_image);
            foodDescription = view.findViewById(R.id.food_description);
            foodType = view.findViewById(R.id.food_type);
            deliveryStatus = view.findViewById(R.id.delivery_status);
            deliveredOn = view.findViewById(R.id.delivered_on);
            delivererName = view.findViewById(R.id.deliverer_name);
            delivererContactNumber = view.findViewById(R.id.deliverer_contact_number);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(listener!=null)
                        listener.onClick(getAdapterPosition());

                }
            });

            donationImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(listener!=null)
                        listener.onClickImage(donationImageView,getAdapterPosition());

                }
            });

            deliveryImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(listener!=null)
                        listener.onClickImage(deliveryImageView,getAdapterPosition());

                }
            });

        }
    }

    @NonNull
    @Override
    public DonationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.donation_item,viewGroup,false);
        return new DonationViewHolder(view,mListener);

    }

    public void setOnItemClickListener(OnClickListener listener){

        mListener = listener;

    }


    @Override
    public void onBindViewHolder(@NonNull DonationViewHolder donationViewHolder, int i) {

        DonationDetails mDonationDetails = userDonations.get(i);

        donationViewHolder.foodDescription.setText(mDonationDetails.getFoodDescription().trim());
        donationViewHolder.foodType.setText(mDonationDetails.getFoodType().trim());
        String deliveredOn = "";
        if(mDonationDetails.getStatus().trim().equals("pending")){

            String nil = "Nil";
            donationViewHolder.delivererName.setText(nil);
            donationViewHolder.delivererContactNumber.setText(nil);
            donationViewHolder.deliveryStatus.setText(mDonationDetails.getStatus().trim().toUpperCase());
            deliveredOn = "Delivered On : Nil";
            donationViewHolder.deliveredOn.setText(deliveredOn);

        }
        else{

            donationViewHolder.delivererName.setText(mDonationDetails.getDelivererName());
            donationViewHolder.delivererContactNumber.setText(mDonationDetails.getDelivererContactNumber());
            donationViewHolder.deliveryStatus.setText(mDonationDetails.getStatus().trim().toUpperCase());
            if(mDonationDetails.getStatus().trim().equals("picked"))
                deliveredOn = "Delivered On : " + "Nil";
            else
                deliveredOn = "Delivered On : " + mDonationDetails.getDeliveredOn();
            donationViewHolder.deliveredOn.setText(deliveredOn);

        }

        String donationImageUrl = mDonationDetails.getDonationImageUrl();
        String deliveryImageUrl = mDonationDetails.getDeliveryImgUrl();

        if(donationImageUrl != null)
            Picasso.get().load(donationImageUrl).into(donationViewHolder.donationImageView);
        else
            donationViewHolder.donationImageView.setImageResource(R.drawable.no_image);

        if(deliveryImageUrl != null)
            Picasso.get().load(deliveryImageUrl).into(donationViewHolder.deliveryImageView);
        else
            donationViewHolder.deliveryImageView.setImageResource(R.drawable.no_image);

    }

    @Override
    public int getItemCount() {
        return userDonations.size();
    }

}