package id.co.fxcorp.ngantri;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import id.co.fxcorp.db.PlaceModel;

public class DialogChoosePlace {

    public static AlertDialog open(Context context, PlaceModel place, final DialogInterface.OnClickListener positiveClick) {
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

        btn_positive.setText("BUKA ANTRIAN");
        txt_name    .setText(place.getString(PlaceModel.NAME));
        txt_description.setText(place.getString(PlaceModel.DESCRIPTION));
        txt_info.setText(place.getInt(PlaceModel.NUMBER) + " antrian");

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
                    if (positiveClick != null) {
                        positiveClick.onClick(dialog, view.getId());
                    }
                } catch (Exception e) {
                    Log.e("DialogChoosePlace", "", e);
                }
            }
        });
        dialog.show();
        return dialog;
    }

    public static AlertDialog take(Context context, PlaceModel place, final DialogInterface.OnClickListener positiveClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_choose_place, null);
        TextView txt_name = view.findViewById(R.id.txt_name);
        TextView txt_description = view.findViewById(R.id.txt_description);
        TextView txt_info = view.findViewById(R.id.txt_info);
        AppCompatButton btn_positive = view.findViewById(R.id.btn_positive);

        builder.setView(view);
        final AlertDialog dialog = builder.create();

        btn_positive.setText("AMBIL ANTRIAN");
        txt_name    .setText(place.getString(PlaceModel.NAME));
        txt_description.setText(place.getString(PlaceModel.DESCRIPTION));

        int number   = place.getInt(PlaceModel.NUMBER);
        int duration = place.getInt(PlaceModel.DURATION);
        if (number == 0) {
            txt_info.setVisibility(View.INVISIBLE);
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
                    if (positiveClick != null) {
                        positiveClick.onClick(dialog, view.getId());
                    }
                } catch (Exception e) {
                    Log.e("DialogChoosePlace", "", e);
                }
            }
        });
        dialog.show();
        return dialog;
    }

}
