package id.co.fxcorp.message;

import android.content.Context;
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

import id.co.fxcorp.db.ChatModel;
import id.co.fxcorp.db.UserDB;
import id.co.fxcorp.ngantri.R;
import id.co.fxcorp.ngantri.SimpleRecyclerVH;
import id.co.fxcorp.util.DateUtil;

public class ChatHolder extends RecyclerView.ViewHolder {


    TextView txt_name;
    TextView  txt_text;
    TextView  txt_time;
    ImageView img_square;
    ImageView img_landscape;
    ImageView img_potrait ;
    View      vw_angle;
    ProgressBar prg_image;
    View      lyt_selected;

    public ChatHolder(View itemView) {
        super(itemView);
        txt_name = itemView.findViewById(R.id.txt_name);
        txt_text = itemView.findViewById(R.id.txt_text);
        txt_time = itemView.findViewById(R.id.txt_time);
        img_square = itemView.findViewById(R.id.img_square);
        img_landscape = itemView.findViewById(R.id.img_landscape);
        img_potrait = itemView.findViewById(R.id.img_potrait);
        vw_angle = itemView.findViewById(R.id.vw_angle);
        prg_image = itemView.findViewById(R.id.prg_image);
        lyt_selected = itemView.findViewById(R.id.lyt_selected);
    }

    public void bind(ItemHolder item) {


    }

    public static class ItemHolder extends ChatModel {

        private int[] NAME_COLOURS = new int[]{R.color.name_1, R.color.name_2, R.color.name_3,
                R.color.name_4, R.color.name_5, R.color.name_6,
                R.color.name_7, R.color.name_8, R.color.name_9, R.color.name_10};

        String time;
        SpannableStringBuilder text_span;
        int name_color;

        public ItemHolder(Context context, ChatModel ref) {
            super();
            copyFrom(ref);
            time = DateUtil.formatTime(ref.created_time);
            if (!TextUtils.isEmpty(ref.text)) {
                text_span = new SpannableStringBuilder();
                text_span.append(ref.text);
                String space = "";
                if (time.length() > 5) {
                    space = "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;";
                }
                else {
                    space = "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;";
                }
                if (ref.userid.equals(UserDB.MySELF.id)) {
                    space += "&#160;&#160;&#160;&#160;";
                }
                text_span.append(Html.fromHtml(space));
            }
            name_color = context.getResources().getColor(NAME_COLOURS[Long.valueOf(number).intValue() % NAME_COLOURS.length]);
        }

    }

}
