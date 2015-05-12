package tools.lastfm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squeezymo.lastfmeventsmap.R;
import com.squeezymo.lastfmeventsmap.db.CouchbaseManager;
import com.squeezymo.lastfmeventsmap.model.LastFmEvent;
import com.squeezymo.lastfmeventsmap.model.LastFmEventsResponse;
import com.squeezymo.lastfmeventsmap.model.LastFmImage;
import com.squeezymo.lastfmeventsmap.prefs.Global;
import com.squeezymo.lastfmeventsmap.prefs.Preferences;

import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import tools.Obfuscator;
import tools.serialization.MapSerializer;

public class LastFmClient {
    private static final String LOG_TAG = LastFmClient.class.getCanonicalName();
    private static final String BASE_URL = "https://ws.audioscrobbler.com/2.0/";
    private static final AsyncHttpClient mAsyncClient = new AsyncHttpClient();
    private static final JsonParser mJsonParser = new JsonParser();
    private static final Gson mGson = new GsonBuilder()
            .registerTypeAdapter(LastFmEvent.Artists.class, new LastFmEventArtistsAdapter())
            .registerTypeAdapter(LastFmEvent.Tags.class, new LastFmEventTagsAdapter())
            .create();

    private static boolean initialized;
    private static Context mContext;
    private static LastFmAuthenticator mAuthenticator;

    private static class LastFmEventArtistsAdapter implements JsonDeserializer<LastFmEvent.Artists> {

        public LastFmEvent.Artists deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
            final Map<String, Object> map = new HashMap<String, Object>(2);
            final JsonObject jsonObj = json.getAsJsonObject();

            map.put("headliner", jsonObj.get("headliner").getAsString());

            if (jsonObj.get("artist").isJsonPrimitive()) {
                map.put("artist", new LinkedList<String>() {{ add(jsonObj.get("artist").getAsString()); }});
            }
            else {
                JsonArray jsonArr = jsonObj.get("artist").getAsJsonArray();
                Iterator<JsonElement> it = jsonArr.iterator();
                List<String> artists = new LinkedList<String>();

                while (it.hasNext()) {
                    artists.add(it.next().getAsString());
                }

                map.put("artist", artists);
            }

            return MapSerializer.deserialize(map, LastFmEvent.Artists.class);
        }

    }

    private static class LastFmEventTagsAdapter implements JsonDeserializer<LastFmEvent.Tags> {

        public LastFmEvent.Tags deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
            final Map<String, Object> map = new HashMap<String, Object>(1);
            final JsonObject jsonObj = json.getAsJsonObject();

            if (jsonObj.get("tag").isJsonPrimitive()) {
                map.put("tag", new LinkedList<String>() {{ add(jsonObj.get("tag").getAsString()); }});
            }
            else {
                JsonArray jsonArr = jsonObj.get("tag").getAsJsonArray();
                Iterator<JsonElement> it = jsonArr.iterator();
                List<String> tags = new LinkedList<String>();

                while (it.hasNext()) {
                    tags.add(it.next().getAsString());
                }

                map.put("tag", tags);
            }

            return MapSerializer.deserialize(map, LastFmEvent.Tags.class);
        }

    }

    public static void initialize(Context context) {
        mContext = context.getApplicationContext();
        initialized = true;
    }

    public static void authenticate(final LastFmAuthenticator authenticator) {
        if (!initialized)
            throw new IllegalStateException("Must be initialized before using");

        checkConnectivity();

        mAsyncClient.post(
                mContext,
                BASE_URL,
                new RequestParams() {{
                    add("method", "auth.getMobileSession");
                    add("username", authenticator.getUsername());
                    add("password", authenticator.getPassword());
                    add("api_key", authenticator.getApiKey());
                    add("api_sig", authenticator.getSignature());
                }},
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        mAuthenticator = authenticator;

                        if (mContext != null) {
                            String sessionKey = "";

                            Document doc = Jsoup.parse(new String(responseBody));
                            Elements elems = doc.getElementsByTag("key");
                            if (elems.size() > 0) {
                                sessionKey = elems.first().text();
                            } else {
                                throw new IllegalStateException("No session key in response body");
                            }

                            SharedPreferences prefs = mContext.getSharedPreferences(Preferences.USER_PREFS, Context.MODE_PRIVATE);
                            SharedPreferences.Editor prefEditor = prefs.edit();
                            prefEditor.putString(Preferences.LOGIN_PREF, authenticator.getUsername());
                            prefEditor.putString(Preferences.PASSWORD_PREF, Obfuscator.encode(authenticator.getPassword()));
                            prefEditor.putString(Preferences.SESSION_PREF, Obfuscator.encode(sessionKey));
                            prefEditor.commit();

                            mContext.sendBroadcast(new Intent(Global.LOG_IN_SUCCESS));
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        if (mContext != null) {
                            Intent response = new Intent(Global.LOG_IN_FAILURE);

                            Document doc = Jsoup.parse(new String(responseBody));
                            Elements elems = doc.getElementsByTag("error");
                            if (elems.size() > 0) {
                                response.putExtra(Global.EXTRA_ERR, Integer.parseInt(elems.first().attr("code")));
                            }

                            mContext.sendBroadcast(response);
                        }
                    }
                }
        );
    }

    public static void retrieveEvents(final LastFmEventFilter filter) {
        if (!initialized)
            throw new IllegalStateException("Must be initialized before using");

        checkConnectivity();

        mAsyncClient.post(
                mContext,
                BASE_URL,
                new RequestParams() {{
                    add("method", "geo.getEvents");
                    add("long", Double.toString(filter.getLatLng().longitude));
                    add("lat", Double.toString(filter.getLatLng().latitude));
                    add("distance", Integer.toString(filter.getDistance()));
                    if ( filter.getTag() != null ) {
                        add("tag", filter.getTag());
                    }
                    add("festivalsonly", filter.isFestivalsOnly() ? "1" : "0");
                    add("limit", Integer.toString(filter.getLimit()));
                    add("page", Integer.toString(filter.getPage()));
                    add("format", "json");

                    if ( mAuthenticator != null ) {
                        add("api_key", mAuthenticator.getApiKey());
                    }
                    else if ( mContext != null ) {
                        add("api_key", mContext.getResources().getString(R.string.api_key));
                    }
                    else {
                        throw new IllegalStateException("API key cannot be retrieved");
                    }
                }},
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, final byte[] responseBody) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Looper.prepare();
/*
RESPONSE RECEIVED:
    {"status":"ok"}
    java.lang.NullPointerException: Attempt to invoke virtual method 'java.util.List com.squeezymo.lastfmeventsmap.model.LastFmEventsResponse.getEvents()' on a null object reference
 */
                                LastFmEventsResponse response = mGson.fromJson(
                                        mJsonParser.parse(new String(responseBody)).getAsJsonObject().getAsJsonObject("events"),
                                        LastFmEventsResponse.class
                                );

                                List<LastFmEvent> events = response.getEvents();
                                LastFmEventsResponse.Info info = response.getInfo();

                                if (info != null) {
                                    if ( filter.getPage() < info.getTotalPages() ) {
                                        filter.turnPage();
                                        //retrieveEvents(filter);
                                    }
                                }
                                else {
                                    retrieveEvents(filter);
                                    return;
                                }

                                if (events != null) {
                                    Iterator<LastFmEvent> iterator = events.iterator();

                                    while (iterator.hasNext()) {
                                        LastFmEvent event = iterator.next();
                                        LastFmEvent.Artists lineup = event.getArtists();

                                        if (lineup != null) {
                                            lineup.getExtras().remove(lineup.getHeadliner());
                                        }

                                        try {
                                            CouchbaseManager.insertOrUpdate(event);
                                        } catch (CouchbaseLiteException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                if (mContext != null && events != null) {
                                    mContext.sendBroadcast(new Intent(Global.EVENTS_UPDATED));
                                }

                                Looper.loop();
                            }
                        }).start();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.d(LOG_TAG, "FAILURE: " + statusCode);
                    }
                }
        );

    }

    public static void requestImage(final LastFmEvent event, final LastFmImage image, final Handler callbackHandler) {
        if (!initialized)
            throw new IllegalStateException("Must be initialized before using");

        if (TextUtils.isEmpty(image.getUrl()))
            return;

        checkConnectivity();

        mAsyncClient.get(
                mContext,
                image.getUrl(),
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, final byte[] responseBody) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Looper.prepare();

                                try {
                                    CouchbaseManager.addImageAttachment(event, image, responseBody);

                                    if (callbackHandler != null) {
                                        Message.obtain(callbackHandler, Global.IMAGE_DOWNLOADED, event).sendToTarget();
                                    }
                                } catch (CouchbaseLiteException e) {
                                    e.printStackTrace();
                                }

                                Looper.loop();
                            }
                        }).start();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    }
                }
        );
    }

    private static void checkConnectivity() {
        if (!initialized)
            throw new IllegalStateException("Must be initialized before using");
    }

}
