package com.savor.ads.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.savor.ads.BuildConfig;
import com.savor.ads.R;
import com.savor.ads.adapter.ProjectionImgAdapter;
import com.savor.ads.bean.MiniProgramProjection;
import com.savor.ads.bean.ProjectionImg;
import com.savor.ads.utils.ConstantValues;
import com.savor.ads.utils.DensityUtil;
import com.savor.ads.utils.GlideImageLoader;

import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 展示图片投屏列表
 */
public class ProjectionImgListDialog extends Dialog{

    private Context mContext;
    private LinearLayout projectionPersonnelLayout;
    private ImageView projectionWxheadIV;
    private TextView projectionWxnicknameTV;
    private LinearLayout projectionImgLayout;
    private int imgAngle=3;
    private int avatarAngle=50;
    private HashMap<String,Object> imgMap = new HashMap<>();
    private Handler handler = new Handler();
//    private ListView projectionImgListLV;
//    private ProjectionImgAdapter projectionImgAdapter=null;
//    private List<MiniProgramProjection> listImg = new ArrayList<>();
    private int height =0;
    public ProjectionImgListDialog(@NonNull Context context) {
        super(context, R.style.miniProgramImagesDialog);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_projection_img_list);
        setDialogAttributes();
        initViews();
    }

    private void setDialogAttributes() {
        height = DensityUtil.getScreenHeight(mContext)/4;
        Window window = getWindow(); // 得到对话框
        window.getDecorView().setPadding(0, 20, 20, 0);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.width = height;
        wl.height = WindowManager.LayoutParams.MATCH_PARENT;
        wl.gravity = Gravity.RIGHT;
        wl.format = PixelFormat.RGBA_8888;
        //设置window type
        wl.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wl.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        window.setDimAmount(0f);
        window.setAttributes(wl);
    }

    private void initViews(){
        projectionPersonnelLayout = findViewById(R.id.projection_personnel_layout);
        projectionWxheadIV = findViewById(R.id.projection_wxhead);
        projectionWxnicknameTV = findViewById(R.id.projection_wxnickname);
        projectionImgLayout =  findViewById(R.id.projection_img_layout);
//        projectionImgListLV = (ListView) findViewById(R.id.projection_list);
//        projectionImgAdapter = new ProjectionImgAdapter(mContext,listImg);
//        projectionImgListLV.setAdapter(projectionImgAdapter);
    }
    public void setProjectionPersonInfo(String wxhead,String wxnickname){
        if (!TextUtils.isEmpty(wxhead)){
            projectionWxheadIV.setVisibility(View.VISIBLE);
            GlideImageLoader.loadRoundImage(mContext,wxhead,projectionWxheadIV,R.mipmap.wxavatar);
        }else{
            projectionWxheadIV.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(wxnickname)){
            projectionWxnicknameTV.setVisibility(View.VISIBLE);
            projectionWxnicknameTV.setText(wxnickname);
        }else{
            projectionWxnicknameTV.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(wxhead)&&TextUtils.isEmpty(wxnickname)){
            projectionPersonnelLayout.setVisibility(View.GONE);
        }else{
            projectionPersonnelLayout.setVisibility(View.VISIBLE);
        }
    }
    public void setContent(ArrayList<ProjectionImg> list,int type){
        setContent(list,type,null);
    }
    public void setContent(ArrayList<ProjectionImg> list,int type,String progess){
        synchronized(ProjectionImgListDialog.class){
            for (int i =0;i<list.size();i++){
                int childCount = projectionImgLayout.getChildCount();
                if (childCount<=i){
                    ProjectionImg img = list.get(i);
                    String url = img.getUrl();
                    if (!TextUtils.isEmpty(url)){
                        if (type==1){
                            url = BuildConfig.OSS_ENDPOINT+url+ ConstantValues.PROJECTION_IMG_THUMBNAIL_PARAM;
                        }else{
                            url = BuildConfig.OSS_ENDPOINT+url+ConstantValues.PROJECTION_VIDEO_THUMBNAIL_PARAM;
                        }

                    }
                    View view = View.inflate(mContext, R.layout.item_projection_img,null);
                    view.setTag(img.getImg_id());
                    if (type==1){
                        imgMap.put(img.getImg_id(),view);
                    }else{
                        imgMap.put(img.getVideo_id(),view);
                    }
                    TextView projection_progress = view.findViewById(R.id.projection_progress);
                    if (!TextUtils.isEmpty(progess)){
                        projection_progress.setText(progess);
                    }else {
                        projection_progress.setText("未下载");
                    }
                    projection_progress.setTextSize(20);
                    final ImageView projection_img =  view.findViewById(R.id.projection_img);
                    if (!TextUtils.isEmpty(url)){
                        GlideImageLoader.loadRoundImageWithAngle(mContext, url, projection_img,R.mipmap.redian_icon,imgAngle);
                    }
                    projectionImgLayout.addView(view);
                }
            }
        }

    }
    public void setContent(List<ProjectionImg> list){
        synchronized(ProjectionImgListDialog.class){
            for (int i =0;i<list.size();i++){
                int childCount = projectionImgLayout.getChildCount();
                if (childCount<=i){
                    ProjectionImg img = list.get(i);
                    String url = img.getUrl();
                    String img_id = img.getImg_id();
                    if (!TextUtils.isEmpty(url)){
                        url = BuildConfig.OSS_ENDPOINT+url+ ConstantValues.PROJECTION_IMG_THUMBNAIL_PARAM;
                    }
                    View view = View.inflate(mContext, R.layout.item_projection_img,null);
                    view.setTag(img_id);
                    imgMap.put(img_id,view);

                    TextView projection_progress = view.findViewById(R.id.projection_progress);
                    projection_progress.setText("未下载");
                    projection_progress.setTextSize(20);
                    final ImageView projection_img =  view.findViewById(R.id.projection_img);
                    if (!TextUtils.isEmpty(url)){
                        GlideImageLoader.loadRoundImageWithAngle(mContext, url, projection_img,R.mipmap.redian_icon,imgAngle);
                    }
                    projectionImgLayout.addView(view);
                }
            }
        }

    }
    public void clearContent(){
        if(projectionImgLayout!=null){
            projectionImgLayout.removeAllViews();
        }
        if (imgMap!=null){
            imgMap.clear();
        }
    }


    public void setImgDownloadProgress(String imgId,String progress){
        if (imgMap!=null){
            View view = (View) imgMap.get(imgId);
            if (view!=null){
                TextView img_progress =  view.findViewById(R.id.projection_progress);
                img_progress.setText(progress);
            }
        }
        handler.removeCallbacks(mExitRunnable);
        handler.postDelayed(mExitRunnable,1000*60*2);
    }

    private Runnable mExitRunnable = new Runnable() {
        @Override
        public void run() {
            clearContent();
            dismiss();
        }
    };


    @Override
    public void show() {
        super.show();
    }
}
