package com.example.naimish.assignment.executor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.naimish.assignment.Model.Track;
import com.example.naimish.assignment.R;
import com.example.naimish.assignment.view.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {

    private ArrayList<Track> tracks;
    private Context context;

    public RecyclerViewAdapter(Context context) {
        tracks = new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tracks_list_item, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        Track track = tracks.get(position);
        holder.name.setText(track.getName());
        holder.artist.setText(track.getArtistName());
        String imagepath = String.format("http://direct.napster.com/imageserver/v2/albums/%s/images/%s", track.getAlbumId(), "200x200.jpg");

        Glide.with(context)
                .load(imagepath)
                .apply(new RequestOptions()
                        .error(R.drawable.error)
                        .centerCrop()
                        .dontAnimate())
                .into(holder.albumArt);

        if (track.isPlaying())
            holder.play_pause.setImageResource(R.drawable.ic_pause);
        else
            holder.play_pause.setImageResource(R.drawable.ic_play_arrow);

        holder.track = track;

    }

    public void addItems(List<Track> tracks) {
        this.tracks = (ArrayList<Track>) tracks;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        Track track;
        CardView cardView;
        TextView name, artist;
        ImageView albumArt;
        ImageButton play_pause;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            name = itemView.findViewById(R.id.track_name);
            artist = itemView.findViewById(R.id.artist_name);
            albumArt = itemView.findViewById(R.id.albumArt);
            play_pause = itemView.findViewById(R.id.play_pause);

            cardView.setOnClickListener(this);
            play_pause.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(context, "" + track.isPlaying(), Toast.LENGTH_SHORT).show();
            if (track.isPlaying()) {
                track.setPlaying(false);
                notifyItemChanged(getAdapterPosition());
                new PlayMediaExec(context).startPlaying(getAdapterPosition(),false);
            } else {
                ((MainActivity)context).updateCurrentPosition(getAdapterPosition());
            }
        }
    }
}
