package com.savor.ads.player;

import android.view.View;
import android.view.ViewGroup;

public final class SavorPlayerFactory {

    public static IVideoPlayer getPlayer(PlayerType type, ViewGroup container) {
        IVideoPlayer player = null;
        if (container != null) {
            switch (type) {
                case GGPlayer:
                    player = new GGVideoPlayer(container.getContext());
                    container.addView((View) player, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
                    break;
            }
        }

        return player;
    }
}
