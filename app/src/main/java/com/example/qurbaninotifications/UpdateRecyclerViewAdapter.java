package com.example.qurbaninotifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UpdateRecyclerViewAdapter extends RecyclerView.Adapter<UpdateRecyclerViewAdapter.MyViewHolder>{

    private Context context;
    private List<Updates> data;
    private LayoutInflater inflater;
    public UpdateRecyclerViewAdapter(Context context,List<Updates> data){
        this.context=context;
        this.data=data;
        inflater=LayoutInflater.from(context);
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.custom_row_for_update_rv_adapter,parent,false);
        MyViewHolder holder=new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        Updates current=data.get(position);
        holder.update.setText(current.getUpdate());
        holder.cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                data.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    protected static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView update;
        private ImageButton cross;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            update=itemView.findViewById(R.id.update_txt);
            cross=itemView.findViewById(R.id.close_btn);
        }
    }
}
