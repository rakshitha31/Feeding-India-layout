package com.android.developer.feedingindia.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.developer.feedingindia.R;
import com.android.developer.feedingindia.pojos.FeedingIndiaEvent;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationsViewHolder> {
    private ArrayList<FeedingIndiaEvent> eventList;

    public static class NotificationsViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageView;
        public TextView eventName;
        public TextView eventDescription;
        public NotificationsViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_event);
            eventName = itemView.findViewById(R.id.name_event);
            eventDescription = itemView.findViewById(R.id.description_event);

        }
    }

    public NotificationsAdapter(ArrayList<FeedingIndiaEvent> list){
        eventList = list;
    }

    @NonNull
    @Override
    public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notification_item,viewGroup,false);
        NotificationsViewHolder viewHolder = new
                NotificationsViewHolder(v);
        return  viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsViewHolder notificationsViewHolder, int i) {
        FeedingIndiaEvent feedingIndiaEvent = eventList.get(i);
        Picasso.get().load(feedingIndiaEvent.getImageUrl()).into(notificationsViewHolder.imageView);
        //notificationsViewHolder.imageView.setImageResource();
        notificationsViewHolder.eventName.setText(feedingIndiaEvent.getEventName());
        notificationsViewHolder.eventDescription.setText(feedingIndiaEvent.getEventDescription());
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}
