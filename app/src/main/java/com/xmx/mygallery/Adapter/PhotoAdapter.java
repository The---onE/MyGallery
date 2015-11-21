package com.xmx.mygallery.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.xmx.mygallery.Entities.AlbumItem;
import com.xmx.mygallery.ImageView.GifImageView;
import com.xmx.mygallery.R;

public class PhotoAdapter extends BaseAdapter {
    private Context context;
    private AlbumItem album;

    public PhotoAdapter(Context context, AlbumItem album) {
        this.context = context;
        this.album = album;
    }

    @Override
    public int getCount() {
        return album.getPaths().size();
    }

    @Override
    public String getItem(int position) {
        return album.getPaths().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        GifImageView iv;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.photo_item, null);
            holder = new ViewHolder();
            holder.iv = (GifImageView) convertView.findViewById(R.id.photo_img_view);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.iv.setImageResource(R.drawable.pic_loading);
        holder.iv.setImageByPathLoader(album.getPaths().get(position));
        return convertView;
    }
}
