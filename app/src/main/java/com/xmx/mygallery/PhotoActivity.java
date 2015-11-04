package com.xmx.mygallery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.xmx.mygallery.Adapter.PhotoAdapter;
import com.xmx.mygallery.Entities.AlbumItem;

public class PhotoActivity extends Activity {
    private AlbumItem album;
    private PhotoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_activity);

        album = (AlbumItem) getIntent().getExtras().get("album");

        GridView gv = (GridView) findViewById(R.id.photo_gridview);
        adapter = new PhotoAdapter(this, album);
        gv.setAdapter(adapter);
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
