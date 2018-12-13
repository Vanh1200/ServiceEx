package com.example.vanh1200.serviceex;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;


public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
    private static final String TAG = "SongAdapter";
    private List<Song> mSongs;
    private OnItemClicked mListener;

    public SongAdapter(List<Song> songs) {
        mSongs = songs;
    }

    public void setListener(OnItemClicked listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_song, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.bindData(mSongs.get(i));
    }

    @Override
    public int getItemCount() {
        return mSongs == null ? 0 : mSongs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mImageSong;
        private TextView mTextTitle;
        private TextView mTextSinger;
        private ImageView mImageMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews(itemView);
        }

        private void initViews(View itemView) {
            mImageSong = itemView.findViewById(R.id.image_song);
            mTextTitle = itemView.findViewById(R.id.text_title);
            mTextSinger = itemView.findViewById(R.id.text_singer);
            mImageMore = itemView.findViewById(R.id.image_more);
        }

        public void bindData(Song song) {
            Glide.with(itemView.getContext())
                    .load(song.getThumbnail())
                    .apply(RequestOptions.circleCropTransform())
                    .into(mImageSong);
            mTextSinger.setText(song.getSinger());
            mTextTitle.setText(song.getTitle());
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClickListener(getAdapterPosition());
            }
        }
    }

    public interface OnItemClicked {
        void onItemClickListener(int position);
    }
}
