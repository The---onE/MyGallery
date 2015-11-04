package com.xmx.mygallery;

import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.xmx.mygallery.Adapter.AlbumAdapter;
import com.xmx.mygallery.Entities.AlbumItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private ListView albumGV;
    private List<AlbumItem> albumList;

    //设置获取图片的字段信息
    private static final String[] STORE_IMAGES = {
            MediaStore.Images.Media.DISPLAY_NAME, //名称
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.LONGITUDE, //经度
            MediaStore.Images.Media._ID, //id
            MediaStore.Images.Media.BUCKET_ID, //dir id 目录
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME //dir name 目录名称
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.album, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        albumGV = (ListView) getActivity().findViewById(R.id.album_listview);
        albumList = getPhotoAlbum();
        albumGV.setAdapter(new AlbumAdapter(albumList, getContext()));
        albumGV.setOnItemClickListener(albumClickListener);
    }

    AdapterView.OnItemClickListener albumClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getActivity(), PhotoActivity.class);
            intent.putExtra("album", albumList.get(position));
            startActivity(intent);
        }
    };

    //按相册获取图片信息
    private List<AlbumItem> getPhotoAlbum() {
        List<AlbumItem> albumList = new ArrayList<>();
        Cursor cursor = MediaStore.Images.Media.query(getActivity().getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, STORE_IMAGES,
                null, MediaStore.Images.Media.DATE_MODIFIED);
        Map<String, AlbumItem> countMap = new LinkedHashMap<>();
        AlbumItem pa;
        cursor.moveToLast();
        cursor.moveToNext();
        while (cursor.moveToPrevious()) {
            String path = cursor.getString(1);
            String id = cursor.getString(3);
            String dir_id = cursor.getString(4);
            String dir = cursor.getString(5);
            if (!countMap.containsKey(dir_id)) {
                pa = new AlbumItem();
                pa.setName(dir);
                pa.getPaths().add(path);
                countMap.put(dir_id, pa);
            } else {
                pa = countMap.get(dir_id);
                pa.increaseCount();
                pa.getPaths().add(path);
            }
        }
        cursor.close();
        Iterable<String> it = countMap.keySet();
        for (String key : it) {
            albumList.add(countMap.get(key));
        }
        return albumList;
    }
}
