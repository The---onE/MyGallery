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
import android.widget.EditText;
import android.widget.Toast;

import com.xmx.mygallery.Tools.ActivityBase.BaseActivity;

public class MainActivity extends BaseActivity {
    static final int GIF_DECODER = 1;
    static final int MOVIE = 2;
    private int selected;
    private long exitTime = 0;
    static long LONGEST_EXIT_TIME = 2000;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        Toolbar toolbar = getViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    private void loaderTypeDialog() {
        AlertDialog.Builder gifLoaderDialog = new AlertDialog.Builder(this);
        final int type = getSharedPreferences("LOADER", Context.MODE_PRIVATE).getInt("loaderType", MOVIE);
        selected = type - 1;
        gifLoaderDialog.setSingleChoiceItems(
                new String[]{"GifDecoder Mode", "Movie Mode"}, selected,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected = which;
                    }
                })
                .setTitle(R.string.gif_loader)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int choose = selected + 1;
                                if (choose != type) {
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

    private void flipIntervalDialog() {
        AlertDialog.Builder flipIntervalDialog = new AlertDialog.Builder(this);
        int interval = getSharedPreferences("FLIP", Context.MODE_PRIVATE).getInt("FlipInterval", 1000);
        final EditText intervalEdit = new EditText(this);
        intervalEdit.setText("" + interval);
        flipIntervalDialog.setView(intervalEdit)
                .setTitle(R.string.flip_interval)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String s = intervalEdit.getText().toString();
                                try {
                                    int interval = Integer.parseInt(s);
                                    SharedPreferences sp = getSharedPreferences("FLIP", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putInt("FlipInterval", interval);
                                    editor.apply();
                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(), "必须输入数字！", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.loader_type) {
            loaderTypeDialog();
            return true;
        } else if (id == R.id.flip_interval) {
            flipIntervalDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > LONGEST_EXIT_TIME) {
                Toast.makeText(getApplicationContext(), "再按一次退出", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
