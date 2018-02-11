package edu.wwu.cs.ctrlf;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.SparseArray;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private CameraSource cameraSource;
    private SurfaceView cameraArea;
    private boolean running;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // start/stop button
        FloatingActionButton startStopButton = findViewById(R.id.start_stop_button);
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (running = !running) {
                    ((FloatingActionButton) view).setImageDrawable(Resources.getSystem()
                            .getDrawable(android.R.drawable.ic_media_pause, getTheme()));
                } else {
                    ((FloatingActionButton) view).setImageDrawable(Resources.getSystem()
                            .getDrawable(android.R.drawable.ic_media_play, getTheme()));
                }
            }
        });

        // help button
        FloatingActionButton helpButton = findViewById(R.id.help_Button);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(MainActivity.this);
                //setting custom layout to dialog
                dialog.setContentView(R.layout.help_dialog_layout);
                dialog.setTitle("Custom Dialog");
                //adding text dynamically
                TextView txt = (TextView) dialog.findViewById(R.id.textView);
                txt.setText(R.string.dialog_message);
                //adding button click event
                Button dismissButton = (Button) dialog.findViewById(R.id.button);
                dismissButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        // search
        SearchView searchView = (SearchView) findViewById(R.id.search_bar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                callSearch(query);
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//              if (searchView.isExpanded() && TextUtils.isEmpty(newText)) {
                if (newText.length() > 3) {
                    callSearch(newText);
                }
//              }
                return true;
            }

            public void callSearch(String query) {

                TextView view = findViewById(R.id.where_text_goes);
                String content = view.getText().toString()
                        .replaceAll("(?i)(?:<font.*?>|</font>)", "")
                        .replaceAll("(?i)" + query, "<font color=red>" + query + "</font>");
                view.setText(Html.fromHtml(content));
            }

        });

        // text recognition and camera setup
        TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        recognizer.setProcessor(new Processor());

        cameraSource = new CameraSource.Builder(getApplicationContext(), recognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setRequestedFps(1.0f)
                .setAutoFocusEnabled(true)
                .build();

        cameraArea = findViewById(R.id.camera_area);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 123);
                return;
            }
            cameraSource.start(cameraArea.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // permissions handling
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 123) {
            try {
                cameraSource.start(cameraArea.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // text detection and display
    private class Processor implements Detector.Processor<TextBlock> {
        @Override
        public void release() {

        }

        // detect
        @Override
        public void receiveDetections(Detector.Detections<TextBlock> detections) {
            if (running) {
                TextView text = findViewById(R.id.where_text_goes);
                // scrollbar(s)
                text.setMovementMethod(new ScrollingMovementMethod());

                SparseArray<TextBlock> blocks = detections.getDetectedItems();
                for (int i = 0; i < blocks.size(); i++) {
                    TextBlock block = blocks.valueAt(i);
                    for (Text component : block.getComponents()) {
                        runOnUiThread(new Appender(text, component));
                    }
                }
            }
        }

        // display
        private class Appender implements Runnable {
            private final TextView text;
            private final Text component;

            public Appender(TextView text, Text component) {
                this.text = text;
                this.component = component;
            }

            @Override
            public void run() {
                String s = component.getValue();
                CharSequence query = ((SearchView) findViewById(R.id.search_bar)).getQuery();
                if (!TextUtils.isEmpty(query) && s.contains(query)) {
                    if (!text.getText().toString().toLowerCase().replaceAll("[^a-zA-Z0-9 -]", "")
                            .contains(s.toLowerCase().replaceAll("[^a-zA-Z0-9 -]", ""))) {
                        text.append(Html.fromHtml(Html.escapeHtml(s)
                                .replaceAll("(?i)(" + query + ")", "<font color=red>" + query + "</font>") + "<br>"));
                    }
                }
            }
        }
    }
}
