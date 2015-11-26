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

public class PhotoActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_activity);

        final AlbumItem album = (AlbumItem) getIntent().getExtras().get("album");
        if (album != null) {
            TextView tv = (TextView) findViewById(R.id.photo_path);
            String p = album.getPaths().get(0);
            int end = p.lastIndexOf("/");
            if (end != -1) {
                tv.setText(p.substring(0, end));
            } else {
                tv.setText(null);
            }

            GridView gv = (GridView) findViewById(R.id.photo_gridview);
            PhotoAdapter adapter = new PhotoAdapter(this, album);
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
}
