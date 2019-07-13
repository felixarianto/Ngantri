package id.co.fxcorp.ngantri;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class SimpleRecyclerAdapter<T> extends RecyclerView.Adapter<SimpleRecyclerVH> {

    private final int[] LAYOUT;
    public SimpleRecyclerAdapter(int ...layout) {
        LAYOUT = layout;
    }

    public ArrayList<T> DATA = new ArrayList<>();

    @NonNull
    @Override
    public final SimpleRecyclerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SimpleRecyclerVH(LayoutInflater.from(parent.getContext()).inflate(LAYOUT[viewType], null));
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public final void onBindViewHolder(@NonNull SimpleRecyclerVH holder, int position) {
        onBind(holder, DATA.get(position), position);
    }

    public void onBind(SimpleRecyclerVH holder, T value, int position) {

    }

    @Override
    public int getItemCount() {
        return DATA.size();
    }

}
