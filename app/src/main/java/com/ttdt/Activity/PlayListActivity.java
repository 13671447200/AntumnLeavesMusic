package com.ttdt.Activity;

import android.content.Intent;
import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.bigkoo.svprogresshud.SVProgressHUD;
import com.ttdt.Adaper.SongListAdapter;
import com.ttdt.Manager.SongManager;
import com.ttdt.R;
import com.ttdt.Util.Util;
import com.ttdt.modle.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/9/21.
 */

public class PlayListActivity extends PlayMusicBaseActivity {

    private TextView tv_play_list_title;
    private String name;
    private int id;
    private SongManager songManager;
    private ListView list_view;
    List<Song> arraySong = new ArrayList<Song>();
    private SongListAdapter songListAdapter = null;

    @Override
    int getViewId() {
        return R.layout.activity_play_list;
    }

    @Override
    void setLister() {
        super.setLister();
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id1) {
                try {
                    if (service == null) {
                        return;
                    }
                    if (service.getID() != id) {
                        service.setSongArray(arraySong, id);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    service.openAudio(position);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 1000);
                    } else {
                        service.openAudio(position);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    void initData() {
        super.initData();
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        id = intent.getIntExtra("id", -1);
        tv_play_list_title.setText(name);
        songListAdapter = new SongListAdapter(arraySong, context);
        list_view.setAdapter(songListAdapter);
        songManager = SongManager.getInstance();
        if (id != -1) {
            getSongArrayByNet();
        } else {
            getSongArrayByLocal();
        }
    }

    private void getSongArrayByLocal() {
        songManager.getLocalMusic(new SongManager.GetSongHD() {
            @Override
            public void success(List<Song> songArray) {
                setDataAndInform(songArray, false);
            }

            @Override
            public void fail() {

            }
        });
    }

    private void getSongArrayByNet() {
        SVProgressHUD.showWithStatus(context, context.getString(R.string.loading));
        songManager.rankDataByNet(id, new SongManager.GetSongHD() {
            @Override
            public void success(List<Song> songArray) {
                if (SVProgressHUD.isShowing(context)) {
                    SVProgressHUD.dismiss(context);
                }
                setDataAndInform(songArray, false);
            }

            @Override
            public void fail() {
                if (SVProgressHUD.isShowing(context)) {
                    SVProgressHUD.dismiss(context);
                }
                Util.prompting(context, "请求数据失败");
            }
        });
    }

    @Override
    void initView() {
        super.initView();
        list_view = (ListView) findViewById(R.id.list_view);
        tv_play_list_title = (TextView) findViewById(R.id.tv_play_list_title);
    }

    private void setDataAndInform(List<Song> songArray, boolean isClean) {
        try {
            if (isClean) {
                arraySong.clear();
            }
            arraySong.addAll(songArray);
            songListAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onStart() {
        super.onStart();// ATTENTION: This was auto-generated to implement the App Indexing API.
    }

    @Override
    public void onStop() {
        super.onStop();
    }


}
