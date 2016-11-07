package com.example.lenovo.myapplication;


import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.example.lenovo.myapplication.db.MovieHelper;
import com.example.lenovo.myapplication.db.MoviesContract;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.like.LikeButton;
import com.like.OnLikeListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {
    TextView FilmName, overView, releaseDate, voteAvg, review_field;
    ImageView image_path, back_path;
    Button review;
    String trailer_id, key;
    String review_content;
    String json;
    FloatingActionButton trailer;
    LikeButton likeButton;

    String film_name, over_view, release_date, vote_avg, image_key, backDrop_path, id_key;


    public DetailFragment() {
        // Required empty public constructor

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = null;
        if (MainActivity.mTWO_PANE) {
            bundle = getArguments();
        } else {
            bundle = getActivity().getIntent().getExtras();
        }
        film_name = bundle.getString("FILM_NAME_KEY");
        over_view = bundle.getString("OVER_VIEW_KEY");
        release_date = bundle.getString("RELEASE_DATE_KEY");
        vote_avg = bundle.getString("VOTE_AVERAGE_KEY");
        image_key = bundle.getString("IMAGE_KEY");
        backDrop_path = bundle.getString("BACKDROP_PATH_KEY");
        id_key = bundle.getString("ID_KEY");
        json = bundle.getString("jsonString");

        StringBuilder builder = new StringBuilder();
        builder.append(" https://api.themoviedb.org/3/movie/");
        builder.append(id_key);
        builder.append("/videos?api_key=1a618051961d7a730414257885f0d9d3&language=en-US");
        String Full_id = builder.toString();

        fetch_youtube_key(Full_id);

        StringBuilder b = new StringBuilder();
        b.append(" https://api.themoviedb.org/3/movie/");
        b.append(id_key);
        b.append("/reviews?api_key=1a618051961d7a730414257885f0d9d3&language=en-US");
        String full = b.toString();

        fetch_review(full);


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(film_name);
        toolbar.setNavigationIcon(getActivity().getResources().getDrawable(R.mipmap.ic_arrow_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        FilmName = (TextView) view.findViewById(R.id.film_name);
        overView = (TextView) view.findViewById(R.id.over_view);
        releaseDate = (TextView) view.findViewById(R.id.release_date);
        voteAvg = (TextView) view.findViewById(R.id.vote_average);
        image_path = (ImageView) view.findViewById(R.id.img);
        back_path = (ImageView) view.findViewById(R.id.back_path);
        trailer = (FloatingActionButton) view.findViewById(R.id.fab);
        review_field = (TextView) view.findViewById(R.id.review_field);
        likeButton = (LikeButton) view.findViewById(R.id.star_button);


        FilmName.setText(film_name);
        overView.setText(over_view);
        releaseDate.setText(release_date);
        voteAvg.setText(vote_avg);
        String full_image = Constants.IMG_BASE + image_key;
        String full_back_image = Constants.BACK_DROP_PATH + backDrop_path;


        Glide.with(this).load(full_image).placeholder(R.drawable.ic_dots).into(image_path);
        Glide.with(this).load(full_back_image).placeholder(R.drawable.ic_dots).into(back_path);


        trailer_id = id_key;

        trailer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + key));
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://www.youtube.com/watch?v=" + key));
                    startActivity(intent);
                }

            }
        });


        review = (Button) view.findViewById(R.id.review);
        review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String responce = review_content;
                ReviewFragment dialog = new ReviewFragment();
                Bundle args = new Bundle();
                args.putString("reviewKey", responce);
                dialog.setArguments(args);
                // TODO modify to work on tablets
                getFragmentManager().beginTransaction().replace(R.id.activity_detail, dialog).addToBackStack(null).commit();
                review_field.setText(responce);

            }
        });


        likeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                MovieHelper movieHelper = new MovieHelper(getContext());
                SQLiteDatabase db = movieHelper.getWritableDatabase();

                ContentValues contentValues = new ContentValues();
                contentValues.put(MoviesContract.MovieEntry.COLUMN_NAME_TITLE, json);
                db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, contentValues);

            }

            @Override
            public void unLiked(LikeButton likeButton) {
                String selection = MoviesContract.MovieEntry.COLUMN_NAME_TITLE + " = ?";
                String[] selectionArgs = {json};

                MovieHelper movieHelper = new MovieHelper(getContext());
                SQLiteDatabase db = movieHelper.getWritableDatabase();
                db.delete(MoviesContract.MovieEntry.TABLE_NAME, selection, selectionArgs);

            }
        });

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String jsonString = null;
        StringBuilder builder = new StringBuilder();
        MovieHelper movieHelper = new MovieHelper(getActivity());
        SQLiteDatabase db = movieHelper.getReadableDatabase();
        Cursor cursor = db.query(MoviesContract.MovieEntry.TABLE_NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            jsonString = cursor.getString(1);
            builder.append(jsonString);
        }

        jsonString = builder.toString();

        if (jsonString.contains(json)) {
            likeButton.setLiked(true);
        } else {
            likeButton.setLiked(false);

        }

    }

    public void fetch_youtube_key(String id) {


        StringRequest string = new StringRequest(Request.Method.GET, id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray array = jsonObject.getJSONArray("results");
                    JSONObject first = array.getJSONObject(0);
                    key = first.getString("key");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        MySingleton.getInstance(getActivity()).addToRequestQueue(string);

    }

    public void fetch_review(String id) {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray array = jsonObject.getJSONArray("results");
                    JSONObject first = array.getJSONObject(0);
                    review_content = first.getString("content");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        MySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);

    }


}
