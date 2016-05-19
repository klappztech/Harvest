package fortunate.harvest;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        TextView icon = (TextView) convertView.findViewById(R.id.serial);

        title.setText(messageList.get(position).title);
        description.setText(messageList.get(position).description);

        long time_pub = messageList.get(position).date_pub;

        //Date timeInMillis = new Date();
        //timeInMillis.setTime(time_pub*1000); // to millisec

        //Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //String TimeStr = formatter.format(timeInMillis);


        date.setText(getTimeAgo(time_pub*1000));

        if(messageList.get(position).read == 1) {
            title.setTypeface(null, Typeface.NORMAL);
        }


        String color = bgColors[position % bgColors.length];
        icon.setBackgroundColor(Color.parseColor(color));
        icon.setText(title.getText().subSequence(0,1));

        return convertView;
    }

    public String getTimeAgo(long time) {

        final int SECOND_MILLIS = 1000;
        final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        final long DAY_MILLIS = 24 * HOUR_MILLIS;
        final long MONTH_MILLIS = 30 * DAY_MILLIS;
        long diff=0;

        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        if (isToday(time)) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            Date resultdate = new Date(time);
            return sdf.format(resultdate).toString();
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a, dd/MM/yyyy");
            Date resultdate = new Date(time);
            return sdf.format(resultdate).toString();
        }

    }

    private boolean isToday(long time) {

        final int SECOND_MILLIS = 1000;
        final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        final long DAY_MILLIS = 24 * HOUR_MILLIS;
        final long MONTH_MILLIS = 30 * DAY_MILLIS;

        Date resultdate = new Date(time);
        long now = System.currentTimeMillis();

        if( Math.floor(now/DAY_MILLIS) == Math.floor(time/DAY_MILLIS)  ) {
            return true;
        } else {
            return false;
        }

    }

}