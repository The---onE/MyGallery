package com.xmx.mygallery;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    static final int GIF_DECODER = 1;
    static final int MOVIE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem loaderType = menu.findItem(R.id.loader_type);
        SharedPreferences sp = getSharedPreferences("LOADER", Context.MODE_PRIVATE);
        int type = sp.getInt("loaderType", MOVIE);
        switch (type) {
            case MOVIE:
                loaderType.setTitle("Gif loader:Movie mode");
                break;
            case GIF_DECODER:
                loaderType.setTitle("Gif loader:GifDecoder mode");
                break;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.loader_type) {
            SharedPreferences sp = getSharedPreferences("LOADER", Context.MODE_PRIVATE);
            int type = sp.getInt("loaderType", MOVIE);
            SharedPreferences.Editor editor = sp.edit();
            switch (type) {
                case MOVIE:
                    editor.putInt("loaderType", GIF_DECODER);
                    item.setTitle("Gif loader:GifDecoder mode");
                    break;
                case GIF_DECODER:
                    editor.putInt("loaderType", MOVIE);
                    item.setTitle("Gif loader:Movie mode");
                    break;
            }
            editor.apply();
            finish();
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
