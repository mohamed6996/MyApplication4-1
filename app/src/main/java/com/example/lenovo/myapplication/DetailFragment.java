package com.example.lenovo.myapplication;


import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.example.lenovo.myapplication.db.MovieHelper;
import com.example.lenovo.myapplication.db.MoviesContract;
import com.google.gson.Gson;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {
    TextView overView, releaseDate, voteAvg, review_field, favorite;
    ImageView image_path;
    Button trailer, review;
    String trailer_id, key;
    String review_content;
    String json;
    boolean clicked;

    String over_view, release_date, vote_avg, image_key, id_key;


    public DetailFragment() {
        // Required empty public constructor

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Iconify
                .with(new FontAwesomeModule());
        Bundle bundle = null;
        if (MainActivity.mTWO_PANE) {
            bundle = getArguments();
        } else {
            bundle = getActivity().getIntent().getExtras();
        }

        over_view = bundle.getString("OVER_VIEW_KEY");
        release_date = bundle.getString("RELEASE_DATE_KEY");
        vote_avg = bundle.getString("VOTE_AVERAGE_KEY");
        image_key = bundle.getString("IMAGE_KEY");
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


        overView = (TextView) view.findViewById(R.id.over_view);
        releaseDate = (TextView) view.findViewById(R.id.release_date);
        voteAvg = (TextView) view.findViewById(R.id.vote_average);
        image_path = (ImageView) view.findViewById(R.id.img);
        trailer = (Button) view.findViewById(R.id.trailer);
        review_field = (TextView) view.findViewById(R.id.review_field);
        favorite = (TextView) view.findViewById(R.id.favor);

        overView.setText(over_view);
        releaseDate.setText(release_date);
        voteAvg.setText(vote_avg);
        String full_image = Constants.IMG_BASE + image_key;
        Glide.with(this).load(full_image).placeholder(R.drawable.ic_dots).into(image_path);
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
                review_field.setText(responce);

            }
        });


        clicked = false;

        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clicked == false) {
                    clicked = true;
                    favorite.setBackgroundColor(R.color.favorite_color_red);
                    MovieHelper movieHelper = new MovieHelper(getContext());
                    SQLiteDatabase db = movieHelper.getWritableDatabase();

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MoviesContract.MovieEntry.COLUMN_NAME_TITLE, json);
                    db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, contentValues);


                } else {

                    Toast.makeText(getContext(), "already exist", Toast.LENGTH_LONG).show();

                }
            }
        });


      /*  favorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    MovieHelper movieHelper = new MovieHelper(getContext());
                    SQLiteDatabase db = movieHelper.getWritableDatabase();

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MoviesContract.MovieEntry.COLUMN_NAME_TITLE, json);
                    db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, contentValues);

                } else {
                    // The toggle is disabled
                    Toast.makeText(getContext(), "already exist", Toast.LENGTH_LONG).show();



                }
            }
        });*/


              /*  ColorDrawable drawable = (ColorDrawable) favorite.getBackground();

                if (drawable.getColor() == R.color.favorite_color_white){
                    MovieHelper movieHelper = new MovieHelper(getContext());
                    SQLiteDatabase db = movieHelper.getWritableDatabase();

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MoviesContract.MovieEntry.COLUMN_NAME_TITLE, json);
                    db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, contentValues);

                    favorite.setBackgroundColor(R.color.favorite_color_red);
                }
                else {
                    Toast.makeText(getContext(), "already exist", Toast.LENGTH_LONG).show();
                }*/


        // font awesome


        return view;
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
