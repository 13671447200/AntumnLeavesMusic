package com.ttdt.Service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ttdt.Activity.ConcretePlayMusicActivity;
import com.ttdt.MusicPlayerA;
import com.ttdt.R;
import com.ttdt.Util.AES;
import com.ttdt.Util.Cons;
import com.ttdt.Util.Custom.MainActivityObserver;
import com.ttdt.Util.Custom.MyJsonRequest;
import com.ttdt.Util.Util;
import com.ttdt.modle.Song;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import static com.ttdt.Util.Util.requestQueue;

public class MusicPlayerService extends Service {

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private List<Song> songArray = null;
    private int id = -2;
    private int position = 0;
    private int wyID= 0;

    public static int RANDOM = 1;
    public static int ORDER = 2;//列表循环
    public static int SINGLE = 3;

    public int MODE = ORDER;
    NotificationManager manager;
    //观察者
    private MyObservable observable;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            if(what == OPEN_AUDIO){
                openAudio((String) msg.obj,msg.arg1);
            }
        }
    };

    //添加观察者
    public void addObservable(Observer observer){
        observable.addObserver(observer);
    }
    @Override
    public void onCreate() {
        observable = new MyObservable();
    }

    public void setSongArray(List<Song> songArray,int id) {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.reset();
            this.songArray = null;
        }
        this.id = id;
        this.songArray = songArray;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    private MusicPlayerA.Stub iBinder = new MusicPlayerA.Stub() {
        MusicPlayerService service = MusicPlayerService.this;

        @Override
        public void setSongArray(List<Song> songArray,int id) throws RemoteException {
            service.setSongArray(songArray,id);
        }

        @Override
        public void openAudio(int position) throws RemoteException {
            service.openAudio(position);
        }

        @Override
        public int getSongID() throws RemoteException {
            return wyID;
        }

        @Override
        public String getAlbum() throws RemoteException {
            return songArray.get(position).getAlbumName();
        }

        @Override
        public void addObservable(MainActivityObserver myObserver) throws RemoteException {
            service.addObservable(myObserver);
        }

        @Override
        public boolean isOK() throws RemoteException {
            if(songArray == null || songArray.size() < 1){
                return false;
            }
            return true;
        }

        @Override
        public void start() throws RemoteException {
            service.start();
        }

        @Override
        public void pause() throws RemoteException {
            service.pause();
        }

        @Override
        public void stop() throws RemoteException {
            service.stop();
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return mediaPlayer.getCurrentPosition();
        }



        @Override
        public int getID() throws RemoteException {
            return id;
        }

        @Override
        public String getDuration() throws RemoteException {
            if (songArray != null && songArray.size() > position) {
                return songArray.get(position).getTime();
            }
            return "0";
        }

        @Override
        public String getArtist() throws RemoteException {
            if (songArray != null && songArray.size() > position) {
                return songArray.get(position).getArtist();
            }
            return null;
        }

        @Override
        public String getImageUrl() throws RemoteException {
            return songArray.get(position).getArtistImage();
        }

        @Override
        public String getName() throws RemoteException {
            if (songArray != null && songArray.size() > position) {
                return songArray.get(position).getName();
            }
            return null;
        }

        @Override
        public String getAudioPath() throws RemoteException {
            if (songArray != null && songArray.size() > position) {
                return songArray.get(position).getUrl();
            }
            return null;
        }

        @Override
        public void next() throws RemoteException {
            service.next();
        }

        @Override
        public void pre() throws RemoteException {
            service.pre();
        }

        @Override
        public void setPlayMode(int playmode) throws RemoteException {
            if(playmode!=RANDOM  && playmode != ORDER && playmode != SINGLE){
                if(MODE == RANDOM || MODE == ORDER){
                    MODE++;
                }else if(MODE == SINGLE){
                    MODE = RANDOM;
                }
            }else{
                MODE = playmode;
            }
        }

        @Override
        public int getPlayMode() throws RemoteException {
            return MODE;
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return mediaPlayer.isPlaying();
        }

        @Override
        public void seekTo(int position) throws RemoteException {
            mediaPlayer.seekTo(position);
        }

        @Override
        public int getAudioSessionId() throws RemoteException {
            return mediaPlayer.getAudioSessionId();
        }
    };

    private void pre() {
        if (mediaPlayer != null && songArray != null) {
            if(MODE == ORDER) {
                if (position == 0) {
                    position = songArray.size() - 1;
                } else {
                    position--;
                }
            }else if(MODE == RANDOM){
                position = new Random().nextInt(songArray.size()-1);
            }else if(MODE == SINGLE){
                return;
            }
            openAudio(position);
        }
    }


    private void next() {
        if (mediaPlayer != null && songArray != null) {
            if(MODE == ORDER) {
                if (position == songArray.size() - 1) {
                    position = 0;
                } else {
                    position++;
                }
            }else if(MODE == RANDOM){
                position = new Random().nextInt(songArray.size()-1);
            }else if(MODE == SINGLE){
                return;
            }
            openAudio(position);
        }
    }

    private void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    private void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void start() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            Song song = songArray.get(position);
            //当播放歌曲的时候，在状态显示正在播放，点击的时候，可以进入音乐播放页面
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            //最主要
            Intent intent = new Intent(this, ConcretePlayMusicActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.notification_music_playing)
                    .setContentTitle(song.getName()+"(天天动听正在播放)")
                    .setContentText(song.getArtist() + "--" +song.getAlbumName())
                    .setContentIntent(pendingIntent)
                    .build();
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            manager.notify(1, notification);
        }
    }

    private void openAudio(int position) {
        if (songArray != null && songArray.size() > position) {
            Song song = songArray.get(position);
            String url = song.getUrl();
            if(url == null){
                getSongUrl(song,position);
            }else{
                openAudio(url,position);
            }
        }else{
            Toast.makeText(this, "数据异常", Toast.LENGTH_SHORT).show();
        }
    }

    private void openAudio(String url,int position) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            } else {
                mediaPlayer.reset();
            }
            if (url != null) {
                mediaPlayer.setOnPreparedListener(new MyOnPreparedListener(position));
                mediaPlayer.setOnErrorListener(new MyOnErrorListener());
                mediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepareAsync();//异步准备
                observable.notifyChanged(songArray.get(position));
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (e != null) {
                Toast.makeText(this, e.getCause().toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            next();
        }
    }
    class MyOnErrorListener implements MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            next();
            return true;
        }
    }
    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener{

        int index;

        public MyOnPreparedListener(int position){
            index = position;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            position = this.index;
            wyID = songArray.get(index).getWyID();
            start();
        }
    }

    private int OPEN_AUDIO = 0;

    private void getSongUrl(final Song song, final int position) {
        try {
            String params = AES.getParams(song.getWyID());
//            JSONObject paramsJson = new JSONObject();
//            paramsJson.put("params", params[0]);
//            paramsJson.put("encSecKey", params[1]);
            final MyJsonRequest jor = new MyJsonRequest(Request.Method.POST,Cons.baseUrl + "weapi/song/enhance/player/url?csrf_token="
                    , params,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            try {
                                if(jsonObject.has("data")){
                                    JSONArray data = jsonObject.getJSONArray("data");
                                    if(data.length() > 0){
                                        JSONObject json = data.getJSONObject(0);
                                        if (json.has("url")) {
                                            String url = json.getString("url");
                                            if(url != null && !url.trim().equals("")){
                                                Message message = Message.obtain();
                                                message.what = OPEN_AUDIO;
                                                message.obj = url;
                                                message.arg1 = position;
                                                handler.sendMessage(message);
                                            }
                                        }
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    volleyError.printStackTrace();
                    Util.prompting(MusicPlayerService.this,"getUrl失败");
                }
            });
            requestQueue.add(jor);
        } catch (Exception e) {

        }
    }

    @Override
    public void onDestroy() {
        if(manager != null){
            manager.cancel(1);
        }
        super.onDestroy();
    }

    private class MyObservable extends Observable {
        public void notifyChanged(Song song){
            this.setChanged();
            this.notifyObservers(song);
        }
    }
}
