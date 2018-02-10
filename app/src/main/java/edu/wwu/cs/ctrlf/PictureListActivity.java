package edu.wwu.cs.ctrlf;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import edu.wwu.cs.ctrlf.dummy.DummyContent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Pictures. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PictureDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class PictureListActivity extends AppCompatActivity {

    public static final File ROOT_FOLDER = new File(Environment.getExternalStorageDirectory() + File.separator + "Pictures" + File.separator);
    /**
     * Random constant.
     */
    private static final int REQUEST_PICTURE = 37;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private Uri outputFileUri;

    // needed for gallery
    private final String image_titles[] = {
            "Img1",
    };
    private final Integer image_ids[] = {
            R.drawable.img1,
    };
    private ArrayList<CreateList> prepareData(){

        ArrayList<CreateList> theimage = new ArrayList<>();
        for(int i = 0; i< image_titles.length; i++){
            CreateList createList = new CreateList();
            createList.setImage_title(image_titles[i]);
            createList.setImage_ID(image_ids[i]);
            theimage.add(createList);
        }
        return theimage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //start of our code
        setContentView(R.layout.activity_picture_list);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.picture_list);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),2);
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<CreateList> createLists = prepareData();
        MyAdapter adapter = new MyAdapter(getApplicationContext(), createLists);
        recyclerView.setAdapter(adapter); //end

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(takePhotoOnClick());

        if (findViewById(R.id.picture_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        /*View recyclerView = findViewById(R.id.picture_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);*/
    }
/*
    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, DummyContent.ITEMS, mTwoPane));
    }
*/
    @NonNull
    private View.OnClickListener takePhotoOnClick() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ROOT_FOLDER.mkdirs();
                File outputFile = new File(ROOT_FOLDER, "IMG_" + System.currentTimeMillis() + ".jpg");
                outputFileUri = Uri.fromFile(outputFile);

                List<Intent> takeNewImageIntents = new ArrayList<>();
                Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                for (ResolveInfo res : getPackageManager().queryIntentActivities(captureIntent, 0)) {
                    String packageName = res.activityInfo.packageName;
                    Intent intent = new Intent(captureIntent);
                    intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                    intent.setPackage(packageName);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                    takeNewImageIntents.add(intent);
                }

                Intent pickExistingImageIntent = new Intent();
                pickExistingImageIntent.setType("image/*");
                pickExistingImageIntent.setAction(Intent.ACTION_GET_CONTENT);

                Intent chooser = Intent.createChooser(pickExistingImageIntent, "Select Source");
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, takeNewImageIntents.toArray(new Parcelable[0]));
                startActivityForResult(chooser, REQUEST_PICTURE);
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICTURE && resultCode == RESULT_OK) {

            Uri realUri = data.getData();

            Intent showPictureIntent = new Intent(getApplicationContext(), ShowPictureActivity.class);
            showPictureIntent.putExtra(ShowPictureActivity.PICTURE_URI, realUri);
            startActivity(showPictureIntent);

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("outputFileUri", outputFileUri.toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        outputFileUri = Uri.parse(savedInstanceState.getString("outputFileUri"));
    }

/*
    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final PictureListActivity mParentActivity;
        private final List<DummyContent.DummyItem> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DummyContent.DummyItem item = (DummyContent.DummyItem) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(PictureDetailFragment.ARG_ITEM_ID, item.id);
                    PictureDetailFragment fragment = new PictureDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.picture_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, PictureDetailActivity.class);
                    intent.putExtra(PictureDetailFragment.ARG_ITEM_ID, item.id);

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(PictureListActivity parent,
                                      List<DummyContent.DummyItem> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.picture_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mIdView.setText(mValues.get(position).id);
            holder.mContentView.setText(mValues.get(position).content);

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.content);
            }
        }
    }*/
}
