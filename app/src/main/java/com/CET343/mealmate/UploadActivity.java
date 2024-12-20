package com.CET343.mealmate;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class UploadActivity extends AppCompatActivity {

    ImageView uploadImage;
    Button saveButton, addIngredientButton;
    EditText uploadRecipe, uploadDesc;
    LinearLayout ingredientsContainer;
    String imageURL;
    Uri uri;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        uploadImage = findViewById(R.id.uploadImage);
        uploadDesc = findViewById(R.id.uploadDesc);
        uploadRecipe = findViewById(R.id.uploadRecipe);
        ingredientsContainer = findViewById(R.id.ingredientsContainer);
        addIngredientButton = findViewById(R.id.addIngredientButton);
        saveButton = findViewById(R.id.saveButton);

        // Set up ActivityResultLauncher for image picking
        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                uri = data.getData();
                                uploadImage.setImageURI(uri);
                            }
                        } else {
                            Toast.makeText(UploadActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Open the image picker when the user clicks on the image view
        uploadImage.setOnClickListener(view -> {
            Intent photoPicker = new Intent(Intent.ACTION_PICK);
            photoPicker.setType("image/*");
            activityResultLauncher.launch(photoPicker);
        });

        // Add initial ingredient boxes
        for (int i = 0; i < 3; i++) {
            addIngredientBox(null);
        }

        // Handle adding new ingredient boxes
        addIngredientButton.setOnClickListener(v -> addIngredientBox(null));

        // Handle save button click
        saveButton.setOnClickListener(view -> saveData());
    }

    private void addIngredientBox(String ingredientText) {
        EditText ingredientBox = new EditText(this);
        ingredientBox.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        ingredientBox.setHint("Ingredient");
        ingredientBox.setText(ingredientText != null ? ingredientText : "");
        ingredientBox.setPadding(16, 16, 16, 16);
        ingredientsContainer.addView(ingredientBox);
    }

    // Save data to Firebase
    // Save data to Firebase
    public void saveData() {
        // If no image is selected, set imageURL to null
        if (uri != null) {
            // Create a reference for the image in Firebase Storage
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReference()
                    .child("Android Image")
                    .child(uri.getLastPathSegment());

            // Show a progress dialog while the image is uploading
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setView(R.layout.progress_layout);
            AlertDialog dialog = builder.create();
            dialog.show();

            // Upload the file to Firebase Storage
            storageReference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                taskSnapshot.getStorage().getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            imageURL = uri.toString();
                            uploadData();
                            dialog.dismiss();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(UploadActivity.this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
            }).addOnFailureListener(e -> {
                Toast.makeText(UploadActivity.this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        } else {
            // If no image is selected, just upload the data without imageURL
            imageURL = null;
            uploadData();
        }
    }

    public void uploadData() {
        String recipe = uploadRecipe.getText().toString().trim();
        String desc = uploadDesc.getText().toString().trim();

        if (recipe.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Collect all ingredients into a List<String>
        List<String> ingredients = new ArrayList<>();
        for (int i = 0; i < ingredientsContainer.getChildCount(); i++) {
            View child = ingredientsContainer.getChildAt(i);
            if (child instanceof EditText) {
                String ingredient = ((EditText) child).getText().toString().trim();
                if (!ingredient.isEmpty()) {
                    ingredients.add(ingredient);
                }
            }
        }

        if (ingredients.isEmpty()) {
            Toast.makeText(this, "Please add at least one ingredient", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a List<Boolean> to store the ingredient states, set all to false
        List<Boolean> ingredientStates = new ArrayList<>();
        for (int i = 0; i < ingredients.size(); i++) {
            ingredientStates.add(false);  // Set each ingredient state to false by default
        }

        // Create a DataClass object with the recipe data, ingredients, and ingredient states
        DataClass dataClass = new DataClass(recipe, desc, ingredients, ingredientStates, imageURL);

        // Save the recipe to Firebase
        FirebaseDatabase.getInstance().getReference("Recipes").child(recipe)
                .setValue(dataClass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(UploadActivity.this, "Saved successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(UploadActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

}
