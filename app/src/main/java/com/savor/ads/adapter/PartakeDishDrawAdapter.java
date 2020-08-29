package com.savor.ads.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.savor.ads.R;
import com.savor.ads.bean.PartakeUser;
import com.savor.ads.utils.Base64Utils;
import com.savor.ads.utils.GlideImageLoader;

import java.util.Base64;
import java.util.List;

public class PartakeDishDrawAdapter extends RecyclerView.Adapter<PartakeDishDrawAdapter.PartakeDishViewHolder> {


    private Context context;
    private List<PartakeUser> datas;
    CarouselLayoutManager layoutManager;
    public void setLayoutManager(CarouselLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    public PartakeDishDrawAdapter(Context mContext,List<PartakeUser> datas){
        this.context = mContext;
        this.datas = datas;
    }
    @Override
    public int getItemCount() {
        return datas!=null?datas.size():0;
    }
    @NonNull
    @Override
    public PartakeDishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_partake_dish, parent, false);
        return new PartakeDishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PartakeDishViewHolder holder, int position) {
        PartakeUser user = datas.get(position);
        String avatarUrl = Base64Utils.getFromBase64(user.getAvatarUrl());
        GlideImageLoader.loadImage(context,avatarUrl,holder.userImgIV,R.mipmap.avatar,R.mipmap.avatar);
        holder.userNicknameTV.setText(user.getNickName());
    }

    class PartakeDishViewHolder extends RecyclerView.ViewHolder {

        private  View view;
        private ImageView userImgIV;
        private TextView userNicknameTV;
        PartakeDishViewHolder(final View view) {
            super(view);
            userImgIV = view.findViewById(R.id.user_img);
            userNicknameTV = view.findViewById(R.id.user_nickname);
        }
    }
}
