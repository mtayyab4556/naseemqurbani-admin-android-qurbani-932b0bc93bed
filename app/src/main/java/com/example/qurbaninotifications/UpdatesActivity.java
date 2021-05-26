package com.example.qurbaninotifications;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class UpdatesActivity extends AppCompatActivity {

    private UpdateRecyclerViewAdapter adapter;
    private List<Updates> data;
    private EditText updateInfo;
    private RecyclerView recyclerView;
    private Button addUpdate,subitUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updates);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("Updates");

        data=new ArrayList<>();
        adapter=new UpdateRecyclerViewAdapter(getApplicationContext(),data);

        updateInfo = findViewById(R.id.update_info_et);
        recyclerView = findViewById(R.id.recycler_view);
        addUpdate = findViewById(R.id.add_update_btn);
        subitUpdate=findViewById(R.id.submit_update_btn);

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);
        addUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!updateInfo.getText().toString().isEmpty()){
                    data.add(new Updates(updateInfo.getText().toString()));
                    adapter.notifyDataSetChanged();
                }
            }
        });

        subitUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myRef.setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            T.message(getApplicationContext(),"Update is Submited");
                        }
                    }
                });
            }
        });
    }
}
