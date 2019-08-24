package id.co.fxcorp.message;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import id.co.fxcorp.db.ChatModel;
import id.co.fxcorp.db.UserDB;
import id.co.fxcorp.ngantri.R;
import id.co.fxcorp.util.DateUtil;
import id.co.fxcorp.util.GenericFileProvider;

public class ChatAdapter extends RecyclerView.Adapter<ChatHolder>{

    private static final String TAG = "ChatAdapter";

    public HashMap<String, ChatHolder.ItemHolder> SELECTED = new HashMap<>();
    public ArrayList<ChatHolder.ItemHolder> DATA = new ArrayList<>();

    @Override
    public long getItemId(int position) {
        return DATA.get(position).id.hashCode();
    }

    @Override
    public int getItemCount() {
        return DATA.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (DATA.get(position).userid.equals(UserDB.MySELF.id)) {
            return 1;
        }
        else {
            return 0;
        }
    }

    ArrayList<ChatHolder> holder_0;
    ArrayList<ChatHolder> holder_1;

    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (holder_0 == null) {
            holder_0 = new ArrayList<>();
            holder_1 = new ArrayList<>();

            holder_0.add(new ChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.messaging_adapter_left, null)));
            holder_0.add(new ChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.messaging_adapter_left, null)));
            holder_0.add(new ChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.messaging_adapter_left, null)));
            holder_0.add(new ChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.messaging_adapter_left, null)));
            holder_0.add(new ChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.messaging_adapter_left, null)));
            holder_0.add(new ChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.messaging_adapter_left, null)));

            holder_1.add(new ChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.messaging_adapter_right, null)));
            holder_1.add(new ChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.messaging_adapter_right, null)));
            holder_1.add(new ChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.messaging_adapter_right, null)));
            holder_1.add(new ChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.messaging_adapter_right, null)));
            holder_1.add(new ChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.messaging_adapter_right, null)));
            holder_1.add(new ChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.messaging_adapter_right, null)));
        }
        if (viewType == 1) {
            if (!holder_1.isEmpty()) {
                return holder_1.remove(0);
            }
            return new ChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.messaging_adapter_right, null));
        }
        else {
            if (!holder_0.isEmpty()) {
                return holder_0.remove(0);
            }
            return new ChatHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.messaging_adapter_left, null));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatHolder holder, int position) {
        long t0 = System.currentTimeMillis(), t1;
        final ChatHolder.ItemHolder item = DATA.get(position);
        int next = position - 1;
        int prev = position + 1;
        if (prev < DATA.size() && DATA.get(prev).userid.equals(item.userid)) {
            holder.vw_buble.setEnabled(false);
            holder.itemView.setPadding(0, 6, 0, 0);
            if (holder.txt_name != null) {
                holder.txt_name  .setText("");
                holder.txt_name  .setVisibility(View.GONE);
            }
        }
        else {
            holder.vw_buble.setEnabled(true);
            holder.itemView.setPadding(0, 30, 0, 0);
            if (holder.txt_name != null) {
                if (item.number == 0) {
                    holder.txt_name.setText(item.name);
                }
                else {
                    holder.txt_name.setText(new StringBuffer().append(item.number).append(" | ").append(item.name));
                }
                holder.txt_name.setTextColor(item.name_color);
                holder.txt_name.setVisibility(View.VISIBLE);
            }
        }

        if (prev < DATA.size() && DATA.get(prev).date.equals(item.date)) {
            holder.pg_date.setVisibility(View.GONE);
        }
        else {
            holder.txt_date.setText(item.date);
            holder.pg_date.setVisibility(View.VISIBLE);
        }


        if (item.call > 0) {
            holder.txt_notif.setText("Panggilan ke - " + item.call);
            holder.txt_notif.setVisibility(View.VISIBLE);
        }
        else {
            holder.txt_notif.setVisibility(View.GONE);
        }

        holder.txt_time.setText(item.time);
        if (item.text_span != null) {
            holder.txt_text.setText(item.text_span);
            holder.txt_text.setVisibility(View.VISIBLE);
        }
        else {
            holder.txt_text.setText("");
            holder.txt_text.setVisibility(View.GONE);
        }

        if (item.userid.equals(UserDB.MySELF.id)) {
            if (item.status == ChatModel.STATUS_SEND) {
                holder.txt_time.setCompoundDrawablesWithIntrinsicBounds(0 , 0, R.drawable.ic_sending, 0);
            }
            else {
                holder.txt_time.setCompoundDrawablesWithIntrinsicBounds(0 , 0, R.drawable.ic_delivered, 0);
            }
        }
        if (TextUtils.isEmpty(item.image)) {
            holder.bg_time.setVisibility(View.GONE);
            holder.txt_time.setEnabled(true);
            holder.img_landscape.setImageResource(0);
            holder.img_landscape.setVisibility(View.GONE);
            holder.img_square.setImageResource(0);
            holder.img_square.setVisibility(View.GONE);
            holder.img_potrait.setImageResource(0);
            holder.img_potrait.setVisibility(View.GONE);
            holder.prg_image.setIndeterminate(false);
            holder.prg_image.setVisibility(View.GONE);
        }
        else {
            holder.bg_time.setVisibility(View.VISIBLE);
            holder.txt_time.setEnabled(false);

            ImageView img_image;
            long scale = item.image_scale;
            if (scale == 1) {
                img_image = holder.img_potrait;

                holder.img_landscape.setImageResource(0);
                holder.img_landscape.setVisibility(View.GONE);
                holder.img_square.setImageResource(0);
                holder.img_square.setVisibility(View.GONE);
            }
            else if (scale == 2) {
                img_image = holder.img_landscape;
                holder.img_square.setImageResource(0);
                holder.img_square.setVisibility(View.GONE);
                holder.img_potrait.setImageResource(0);
                holder.img_potrait.setVisibility(View.GONE);
            }
            else {
                img_image = holder.img_square;
                holder.img_landscape.setImageResource(0);
                holder.img_landscape.setVisibility(View.GONE);
                holder.img_potrait.setImageResource(0);
                holder.img_potrait.setVisibility(View.GONE);
            }
            img_image.setVisibility(View.VISIBLE);
            holder.prg_image.setIndeterminate(true);
            holder.prg_image.setVisibility(View.VISIBLE);

            Glide.with(img_image).load(item.image).thumbnail(0.5f)
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (item.status == ChatModel.STATUS_DELIVERED) {
                            holder.prg_image.setIndeterminate(false);
                            holder.prg_image.setVisibility(View.GONE);
                        }
                        return false;
                    }

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }
            }).into(img_image);
            img_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri uri;
                        if (item.image.startsWith("http")) {
                            uri = Uri.parse(item.image);
                        }
                        else {
                            uri = GenericFileProvider.getUriForFile(view.getContext(), item.image);
                            view.getContext().grantUriPermission(view.getContext().getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                        intent.setDataAndType(uri,"image/*");
                        view.getContext().startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "", e);
                    }
                }
            });
        }

        if (SELECTED.containsKey(item.id)) {
            holder.lyt_selected.setVisibility(View.VISIBLE);
        }
        else {
            holder.lyt_selected.setVisibility(View.INVISIBLE);
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                SELECTED.put(item.id, item);
                holder.lyt_selected.setVisibility(View.VISIBLE);
                onSelectedChanged(SELECTED);
                return false;
            }
        });
        holder.lyt_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SELECTED.remove(item.id);
                view.setVisibility(View.INVISIBLE);
                onSelectedChanged(SELECTED);
            }
        });

        t1 = System.currentTimeMillis();
        Log.w(TAG, "onBindViewHolder " + (t1 - t0));
    }

    public void onSelectedChanged(HashMap<String, ChatHolder.ItemHolder> selected) {

    }

}
