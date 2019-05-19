package id.co.fxcorp.ngantri;

import android.content.Context;
import android.content.DialogInterface;
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

import id.co.fxcorp.db.AntriDB;
import id.co.fxcorp.db.AntriModel;
import id.co.fxcorp.db.PlaceDB;
import id.co.fxcorp.db.PlaceModel;

public class DialogChoosePlace {

    public static AlertDialog open(final Context context, final PlaceModel place, final DialogInterface.OnClickListener positiveClick) {
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
             .load("https://anakjajan.files.wordpress.com/2017/07/dscf1931.jpg?w=474&h=593")
             .into(img_photo);

        if (place.isOnline()) {
            btn_positive.setText("LANJUTKAN ANTRIAN");
        }
        else {
            btn_positive.setText("BUKA ANTRIAN");
        }

        txt_name    .setText(place.getString(PlaceModel.NAME));
        txt_description.setText(place.getString(PlaceModel.DESCRIPTION));

        if (place.getNumberCurrent() == 0) {
            txt_info.setText("0 antrian");
        }
        else {
            txt_info.setText((1 + place.getNumberLast() - place.getNumberCurrent()) + " antrian");
        }

        view.findViewById(R.id.btn_negative).setOnClickListener(new View.OnClickListener() {
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
                .load("https://anakjajan.files.wordpress.com/2017/07/dscf1931.jpg?w=474&h=593")
                .into(img_photo);

        btn_positive.setText("TUTUP ANTRIAN");

        txt_name    .setText(place.getString(PlaceModel.NAME));
        txt_description.setText(place.getString(PlaceModel.DESCRIPTION));

        if (place.getNumberCurrent() == 0) {
            txt_info.setText("0 antrian");
        }
        else {
            txt_info.setText((1 + place.getNumberLast() - place.getNumberCurrent()) + " antrian");
        }

        view.findViewById(R.id.btn_negative).setOnClickListener(new View.OnClickListener() {
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
                    if (!place.isOnline()) {
                        dialog.dismiss();
                        if (positiveClick != null) {
                            positiveClick.onClick(dialog, view.getId());
                        }
                    }
                    else {
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
                .load("https://anakjajan.files.wordpress.com/2017/07/dscf1931.jpg?w=474&h=593")
                .into(img_photo);

        btn_positive.setText("AMBIL ANTRIAN");
        txt_name    .setText(place.getString(PlaceModel.NAME));
        txt_description.setText(place.getString(PlaceModel.DESCRIPTION));

        long number   = place.getNumberCurrent();
        if (place.getNumberCurrent() > 0) {
            number    = 1 + place.getNumberLast() - place.getNumberCurrent();
        }
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
        view.findViewById(R.id.btn_negative).setOnClickListener(new View.OnClickListener() {
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

                                AntriDB.insert(place.getPlaceId(), antri).addOnSuccessListener(new OnSuccessListener<Void>() {
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
