// MessageAdapter.java (Finalized Code)

package com.develophub.roomfinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    // 1. EXISTING INTERFACE: Contact
    public interface OnContactClickListener {
        void onContactClick(String senderId);
    }

    // 2. NEW INTERFACE: Delete
    public interface OnMessageDeleteListener {
        void onDeleteClick(MessageModel message, int position);
    }

    private OnContactClickListener contactListener;
    private OnMessageDeleteListener deleteListener;

    private List<MessageModel> messages;
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("h:mm a", Locale.getDefault());
    private final String currentUserId;

    // FIX: कंस्ट्रक्टर को दोनों लिसनर स्वीकार करने के लिए अपडेट किया गया
    public MessageAdapter(List<MessageModel> messages, String currentUserId, OnContactClickListener contactListener, OnMessageDeleteListener deleteListener){
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.contactListener = contactListener;
        this.deleteListener = deleteListener;
    }

    public void setMessages(List<MessageModel> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageModel msg = messages.get(position);

        boolean isCurrentUser = currentUserId != null && currentUserId.equals(msg.getSenderId());

        // 1. Sender Name Display Logic
        holder.tvSenderName.setVisibility(View.VISIBLE);
        holder.tvSenderName.setText(msg.getSenderName());


        // 2. Message Content & Timestamp
        holder.tvMessageText.setText(msg.getMessageText());
        String formattedTime = TIME_FORMAT.format(new Date(msg.getTimestamp()));
        holder.tvTimestamp.setText(formattedTime);

        // ⭐ Delete Button Visibility and Click Listener Logic
        if (isCurrentUser) {
            holder.btnDeleteMessage.setVisibility(View.VISIBLE);
        } else {
            holder.btnDeleteMessage.setVisibility(View.GONE);
        }

        holder.btnDeleteMessage.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(msg, position);
            }
        });

        // Contact Button Click Listener Logic
        holder.btnContact.setOnClickListener(v -> {
            if (contactListener != null) {
                contactListener.onContactClick(msg.getSenderId());
            }
        });

        // 3. Bubble Alignment Logic (अपरिवर्तित)
        if (holder.messageContainer.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.messageContainer.getLayoutParams();

            if (isCurrentUser) {
                params.leftMargin = 200;
                params.rightMargin = 0;
            } else {
                params.leftMargin = 0;
                params.rightMargin = 200;
            }
            holder.messageContainer.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder{

        View messageContainer;
        TextView tvSenderName;
        TextView tvMessageText;
        TextView tvTimestamp;
        Button btnContact;
        ImageButton btnDeleteMessage;

        public MessageViewHolder(@NonNull View itemView){
            super(itemView);
            messageContainer = itemView;

            // ID Binding
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            btnContact = itemView.findViewById(R.id.btnContact);
            btnDeleteMessage = itemView.findViewById(R.id.btnDeleteMessage); // Delete Button Binding
        }
    }
}