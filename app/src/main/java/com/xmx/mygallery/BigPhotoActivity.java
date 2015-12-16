package com.xmx.mygallery;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xmx.mygallery.ImageView.BigGifImageView;

import java.io.File;
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
        RelativeLayout photoButtons = (RelativeLayout) l.findViewById(R.id.big_photo_photo_button);

        Button anticlockwise = (Button) photoButtons.findViewById(R.id.big_photo_anticlockwise);
        anticlockwise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv.anticlockwiseRotation();
            }
        });

        Button clockwise = (Button) photoButtons.findViewById(R.id.big_photo_clockwise);
        clockwise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv.clockwiseRotation();
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
        }
        return l;
    }

    protected boolean checkIsImage(String path) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        return opts.outMimeType != null;
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
            vp = new ViewPager(this);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            vp.setLayoutParams(params);

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
            layout.addView(vp);
            vp.setCurrentItem(index);

            LinearLayout buttonLayout = new LinearLayout(this);
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            buttonLayout.setLayoutParams(p);
            buttonLayout.setGravity(Gravity.CENTER);
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

            Button nextPage = new Button(this);
            nextPage.setLayoutParams(new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            nextPage.setText("上一页");
            nextPage.setTextSize(12);
            nextPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int cp = vp.getCurrentItem();
                    cp = (cp - 1 + paths.size()) % paths.size();
                    vp.setCurrentItem(cp);
                }
            });
            buttonLayout.addView(nextPage);

            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    int cp = vp.getCurrentItem();
                    cp = (cp + 1) % paths.size();
                    vp.setCurrentItem(cp);
                    handler.postDelayed(this, 1000);
                }
            };
            Button autoPlay = new Button(this);
            autoPlay.setLayoutParams(new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            autoPlay.setText("播放");
            autoPlay.setTextSize(12);
            autoPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!playFlag) {
                        handler.postDelayed(runnable, 1000);
                        ((Button) v).setText("停止");
                        playFlag = true;
                    } else {
                        handler.removeCallbacks(runnable);
                        ((Button) v).setText("播放");
                        playFlag = false;
                    }
                }
            });
            buttonLayout.addView(autoPlay);

            Button lastPage = new Button(this);
            lastPage.setLayoutParams(new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            lastPage.setText("下一页");
            lastPage.setTextSize(12);
            lastPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int cp = vp.getCurrentItem();
                    cp = (cp + 1) % paths.size();
                    vp.setCurrentItem(cp);
                }
            });
            buttonLayout.addView(lastPage);

            layout.addView(buttonLayout);

        } else {
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
