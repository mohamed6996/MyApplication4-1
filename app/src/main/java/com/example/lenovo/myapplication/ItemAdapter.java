package com.example.lenovo.myapplication;

import android.app.ActivityOptions;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lenovo.myapplication.db.MovieHelper;
import com.example.lenovo.myapplication.db.MoviesContract;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 10/24/2016.
 */

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.VH> {
    List<ItemModel> mDataset;
    Context context;

    public ItemAdapter(List<ItemModel> mDataset, Context context) {
        this.mDataset = mDataset;
        this.context = context;
    }


    @Override

    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);
        return new VH(view, this.context, this.mDataset);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        ItemModel model = mDataset.get(position);

        String FULL_IMG = Constants.IMG_BASE + model.getImagePath();

        Glide.with(context).load(FULL_IMG).into(holder.imageView);
        holder.textView.setText(model.getFilm_name());
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class VH extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView, favorits;
        TextView textView;

        Context vhContext;
        List<ItemModel> vhDataSet = new ArrayList<>();

        public VH(View itemView, final Context context, final List<ItemModel> vhDataSet) {
            super(itemView);

            this.vhContext = context;
            this.vhDataSet = vhDataSet;
            textView = (TextView) itemView.findViewById(R.id.count);

            imageView = (ImageView) itemView.findViewById(R.id.img);
            imageView.setOnClickListener(this);

            favorits = (ImageView) itemView.findViewById(R.id.img_favorit);
            favorits.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   /* int position = getAdapterPosition();
                    ItemModel itemModel = vhDataSet.get(position);

                    SharedPreferences sharedPreferences = vhContext.getSharedPreferences("FAVORITE", vhContext.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    Gson gson = new Gson();
                    String jsonString = gson.toJson(itemModel, ItemModel.class);

                    editor.putString("jsonString", jsonString);
                    editor.apply();*/

                    int position = getAdapterPosition();
                    ItemModel itemModel = vhDataSet.get(position);

                    Gson gson = new Gson();
                    String jsonString = gson.toJson(itemModel, ItemModel.class);


                    MovieHelper movieHelper = new MovieHelper(vhContext);
                    SQLiteDatabase db = movieHelper.getWritableDatabase();

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MoviesContract.MovieEntry.COLUMN_NAME_TITLE, jsonString);

                    Long row_id = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, contentValues);
                    Toast.makeText(vhContext, "" + row_id , Toast.LENGTH_LONG).show();

                }
            });


        }

        @Override
        public void onClick(View v) {

            int position = getAdapterPosition();
            ItemModel m = vhDataSet.get(position);

            Bundle b = new Bundle();
            b.putString("OVER_VIEW_KEY", m.getOver_view());
            b.putString("RELEASE_DATE_KEY", m.getRelease_date());
            b.putString("VOTE_AVERAGE_KEY", m.getVote_average());
            b.putString("IMAGE_KEY", m.getImagePath());
            b.putString("ID_KEY", m.getId());

            if (MainActivity.mTWO_PANE) {
                DetailFragment detailFragment = new DetailFragment();
                detailFragment.setArguments(b);
                ((MainActivity) vhContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, detailFragment)
                        .commit();
            } else {
                Intent intent = new Intent(this.vhContext, DetailActivity.class);
                intent.putExtras(b);
                this.vhContext.startActivity(intent);
            }

        }
    }
}
