package br.com.thiengo.gcmexample.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import br.com.thiengo.gcmexample.PM_MessagesActivity;
import br.com.thiengo.gcmexample.R;
import br.com.thiengo.gcmexample.domain.User;

/**
 * Created by viniciusthiengo on 4/5/15.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {
    private Context mContext;
    private List<User> mList;
    private LayoutInflater mLayoutInflater;
    private User mUser;


    public UserAdapter(Context c, List<User> l, User u){
        mContext = c;
        mList = l;
        mUser = u;
        mLayoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        v = mLayoutInflater.inflate(R.layout.item_user, viewGroup, false);
        MyViewHolder mvh = new MyViewHolder(v);
        return mvh;
    }


    @Override
    public void onBindViewHolder(final MyViewHolder myViewHolder, final int position) {
        myViewHolder.tvNickname.setText( mList.get(position).getNickname() );

        myViewHolder.tvNewMessages.setText(mList.get(position).getNumberNewMessages() + "");
        myViewHolder.tvNewMessages.setVisibility(mList.get(position).getNumberNewMessages() > 0 ? View.VISIBLE : View.INVISIBLE);

        myViewHolder.tvIsTyping.setText( mList.get(position).isTyping() ? "digitando..." : "" );
        myViewHolder.tvIsTyping.setVisibility( mList.get(position).isTyping() ? View.VISIBLE : View.INVISIBLE );
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }


    public void addListItem(User u, int position){
        mList.add(position, u);
        notifyItemInserted(position);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView tvNickname;
        public TextView tvIsTyping;
        public TextView tvNewMessages;


        public MyViewHolder(View itemView) {
            super(itemView);

            tvNickname = (TextView) itemView.findViewById(R.id.tv_nickname);
            tvIsTyping = (TextView) itemView.findViewById(R.id.tv_is_typing);
            tvNewMessages = (TextView) itemView.findViewById(R.id.tv_new_messages);

            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            mList.get(getAdapterPosition()).setNumberNewMessages(0);
            UserAdapter.this.notifyItemChanged( getAdapterPosition() );

            Bundle bundle = new Bundle();
            bundle.putParcelable(User.USER_KEY, mUser);
            bundle.putParcelable(User.USER_TO_KEY, mList.get(getAdapterPosition()) );

            Intent intent = new Intent(mContext, PM_MessagesActivity.class);
            intent.putExtras(bundle);

            mContext.startActivity( intent );
        }
    }
}
