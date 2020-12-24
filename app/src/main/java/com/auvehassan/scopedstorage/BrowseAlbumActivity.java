package com.auvehassan.scopedstorage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ViewTreeObserver;

import java.util.ArrayList;

public class BrowseAlbumActivity extends AppCompatActivity {

ArrayList<Uri> imageList = new ArrayList<Uri>();

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_browse_album);
    RecyclerView recyclerView = findViewById(R.id.recyclerView);
    recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            findViewById(R.id.recyclerView).getViewTreeObserver().removeOnPreDrawListener(this);
            int columns = 3;
            int imageSize = recyclerView.getWidth() / columns;
            AlbumRecyclerviewAdapter adapter = new AlbumRecyclerviewAdapter(BrowseAlbumActivity.this, imageList, imageSize);
            recyclerView.setLayoutManager(new GridLayoutManager(BrowseAlbumActivity.this, columns));
            recyclerView.setAdapter(adapter);
            loadImages(adapter);
            return false;
        }
    });
}


private void loadImages(AlbumRecyclerviewAdapter adapter) {
        @SuppressLint("Recycle")
        Cursor cursor = getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null,
                        null,
                        null,
                        MediaStore.MediaColumns.DATE_ADDED);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                imageList.add(uri);
            }
            cursor.close();

        runOnUiThread(adapter :: notifyDataSetChanged);
    }
}