package com.auvehassan.scopedstorage;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

private static final int PICK_FILE = 1;
private final List<String> permissionsToRequire = new ArrayList<>();

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    initPermission();

    findViewById(R.id.browseAlbum).setOnClickListener(v -> {
        startActivity(new Intent(this, BrowseAlbumActivity.class));
    });

    findViewById(R.id.addImageToAlbum).setOnClickListener(v -> {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.demo);
        String displayName = System.currentTimeMillis() + ".jpg";
        String mimeType = "image/jpeg";
        Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
        addBitmapToAlbum(bitmap, displayName, mimeType, compressFormat);
    });

    findViewById(R.id.downloadFile).setOnClickListener(v -> {
        String fileUrl = "https://raw.githubusercontent.com/auvehassan/AndroidScopedStorage/main/android.txt";
        String fileName = "android.txt";

        new Thread(() -> downloadFile(fileUrl, fileName)).start();
    });

    findViewById(R.id.pickFile).setOnClickListener(v -> {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            pickFileAndCopyUriToExternalFilesDir();
        } else {
            Toast.makeText(this, "Build version is not supported", Toast.LENGTH_SHORT).show();
        }
    });
}

private void addBitmapToAlbum(Bitmap bitmap, String displayName, String mimeType, Bitmap.CompressFormat compressFormat) {
    ContentValues values = new ContentValues();
    values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
    values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
    } else {
        values.put(MediaStore.MediaColumns.DATA, Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_DCIM + "/" + displayName);
    }

    Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

    if(uri != null) {
        OutputStream outputStream = null;
        try {
            outputStream = getContentResolver().openOutputStream(uri);
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        if(outputStream != null) {
            bitmap.compress(compressFormat, 100, outputStream);
            try {
                outputStream.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Add bitmap to album succeeded.", Toast.LENGTH_SHORT).show();
        }
    }
}

private void initPermission() {
    if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        permissionsToRequire.add(Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        permissionsToRequire.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    String[] arr = permissionsToRequire.toArray(new String[0]);

    if(!permissionsToRequire.isEmpty()) {
        ActivityCompat.requestPermissions(this, arr, 0);
    }
}

@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if(requestCode == 0) {
        for(int grantResult : grantResults) {
            if(grantResult != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "You must allow all the permissions.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }
}


@RequiresApi(api = Build.VERSION_CODES.KITKAT)
private void pickFileAndCopyUriToExternalFilesDir() {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("*/*");
    startActivityForResult(intent, PICK_FILE);
}

@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if(requestCode == PICK_FILE) {
        if(resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if(uri != null) {
                String fileName = getFileNameByUri(uri);
                copyUriToExternalFilesDir(uri, fileName);
            }
        }
    }
}

private void copyUriToExternalFilesDir(Uri uri, String fileName) {
    try {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempDir = getExternalFilesDir("temp");

        if(inputStream != null && tempDir != null) {
            File file = new File(tempDir + "/" + fileName);
            FileOutputStream fos = new FileOutputStream(file);
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            byte[] byteArray = new byte[]{(byte) (1024 >>> 8), (byte) 1024};

            int bytes = bis.read(byteArray);
            while(bytes > 0) {
                bos.write(byteArray, 0, bytes);
                bos.flush();
                bytes = bis.read(byteArray);
            }
            bos.close();
            fos.close();
            runOnUiThread(() -> Toast.makeText(this, "Copy file succeeded into:\n" + tempDir, Toast.LENGTH_SHORT).show());
        }

    } catch(Exception eInputStream) {
        eInputStream.printStackTrace();
    }
}

private String getFileNameByUri(Uri uri) {
    String fileName = System.currentTimeMillis() + "";
    Cursor cursor = getContentResolver().query(uri, null, null, null, null);
    if(cursor != null && cursor.getCount() > 0) {
        cursor.moveToFirst();
        fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
        cursor.close();
    }
    return fileName;
}

private void downloadFile(String fileUrl, String fileName) {
    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        Toast.makeText(this, "You must use device running Android 10 or higher", Toast.LENGTH_SHORT).show();
        return;
    }

    try {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(8000);

        InputStream inputStream = connection.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if(uri != null) {
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            if(outputStream != null) {
                BufferedOutputStream bos = new BufferedOutputStream(outputStream);
                byte[] buffer = new byte[]{(byte) (1024 >>> 8), (byte) 1024};
                int bytes = bis.read(buffer);
                while(bytes >= 0) {
                    bos.write(buffer, 0, bytes);
                    bos.flush();
                    bytes = bis.read(buffer);
                }
                bos.close();

                runOnUiThread(() -> Toast.makeText(MainActivity.this, fileName + " is in Download directory now", Toast.LENGTH_SHORT).show());
            }
        }
        bis.close();
    } catch(Exception e) {
        e.printStackTrace();
    }

}
}