package com.example.messengerr.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messengerr.Models.MessageModel;
import com.example.messengerr.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter{

  ArrayList<MessageModel> messageModels;
  Context context;
  int SENDER_VIEW_TYPE =1;
  int RECEIVER_VIEW_TYPE =2;
  String receiveID ;
  public ChatAdapter(ArrayList<MessageModel> messageModels, Context context) {
    this.messageModels = messageModels;
    this.context = context;
  }

  public ChatAdapter(ArrayList<MessageModel> messageModels, Context context, String receiveID) {
    this.messageModels = messageModels;
    this.context = context;
    this.receiveID = receiveID;
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    if(viewType == SENDER_VIEW_TYPE) {
      View view = LayoutInflater.from(context).inflate(R.layout.sample_sender , parent,false);
      return new SenderViewHolder(view);
    }else{
      View view = LayoutInflater.from(context).inflate(R.layout.sample_receiver , parent,false);
      return new ReceieverViewHolder(view);
    }

  }

  @Override
  public int getItemViewType(int position) {

     if(messageModels.get(position).getuId().equals(FirebaseAuth.getInstance().getUid())){
       return SENDER_VIEW_TYPE;
     }
     else{
       return RECEIVER_VIEW_TYPE;
     }

  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    MessageModel messageModel = messageModels.get(position);
    if(holder.getClass() == SenderViewHolder.class){
      ((SenderViewHolder)holder).sendermsg.setText(messageModel.getMessage());
    }else{
      ((ReceieverViewHolder)holder).receivermsg.setText(messageModel.getMessage());
    }

    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View view) {
        new AlertDialog.Builder(context)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this msg?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialogInterface, int i) {

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    String senderRoom;
                    if(receiveID.isEmpty()){
                      senderRoom = FirebaseAuth.getInstance().getUid() ;
                      database.getReference().child("Chat Room").child(senderRoom)
                              .child(messageModel.getMessageId())
                              .setValue(null);
                    }else {
                      senderRoom = FirebaseAuth.getInstance().getUid() + receiveID;
                      database.getReference().child("chats").child(senderRoom)
                              .child(messageModel.getMessageId())
                              .setValue(null);
                    }






                  }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialogInterface, int i) {

                    dialogInterface.dismiss();
                  }
                }).show();
        return false;
      }
    });
  }

  @Override
  public int getItemCount() {
    return messageModels.size();
  }

  public class ReceieverViewHolder extends RecyclerView.ViewHolder{

    TextView receivermsg , receivertime;
    public ReceieverViewHolder(@NonNull View itemView) {
      super(itemView);
      receivermsg = itemView.findViewById(R.id.receiverText);
      receivertime = itemView.findViewById(R.id.receiverTime);

    }
  }

  public class SenderViewHolder extends RecyclerView.ViewHolder{
    TextView sendermsg , sendertime;
    public SenderViewHolder(@NonNull View itemView) {
      super(itemView);
      sendermsg = itemView.findViewById(R.id.sendersText);
      sendertime = itemView.findViewById(R.id.senderTime);

    }
  }

}
