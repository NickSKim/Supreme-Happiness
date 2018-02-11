package edu.wwu.cs.ctrlf;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.FocusingProcessor;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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

        TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        recognizer.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {
                SparseArray<TextBlock> blocks = detections.getDetectedItems();
                for (int i = 0; i < blocks.size(); i++) {
                    TextBlock block = blocks.valueAt(i);
                    for (Text text : block.getComponents()) {
                        Log.i("Detected Text", text.getValue());
                    }
                }
            }
        });
        try (InputStream is = getContentResolver().openInputStream(sourceUri)) {
            recognizer.receiveFrame(new Frame.Builder()
                    .setBitmap(BitmapFactory.decodeStream(is))
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Uri sourceUri = savedInstanceState.getParcelable(PICTURE_URI);

        ImageView imageView = findViewById(R.id.image_to_show);
        imageView.setImageURI(sourceUri);
    }
}
