package br.com.thiengo.gcmexample.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import java.util.List;

import br.com.thiengo.gcmexample.PM_MessagesActivity;
import br.com.thiengo.gcmexample.R;
import br.com.thiengo.gcmexample.domain.Message;
import br.com.thiengo.gcmexample.domain.User;
import br.com.thiengo.gcmexample.extra.Util;

/**
 * Created by viniciusthiengo on 4/5/15.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {
    private Context mContext;
    private List<Message> mList;
    private LayoutInflater mLayoutInflater;
    private User mUserFrom;


    public MessageAdapter(Context c, List<Message> l, User uf){
        mContext = c;
        mList = l;
        mUserFrom = uf;
        mLayoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        v = mLayoutInflater.inflate(R.layout.item_message, viewGroup, false);
        MyViewHolder mvh = new MyViewHolder(v);
        return mvh;
    }


    @Override
    public void onBindViewHolder(final MyViewHolder myViewHolder, final int position) {
        Message m = mList.get(position);

        if( m.getUserFrom().getId() == mUserFrom.getId() ){ // LEFT LAYOUT IF IS MESSAGE FROM USER OF THE DEVICE
            myViewHolder.rlContainerLeft.setVisibility(View.VISIBLE);
            myViewHolder.rlContainerRight.setVisibility(View.GONE);
            myViewHolder.tvNicknameLeft.setText(m.getUserFrom().getNickname());
            myViewHolder.tvTimeLeft.setText( Util.getTimeAgo(m.getRegTime())  );
            myViewHolder.tvMessageLeft.setText( m.getMessage() );

            myViewHolder
                .ivReadIconLeft
                .setImageResource(mList.get(position).getWasRead() == 0 ? R.drawable.ic_sent : R.drawable.ic_was_read);
        }
        else{
            myViewHolder.rlContainerLeft.setVisibility(View.GONE);
            myViewHolder.rlContainerRight.setVisibility(View.VISIBLE);
            myViewHolder.tvNicknameRight.setText(m.getUserFrom().getNickname());
            myViewHolder.tvTimeRight.setText( Util.getTimeAgo(m.getRegTime()) );
            myViewHolder.tvMessageRight.setText( m.getMessage() );
        }
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }


    public void addListItem(Message m, int position){
        mList.add(position, m);
        notifyItemInserted(position);
    }


    public void removeListItem(int position){
        mList.remove(position);
        notifyItemRemoved(position);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public RelativeLayout rlContainerLeft;
        public TextView tvNicknameLeft;
        public TextView tvTimeLeft;
        public TextView tvMessageLeft;
        public ImageView ivReadIconLeft;

        public RelativeLayout rlContainerRight;
        public TextView tvNicknameRight;
        public TextView tvTimeRight;
        public TextView tvMessageRight;


        public MyViewHolder(View itemView) {
            super(itemView);

            rlContainerLeft = (RelativeLayout) itemView.findViewById(R.id.rl_container_left);
            tvNicknameLeft = (TextView) itemView.findViewById(R.id.tv_nickname_left);
            tvTimeLeft = (TextView) itemView.findViewById(R.id.tv_time_left);
            tvMessageLeft = (TextView) itemView.findViewById(R.id.tv_message_left);
            ivReadIconLeft = (ImageView) itemView.findViewById(R.id.iv_read_icon_left);

            rlContainerRight = (RelativeLayout) itemView.findViewById(R.id.rl_container_right);
            tvNicknameRight = (TextView) itemView.findViewById(R.id.tv_nickname_right);
            tvTimeRight = (TextView) itemView.findViewById(R.id.tv_time_right);
            tvMessageRight = (TextView) itemView.findViewById(R.id.tv_message_right);

            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            final Message m = mList.get(getAdapterPosition());

            if( m.getUserFrom().getId() == mUserFrom.getId() ){

                new MaterialDialog.Builder( mContext )
                    .title("Remover mensagem")
                    .positiveText("Remover")
                    .negativeText("Cancelar")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {

                            ((PM_MessagesActivity) mContext).removeMessage(m);
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            dialog.dismiss();
                        }
                    })
                    .theme(Theme.LIGHT)
                    .show();
            }

            return true;
        }
    }
}
