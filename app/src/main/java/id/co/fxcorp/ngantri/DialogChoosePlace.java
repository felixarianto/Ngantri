package id.co.fxcorp.ngantri;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

import id.co.fxcorp.db.AntriDB;
import id.co.fxcorp.db.AntriModel;
import id.co.fxcorp.db.PlaceDB;
import id.co.fxcorp.db.PlaceModel;
import id.co.fxcorp.util.Dpi;

public class DialogChoosePlace {

    public static AlertDialog open(final Context context, final PlaceModel place) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_choose_place, null);
        ImageView img_photo = view.findViewById(R.id.img_photo);
        TextView txt_name = view.findViewById(R.id.txt_name);
        TextView txt_description = view.findViewById(R.id.txt_description);
        TextView txt_info = view.findViewById(R.id.txt_info);
        final AppCompatButton btn_negative = view.findViewById(R.id.btn_negative);
        final AppCompatButton btn_positive = view.findViewById(R.id.btn_positive);

        Drawable drw_edit = context.getResources().getDrawable(R.drawable.ic_edit);
        drw_edit.setColorFilter(context.getResources().getColor(R.color.orange_400), PorterDuff.Mode.SRC_ATOP);
        txt_name.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null , drw_edit, null);
        txt_name.setCompoundDrawablePadding(Dpi.px(10));
        txt_name.setPadding(Dpi.px(40), 0, 0 ,0);

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Glide.with(img_photo)
             .load(place.getPhoto())
             .into(img_photo);

        if (place.isOnline()) {
            btn_positive.setText("LANJUTKAN");
        }
        else {
            btn_positive.setText("BUKA ANTRIAN");
        }

        txt_name       .setText(place.getString(PlaceModel.NAME));
        txt_description.setText(place.getString(PlaceModel.DESCRIPTION));

        if (place.getNumberQty() == 0) {
            txt_info.setText("Tidak ada antrian");
        }
        else {
            txt_info.setText(place.getNumberQty() + " antrian");
        }

        txt_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    dialog.dismiss();
                    Intent intent = new Intent(context, PlaceActivity.class);
                    intent.setAction("OPEN_PLACE");
                    intent.putExtra(PlaceModel.PLACE_ID, place.getString(PlaceModel.PLACE_ID));
                    intent.putExtra(PlaceModel.NAME,     place.getString(PlaceModel.NAME));
                    ((Activity)context).startActivity(intent);
                } catch (Exception e) {
                    Log.e("DialogChoosePlace", "", e);
                }
            }
        });

        view.findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    Log.e("DialogChoosePlace", "", e);
                }
            }
        });

        final DialogInterface.OnClickListener positiveClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((Activity) context).startActivity(new Intent(context, Portal2Activity.class)
                        .putExtra(PlaceModel.PLACE_ID, place.getPlaceId())
                        .putExtra(PlaceModel.NAME, place.getName())
                );
            }
        };

        view.findViewById(R.id.btn_positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                try {
                    if (place.isOnline()) {
                        dialog.dismiss();
                        if (positiveClick != null) {
                            positiveClick.onClick(dialog, view.getId());
                        }
                    }
                    else {
                        PlaceDB.setOnline(place.getPlaceId(), true)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                dialog.dismiss();
                                if (positiveClick != null) {
                                    positiveClick.onClick(dialog, view.getId());
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Koneksi bermasalah, mohon coba lagi", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e("DialogChoosePlace", "", e);
                }
            }
        });
        dialog.show();
        return dialog;
    }

    public static AlertDialog close(final Context context, final PlaceModel place, final DialogInterface.OnClickListener positiveClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_choose_place, null);
        ImageView img_photo = view.findViewById(R.id.img_photo);
        TextView txt_name = view.findViewById(R.id.txt_name);
        TextView txt_description = view.findViewById(R.id.txt_description);
        TextView txt_info = view.findViewById(R.id.txt_info);
        AppCompatButton btn_positive = view.findViewById(R.id.btn_positive);

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Glide.with(img_photo)
                .load(place.getPhoto())
                .into(img_photo);

        btn_positive.setText("YA, TUTUP SEKARANG");

        txt_name    .setText("Anda akan menutup " + place.getString(PlaceModel.NAME) + "?");

        if (place.getNumberQty() == 0) {
            txt_description.setText("");
            txt_info.setText("Tidak ada antrian tersisa");
        }
        else {
            txt_description.setText("Seluruh Antrian yang tersisa akan dibatalkan.");
            txt_info.setText(place.getNumberQty() + " antrian tersisa");
        }

        view.findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    Log.e("DialogChoosePlace", "", e);
                }
            }
        });
        view.findViewById(R.id.btn_positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                try {
                    AntriDB.getAntriListAtPlace(place.getPlaceId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                            while (iterator.hasNext()) {
                                iterator.next().getRef().removeValue();
                            }
                            PlaceDB.setOnline(place.getPlaceId(), false)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dialog.dismiss();
                                    if (positiveClick != null) {
                                        positiveClick.onClick(dialog, view.getId());
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Koneksi bermasalah, mohon coba lagi", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } catch (Exception e) {
                    Log.e("DialogChoosePlace", "", e);
                }
            }
        });
        dialog.show();
        return dialog;
    }

    public static AlertDialog take(final Context context, final PlaceModel place) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_choose_place, null);
        ImageView img_photo = view.findViewById(R.id.img_photo);
        final TextView txt_name = view.findViewById(R.id.txt_name);
        final TextView txt_description = view.findViewById(R.id.txt_description);
        final TextView txt_info = view.findViewById(R.id.txt_info);
        final AppCompatButton btn_positive = view.findViewById(R.id.btn_positive);

        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Glide.with(img_photo)
                .load(place.getPhoto())
                .into(img_photo);

        btn_positive.setText("AMBIL ANTRIAN");
        txt_name    .setText(place.getString(PlaceModel.NAME));
        txt_description.setText(place.getString(PlaceModel.DESCRIPTION));

        long number   = place.getNumberQty();
        long duration = place.getLong(PlaceModel.DURATION);
        if (number == 0) {
            txt_info.setText("Tidak ada antrian");
        }
        else {
            if (duration == 0) {
                txt_info.setText(number + " antrian | Tidak dapat diestimasi");
            }
            else {
                txt_info.setText(number + " antrian | " + (duration * number) + " menit");
            }
        }
        view.findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    Log.e("DialogChoosePlace", "", e);
                }
            }
        });
        view.findViewById(R.id.btn_positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    btn_positive.setEnabled(false);
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(PlaceDB.PLACE);
                    ref.child(place.getPlaceId()).runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                            PlaceModel place = new PlaceModel(mutableData);
                            if (place == null) {
                                return Transaction.success(mutableData);
                            }
                            long new_number = place.getNumberLast() + 1;
                            if (place.getNumberCurrent() == 0) {
                                place.put(PlaceModel.NUMBER_CURRENT, new_number);
                            }
                            place.put(PlaceModel.NUMBER_LAST, new_number);
                            place.put(PlaceModel.NUMBER_QTY, place.getNumberQty() + 1);
                            mutableData.setValue(place);
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean commited, @Nullable DataSnapshot dataSnapshot) {
                            if (commited) {
                                PlaceModel place = new PlaceModel(dataSnapshot);
                                long new_number = place.getNumberLast();

                                final AntriModel antri = new AntriModel();
                                antri.cust_id   = AppInfo.getUserId();
                                antri.cust_name = AppInfo.getUserName();
                                antri.cust_photo = AppInfo.getUserPhoto();
                                antri.number = new_number;
                                antri.place_id   = place.getPlaceId();
                                antri.place_name = place.getName();
                                antri.place_photo = place.getPhoto();
                                antri.created_time = System.currentTimeMillis();

                                AntriDB.insert(antri).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        txt_info.setText("Nomor Anda: " + antri.number + "");
                                        btn_positive.setText("OK");
                                        btn_positive.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                dialog.dismiss();
                                            }
                                        });
                                        btn_positive.setEnabled(true);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        btn_positive.setEnabled(true);
                                        Toast.makeText(context, "Koneksi berasalah, Mohon coba lagi", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else {
                                btn_positive.setEnabled(true);
                                Toast.makeText(context, "Koneksi berasalah, Mohon coba lagi", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e("DialogChoosePlace", "", e);
                }
            }
        });
        dialog.show();
        return dialog;
    }



}
