package com.savor.ads.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.savor.ads.R;
import com.savor.ads.adapter.DownloadListAdapter;
import com.savor.ads.adapter.PlaylistAdapter;
import com.savor.ads.bean.MediaLibBean;
import com.savor.ads.bean.ProgramBean;
import com.savor.ads.bean.ProgramBeanResult;
import com.savor.ads.bean.SetBoxTopResult;
import com.savor.ads.core.AppApi;
import com.savor.ads.core.Session;
import com.savor.ads.database.DBHelper;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanghq on 2016/12/12.
 */

public class PlayListDialog extends Dialog {

    private Callback mCallback;

    private TextView mTitleTv;
    private TextView mDescTv;
    private TextView mShowDownloadTv;
    private GridView mPlaylistGv;
    private GridView mDownloadListGv;

    private PlaylistAdapter mPlaylistAdapter;
    private DownloadListAdapter mDownloadListAdapter;

    private Session mSession;

    private String mProDownloadPeriod;
    private String mAdvDownloadPeriod;
    private String mAdsDownloadPeriod;

    public PlayListDialog(Context context, Callback callback) {
        super(context, R.style.box_info_dialog_theme);
        mCallback = callback;
        mSession = Session.get(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_play_list);
        setDialogAttributes();

        mTitleTv = (TextView) findViewById(R.id.tv_title);
        mDescTv = (TextView) findViewById(R.id.tv_desc);
        mShowDownloadTv = (TextView) findViewById(R.id.tv_show_download_list);
        mPlaylistGv = (GridView) findViewById(R.id.gv_play_list);
        mDownloadListGv = (GridView) findViewById(R.id.gv_download_list);

        mPlaylistAdapter = new PlaylistAdapter(getContext(), null);
        mPlaylistGv.setAdapter(mPlaylistAdapter);
        mPlaylistGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mCallback != null) {
                    mCallback.onMediaItemSelect(position);
                    PlayListDialog.this.dismiss();
                }
            }
        });

        mDownloadListAdapter = new DownloadListAdapter(getContext(), null);
        mDownloadListGv.setAdapter(mDownloadListAdapter);

        mShowDownloadTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShowDownloadTv.setVisibility(View.GONE);
                mPlaylistGv.setVisibility(View.GONE);
                mDownloadListGv.setVisibility(View.VISIBLE);
                mDownloadListGv.requestFocus();

                mTitleTv.setText("下载列表");
                mDescTv.setText(String.format("广告版本：%s    节目版本：%s\r\n宣传片版本：%s",
                        mAdsDownloadPeriod, mProDownloadPeriod, mAdvDownloadPeriod));
            }
        });
    }


    private void setDialogAttributes() {
        Window window = getWindow(); // 得到对话框
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = WindowManager.LayoutParams.MATCH_PARENT;
        wl.height = WindowManager.LayoutParams.MATCH_PARENT;
        wl.gravity = Gravity.CENTER;
        window.setAttributes(wl);
    }

    public <T extends MediaLibBean> void showPlaylist(ArrayList< T> playlist) {
        super.show();
        mShowDownloadTv.setVisibility(View.VISIBLE);
        mPlaylistGv.setVisibility(View.VISIBLE);
        mDownloadListGv.setVisibility(View.GONE);
        mProDownloadPeriod = "";
        mAdvDownloadPeriod = "";
        mAdsDownloadPeriod = "";

        String adsPeriod = Session.get(getContext()).getAdsPeriod();
        String proPeriod = Session.get(getContext()).getProPeriod();
        mTitleTv.setText("播放列表");
        if (playlist != null) {
            mDescTv.setText(String.format("共%s个内容    广告版本：%s    节目版本：%s", playlist.size(), adsPeriod, proPeriod));
            mPlaylistAdapter.setPlaylist(playlist);
        }

        fillDownloadDetail();
        mShowDownloadTv.requestFocus();
    }

    private void fillDownloadDetail() {
        ArrayList<MediaLibBean> downloadList = new ArrayList<>();

        DBHelper dbHelper = DBHelper.get(getContext());
        Gson gson = new Gson();
        fillDownloadDataByType(ConstantValues.ADS_DATA_PATH, downloadList, dbHelper, gson);

        fillDownloadDataByType(ConstantValues.PRO_DATA_PATH, downloadList, dbHelper, gson);

        fillDownloadDataByType(ConstantValues.ADV_DATA_PATH, downloadList, dbHelper, gson);

        mDownloadListAdapter.setPlaylist(downloadList);
    }

    private void fillDownloadDataByType(String filePath, ArrayList<MediaLibBean> downloadList, DBHelper dbHelper, Gson gson) {
        String jsonData = FileUtils.read(filePath);
        if (!TextUtils.isEmpty(jsonData)) {
            ProgramBean programBean = null;
            try {
                if (ConstantValues.ADS_DATA_PATH.equals(filePath) || ConstantValues.ADV_DATA_PATH.equals(filePath)) {
                    // 宣传片和广告
                    ProgramBeanResult programBeanResult = gson.fromJson(jsonData, new TypeToken<ProgramBeanResult>() {
                    }.getType());
                    if (programBeanResult.getCode() == AppApi.HTTP_RESPONSE_STATE_SUCCESS && programBeanResult.getResult() != null) {
                        programBean = programBeanResult.getResult();
                    }
                } else {
                    // 节目单
                    SetBoxTopResult setBoxTopResult = gson.fromJson(jsonData, new TypeToken<SetBoxTopResult>() {
                    }.getType());
                    if (setBoxTopResult.getCode() == AppApi.HTTP_RESPONSE_STATE_SUCCESS) {
                        if (setBoxTopResult.getResult() != null && setBoxTopResult.getResult().getPlaybill_list() != null) {
                            //该集合包含三部分数据，1:真实节目，2：宣传片占位符.3:广告占位符
                            for (ProgramBean item : setBoxTopResult.getResult().getPlaybill_list()) {
                                if (ConstantValues.PRO.equals(item.getVersion().getType())) {
                                    programBean = item;
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (programBean != null) {
                if (programBean.getVersion() != null) {
                    switch (programBean.getVersion().getType()) {
                        case ConstantValues.PRO:
                            mProDownloadPeriod = programBean.getVersion().getVersion();
                            break;
                        case ConstantValues.ADV:
                            mAdvDownloadPeriod = programBean.getVersion().getVersion();
                            break;
                        case ConstantValues.ADS:
                            mAdsDownloadPeriod = programBean.getVersion().getVersion();
                            break;
                    }
                }
                if (programBean.getMedia_lib() != null && programBean.getMedia_lib().size() > 0) {
                    for (MediaLibBean bean : programBean.getMedia_lib()) {
                        String selection = null;
                        String[] selectionArgs = null;
                        if (ConstantValues.ADS_DATA_PATH.equals(filePath) || ConstantValues.ADV_DATA_PATH.equals(filePath)) {
                            selection = DBHelper.MediaDBInfo.FieldName.VID
                                    + "=? and "
                                    + DBHelper.MediaDBInfo.FieldName.LOCATION_ID
                                    + "=?";
                            selectionArgs = new String[]{bean.getVid(), bean.getLocation_id() + ""};
                        } else {
                            selection = DBHelper.MediaDBInfo.FieldName.VID
                                    + "=? and "
                                    + DBHelper.MediaDBInfo.FieldName.ADS_ORDER
                                    + "=?";
                            selectionArgs = new String[]{bean.getVid(), bean.getOrder() + ""};
                        }

                        List<MediaLibBean> list = null;
                        if (ConstantValues.ADS_DATA_PATH.equals(filePath)) {
                            list = dbHelper.findNewAdsByWhere(selection, selectionArgs);
                        } else {
                            list = dbHelper.findNewPlayListByWhere(selection, selectionArgs);
                        }
                        if (list != null && list.size() >= 1) {
                            bean.setDownload_state(1);
                        } else {
                            bean.setDownload_state(0);
                        }
                        downloadList.add(bean);
                    }
                }
            }
        }
    }

    public interface Callback {
        void onMediaItemSelect(int index);
    }
}
