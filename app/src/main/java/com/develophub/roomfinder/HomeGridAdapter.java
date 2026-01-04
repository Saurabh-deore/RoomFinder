package com.develophub.roomfinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class HomeGridAdapter extends BaseAdapter implements Filterable {

    private Context context;
    private final List<String> originalTitles;
    private final List<Integer> originalIcons;

    private List<String> filteredTitles;
    private List<Integer> filteredIcons;

    public HomeGridAdapter(Context context, int[] icons, String[] titles) {
        this.context = context;
        this.originalTitles = new ArrayList<>();
        this.originalIcons = new ArrayList<>();

        for (String t : titles) originalTitles.add(t);
        for (int i : icons) originalIcons.add(i);

        // Shuruat mein filtered list original jaisi hogi
        this.filteredTitles = new ArrayList<>(originalTitles);
        this.filteredIcons = new ArrayList<>(originalIcons);
    }

    @Override
    public int getCount() {
        return filteredTitles.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredTitles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int pos, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
            holder = new ViewHolder();
            holder.icon = view.findViewById(R.id.featureIcon);
            holder.title = view.findViewById(R.id.featureTitle);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.icon.setImageResource(filteredIcons.get(pos));
        holder.title.setText(filteredTitles.get(pos));

        return view;
    }

    static class ViewHolder {
        ImageView icon;
        TextView title;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                // Agar search bar khali hai (Clear kiya gaya hai)
                if (constraint == null || constraint.length() == 0) {
                    results.values = originalTitles;
                    results.count = originalTitles.size();
                } else {
                    String query = constraint.toString().toLowerCase().trim();
                    List<String> tempTitles = new ArrayList<>();

                    for (String title : originalTitles) {
                        if (title.toLowerCase().contains(query)) {
                            tempTitles.add(title);
                        }
                    }
                    results.values = tempTitles;
                    results.count = tempTitles.size();
                }
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.values != null) {
                    filteredTitles = (List<String>) results.values;

                    // Nayi list create karein icons ke liye
                    filteredIcons = new ArrayList<>();

                    // Sabse important step: Title ke hisaab se sahi Icon match karna
                    for (String title : filteredTitles) {
                        int index = originalTitles.indexOf(title);
                        if (index != -1) {
                            filteredIcons.add(originalIcons.get(index));
                        }
                    }

                    // UI refresh karein
                    notifyDataSetChanged();
                }
            }
        };
    }
}