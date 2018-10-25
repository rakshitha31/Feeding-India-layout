package com.android.developer.feedingindia.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.android.developer.feedingindia.R;
import com.android.developer.feedingindia.activities.EnlargeImageViewActivity;
import com.android.developer.feedingindia.adapters.DeliveriesAdapter;
import com.android.developer.feedingindia.pojos.DeliveryDetails;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import static android.app.Activity.RESULT_OK;

public class MyDeliveriesFragment extends Fragment {

    private DatabaseReference deliveryDatabaseReference;
    private long deliveryCount,readDeliveryCount;
    private ProgressBar progressBar;
    private RecyclerView mRecyclerView;
    private ArrayList<DeliveryDetails> deliveriesList;
    private HashMap<Integer,Uri> mImageUriList;
    private AlertDialog mAlertDialog;
    private AlertDialog.Builder mBuilder;
    private HashMap<String,String> donationIdToPushId;
    private StorageReference mDeliveryPhotoReference;
    private ChildEventListener mChildEventListener;
    private DeliveriesAdapter mAdapter;
    private ImageView mImageView;

    public MyDeliveriesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deliveryCount = 0;
        readDeliveryCount = 0;
        deliveriesList = new ArrayList<>();
        donationIdToPushId = new HashMap<>();
        mImageUriList = new HashMap<>();
        deliveryDatabaseReference = FirebaseDatabase.getInstance().getReference().
                child("Deliveries").child(FirebaseAuth.getInstance().getUid());

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                deliveriesList.add(dataSnapshot.getValue(DeliveryDetails.class));

                donationIdToPushId.put(deliveriesList.get(deliveriesList.size()-1).getDonationId(),dataSnapshot.getKey());

                readDeliveryCount++;

                if(readDeliveryCount == deliveryCount) {
                    sortList();
                    enableUserInteraction();
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mDeliveryPhotoReference = FirebaseStorage.getInstance().getReference().child("delivery_photos");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mBuilder = new android.support.v7.app.AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
        else
            mBuilder = new android.support.v7.app.AlertDialog.Builder(getContext());


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_deliveries, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        mRecyclerView = view.findViewById(R.id.deliveries_container);

        return  view;
    }

    @Override
    public void onResume() {

        super.onResume();

        if(deliveriesList.size() == 0) {

            progressBar.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);

            deliveryDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    deliveryCount = readDeliveryCount = 0;
                    deliveriesList.clear();
                    donationIdToPushId.clear();
                    mImageUriList.clear();

                    deliveryCount = dataSnapshot.getChildrenCount();

                    if (deliveryCount == 0) {
                        sortList();
                        enableUserInteraction();
                    }

                    else
                        deliveryDatabaseReference.addChildEventListener(mChildEventListener);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    makeToast(databaseError.getMessage());

                }
            });

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mChildEventListener!=null)
            deliveryDatabaseReference.removeEventListener(mChildEventListener);

        if(mAlertDialog!=null)
            if(mAlertDialog.isShowing())
                mAlertDialog.cancel();

    }

    private void buildRecyclerView(){

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new DeliveriesAdapter(deliveriesList);
        mRecyclerView.setAdapter(mAdapter);
        attachListenerToRecyclerView();

    }

    private void attachListenerToRecyclerView(){

        mAdapter.setOnItemClickListener(new DeliveriesAdapter.OnClickListener() {
            @Override
            public void onClick(final View view, final int position) {

                String status = deliveriesList.get(position).getStatus();

                if(status.equals("pending")) {

                    mAlertDialog = mBuilder.setMessage("What's the status of delivery?")
                            .setTitle("Status")
                            .setPositiveButton("DELIVERED", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    DeliveryDetails mDeliveryDetails = deliveriesList.get(position);

                                    String nextStatus = "delivered";
                                    mDeliveryDetails.setStatus(nextStatus);

                                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                    Date date = new Date();
                                    String deliveredOn = formatter.format(date);
                                    deliveriesList.get(position).setDeliveredOn(deliveredOn);

                                    Long timeStamp = System.currentTimeMillis()/1000;

                                    deliveryDatabaseReference.child(donationIdToPushId.get(mDeliveryDetails.getDonationId())).child("status").setValue("delivered");
                                    deliveryDatabaseReference.child(donationIdToPushId.get(mDeliveryDetails.getDonationId())).child("deliveredOn").setValue(deliveredOn);

                                    deliveryDatabaseReference.child(donationIdToPushId.get(mDeliveryDetails.getDonationId())).child("timeStamp").setValue(timeStamp);

                                    FirebaseDatabase.getInstance().getReference().child("Donations").child(mDeliveryDetails.getDonationUserId()).child(mDeliveryDetails.getDonationId()).child("status").setValue("delivered");
                                    FirebaseDatabase.getInstance().getReference().child("Donations").child(mDeliveryDetails.getDonationUserId()).child(mDeliveryDetails.getDonationId()).child("deliveredOn").setValue(deliveredOn);
                                    HomeFragment.donorLocation = null;
                                    HomeFragment.hungerSpotLocation = null;

                                    Uri uri = mImageUriList.get(position);

                                    if(uri!=null) {
                                        deliveriesList.get(position).setDeliveryImgUrl(uri.toString());
                                        mAdapter.notifyItemChanged(position);
                                        addImageToFireStorage(uri, position);
                                    }

                                }
                            })

                            .setNegativeButton("CANCEL DELIVERY", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {

                                    deliveryDatabaseReference.child(donationIdToPushId.get(deliveriesList.get(position).getDonationId())).removeValue();
                                    FirebaseDatabase.getInstance().getReference().child("Donations").child(deliveriesList.get(position).getDonationUserId()).child(deliveriesList.get(position).getDonationId()).child("status").setValue("pending");
                                    HomeFragment.donorLocation = null;
                                    HomeFragment.hungerSpotLocation = null;
                                    deliveriesList.remove(position);
                                    mAdapter.notifyItemRemoved(position);

                                }
                            })
                            .create();

                    mAlertDialog.show();

                }
            }

            @Override
            public void onClickAddDeliveryImage(View view, int position) {

                String status = deliveriesList.get(position).getStatus();

                if(status.equals("pending")) {

                    mImageView = view.findViewById(R.id.delivery_image);
                    Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    gallery.setType("image/*");
                    startActivityForResult(gallery, position);
                    mAdapter.notifyItemChanged(position);

                }

            }

            @Override
            public void onClickImage(ImageView view, int position) {
                String passingImageUrl = deliveriesList.get(position).getDeliveryImgUrl();
                if(passingImageUrl!=null) {
                    Intent intent = new Intent(getContext(),EnlargeImageViewActivity.class);
                    intent.putExtra("ImageUrl", passingImageUrl);
                    intent.putExtra("type","delivery");
                    startActivity(intent);
                }

            }
        });

    }

    private void addImageToFireStorage(Uri imageUri, final int position){

        final StorageReference photoRef =
                mDeliveryPhotoReference.child(deliveriesList.get(position).getDonationId() + ".jpeg");
        UploadTask uploadTask = photoRef.putFile(imageUri);

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                if (!task.isSuccessful()) {
                    makeToast("Image was not uploaded! Please Try Again");
                    throw task.getException();
                }

                return photoRef.getDownloadUrl();
            }

        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {

                    Uri downloadUri = task.getResult();
                    String imageUrl = downloadUri.toString();
                    deliveryDatabaseReference.child(donationIdToPushId.get(deliveriesList.get(position).getDonationId())).child("deliveryImgUrl").setValue(imageUrl);
                    DeliveryDetails mDeliveryDetails = deliveriesList.get(position);
                    FirebaseDatabase.getInstance().getReference().child("Donations").child(mDeliveryDetails.getDonationUserId()).child(mDeliveryDetails.getDonationId()).child("deliveryImgUrl").setValue(imageUrl);


                } else {
                    makeToast("Image was not uploaded! Please Try Again");
                }
            }
        });


    }

    private void enableUserInteraction()
    {

        mRecyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        buildRecyclerView();

    }

    public void onActivityResult(final int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode < deliveriesList.size()) {

            final Uri imageUri = data.getData();
            if(mImageView!=null)
                mImageView.setImageURI(imageUri);

            mImageUriList.put(requestCode,imageUri);
            mAdapter.notifyItemChanged(requestCode);
        }

    }

    private void makeToast(String message){

        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();

    }

    private void sortList(){

        if(deliveriesList != null){

            Collections.sort(deliveriesList, new Comparator<DeliveryDetails>() {
                @Override
                public int compare(DeliveryDetails o1, DeliveryDetails o2) {
                    return Long.valueOf(o2.getTimeStamp()).compareTo(o1.getTimeStamp());
                }
            });
        }

    }

}