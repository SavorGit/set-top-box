package com.savor.ads.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.savor.ads.R;
import com.savor.ads.bean.PartakeUser;
import com.savor.ads.utils.Base64Utils;
import com.savor.ads.utils.GlideImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhang.haiqiang on 2018/1/17.
 */

public class SignInUserAdapter<T extends PartakeUser> extends BaseAdapter {
    private Context mContext;
    private List<T> mUserList;


    public SignInUserAdapter(Context context, ArrayList<T> userList) {
        mContext = context;
        mUserList = userList;
    }

    @Override
    public int getCount() {
        return mUserList == null ? 0 : mUserList.size();
    }

    @Override
    public Object getItem(int position) {
        return mUserList == null ? null : mUserList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_sign_in_user, null);

            viewHolder = new ViewHolder();
            viewHolder.signInUserTv = convertView.findViewById(R.id.tv_sign_in_user);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String userAvatarUrl = mUserList.get(position).getAvatarUrl();
        String signInUser = Base64Utils.getFromBase64(userAvatarUrl);
        GlideImageLoader.loadRoundImage(mContext,signInUser,viewHolder.signInUserTv,R.mipmap.wxavatar);
        return convertView;
    }

    public void setSignInUser(List<T> userList) {
        mUserList = userList;
        notifyDataSetChanged();
    }

    public interface GetPosition{
        void getPosition(int position);
    }

    class ViewHolder {
        ImageView signInUserTv;
    }
}
