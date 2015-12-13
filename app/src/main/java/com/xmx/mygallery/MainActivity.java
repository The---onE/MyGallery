package com.xmx.mygallery;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    static final int GIF_DECODER = 1;
    static final int MOVIE = 2;
    private int loaderType;
    private int selected;
    private long exitTime = 0;
    static long LONGEST_EXIT_TIME = 2000;

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

        MenuItem loaderItem = menu.findItem(R.id.loader_type);
        loaderItem.setTitle("Gif Loader Mode");
        SharedPreferences sp = getSharedPreferences("LOADER", Context.MODE_PRIVATE);
        loaderType = sp.getInt("loaderType", MOVIE);
        /*switch (type) {
            case MOVIE:
                loaderItem.setTitle("Gif loader:Movie mode");
                break;
            case GIF_DECODER:
                loaderItem.setTitle("Gif loader:GifDecoder mode");
                break;
        }*/

        return true;
    }

    private void loaderTypeDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        int type = getSharedPreferences("LOADER", Context.MODE_PRIVATE).getInt("loaderType", MOVIE);
        dialog.setTitle("Gif Loader")
                .setSingleChoiceItems(
                        new String[]{"GifDecoder Mode", "Movie Mode"}, type - 1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selected = which;
                            }
                        })
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int choose = selected + 1;
                                if (choose != loaderType) {
                                    SharedPreferences sp = getSharedPreferences("LOADER", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putInt("loaderType", choose);
                                    editor.apply();
                                    finish();
                                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }).show();

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

            loaderTypeDialog();

            /*SharedPreferences.Editor editor = sp.edit();
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
            startActivity(intent);*/
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > LONGEST_EXIT_TIME) {
                Toast.makeText(getApplicationContext(), "Press Again to Exit", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
