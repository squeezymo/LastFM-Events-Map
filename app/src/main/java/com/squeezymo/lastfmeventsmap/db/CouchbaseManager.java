package com.squeezymo.lastfmeventsmap.db;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.Revision;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.View;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.support.Base64;
import com.squeezymo.lastfmeventsmap.model.LastFmEvent;
import com.squeezymo.lastfmeventsmap.model.LastFmImage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import tools.lastfm.LastFmClient;
import tools.lastfm.LastFmEventFilter;
import tools.serialization.MapSerializer;

public class CouchbaseManager {
    private static final String LOG_TAG = CouchbaseManager.class.getCanonicalName();

    private static final String DB_EVENTS = "events";
    private static final String VIEW_EVENTS_BY_TIMESTAMP = DB_EVENTS + "_by_timestamp";
    private static final String ATTACHMENT_IMAGE = "image_";
    private static final String START_DATE_KEY = "startDate";
    private static final String LATITUDE_KEY = "lat";
    private static final String LONGITUDE_KEY = "lng";

    private static Manager mManager;
    private static Database mDatabase;

    public static synchronized void createDB(final Context context) {
        try {
            mManager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
            mDatabase = mManager.getDatabase(DB_EVENTS);

            View byLocationView = mDatabase.getView(VIEW_EVENTS_BY_TIMESTAMP);
            byLocationView.setMap(new Mapper() {
                @Override
                public void map(Map<String, Object> eventDoc, Emitter emitter) {
                    LastFmEvent event = MapSerializer.deserialize(eventDoc, LastFmEvent.class);

                    if (event == null)
                        return;

                    emitter.emit(event.getStartDateStamp(), null);
                }
            }, "4");
        } catch (IOException e) {
            Log.e(LOG_TAG, "Cannot create manager object");
            return;
        }
        catch (CouchbaseLiteException e) {
            Log.e(LOG_TAG, "Cannot get database");
            return;
        }
    }

    private static Document getDocumentByID(final Object idObj) {
        Document doc = null;

        if ( idObj != null ) {
            if ( idObj instanceof Long ) {
                doc = mDatabase.getDocument(Long.toString((Long) idObj));
            }
            else if ( idObj instanceof String ) {
                String id = (String) idObj;
                if (!TextUtils.isEmpty(id)) {
                    doc = mDatabase.getDocument(id);
                }
            }
        }
        if ( doc == null ) {
            doc = mDatabase.createDocument();
        }

        return doc;
    }

    public static synchronized void insertOrUpdate(final Object obj) throws CouchbaseLiteException {
        final Map<String, Object> properties = MapSerializer.serialize(obj);
        final Document doc = getDocumentByID(properties.get("id"));

        doc.update(new Document.DocumentUpdater() {
            @Override
            public boolean update(UnsavedRevision newRevision) {
                newRevision.setUserProperties(properties);
                return true;
            }
        });
    }

    public static synchronized void addImageAttachment(final LastFmEvent event, final LastFmImage image, final byte[] img) throws CouchbaseLiteException {
        final Document doc = getDocumentByID(event.getId());
        final UnsavedRevision newRev = doc.getCurrentRevision().createRevision();
        newRev.setAttachment(ATTACHMENT_IMAGE + image.getSize(), "image/jpeg", new ByteArrayInputStream(img));
        newRev.save();
    }

    public static List<LastFmEvent> getAllEvents() throws CouchbaseLiteException {
        List<LastFmEvent> events = new ArrayList<LastFmEvent>();

        QueryEnumerator result = mDatabase.createAllDocumentsQuery().run();
        Log.d(LOG_TAG, "EVENTS FOUND (ALL): " + result.getCount() + "\nDOCS IN DATABASE" + mDatabase.getDocumentCount());

        if ( result != null ) {
            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                QueryRow eventRow = it.next();
                LastFmEvent event = MapSerializer.deserialize(eventRow.getDocument().getProperties(), LastFmEvent.class);
                events.add(event);
            }
        }

        return events;
    }

    public static List<LastFmEvent> getAllEvents(final LastFmEventFilter filter) throws CouchbaseLiteException {
        List<LastFmEvent> events = new ArrayList<LastFmEvent>();

        Query query = mDatabase.getView(VIEW_EVENTS_BY_TIMESTAMP).createQuery();
        if ( filter != null ) {
            query.setStartKey(filter.getStartDate().getTime());
            query.setEndKey(filter.getEndDate().getTime());
        }

        QueryEnumerator result = query.run();
        Log.d(LOG_TAG, "EVENTS FOUND (WITH FILTER): " + result.getCount() + "\nDOCS IN DATABASE" + mDatabase.getDocumentCount());

        Iterator<QueryRow> it = result;

        while (it.hasNext()) {
            QueryRow row = it.next();
            LastFmEvent event = MapSerializer.deserialize(row.getDocument().getProperties(), LastFmEvent.class);
            events.add(event);
        }

        return events;
    }

    public static Bitmap retrieveImage(final LastFmEvent event, final LastFmImage.Size size, final Handler callbackHandler) throws CouchbaseLiteException {
        if (event.getImages() == null)
            return null;

        LastFmImage image = null;

        for (LastFmImage currImage : event.getImages()) {
            if (currImage.getSize().equals(size.getTxt())) {
                image = currImage;
                break;
            }
        }

        if (image == null)
            return null;

        Document eventDoc = getDocumentByID(event.getId());
        Attachment att = eventDoc.getCurrentRevision().getAttachment(ATTACHMENT_IMAGE + image.getSize());

        if (att == null) {
            LastFmClient.requestImage(event, image, callbackHandler);
            return null;
        }

        InputStream stream = att.getContent();
        return BitmapFactory.decodeStream(stream);
    }

}
