package com.squeezymo.lastfmeventsmap.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.squeezymo.lastfmeventsmap.R;
import com.squeezymo.lastfmeventsmap.db.CouchbaseManager;
import com.squeezymo.lastfmeventsmap.model.LastFmEvent;
import com.squeezymo.lastfmeventsmap.model.LastFmImage;
import com.squeezymo.lastfmeventsmap.prefs.Globals;
import com.squeezymo.lastfmeventsmap.ui.activities.EventLookupActivity;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tools.TimeConverter;

public class EventsListAdapter
        extends RecyclerView.Adapter<EventsListAdapter.EventViewHolder>
        implements StickyRecyclerHeadersAdapter<EventsListAdapter.HeaderViewHolder> {

    private static final String LOG_TAG = EventsListAdapter.class.getCanonicalName();

    private Context mContext;
    private Map<Long, Integer> mPositionByEventId;
    private List<LastFmEvent> mEvents;
    private Handler mHandler;

    /* VIEW TAGS */
    private enum EventViewTag {
        ATTENDANCE
    };

    /* TAG BACKGROUND SPAN */
    public class TagBackgroundSpan extends ReplacementSpan {
        private final int mPadding = 10;
        private int mBackgroundColor;
        private int mTextColor;

        public TagBackgroundSpan(int backgroundColor, int textColor) {
            super();
            mBackgroundColor = backgroundColor;
            mTextColor = textColor;
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            return (int) (mPadding + paint.measureText(text.subSequence(start, end).toString()) + mPadding);
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            float width = paint.measureText(text.subSequence(start, end).toString());
            RectF rect = new RectF(x, top+mPadding/2, x+width+2*mPadding, bottom);
            paint.setColor(mBackgroundColor);
            canvas.drawRoundRect(rect, mPadding/2, mPadding/2, paint);
            paint.setColor(mTextColor);
            canvas.drawText(text, start, end, x+mPadding, y, paint);
        }
    }

    /* HEADER VIEW HOLDER */
    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView date;

        public HeaderViewHolder(View v) {
            super(v);
            date = (TextView) v.findViewById(R.id.date);
        }

        public void bindItem(long timestamp) {
            try {
                String header = TimeConverter.getDateHeaderFromTimestamp(timestamp);
                switch (header) {
                    case TimeConverter.TODAY:
                        date.setText(mContext.getResources().getString(R.string.header_today));
                        break;
                    case TimeConverter.TOMORROW:
                        date.setText(mContext.getResources().getString(R.string.header_tomorrow));
                        break;
                    default:
                        date.setText(header);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    /* EVENT VIEW HOLDER */
    public class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private LastFmEvent event;
        private ImageView image;
        private TextView title;
        private TextView artists;
        private TextView venue;
        private TextView tags;
        private TextView attendance;

        public EventViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.event_image);
            title = (TextView) v.findViewById(R.id.event_title);
            artists = (TextView) v.findViewById(R.id.event_artists);
            venue = (TextView) v.findViewById(R.id.event_venue);
            tags = (TextView) v.findViewById(R.id.event_tags);

            attendance = (TextView) v.findViewById(R.id.event_attendance);
            attendance.setTag(EventViewTag.ATTENDANCE);
            attendance.setOnClickListener(this);

            v.setOnClickListener(this);
        }

        public void bindItem(LastFmEvent event) {
            this.event = event;

            /* TITLE */
            title.setText(event.getTitle());

            /* IMAGE */
            try {
                Bitmap bitmap = CouchbaseManager.retrieveImage(event, LastFmImage.Size.LARGE, mHandler);

                if (bitmap == null) {
                    bitmap = Bitmap.createScaledBitmap(
                            BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_artist_medium),
                            LastFmImage.Size.LARGE.getWidth(),
                            LastFmImage.Size.LARGE.getHeight(),
                            false
                    );
                }

                image.setImageDrawable(new BitmapDrawable(mContext.getResources(), bitmap));
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }

            /* VENUE */
            if (event.getVenue() != null) {
                venue.setText(
                        Html.fromHtml(
                                "<b>" + event.getVenue().getName() + "</b>" + "<br />" +
                                event.getVenue().getCity() + ", " + event.getVenue().getCountry()
                        )
                );
            }

            /* LINEUP */
            LastFmEvent.Artists lineup = event.getArtists();
            if (lineup != null) {
                StringBuilder lineupStr = new StringBuilder(lineup.getHeadliner());
                if (lineup.getExtras().size() > 0) {
                    lineupStr.append(", ");
                    lineupStr.append(TextUtils.join(", ", lineup.getExtras()));
                }

                artists.setText(Html.fromHtml(lineupStr.toString()));
            }

            /* ATTENDANCE */
            attendance.setText(mContext.getResources().getString(R.string.attendance, event.getAttendance()));

            /* TAGS */
            SpannableString tagsBuilder = new SpannableString(TextUtils.join(" ", event.getTags().getTags()));

            int offset = 0;

            for (String tag : event.getTags().getTags()) {
                ReplacementSpan bgColor = new TagBackgroundSpan(Color.rgb(1, 135, 197), Color.WHITE);
                tagsBuilder.setSpan(bgColor, offset, offset+tag.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                offset += tag.length() + 1;
            }

            tags.setText(tagsBuilder, TextView.BufferType.SPANNABLE);
        }

        @Override
        public void onClick(View view) {
            Object tagObj = view.getTag();

            if (tagObj == null || !(tagObj instanceof EventViewTag)) {
                Intent intent = new Intent(mContext, EventLookupActivity.class);
                intent.putExtra(EventLookupActivity.EXTRA_EVENT, event);
                mContext.startActivity(intent);
            }
            else {
                EventViewTag tag = (EventViewTag) tagObj;
                switch (tag) {
                    case ATTENDANCE:
                        Log.d(LOG_TAG, "Clicked: attendance");
                        break;
                }
            }
        }
    }

    /* ADAPTER */
    public EventsListAdapter(Context context, List<LastFmEvent> events) {
        mContext = context;

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case Globals.IMAGE_DOWNLOADED:
                        notifyEventChanged((LastFmEvent) inputMessage.obj);
                        break;
                }
            }
        };

        mPositionByEventId = new HashMap<Long, Integer>();
        setEvents(events);
    }

    public void setEvents(List<LastFmEvent> events) {
        mEvents = events;
        mPositionByEventId.clear();
        notifyDataSetChanged();
    }

    private void notifyEventChanged(LastFmEvent event) {
        if (event == null)
            return;

        if (mPositionByEventId.containsKey(event.getId())) {
            notifyItemChanged(mPositionByEventId.get(event.getId()));
        }
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.event_viewholder, viewGroup, false);
        return new EventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(EventViewHolder viewHolder, int position) {
        LastFmEvent event = mEvents.get(position);
        mPositionByEventId.put(event.getId(), position);
        viewHolder.bindItem(event);
    }

    @Override
    public long getHeaderId(int position) {
        LastFmEvent event = mEvents.get(position);
        try {
            return TimeConverter.nullifyTimeFromDate(event.getStartDate());
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.header_viewholder, viewGroup, false);
        return new HeaderViewHolder(itemView);
    }

    @Override
    public void onBindHeaderViewHolder(HeaderViewHolder viewHolder, int position) {
        viewHolder.bindItem(getHeaderId(position));
    }
}
