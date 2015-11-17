package com.xmx.mygallery;

import android.app.Activity;
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
    BigGifImageView gif_view;
    String path;
    ArrayList<String> paths;
    int index;
    boolean flipFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.big_photo_activity);
        layout = (LinearLayout) (findViewById(R.id.photo_layout));

        index = getIntent().getIntExtra("index", -1);

        if (index == -1) {
            flipFlag = false;
            path = getIntent().getStringExtra("path");

            gif_view = new BigGifImageView(this);
            gif_view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            gif_view.setImageByPathLoader(path);

            layout.addView(gif_view);
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
                    LinearLayout l = new LinearLayout(BigPhotoActivity.this);
                    l.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT));
                    l.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                    l.setOrientation(LinearLayout.VERTICAL);

                    final BigGifImageView iv = new BigGifImageView(BigPhotoActivity.this);
                    iv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT));
                    boolean flag = iv.setImageByPathLoader(paths.get(position));

                    if (flag) {
                        LinearLayout buttonLayout = new LinearLayout(BigPhotoActivity.this);
                        buttonLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
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

                        Button pause = new Button(BigPhotoActivity.this);
                        pause.setText("暂停/播放");
                        pause.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        pause.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                iv.playPauseGif();
                            }
                        });
                        buttonLayout.addView(pause);

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

                    l.addView(iv);

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
