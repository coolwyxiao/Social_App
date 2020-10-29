package com.lisaxiao.eventreporter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

//将数据和view联系起来
public class EventAdapter extends BaseAdapter {
    Context context;
    List<Event> eventData;
    public EventAdapter(Context context) {
        this.context = context;
        eventData = DataService.getEventData();
    }
    @Override
    public int getCount() {
        return eventData.size(); }

    @Override
    public Event getItem(int position) {
        return eventData.get(position); }

    @Override
    public long getItemId(int position) { //处于第几行，id就是多少
        return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) { //可以将数据加到listView中
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.event_item,
                    parent, false); } //在event_item中找到对应的位置，后期再将将数据填充进去
        TextView eventTitle = (TextView) convertView.findViewById( R.id.event_title);
        TextView eventAddress = (TextView) convertView.findViewById( R.id.event_address);
        TextView eventDescription = (TextView) convertView.findViewById( R.id.event_description);

        ImageView imageView = convertView.findViewById(R.id.event_thumbnail);
        Picasso.get().load("https://toppng.com/uploads/preview/balloons-png-11552940817cuso9cwlfk.png").into(imageView); //不需要把所有图片存下来，而是加入动态可变的url，指向图片
        Event r = eventData.get(position);
        eventTitle.setText(r.getTitle()); //相对应的position/textview中填充不同的内容
        eventAddress.setText(r.getAddress());
        eventDescription.setText(r.getDescription());
        return convertView;
    }
}

