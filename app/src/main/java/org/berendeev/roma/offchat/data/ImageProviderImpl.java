package org.berendeev.roma.offchat.data;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.berendeev.roma.offchat.domain.ImageProvider;
import org.berendeev.roma.offchat.domain.model.Image;

import java.io.File;

public class ImageProviderImpl implements ImageProvider {

    private Context context;

    public ImageProviderImpl(Context context) {
        this.context = context;
    }

    @Override public void provide(Image image, ImageView imageView) {
        Uri uri = Uri.fromFile(new File(image.source()));
        Picasso.with(context)
                .load(uri)
                .into(imageView);
    }
}
