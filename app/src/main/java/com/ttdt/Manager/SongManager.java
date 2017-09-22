package com.ttdt.Manager;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ttdt.MyApplication;
import com.ttdt.Util.Cons;
import com.ttdt.Util.Custom.MyJsonRequest;
import com.ttdt.Util.Util;
import com.ttdt.modle.Song;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/9/21.
 */

public class SongManager {

    private Context context;

    private List<Song> localMusic = null;

    private SongManager() {
    }

    private SongManager(Context context) {
        this.context = context;
    }


    public static SongManager getInstance() {
        return SongManagerHolder.getInstance();
    }

    static class SongManagerHolder {
        static SongManager instance = null;

        public static SongManager getInstance() {
            if (instance == null) {
                instance = new SongManager(MyApplication.getInstance());
            }
            return instance;
        }
    }

    public void getLocalMusic(final GetSongHD getSongHD){

        if(localMusic != null && getSongHD != null){
            getSongHD.success(localMusic);
            return;
        }

        new Thread() {
            @Override
            public void run() {
                super.run();

                localMusic = new ArrayList<>();
                ContentResolver resolver = context.getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        MediaStore.Audio.Media.DISPLAY_NAME,//视频文件在sdcard的名称
                        MediaStore.Audio.Media.DURATION,//视频总时长
                        MediaStore.Audio.Media.DATA,//视频的绝对地址
                        MediaStore.Audio.Media.ARTIST,//歌曲的演唱者

                };
                Cursor cursor = resolver.query(uri, objs, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {

                        Song song = new Song();

                        localMusic.add(song);//写在上面

                        String name = cursor.getString(0);//视频的名称
                        song.setName(name);

                        long duration = cursor.getLong(1);//视频的时长
                        song.setTime(String.valueOf(duration));

                        String data = cursor.getString(2);//视频的播放地址
                        song.setUrl(data);

                        String artist = cursor.getString(3);//艺术家
                        song.setArtist(artist);
                    }
                    cursor.close();
                }
                if(getSongHD != null){
                    getSongHD.success(localMusic);
                }
            }
        }.start();
    }

    public void rankDataByNet(int id, final GetSongHD getSongHD) {
        try {
            //Cons.baseUrl + "api/playlist/detail?" + id + ""
            String url = new StringBuilder()
                    .append(Cons.baseUrl)
                    .append("api/playlist/detail?")
                    .append("id=").append(id).toString();
            MyJsonRequest jor = new MyJsonRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            List<Song> songArray = getSongArrayByWy(jsonObject);
//                            getSongUrl(songArray);
                            getSongHD.success(songArray);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    getSongHD.fail();
                    //  Util.prompting(context, "请求数据失败" + volleyError.getMessage());
                }
            });
            Util.requestQueue.add(jor);
        } catch (Exception e) {
            getSongHD.fail();
            //Util.prompting(context, "请求数据失败，请检查网络是否正常！");
        }
    }

    private List<Song> getSongArrayByWy(JSONObject jsonObject) {
        List<Song> songArray = null;
        try {
            if (jsonObject.has("result")) {
                JSONObject result = jsonObject.getJSONObject("result");
                if (result.has("tracks")) {
                    JSONArray tracks = result.getJSONArray("tracks");
                    songArray = parseJsonSong(tracks);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return songArray;

    }

    public List<Song> parseJsonSong(JSONArray tracks) throws JSONException {
        List<Song> songArray = new ArrayList<>();
        for (int i = 0; i < tracks.length(); i++) {
            JSONObject obj = tracks.getJSONObject(i);
            if (obj.has("id")) {
                Song song = new Song();
                //音乐id
                int id = obj.getInt("id");
                song.setWyID(id);
                //时间
                if (obj.has("duration")) {
                    String duration = obj.getString("duration");
                    song.setTime(duration);
                }
                //歌曲名字
                if (obj.has("name")) {
                    String name = obj.getString("name");
                    song.setName(name);
                }
                //歌唱者
                if (obj.has("artists")) {
                    JSONArray artists = obj.getJSONArray("artists");
                    if (artists.length() > 0) {
                        JSONObject jsonAr = artists.getJSONObject(0);
                        if (jsonAr.has("name")) {
                            song.setArtist(jsonAr.getString("name"));
                        }
//                                    if (jsonAr.has("img1v1Url")) {
//                                        song.setArtistImage(jsonAr.getString("img1v1Url"));
//                                    }
                    }
                }
                //图片 专辑名字
                if (obj.has("album")) {
                    JSONObject album = obj.getJSONObject("album");
                    if (album.has("blurPicUrl")) {
                        song.setArtistImage(album.getString("blurPicUrl"));
                    }
                    if(album.has("name")){
                        song.setAlbumName(album.getString("name"));
                    }
                }
                songArray.add(song);
            }
        }
        return songArray;
    }

    public interface GetSongHD {
        void success(List<Song> songArray);

        void fail();
    }

}
