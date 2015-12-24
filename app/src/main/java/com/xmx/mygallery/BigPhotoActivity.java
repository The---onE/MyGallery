package com.xmx.mygallery;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.xmx.mygallery.Entities.FixedSpeedScroller;
import com.xmx.mygallery.ImageView.BigGifImageView;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class BigPhotoActivity extends Activity {
    RelativeLayout layout;
    //JazzyViewPager vp;
    ViewPager vp;
    String path;
    ArrayList<String> paths;
    int index;
    boolean playFlag = false;

    private RelativeLayout setPhoto(RelativeLayout l, String path, int sum, int index) {

        TextView tv = (TextView) l.findViewById(R.id.big_photo_index);
        if (sum <= 0) {
            l.removeView(tv);
        } else {
            tv.setText("" + index + "/" + sum);
        }

        TextView name = (TextView) l.findViewById(R.id.big_photo_name);
        int end = path.lastIndexOf("/");
        if (end != -1) {
            name.setText(path.substring(end + 1));
        } else {
            name.setText(null);
        }

        final BigGifImageView iv = (BigGifImageView) l.findViewById(R.id.big_photo);
        boolean flag = iv.setImageByPathLoader(path);

        LinearLayout gifButtons = (LinearLayout) l.findViewById(R.id.big_photo_gif_button);
        LinearLayout photoButtons = (LinearLayout) l.findViewById(R.id.big_photo_photo_button);
        Button limit = (Button) photoButtons.findViewById(R.id.big_photo_limit);
        limit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (iv.limited()) {
                    ((Button) v).setText(R.string.unlimited);
                } else {
                    ((Button) v).setText(R.string.limited);
                }
            }
        });

        if (!flag) {
            l.removeView(gifButtons);
        } else {
            Button last = (Button) gifButtons.findViewById(R.id.big_photo_last);
            last.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iv.lastFrame();
                }
            });

            Button upend = (Button) gifButtons.findViewById(R.id.big_photo_upend);
            upend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iv.upend();
                }
            });

            Button pause = (Button) gifButtons.findViewById(R.id.big_photo_pause);
            pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iv.pause();
                }
            });

            Button play = (Button) gifButtons.findViewById(R.id.big_photo_play);
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iv.play();
                }
            });

            Button next = (Button) gifButtons.findViewById(R.id.big_photo_next);
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iv.nextFrame();
                }
            });

            limit.setVisibility(View.INVISIBLE);
        }
        return l;
    }

    protected boolean checkIsImage(String path) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        return opts.outMimeType != null;
    }

    private void setFlipDuration() {
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            Scroller scroller = new Scroller(vp.getContext());
            mScroller.set(vp, scroller);
        } catch (Exception e) {
        }
    }

    private void cancelFlipDuration() {
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(vp.getContext());
            mScroller.set(vp, scroller);
        } catch (Exception e) {
        }
    }

    private void nextPage() {
        int cp = vp.getCurrentItem();
        cp = (cp + 1) % paths.size();
        vp.setCurrentItem(cp);
    }

    private void lastPage() {
        int cp = vp.getCurrentItem();
        cp = (cp - 1 + paths.size()) % paths.size();
        vp.setCurrentItem(cp);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.big_photo_activity);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        layout = (RelativeLayout) (findViewById(R.id.photo_layout));

        paths = new ArrayList<>();
        index = getIntent().getIntExtra("index", -1);

        if (index == -1) {
            path = getIntent().getStringExtra("path");
            if (path == null) {
                Uri uri = getIntent().getData();
                if (uri != null) {
                    path = uri.getPath();
                }
            }

            if (path != null) {
                int end = path.lastIndexOf("/");
                if (end != -1) {
                    String dir;
                    dir = path.substring(0, end);

                    File fileDir = new File(dir);
                    File[] files = fileDir.listFiles();
                    for (File file : files) {
                        String s = file.getPath();
                        if (checkIsImage(s)) {
                            paths.add(s);
                        }
                    }
                    index = paths.indexOf(path);
                }
            }
        } else {
            paths = getIntent().getStringArrayListExtra("paths");
            path = paths.get(index);
        }

        if (paths.size() > 1 && index != -1) {
            //vp = new JazzyViewPager(this);
            vp = (ViewPager) layout.findViewById(R.id.big_photo_viewpager);

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
                    RelativeLayout l = (RelativeLayout) getLayoutInflater().inflate(R.layout.big_photo_item, null);
                    setPhoto(l, paths.get(position), paths.size(), position + 1);

                    container.addView(l);
                    l.setTag("layout" + position);
                    //vp.setObjectForPosition(l, position);
                    return l;
                }

                @Override
                public int getCount() {
                    return paths.size();
                }
            });
            vp.setCurrentItem(index);

            LinearLayout buttonLayout = (LinearLayout) layout.findViewById(R.id.big_photo_flip_button);

            Button lastPage = (Button) buttonLayout.findViewById(R.id.big_photo_last_page);
            lastPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lastPage();
                }
            });

            SharedPreferences sp = getSharedPreferences("FLIP", Context.MODE_PRIVATE);
            final int interval = sp.getInt("FlipInterval", 1000);
            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    nextPage();
                    handler.postDelayed(this, interval);
                }
            };
            Button autoPlay = (Button) buttonLayout.findViewById(R.id.big_photo_auto_flip);
            autoPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!playFlag) {
                        handler.postDelayed(runnable, interval);
                        ((Button) v).setText(R.string.stop_auto_flip);
                        cancelFlipDuration();
                        playFlag = true;
                    } else {
                        handler.removeCallbacks(runnable);
                        ((Button) v).setText(R.string.auto_flip);
                        setFlipDuration();
                        playFlag = false;
                    }
                }
            });

            Button nextPage = (Button) buttonLayout.findViewById(R.id.big_photo_next_page);
            nextPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nextPage();
                }
            });

        } else {
            vp = (ViewPager) layout.findViewById(R.id.big_photo_viewpager);
            LinearLayout buttonLayout = (LinearLayout) layout.findViewById(R.id.big_photo_flip_button);
            layout.removeView(vp);
            layout.removeView(buttonLayout);

            RelativeLayout l = (RelativeLayout) getLayoutInflater().inflate(R.layout.big_photo_item, null);
            setPhoto(l, path, 0, 0);
            layout.addView(l);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        for (int i = 0; i < vp.getChildCount(); i++) {
            RelativeLayout layout = (RelativeLayout) vp.getChildAt(i);
            BigGifImageView iv = (BigGifImageView) layout.findViewById(R.id.big_photo);
            iv.setImageByPathLoader(iv.getPath());
        }
    }
}
