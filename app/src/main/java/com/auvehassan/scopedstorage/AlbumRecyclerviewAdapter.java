package com.auvehassan.scopedstorage;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

/****************************************
 *  Created by Auve on 12/23/2020         
 *  Email: auvehassan@gmail.com  
 ****************************************/

public class AlbumRecyclerviewAdapter extends RecyclerView.Adapter<AlbumRecyclerviewAdapter.ViewHolder> {

Context context;
List<Uri> imageList;
int imageSize;

public AlbumRecyclerviewAdapter(Context context, List<Uri> imageList, int imageSize) {
    this.context = context;
    this.imageList = imageList;
    this.imageSize = imageSize;
}

@NonNull
@Override
public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.album_item, parent, false);
    return new ViewHolder(view);
}

@Override
public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    imageView.getLayoutParams().width = imageSize;
    imageView.getLayoutParams().height = imageSize;
    Uri uri = imageList.get(position);
    RequestOptions options = new RequestOptions().placeholder(R.drawable.demo).override(imageSize, imageSize);
    Glide.with(context).load(uri).apply(options).into(imageView);
}

@Override
public int getItemCount() {
    return imageList.size();
}

ImageView imageView;
public class ViewHolder extends RecyclerView.ViewHolder {
    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.imageView);

    }
}
}