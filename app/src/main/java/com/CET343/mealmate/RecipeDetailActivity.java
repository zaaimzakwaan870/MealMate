package com.CET343.mealmate;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {

    private EditText recipeTitle, recipeDesc;
    private LinearLayout ingredientsLayout;
    private Button editSaveButton, addIngredientButton;
    private boolean isEditing = false;

    private String originalTitle, originalDesc;
    private List<String> originalIngredients;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener sensorEventListener;
    private static final int PERMISSION_REQUEST_SEND_SMS = 123;
    private long lastShakeTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        recipeTitle = findViewById(R.id.recipeTitle);
        recipeDesc = findViewById(R.id.recipeDesc);
        ingredientsLayout = findViewById(R.id.ingredientsLayout);
        editSaveButton = findViewById(R.id.editSaveButton);
        addIngredientButton = findViewById(R.id.addIngredientButton);

        // Initially, hide the Add Ingredient button
        addIngredientButton.setVisibility(View.GONE);

        // Get the recipe data from the intent
        DataClass dataClass = (DataClass) getIntent().getSerializableExtra("recipeData");
        if (dataClass != null) {
            originalTitle = dataClass.getDataTitle();
            originalDesc = dataClass.getDataDesc();
            originalIngredients = new ArrayList<>(dataClass.getDataIngredients());

            // Display recipe details
            recipeTitle.setText(originalTitle);
            recipeDesc.setText(originalDesc);

            // Add ingredients dynamically
            for (String ingredient : originalIngredients) {
                addIngredientView(ingredient);
            }
        }

        // Handle the Edit/Save button click
        editSaveButton.setOnClickListener(view -> {
            if (isEditing) {
                saveChanges();
            } else {
                enableEditing();
            }
        });

        // Handle the Add Ingredient button click
        addIngredientButton.setOnClickListener(view -> {
            if (isEditing) {
                addIngredientView("");
            }
        });

        // Initialize SensorManager and accelerometer
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Define shake detection logic
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float acceleration = (float) Math.sqrt(x * x + y * y + z * z);
                long currentTime = System.currentTimeMillis();

                if (acceleration > 12) { // Adjust threshold as needed
                    if (currentTime - lastShakeTime > 1000) { // Prevent multiple triggers
                        lastShakeTime = currentTime;
                        sendRecipeSms();
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };
    }

    private void addIngredientView(String ingredient) {
        LinearLayout ingredientLayout = new LinearLayout(this);
        ingredientLayout.setOrientation(LinearLayout.HORIZONTAL);
        ingredientLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        EditText ingredientEditText = new EditText(this);
        ingredientEditText.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        ingredientEditText.setText(ingredient);
        ingredientEditText.setHint("Ingredient");
        ingredientEditText.setEnabled(isEditing);

        CheckBox checkBox = new CheckBox(this);
        checkBox.setEnabled(true);

        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");
        deleteButton.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        deleteButton.setEnabled(isEditing);
        deleteButton.setOnClickListener(v -> ingredientsLayout.removeView(ingredientLayout));

        ingredientLayout.addView(ingredientEditText);
        ingredientLayout.addView(checkBox);
        ingredientLayout.addView(deleteButton);

        ingredientsLayout.addView(ingredientLayout);
    }

    private void enableEditing() {
        isEditing = true;
        recipeTitle.setEnabled(true);
        recipeDesc.setEnabled(true);

        addIngredientButton.setVisibility(View.VISIBLE);

        for (int i = 0; i < ingredientsLayout.getChildCount(); i++) {
            View child = ingredientsLayout.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout ingredientLayout = (LinearLayout) child;
                for (int j = 0; j < ingredientLayout.getChildCount(); j++) {
                    View ingredientChild = ingredientLayout.getChildAt(j);
                    ingredientChild.setEnabled(true);
                    if (ingredientChild instanceof Button) {
                        ingredientChild.setVisibility(View.VISIBLE);
                    }
                }
            }
        }

        editSaveButton.setText("Save");
    }

    private void saveChanges() {
        String updatedTitle = recipeTitle.getText().toString().trim();
        String updatedDesc = recipeDesc.getText().toString().trim();

        List<String> updatedIngredients = new ArrayList<>();
        for (int i = 0; i < ingredientsLayout.getChildCount(); i++) {
            View child = ingredientsLayout.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout ingredientLayout = (LinearLayout) child;
                EditText ingredientEditText = (EditText) ingredientLayout.getChildAt(0);
                String ingredient = ingredientEditText.getText().toString().trim();
                if (!ingredient.isEmpty()) {
                    updatedIngredients.add(ingredient);
                }
            }
        }

        if (updatedTitle.isEmpty() || updatedDesc.isEmpty() || updatedIngredients.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        DataClass updatedDataClass = new DataClass(updatedTitle, updatedDesc, updatedIngredients, new ArrayList<>(), null);
        FirebaseDatabase.getInstance().getReference("Recipes").child(updatedTitle)
                .setValue(updatedDataClass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to save changes", Toast.LENGTH_SHORT).show();
                    }
                });

        isEditing = false;
        recipeTitle.setEnabled(false);
        recipeDesc.setEnabled(false);

        for (int i = 0; i < ingredientsLayout.getChildCount(); i++) {
            View child = ingredientsLayout.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout ingredientLayout = (LinearLayout) child;
                for (int j = 0; j < ingredientLayout.getChildCount(); j++) {
                    View ingredientChild = ingredientLayout.getChildAt(j);
                    ingredientChild.setEnabled(false);
                    if (ingredientChild instanceof Button) {
                        ingredientChild.setVisibility(View.GONE);
                    }
                }
            }
        }

        addIngredientButton.setVisibility(View.GONE);
        editSaveButton.setText("Edit");
    }

    private void sendRecipeSms() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
                    PERMISSION_REQUEST_SEND_SMS);
            return;
        }

        String smsMessage = recipeTitle.getText().toString() + "\n\n" +
                recipeDesc.getText().toString() + "\n\nIngredients:\n";

        for (int i = 0; i < ingredientsLayout.getChildCount(); i++) {
            View child = ingredientsLayout.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout ingredientLayout = (LinearLayout) child;
                EditText ingredientEditText = (EditText) ingredientLayout.getChildAt(0);
                String ingredient = ingredientEditText.getText().toString().trim();
                if (!ingredient.isEmpty()) {
                    smsMessage += "- " + ingredient + "\n";
                }
            }
        }

        String phoneNumber = "1234567890"; // Replace with your contact's phone number
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, smsMessage, null, null);
            Toast.makeText(this, "Recipe sent via SMS!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (accelerometer != null) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted. Shake to send the recipe!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied. Cannot send SMS.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
