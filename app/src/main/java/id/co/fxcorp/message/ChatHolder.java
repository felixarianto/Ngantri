package id.co.fxcorp.message;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import id.co.fxcorp.db.ChatModel;
import id.co.fxcorp.db.UserDB;
import id.co.fxcorp.ngantri.R;
import id.co.fxcorp.ngantri.SimpleRecyclerVH;
import id.co.fxcorp.util.DateUtil;

public class ChatHolder extends RecyclerView.ViewHolder {


    View     pg_date;
    TextView txt_date;
    TextView txt_name;
    TextView  txt_text;
    TextView  txt_time;
    TextView  txt_notif;
    ImageView img_square;
    ImageView img_landscape;
    ImageView img_potrait ;
    ProgressBar prg_image;
    View      bg_time;
    View      lyt_selected;
    View      vw_buble;

    public ChatHolder(View itemView) {
        super(itemView);
        pg_date  = itemView.findViewById(R.id.pg_date);
        txt_date = itemView.findViewById(R.id.txt_date);
        txt_name = itemView.findViewById(R.id.txt_name);
        txt_text = itemView.findViewById(R.id.txt_text);
        txt_time = itemView.findViewById(R.id.txt_time);
        txt_notif = itemView.findViewById(R.id.txt_notif);
        img_square = itemView.findViewById(R.id.img_square);
        img_landscape = itemView.findViewById(R.id.img_landscape);
        img_potrait = itemView.findViewById(R.id.img_potrait);
        bg_time    = itemView.findViewById(R.id.bg_time);
        prg_image = itemView.findViewById(R.id.prg_image);
        lyt_selected = itemView.findViewById(R.id.lyt_selected);
        vw_buble    = itemView.findViewById(R.id.vw_buble);
    }

    public static class ItemHolder extends ChatModel {

        private int[] NAME_COLOURS = new int[]{R.color.name_1, R.color.name_2, R.color.name_3,
                R.color.name_4, R.color.name_5, R.color.name_6,
                R.color.name_7, R.color.name_8, R.color.name_9, R.color.name_10};

        String date;
        String time;
        SpannableStringBuilder text_span;
        int name_color;

        public ItemHolder(Context context, ChatModel ref) {
            super();
            copyFrom(ref);
            if (DateUtil.isSameDay(ref.created_time, System.currentTimeMillis())) {
                date = "Hari ini";
            }
            else {
                date = new SimpleDateFormat("dd MMMM yy").format(ref.created_time);
            }
            time = DateUtil.formatTime(ref.created_time);
            if (!TextUtils.isEmpty(ref.text)) {
                text_span = new SpannableStringBuilder();
                text_span.append(ref.text);
                String space = "";
                if (time.length() > 5) {
                    space = "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;";
                }
                else {
                    space = "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;";
                }
                if (ref.userid.equals(UserDB.MySELF.id)) {
                    space += "&#160;&#160;";
                }
                text_span.append(Html.fromHtml(space));
            }
            Integer color = COLOR_ASSIGN.get(userid);
            if (color == null) {
                color = context.getResources().getColor(NAME_COLOURS[COLOR_ASSIGN.size() % NAME_COLOURS.length]);
                COLOR_ASSIGN.put(userid, color);
            }
            name_color = color;
        }

        private static HashMap<String, Integer> COLOR_ASSIGN = new HashMap<>();
    }

}
