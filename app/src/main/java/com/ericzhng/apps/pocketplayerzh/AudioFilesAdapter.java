package com.ericzhng.apps.pocketplayerzh;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ericzhng.apps.pocketplayerzh.androidutils.CommonUtils;
import com.ericzhng.apps.pocketplayerzh.audiostorage.AudioFiles;

import java.util.ArrayList;


public class AudioFilesAdapter extends RecyclerView.Adapter<AudioFilesAdapter.FileViewHolder> {

    // create tag for log
    private static final String TAG = AudioFilesAdapter.class.getSimpleName();

    private ArrayList<AudioFiles> audioList;
    private Context mContext;

    final private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    public AudioFilesAdapter(Context context, ArrayList<AudioFiles> list, ListItemClickListener listener) {
        this.mContext = context;
        this.audioList = list;
        mOnClickListener = listener;
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.item_layout, parent, false);
        view.setFocusable(true);

        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return audioList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    class FileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView  mFileId;
        final ImageView mPlayImage;
        final TextView  mTitle;
        final TextView  mSize;

        public FileViewHolder(View itemView) {
            super(itemView);

            mFileId = itemView.findViewById(R.id.audio_id);
            mPlayImage = itemView.findViewById(R.id.audio_play_image);
            mTitle = itemView.findViewById(R.id.audio_title);
            mSize = itemView.findViewById(R.id.audio_size);

            mPlayImage.setOnClickListener(this);
        }

        public void bind(int position) {
            int posP1 = position + 1;
            mFileId.setText(Integer.toString(posP1));

            boolean status = false;
            updateImage(status);

            mTitle.setText(audioList.get(position).getTitle());
            mSize.setText(CommonUtils.formatFileSize(audioList.get(position).getSize()));
        }

        // helper method to set image
        public void updateImage(boolean status) {

            if (status)
                mPlayImage.setImageResource(android.R.drawable.ic_media_pause);
            else
                mPlayImage.setImageResource(android.R.drawable.ic_media_play);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();

            if (false) {
                updateImage(true);
            }
            else  {
                updateImage(false);
            }

            mOnClickListener.onListItemClick(adapterPosition);
        }
    }
}
