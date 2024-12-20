package com.CET343.mealmate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ComponentActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class DetailActivity extends ComponentActivity {

    TextView detailDesc, detailRecipe, detailIngredients;
    ImageView detailImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        detailDesc = findViewById(R.id.detailDesc);
        detailIngredients = findViewById(R.id.detailIngredients);
        detailRecipe = findViewById(R.id.detailRecipe);
        detailImage = findViewById(R.id.detailImage);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            detailDesc.setText(bundle.getString("Description"));
            detailIngredients.setText(bundle.getString("Ingredients"));
            detailRecipe.setText(bundle.getString("Recipe"));
            Glide.with(this).load(bundle.getString("Image")).into(detailImage);
        }
    }
}