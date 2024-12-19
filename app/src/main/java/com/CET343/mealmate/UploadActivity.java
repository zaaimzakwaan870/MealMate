package com.CET343.mealmate;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UploadActivity extends AppCompatActivity {

    ImageView uploadImage;
    Button saveButton;
    EditText uploadRecipe, uploadDesc, uploadIngredients;
    String imageURL;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        uploadImage = findViewById(R.id.uploadImage);
        uploadDesc = findViewById(R.id.uploadDesc);
        uploadRecipe = findViewById(R.id.uploadRecipe);
        uploadIngredients = findViewById(R.id.uploadIngredient);
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

        // Handle save button click
        saveButton.setOnClickListener(view -> saveData());
    }

    // Save data to Firebase
    public void saveData() {
        if (uri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

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
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Uri urlImage = task.getResult();
                            imageURL = urlImage.toString();
                            uploadData();
                        } else {
                            Toast.makeText(UploadActivity.this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(UploadActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        }).addOnFailureListener(e -> {
            Toast.makeText(UploadActivity.this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }

    // Upload the data to Firebase Database
    public void uploadData() {
        String recipe = uploadRecipe.getText().toString().trim();
        String desc = uploadDesc.getText().toString().trim();
        String ingredients = uploadIngredients.getText().toString().trim();

        if (recipe.isEmpty() || desc.isEmpty() || ingredients.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        DataClass dataClass = new DataClass(recipe, desc, ingredients, imageURL);

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
