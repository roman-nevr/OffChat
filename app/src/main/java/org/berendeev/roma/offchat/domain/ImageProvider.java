package org.berendeev.roma.offchat.domain;

import android.widget.ImageView;

import org.berendeev.roma.offchat.domain.model.Image;

public interface ImageProvider{
    void provide(Image image, ImageView imageView);

    void stopLoading(ImageView imageView);
}
