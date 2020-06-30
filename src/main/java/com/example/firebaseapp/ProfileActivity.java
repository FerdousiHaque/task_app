package com.example.firebaseapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;

import Model.Data;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private EditText titleup;
    private EditText noteup;
    private Button update;
    private Button delete;
    private Toolbar toolbar;
    private FloatingActionButton floatingActionButton;
    private RecyclerView recyclerView;


    //variable to update & delete
    private String title;
    private String note;
    private String post_key;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.toolbar_home);
        setActionBar(toolbar);
        getActionBar().setTitle("Your Task app");

        firebaseAuth=FirebaseAuth.getInstance();
        FirebaseUser mUser = firebaseAuth.getCurrentUser();
        String uid = mUser.getUid();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("TaskNote").child(uid);

        databaseReference.keepSynced(false);
        recyclerView = findViewById(R.id.recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

       floatingActionButton = findViewById(R.id.floatingActionButton);
       floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder myDialog = new AlertDialog.Builder(ProfileActivity.this);
                LayoutInflater inflater = LayoutInflater.from(ProfileActivity.this);


                View myview= inflater.inflate(R.layout.custominputfield,null);

                myDialog.setView(myview);
                final AlertDialog dialog=myDialog.create();

                final EditText title = myview.findViewById(R.id.edt_title);
                final EditText note = myview.findViewById(R.id.edt_note);

                Button button= myview.findViewById(R.id.btn_save);

                button.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(View view) {
                        String mtittle = title.getText().toString().trim();
                        String mnote = note.getText().toString().trim();

                        if (TextUtils.isEmpty(mtittle)) {
                            title.setError("Field Required");
                            return;
                        }
                        if (TextUtils.isEmpty(mnote)) {
                            title.setError("Field Required");
                            return;
                        }

                        String id= databaseReference.push().getKey();
                        String date = DateFormat.getDateInstance().format(new Date());

                        Data data = new Data(mtittle,mnote,date,id);

                        databaseReference.child(id).setValue(data);
                        Toast.makeText(getApplicationContext(), "Data Inserted!",Toast.LENGTH_SHORT).show();

                        dialog.dismiss();

                    }
                });
                dialog.show();

            }
        });

    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        //finish();

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Data, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(
                Data.class,
                R.layout.item_data,
                MyViewHolder.class,
                databaseReference
        ) {
            @Override
            protected void populateViewHolder(MyViewHolder myViewHolder, final Data data, final int i) {
                myViewHolder.setTitle(data.getTittle());
                myViewHolder.setNote(data.getNote());
                myViewHolder.setDate(data.getData());

                myViewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        post_key=getRef(i).getKey();
                        title = data.getTittle();
                        note = data.getNote();

                        updateData();
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        View view;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            view=itemView;
        }

        public void setTitle(String title) {
            TextView textView = view.findViewById(R.id.title);
            textView.setText(title);
        }
        public void setNote(String note) {
            TextView textView = view.findViewById(R.id.note);
            textView.setText(note);
        }
        public void setDate(String date) {
            TextView textView = view.findViewById(R.id.date);
            textView.setText(date);
        }

    }

    public void updateData() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(ProfileActivity.this);
        LayoutInflater inflater = LayoutInflater.from(ProfileActivity.this);


        View myview= inflater.inflate(R.layout.updateinputfield,null);
        myDialog.setView(myview);
        final AlertDialog dialog = myDialog.create();
        dialog.show();

        titleup = myview.findViewById(R.id.edt_titleupdate);
        noteup = myview.findViewById(R.id.edt_noteupdate);

        titleup.setText(title);
        titleup.setSelection(title.length());

        noteup.setText(note);
        noteup.setSelection(note.length());

        update = myview.findViewById(R.id.btn_update);
        delete = myview.findViewById(R.id.btn_cancel);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                title=titleup.getText().toString().trim();
                note=noteup.getText().toString().trim();

                String mData = DateFormat.getDateInstance().format(new Date());
                Data data = new Data(title,note,mData,post_key);

                databaseReference.child(post_key).setValue(data);
                Toast.makeText(ProfileActivity.this, "Task Updated", Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference.child(post_key).removeValue();
                Toast.makeText(ProfileActivity.this, "Task Deleted", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case  R.id.logout1:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}