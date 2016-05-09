package fortunate.harvest;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Ravi on 13/05/15.
 */
public class SwipeListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<Message> messageList;
    private String[] bgColors;

    public SwipeListAdapter(Activity activity, List<Message> messageList) {
        this.activity = activity;
        this.messageList = messageList;
        bgColors = activity.getApplicationContext().getResources().getStringArray(R.array.movie_serial_bg);
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int location) {
        return messageList.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.item_layout, null);

        TextView title = (TextView) convertView.findViewById(R.id.item_title);
        TextView description = (TextView) convertView.findViewById(R.id.item_description);
        TextView date = (TextView) convertView.findViewById(R.id.item_date);

        title.setText(messageList.get(position).title);
        description.setText(messageList.get(position).description);
        date.setText(String.valueOf(messageList.get(position).date_pub));

        //String color = bgColors[position % bgColors.length];
        //description.setBackgroundColor(Color.parseColor(color));

        return convertView;
    }

}