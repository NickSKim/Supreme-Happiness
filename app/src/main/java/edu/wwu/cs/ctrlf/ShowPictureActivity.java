package edu.wwu.cs.ctrlf;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class ShowPictureActivity extends AppCompatActivity {

    public static final String PICTURE_URI = "pictureUri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_picture);

        setSupportActionBar(this.<Toolbar>findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Uri sourceUri = getIntent().getExtras().getParcelable(PICTURE_URI);

        ImageView imageView = findViewById(R.id.image_to_show);
        imageView.setImageURI(sourceUri);
        Log.i("im", imageView.toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Uri sourceUri = savedInstanceState.getParcelable(PICTURE_URI);

        ImageView imageView = findViewById(R.id.image_to_show);
        imageView.setImageURI(sourceUri);
    }
}
