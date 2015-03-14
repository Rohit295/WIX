package com.drr.wix.api;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wix.common.model.LocationDTO;
import com.wix.common.model.RouteExecutionDTO;
import com.wix.common.model.RouteExecutionLocationDTO;
import com.wix.common.model.RouteExecutionStatus;
import com.wix.common.model.UserDTO;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.List;

/**
 * Created by racastur on 01-11-2014.
 */
public class ApiClient {

    private static String TAG = ApiClient.class.getName();

    private static final String USER_ID_HEADER_KEY = "userId";

    private static final String API_BASE_URL = "http://glocal-services.appspot.com/services/v1";

    private static final String LOGIN_URL = API_BASE_URL + "/login?emailId=%s";
    private static final String REGISTER_URL = API_BASE_URL + "/register?userId=%d&deviceId=%s&gcmRegistrationId=%s";

    private static final String UPDATE_ROUTE_EXECUTION_STATUS_URL = API_BASE_URL + "/routeexecutions/%s/status?executionStatus=%s";
    private static final String SAVE_TRACK_LOCATION_URL = API_BASE_URL + "/routeexecutions/%s/location";

    private static final String GET_ASSIGNED_ROUTE_EXECUTIONS_URL = API_BASE_URL + "/routeexecutions";
    private static final String GET_TRACK_LOCATIONS = API_BASE_URL + "/users/%d/tracks/%d/locations";

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

    public UserDTO login(String emailId) {

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

            return mapper.readValue(apiOutput, UserDTO.class);

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

    public RouteExecutionDTO updateRouteExecutionStatus(String userId, String routeExecutionId, RouteExecutionStatus status) {

        try {

            HttpClient client = new DefaultHttpClient();

            String url = String.format(UPDATE_ROUTE_EXECUTION_STATUS_URL, routeExecutionId, status.name());

            HttpUriRequest request = new HttpPost(url);
            request.addHeader(USER_ID_HEADER_KEY, userId);

            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() != 200) {
                logErrorAndThrowException("POST", url, response);
            }

            HttpEntity httpEntity = response.getEntity();
            String apiOutput = EntityUtils.toString(httpEntity);

            return mapper.readValue(apiOutput, RouteExecutionDTO.class);

        } catch (Exception e) {
            Log.e(TAG, "Error creating new track", e);
            throw new RuntimeException(e);
        }

    }

    public void saveLocation(String userId, String routeExecutionId, long timestamp,
                             double latitude, double longitude) {

        try {

            LocationDTO locationDTO = new LocationDTO();
            locationDTO.setLatitude(latitude);
            locationDTO.setLongitude(longitude);

            RouteExecutionLocationDTO dto = new RouteExecutionLocationDTO();
            dto.setTimestamp(timestamp);
            dto.setLocation(locationDTO);

            HttpClient client = new DefaultHttpClient();

            String url = String.format(SAVE_TRACK_LOCATION_URL, routeExecutionId);

            HttpPost request = new HttpPost(url);
            request.addHeader(USER_ID_HEADER_KEY, userId);

            String json = mapper.writeValueAsString(dto);

            StringEntity entity = new StringEntity(json);
            entity.setContentType("application/json");

            request.setEntity(entity);

            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() != 200 && response.getStatusLine().getStatusCode() != 204) {
                logErrorAndThrowException("POST", url, response);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving track location", e);
            throw new RuntimeException(e);
        }

    }

    public List<RouteExecutionDTO> getAssignedRoutExecutions(String userId) {

        try {

            HttpClient client = new DefaultHttpClient();

            String url = String.format(GET_ASSIGNED_ROUTE_EXECUTIONS_URL);

            HttpUriRequest request = new HttpGet(url);
            request.addHeader(USER_ID_HEADER_KEY, userId);

            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() != 200) {
                logErrorAndThrowException("GET", url, response);
            }

            HttpEntity httpEntity = response.getEntity();
            String apiOutput = EntityUtils.toString(httpEntity);

            return mapper.readValue(apiOutput,
                    new TypeReference<List<RouteExecutionDTO>>() {
                    }
            );

        } catch (Exception e) {
            Log.e(TAG, "Error getting all user tracks", e);
            throw new RuntimeException(e);
        }

    }

//    public List<TrackLocationInfo> getLocations(Long userId, Long trackId) {
//
//        try {
//
//            HttpClient client = new DefaultHttpClient();
//
//            String url = String.format(GET_TRACK_LOCATIONS, userId, trackId);
//
//            HttpUriRequest request = new HttpGet(url);
//
//            HttpResponse response = client.execute(request);
//            if (response.getStatusLine().getStatusCode() != 200) {
//                logErrorAndThrowException("GET", url, response);
//            }
//
//            HttpEntity httpEntity = response.getEntity();
//            String apiOutput = EntityUtils.toString(httpEntity);
//
//            return mapper.readValue(apiOutput,
//                    new TypeReference<List<TrackLocationInfo>>() {
//                    }
//            );
//
//        } catch (Exception e) {
//            Log.e(TAG, "Error getting all track locations", e);
//            throw new RuntimeException(e);
//        }
//
//    }

}
