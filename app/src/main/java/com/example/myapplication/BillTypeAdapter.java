package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class BillTypeAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] billTypes;

    // Ensure all icons match the number of billTypes
    private final int[] billIcons = {
            R.drawable.electricity,
            R.drawable.water,
            R.drawable.internet,
            R.drawable.spotify,
            R.drawable.netflix,
            R.drawable.phone,
            R.drawable.gas
    };

    public BillTypeAdapter(Context context, String[] billTypes) {
        super(context, R.layout.spinner_item_with_icon, billTypes);
        this.context = context;
        this.billTypes = billTypes;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.spinner_item_with_icon, parent, false);

            holder = new ViewHolder();
            holder.billText = convertView.findViewById(R.id.billText);
            holder.billIcon = convertView.findViewById(R.id.billIcon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position < billTypes.length) {
            String billType = billTypes[position];
            holder.billText.setText(billType);

            // Safe icon assignment
            if (position < billIcons.length) {
                holder.billIcon.setImageResource(billIcons[position]);
            } else {
                // Optional: fallback to dynamically load by name if mismatch occurs
                int iconResId = context.getResources().getIdentifier(
                        billType.toLowerCase(), "drawable", context.getPackageName());
                if (iconResId != 0) {
                    holder.billIcon.setImageResource(iconResId);
                } else {
                    holder.billIcon.setImageResource(R.drawable.ic_time); // fallback icon
                }
            }

            // Tint logic
            int tintColor;
            switch (billType) {
                case "Spotify":
                    tintColor = ContextCompat.getColor(context, R.color.spotify_green);
                    break;
                case "Netflix":
                    tintColor = ContextCompat.getColor(context, R.color.netflix_red);
                    break;
                case "Internet":
                    tintColor = ContextCompat.getColor(context, R.color.internet_blue);
                    break;
                default:
                    tintColor = ContextCompat.getColor(context, R.color.blue);
                    break;
            }
            //holder.billIcon.setColorFilter(tintColor);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView billText;
        ImageView billIcon;
    }
}
