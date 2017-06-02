package org.berendeev.roma.offchat.presentation.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.berendeev.roma.offchat.domain.ImageProvider;
import org.berendeev.roma.offchat.domain.model.Image;
import org.berendeev.roma.offchat.domain.model.Message;
import org.berendeev.roma.offchat.presentation.App;
import org.berendeev.roma.offchat.presentation.R;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.Gravity.END;
import static android.view.Gravity.START;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.lang.Enum.valueOf;
import static org.berendeev.roma.offchat.domain.model.Message.Owner.me;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    public static final int NOT_ME_VIEW_TYPE = 0;
    public static final int ME_VIEW_TYPE = 1;
    private final Context context;
    private List<Message> messages;

    private ImageProvider imageProvider;

    public ChatAdapter(List<Message> messages, Context context) {
        this.messages = messages;
        this.context = context;
        hasStableIds();
        imageProvider = App.getChatComponent().imageProvider();
    }

    @Override public long getItemId(int position) {
        position = getReversePosition(position);
        return messages.get(position).hashCode();
    }

//    @Override public int getItemViewType(int position) {
//        position = getReversePosition(position);
//        return messages.get(position).owner() == me ? ME_VIEW_TYPE : NOT_ME_VIEW_TYPE;
//    }

    @Override public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
//        if (viewType == ME_VIEW_TYPE){
//            view = inflate(R.layout.chat_item, parent);
//        }
//        if (viewType == NOT_ME_VIEW_TYPE){
//        }

        view = inflate(R.layout.chat_item, parent);
        return new MessageViewHolder(view);
    }

    @Override public void onBindViewHolder(MessageViewHolder holder, int position) {
        position = getReversePosition(position);
        Message message = messages.get(position);

        if (message.image() == Image.EMPTY) {
            holder.imageView.setVisibility(GONE);
        }else {
            holder.imageView.setVisibility(VISIBLE);
            imageProvider.provide(message.image(), holder.imageView);
        }

        if (message.text().isEmpty()){
            holder.message.setVisibility(GONE);
        }else {
            holder.message.setVisibility(VISIBLE);
            holder.message.setText(message.text());
        }
        if (message.owner() == me) {
            holder.linearLayout.setGravity(END);
            holder.message.setGravity(END);
        } else {
            holder.linearLayout.setGravity(START);
            holder.message.setGravity(START);
        }
    }

    @Override public void onViewDetachedFromWindow(MessageViewHolder holder) {
        int adapterPosition = holder.getAdapterPosition();
        if (adapterPosition != RecyclerView.NO_POSITION && messages.get(adapterPosition).image() != Image.EMPTY){
            imageProvider.stopLoading(holder.imageView);
        }
    }

    @Override public int getItemCount() {
        return messages.size();
    }

    private View inflate(@LayoutRes int layoutId, ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
    }

    public void update(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    private int getReversePosition(int position) {
        return messages.size() - position - 1;
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.message) TextView message;
        @BindView(R.id.item_layout) LinearLayout linearLayout;
        @BindView(R.id.image) ImageView imageView;

        public MessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
