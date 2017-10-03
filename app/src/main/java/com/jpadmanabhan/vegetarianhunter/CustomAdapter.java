package com.jpadmanabhan.vegetarianhunter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by jpadmanabhan on 4/9/2015.
 */
class CustomAdapter extends BaseAdapter{
    private final Context context;
    private final ArrayList<Restaurant> data;
    private static LayoutInflater inflater=null;

    public CustomAdapter(ArrayList<Restaurant> arr, Context c){
        context = c;
        data = arr;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder;

        if(rowView == null){

            /****** Inflate layout_list_item.xml file for each row ( Defined below ) *******/
            rowView = inflater.inflate(R.layout.layout_list_item, null);

            /****** View Holder Object to contain layout_list_item.xml file elements ******/

            holder = new ViewHolder();
            holder.name = (TextView) rowView.findViewById(R.id.name);
            holder.description = (TextView) rowView.findViewById(R.id.description);
            holder.distance = (TextView) rowView.findViewById(R.id.distance);
            holder.phone = (TextView) rowView.findViewById(R.id.phone);
            holder.address = (TextView) rowView.findViewById(R.id.address);
            holder.city = (TextView) rowView.findViewById(R.id.city);
            holder.rating = (RatingBar) rowView.findViewById(R.id.ratingBar);

            /************  Set holder with LayoutInflater ************/
            rowView.setTag(holder);
        }else
            holder=(ViewHolder)rowView.getTag();

        /************  Set Model values in Holder elements ***********/
        final Restaurant temp = data.get(position);
        holder.name.setText(temp.getName());
        holder.description.setText(temp.getDescription());
        holder.address.setPaintFlags(holder.address.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        holder.address.setText(temp.getAddress());
        holder.phone.setPaintFlags(holder.phone.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        holder.phone.setText(temp.getPhone());
        holder.distance.setText(temp.getDistance() + " mi");
        holder.city.setPaintFlags(holder.city.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        holder.city.setText(" " + temp.getCity());
        if(temp.getRating().isEmpty()) {
            holder.rating.setRating(0f);
        }else {
            //Log.i("jai", temp.getRating());
            holder.rating.setRating(Float.valueOf(temp.getRating()));
        }
        holder.rating.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        holder.rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                ratingBar.setIsIndicator(true);
            }
        });
        holder.rating.setFocusable(false);
        LayerDrawable stars = (LayerDrawable) holder.rating.getProgressDrawable();
        stars.getDrawable(0).setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(2).setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_ATOP);

        final String tempPhone = holder.phone.getText().toString().trim();
         holder.phone.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent callIntent = new Intent(Intent.ACTION_CALL);
                 callIntent.setData(Uri.parse("tel:"+tempPhone));
                 context.startActivity(callIntent);
             }
         });
        return rowView;
    }

    /********* Create a holder Class to contain inflated xml file elements *********/
    private static class ViewHolder{

        public TextView name;
        public TextView description;
        public TextView phone;
        public TextView address;
        public TextView city;
        public TextView distance;
        public RatingBar rating;

    }



}

