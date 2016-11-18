package com.xmx.mygallery;

import android.app.Activity;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.xmx.mygallery.Tools.CrashHandler;
import com.xmx.mygallery.Tools.Data.DataManager;

import org.xutils.x;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by The_onE on 2016/1/3.
 */
public class Application extends android.app.Application {

    private static Application instance;

    public static Application getInstance() {
        return instance;
    }

    private List<Activity> activityList = new LinkedList<>();

    //添加Activity到容器中
    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    //遍历所有Activity并finish
    public void exit() {
        for (Activity activity : activityList) {
            activity.finish();
        }
        System.exit(0);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG);

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);

        DataManager.getInstance().setContext(this);

        Fresco.initialize(this);
    }
}
