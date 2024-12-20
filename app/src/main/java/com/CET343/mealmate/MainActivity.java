package com.CET343.mealmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    FloatingActionButton fab;
    RecyclerView recyclerView;
    List<DataClass> dataList;
    DatabaseReference databaseReference;
    ValueEventListener eventListener;
    TextView textView;
    MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recyclerView);
        textView = findViewById(R.id.user_details);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        dataList = new ArrayList<>();
        adapter = new MyAdapter(MainActivity.this, dataList, new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DataClass dataClass) {
                // Navigate to RecipeDetailActivity and pass the data
                Intent intent = new Intent(MainActivity.this, RecipeDetailActivity.class);
                intent.putExtra("recipeData", dataClass); // Pass the DataClass object directly
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Recipes");
        dialog.show();

        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    DataClass dataClass = itemSnapshot.getValue(DataClass.class);
                    dataList.add(dataClass);
                }
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
            }
        });

        // Add swipe to delete and swipe right to trigger onItemClick functionality
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Get the recipe item that was swiped
                int position = viewHolder.getAdapterPosition();

                if (position == RecyclerView.NO_POSITION || dataList.isEmpty()) {
                    // If the position is invalid or the list is empty, do nothing
                    return;
                }

                DataClass dataClass = dataList.get(position);

                // If swiped right, trigger the onItemClick functionality
                if (direction == ItemTouchHelper.LEFT) {
                    // Trigger the onItemClick method directly
                    adapter.listener.onItemClick(dataClass); // Call onItemClick directly
                }

                // If swiped left, show a confirmation dialog to delete the recipe
                if (direction == ItemTouchHelper.RIGHT) {
                    // Show a confirmation dialog to delete the recipe
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Delete Recipe")
                            .setMessage("Are you sure you want to delete this recipe?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                // Delete from Firebase database
                                databaseReference.child(dataClass.getDataTitle()).removeValue()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                // Update the dataList and RecyclerView
                                                dataList.remove(position);
                                                adapter.notifyItemRemoved(position);

                                                // If the list is now empty, handle any necessary UI updates (e.g., show a message)
                                                if (dataList.isEmpty()) {
                                                    // Optional: Show a message or empty state
                                                    Toast.makeText(MainActivity.this, "No recipes left", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(MainActivity.this, "Failed to delete recipe", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            })
                            .setNegativeButton("No", (dialog, which) -> {
                                adapter.notifyItemChanged(position); // Revert swipe
                            })
                            .show();
                }
            }
        };

// Attach ItemTouchHelper to RecyclerView
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);


        // Attach ItemTouchHelper to RecyclerView
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, UploadActivity.class);
            startActivity(intent);
        });

        textView.setText(user.getEmail());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu); // Inflate the menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            FirebaseAuth.getInstance().signOut(); // Log out the user
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
