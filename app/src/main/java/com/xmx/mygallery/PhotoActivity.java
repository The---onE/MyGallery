package com.xmx.mygallery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.xmx.mygallery.Adapter.PhotoAdapter;
import com.xmx.mygallery.Entities.AlbumItem;
import com.xmx.mygallery.Tools.ActivityBase.BaseActivity;

public class PhotoActivity extends BaseActivity {

    AlbumItem album;
    TextView tv;
    GridView gv;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.photo_activity);

        album = (AlbumItem) getIntent().getExtras().get("album");
        if (album != null) {
            tv = getViewById(R.id.photo_path);
            String p = album.getPaths().get(0);
            int end = p.lastIndexOf("/");
            if (end != -1) {
                tv.setText(p.substring(0, end));
            } else {
                tv.setText(null);
            }

            gv = getViewById(R.id.photo_gridview);
            PhotoAdapter adapter = new PhotoAdapter(this, album);
            gv.setAdapter(adapter);
        }
    }

    @Override
    protected void setListener() {
        if (album != null) {
            gv.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(PhotoActivity.this, BigPhotoActivity.class);
                    intent.putExtra("paths", album.getPaths());
                    intent.putExtra("index", position);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {

    }
}
