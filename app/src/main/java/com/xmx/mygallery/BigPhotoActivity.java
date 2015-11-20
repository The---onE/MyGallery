package com.xmx.mygallery;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.xmx.mygallery.ImageView.BigGifImageView;

import java.util.ArrayList;

public class BigPhotoActivity extends Activity {
    LinearLayout layout;
    //JazzyViewPager vp;
    ViewPager vp;
    String path;
    ArrayList<String> paths;
    int index;
    boolean flipFlag;

    private LinearLayout setPhoto(String path) {
        LinearLayout l = new LinearLayout(BigPhotoActivity.this);
        l.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        l.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        l.setOrientation(LinearLayout.VERTICAL);

        final BigGifImageView iv = new BigGifImageView(BigPhotoActivity.this);
        iv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        boolean flag = iv.setImageByPathLoader(path);
        l.addView(iv);

        if (flag) {
            LinearLayout buttonLayout = new LinearLayout(BigPhotoActivity.this);
            buttonLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 9));
            buttonLayout.setGravity(Gravity.CENTER);
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

            Button last = new Button(BigPhotoActivity.this);
            last.setText("后退");
            last.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            last.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iv.lastFrame();
                }
            });
            buttonLayout.addView(last);

            Button upend = new Button(BigPhotoActivity.this);
            upend.setText("倒放");
            upend.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            upend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iv.upend();
                }
            });
            buttonLayout.addView(upend);

            Button pause = new Button(BigPhotoActivity.this);
            pause.setText("暂停");
            pause.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iv.pause();
                }
            });
            buttonLayout.addView(pause);

            Button play = new Button(BigPhotoActivity.this);
            play.setText("播放");
            play.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iv.play();
                }
            });
            buttonLayout.addView(play);

            Button next = new Button(BigPhotoActivity.this);
            next.setText("前进");
            next.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iv.nextFrame();
                }
            });
            buttonLayout.addView(next);

            l.addView(buttonLayout);
        }
        return l;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.big_photo_activity);
        layout = (LinearLayout) (findViewById(R.id.photo_layout));

        index = getIntent().getIntExtra("index", -1);

        if (index == -1) {
            flipFlag = false;
            path = getIntent().getStringExtra("path");
            if (path == null) {
                path = getIntent().getData().toString();
                if (path != null) {
                    Uri uri = getIntent().getData();
                    path = uri.getPath();
                }
            }

            if (path != null) {
                LinearLayout l = setPhoto(path);
                layout.addView(l);
            }
        } else {
            flipFlag = true;
            paths = getIntent().getStringArrayListExtra("paths");

            //vp = new JazzyViewPager(this);
            vp = new ViewPager(this);
            vp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            //vp.setTransitionEffect(JazzyViewPager.TransitionEffect.Accordion);
            vp.setAdapter(new PagerAdapter() {
                @Override
                public boolean isViewFromObject(View arg0, Object arg1) {
                    return arg0 == arg1;
                }

                @Override
                public void destroyItem(ViewGroup container, int position, Object object) {
                    container.removeView((View) object);
                }

                @Override
                public Object instantiateItem(ViewGroup container, int position) {
                    LinearLayout l = setPhoto(paths.get(position));

                    container.addView(l);
                    //vp.setObjectForPosition(l, position);
                    return l;
                }

                @Override
                public int getCount() {
                    return paths.size();
                }
            });
            layout.addView(vp);
            vp.setCurrentItem(index);
        }
    }
}
