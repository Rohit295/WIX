package com.drr.wix.api;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wix.common.model.TrackInfo;
import com.wix.common.model.TrackLocationInfo;
import com.wix.common.model.UserInfo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.List;

/**
 * Created by racastur on 01-11-2014.
 */
public class ApiClient {

    private static String TAG = ApiClient.class.getName();

    private static final String API_BASE_URL = "http://glocal-services.appspot.com/services/v1";

    private static final String LOGIN_URL = API_BASE_URL + "/login?emailId=%s";
    private static final String REGISTER_URL = API_BASE_URL + "/register?userId=%d&deviceId=%s&gcmRegistrationId=%s";
    private static final String CREATE_TRACK_URL = API_BASE_URL + "/users/%d/tracks?name=%s";
    private static final String GET_ALL_USER_TRACKS_URL = API_BASE_URL + "/users/%d/tracks";
    private static final String SAVE_TRACK_LOCATION_URL = API_BASE_URL + "/users/%d/tracks/%d/locations?deviceId=%s&timestamp=%d&latitude=%f&longitude=%f";
    private static final String GET_TRACK_LOCATIONS = API_BASE_URL + "/users/%d/tracks/%d/locations";

    private Context ctx;
    private ObjectMapper mapper;

    private static ApiClient instance;

    public synchronized static ApiClient createInstance(Context ctx) {
        if (instance == null) {
            if (ctx == null) {
                throw new RuntimeException("Context cannot be null");
            }
            instance = new ApiClient(ctx);
        }
        return instance;
    }

    public synchronized static ApiClient getInstance() {
        if (instance == null) {
            throw new RuntimeException("Instance is not yet initialized");
        }
        return instance;
    }

    private ApiClient(Context ctx) {

        this.ctx = ctx;

        mapper = new ObjectMapper();
        mapper.setVisibilityChecker(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        mapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    private void logErrorAndThrowException(String method, String url, HttpResponse response) {

        Log.e(TAG, String.format("Error executing [%s] [%s] - [%d][%s]",
                method, url, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));

        String errorMessage = "";
        try {
            errorMessage = EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            Log.e(TAG, "Error reading error response body", e);
        }

        Log.e(TAG, String.format("Error response body [%s]", errorMessage));

        // TODO throw proper exception
        throw new RuntimeException();

    }

    public UserInfo login(String emailId) {

        try {

            HttpClient client = new DefaultHttpClient();

            String url = String.format(LOGIN_URL, emailId);

            HttpUriRequest request = new HttpPost(url);

            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() != 200) {
                logErrorAndThrowException("POST", url, response);
            }

            HttpEntity httpEntity = response.getEntity();
            String apiOutput = EntityUtils.toString(httpEntity);

            return mapper.readValue(apiOutput, UserInfo.class);

        } catch (Exception e) {
            Log.e(TAG, "Error login", e);
            throw new RuntimeException(e);
        }

    }

    public void register(Long userId, Long deviceId, String gcmRegistrationId) {

        try {

            HttpClient client = new DefaultHttpClient();

            String url = String.format(REGISTER_URL, userId, deviceId, gcmRegistrationId);

            HttpUriRequest request = new HttpPost(url);

            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() != 200) {
                logErrorAndThrowException("POST", url, response);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error registering", e);
            throw new RuntimeException(e);
        }

    }

    public TrackInfo createNewTrack(Long userId, String name) {

        try {

            HttpClient client = new DefaultHttpClient();

            String url = String.format(CREATE_TRACK_URL, userId, name);

            HttpUriRequest request = new HttpPost(url);

            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() != 200) {
                logErrorAndThrowException("POST", url, response);
            }

            HttpEntity httpEntity = response.getEntity();
            String apiOutput = EntityUtils.toString(httpEntity);

            return mapper.readValue(apiOutput, TrackInfo.class);

        } catch (Exception e) {
            Log.e(TAG, "Error creating new track", e);
            throw new RuntimeException(e);
        }

    }

    public List<TrackInfo> getTracks(Long userId) {

        try {

            HttpClient client = new DefaultHttpClient();

            String url = String.format(GET_ALL_USER_TRACKS_URL, userId);

            HttpUriRequest request = new HttpGet(url);

            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() != 200) {
                logErrorAndThrowException("GET", url, response);
            }

            HttpEntity httpEntity = response.getEntity();
            String apiOutput = EntityUtils.toString(httpEntity);

            return mapper.readValue(apiOutput,
                    new TypeReference<List<TrackInfo>>() {
                    }
            );

        } catch (Exception e) {
            Log.e(TAG, "Error getting all user tracks", e);
            throw new RuntimeException(e);
        }

    }

    public void saveLocation(Long userId, Long trackId,
                             Long deviceId, Long timestamp,
                             Double latitude, Double longitude) {

        try {

            HttpClient client = new DefaultHttpClient();

            String url = String.format(SAVE_TRACK_LOCATION_URL, userId, trackId, deviceId, timestamp, latitude, longitude);

            HttpUriRequest request = new HttpPost(url);

            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() != 200 && response.getStatusLine().getStatusCode() != 204) {
                logErrorAndThrowException("POST", url, response);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving track location", e);
            throw new RuntimeException(e);
        }

    }

    public List<TrackLocationInfo> getLocations(Long userId, Long trackId) {

        try {

            HttpClient client = new DefaultHttpClient();

            String url = String.format(GET_TRACK_LOCATIONS, userId, trackId);

            HttpUriRequest request = new HttpGet(url);

            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() != 200) {
                logErrorAndThrowException("GET", url, response);
            }

            HttpEntity httpEntity = response.getEntity();
            String apiOutput = EntityUtils.toString(httpEntity);

            return mapper.readValue(apiOutput,
                    new TypeReference<List<TrackLocationInfo>>() {
                    }
            );

        } catch (Exception e) {
            Log.e(TAG, "Error getting all track locations", e);
            throw new RuntimeException(e);
        }

    }

}
