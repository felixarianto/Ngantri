package id.co.fxcorp.ngantri;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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

import java.util.Calendar;
import java.util.Iterator;

import id.co.fxcorp.db.AntriDB;
import id.co.fxcorp.db.AntriModel;
import id.co.fxcorp.db.PlaceDB;
import id.co.fxcorp.db.PlaceModel;
import id.co.fxcorp.message.MessagingActivity;
import id.co.fxcorp.util.DateUtil;

public class DialogChoosePlace {

    public static AlertDialog open(final Context context, final PlaceModel place) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_place, null);
        ImageView img_photo = view.findViewById(R.id.img_photo);
        TextView txt_name = view.findViewById(R.id.txt_name);
        TextView txt_description = view.findViewById(R.id.txt_description);
        TextView txt_info = view.findViewById(R.id.txt_info);
        final View btn_close  = view.findViewById(R.id.btn_close);
        final View btn_forum  = view.findViewById(R.id.btn_forum);
        final View btn_portal = view.findViewById(R.id.btn_portal);
        final View btn_home   = view.findViewById(R.id.btn_home);

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Glide.with(img_photo)
             .load(place.getPhoto())
             .into(img_photo);

        txt_name       .setText(place.getString(PlaceModel.NAME));
        txt_description.setText(place.getString(PlaceModel.DESCRIPTION));

        if (place.getNumberQty() == 0) {
            txt_info.setText("Tidak ada antrian");
        }
        else {
            txt_info.setText(place.getNumberQty() + " Antrian");
        }

        view.findViewById(R.id.lyt_open).setVisibility(View.VISIBLE);

        btn_home.setOnClickListener(new View.OnClickListener() {
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

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    Log.e("DialogChoosePlace", "", e);
                }
            }
        });

        btn_portal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                try {
                    dialog.dismiss();
                    context.startActivity(new Intent(context, Portal2Activity.class)
                            .putExtra(PlaceModel.PLACE_ID, place.getPlaceId())
                            .putExtra(PlaceModel.NAME, place.getName())
                    );
                } catch (Exception e) {
                    Log.e("DialogChoosePlace", "", e);
                }
            }
        });
        dialog.show();

        btn_forum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(view.getContext(), MessagingActivity.class);
                intent.putExtra("title",  place.getName());
                intent.putExtra("thumb",  place.getPhoto());
                intent.putExtra("group",  place.getPlaceId());
                view.getContext().startActivity(intent);
            }
        });
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
                            dialog.dismiss();
                            if (positiveClick != null) {
                                positiveClick.onClick(dialog, view.getId());
                            }
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

    public static AlertDialog take(final Context context, final PlaceModel place, final AntriModel exist) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_place, null);
        ImageView img_photo = view.findViewById(R.id.img_photo);
        final TextView txt_name = view.findViewById(R.id.txt_name);
        final TextView txt_description = view.findViewById(R.id.txt_description);
        final TextView txt_info = view.findViewById(R.id.txt_info);
        final Button btn_chat  = view.findViewById(R.id.btn_chat);
        final Button btn_antri = view.findViewById(R.id.btn_antri);
        final Button btn_book  = view.findViewById(R.id.btn_book);

        builder.setView(view);

        view.findViewById(R.id.lyt_take).setVisibility(View.VISIBLE);

        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Glide.with(img_photo)
                .load(place.getPhoto())
                .into(img_photo);

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


        btn_book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar cal = Calendar.getInstance();
                DatePickerDialog pickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int date) {
                        cal.set(Calendar.YEAR, year);
                        cal.set(Calendar.MONTH, month);
                        cal.set(Calendar.DATE, date);

                        insertAntrian(place
                        , cal.getTimeInMillis()
                        , new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                dialog.dismiss();
                            }
                        }
                        , new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                btn_antri.setEnabled(true);
                                Toast.makeText(context, "Koneksi berasalah, Mohon coba lagi", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }, cal.get(Calendar.YEAR) , cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
                pickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                pickerDialog.show();
            }
        });

        txt_name       .setText(place.getString(PlaceModel.NAME));
        txt_description.setText(place.getString(PlaceModel.DESCRIPTION));


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
        btn_antri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    btn_antri.setEnabled(false);
                    insertAntrian(place
                    , System.currentTimeMillis()
                    , new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dialog.dismiss();
                        }
                    }
                    , new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            btn_antri.setEnabled(true);
                            Toast.makeText(context, "Koneksi berasalah, Mohon coba lagi", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    Log.e("DialogChoosePlace", "", e);
                }
            }
        });
        btn_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(view.getContext(), MessagingActivity.class);
                intent.putExtra("title",  place.getName());
                intent.putExtra("thumb",  place.getPhoto());
                intent.putExtra("group",  place.getPlaceId());
                view.getContext().startActivity(intent);
            }
        });
        dialog.show();
        return dialog;
    }

    public static AlertDialog cancel(final Context context, final PlaceModel place, final AntriModel exist) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_place, null);
        ImageView img_photo = view.findViewById(R.id.img_photo);
        TextView txt_name = view.findViewById(R.id.txt_name);
        TextView txt_description = view.findViewById(R.id.txt_description);
        TextView txt_info = view.findViewById(R.id.txt_info);
        final View btn_close  = view.findViewById(R.id.btn_close);
        final View btn_chat2  = view.findViewById(R.id.btn_chat2);
        final View btn_cancel = view.findViewById(R.id.btn_cancel);

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Glide.with(img_photo)
                .load(place.getPhoto())
                .into(img_photo);

        txt_name       .setText(place.getString(PlaceModel.NAME));
        txt_description.setText(place.getString(PlaceModel.DESCRIPTION));

        long today = System.currentTimeMillis();

        if (DateUtil.isSameDay(exist.time, today)) {
            if (DateUtil.isSameDay(place.getLastOpen(), today) && place.getNumberCurrent() > 0) {
                txt_info.setText("Nomor " + exist.number + " | Saat ini" + place.getNumberCurrent());
            }
            else{
                txt_info.setText("Nomor " + exist.number + " | Hari ini");
            }
        }
        else {
            txt_info.setText("Nomor " + exist.number + " | " + DateUtil.formatDate(exist.time));
        }


        view.findViewById(R.id.lyt_cancel).setVisibility(View.VISIBLE);

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    Log.e("DialogChoosePlace", "", e);
                }
            }
        });


        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                try {
                    btn_cancel.setEnabled(false);
                    AntriDB.setComplete(exist.place_id, exist.id, "Batal")
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            btn_cancel.setEnabled(true);
                        }
                    });
                } catch (Exception e) {
                    Log.e("DialogChoosePlace", "", e);
                }
            }
        });
        dialog.show();

        btn_chat2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(view.getContext(), MessagingActivity.class);
                intent.putExtra("title",  place.getName());
                intent.putExtra("thumb",  place.getPhoto());
                intent.putExtra("group",  place.getPlaceId());
                view.getContext().startActivity(intent);
            }
        });
        return dialog;
    }

    private static void insertAntrian(final PlaceModel place, final long time, final OnSuccessListener onSuccessListener, final OnFailureListener onFailureListener) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(PlaceDB.PLACE);
        ref.child(place.getPlaceId()).child(PlaceDB.SEQNO).child(DateUtil.formatDateReverse(time)).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Long new_number = mutableData.getValue(Long.class);
                if (new_number == null) {
                    new_number = 1l;
                }
                else {
                    new_number++;
                }
                mutableData.setValue(new_number);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean commited, @Nullable DataSnapshot dataSnapshot) {
                if (commited) {
                    long  new_number = dataSnapshot.getValue(Long.class);
                    final AntriModel antri = new AntriModel();
                    antri.cust_id    = AppInfo.getUserId();
                    antri.cust_name  = AppInfo.getUserName();
                    antri.cust_photo = AppInfo.getUserPhoto();
                    antri.number     = new_number;
                    antri.place_id   = place.getPlaceId();
                    antri.place_name = place.getName();
                    antri.place_photo = place.getPhoto();
                    antri.time = time;

                    if (DateUtil.isSameDay(time, System.currentTimeMillis())) {
                        PlaceDB.setNumberQty(place.getPlaceId(), place.getNumberQty() + 1, new_number);
                    }

                    AntriDB.insert(antri).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
                }
                else {
                    onFailureListener.onFailure(databaseError.toException());
                }
            }
        });
    }

}
