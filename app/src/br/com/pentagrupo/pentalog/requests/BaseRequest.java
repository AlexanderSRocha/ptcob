package br.com.pentagrupo.pentalog.requests;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.CompositeRequest;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import br.com.pentagrupo.pentalog.entities.BaseEntity;
import br.com.pentagrupo.pentalog.entities.OperacaoDescarga;

public abstract class BaseRequest {

    private RestClient client;
    private static final Context NULL_CONTEXT = null;

    public BaseRequest(RestClient pClient) {
        client = pClient;
    }
    
    public void insert(BaseEntity pEntity, DMLCallback pCallback) {
        insert(new ArrayList<BaseEntity>(Arrays.asList(pEntity)), pCallback);
    }

    public void insert(ArrayList<? extends BaseEntity> pListEntity, DMLCallback pCallback) {
        insert(pListEntity, pCallback, true);
    }

    public void insert(final ArrayList<? extends BaseEntity> pListEntity, final DMLCallback pCallback, boolean pAllOrNone) {
        CompositeRequest.CompositeRequestBuilder builder = new CompositeRequest.CompositeRequestBuilder();
        builder.setAllOrNone(pAllOrNone);
        Integer index = 0;
        for (BaseEntity iEntity : pListEntity) {
            builder.addRequest(index + "", RestRequest.getRequestForCreate(ApiVersionStrings.getVersionNumber(NULL_CONTEXT), getSObjectType(), iEntity.getObjectMap()));
            index++;
        }
        doDML(pListEntity, pCallback, builder);
    }

    public void update(BaseEntity pEntity, DMLCallback pCallback) {
        update(new ArrayList<BaseEntity>(Arrays.asList(pEntity)), pCallback, true);
    }

    public void update(ArrayList<? extends BaseEntity> pListEntity, DMLCallback pCallback) {
        update(pListEntity, pCallback, true);
    }

    public void update(final ArrayList<? extends BaseEntity> pListEntity, final DMLCallback pCallback, boolean pAllOrNone) {
        CompositeRequest.CompositeRequestBuilder builder =
                new CompositeRequest.CompositeRequestBuilder();
        builder.setAllOrNone(pAllOrNone);
        Integer index = 0;
        for (BaseEntity iEntity : pListEntity) {
            builder.addRequest(index + "", RestRequest.getRequestForUpdate(ApiVersionStrings.getVersionNumber(NULL_CONTEXT), getSObjectType(), iEntity.id, iEntity.getObjectMap()));
            index++;
        }
        doDML(pListEntity, pCallback, builder);
    }

    public void delete(BaseEntity pEntity, DMLCallback pCallback) {
        delete(new ArrayList<BaseEntity>(Arrays.asList(pEntity)), pCallback, true);
    }

    public void delete(ArrayList<? extends BaseEntity> pListEntity, DMLCallback pCallback) {
        delete(pListEntity, pCallback, true);
    }

    public void delete(final ArrayList<? extends BaseEntity> pListEntity, final DMLCallback pCallback, boolean pAllOrNone) {
        CompositeRequest.CompositeRequestBuilder builder =
                new CompositeRequest.CompositeRequestBuilder();
        builder.setAllOrNone(pAllOrNone);
        Integer index = 0;
        for (BaseEntity iEntity : pListEntity) {
            builder.addRequest(index + "", RestRequest.getRequestForDelete(ApiVersionStrings.getVersionNumber(NULL_CONTEXT), getSObjectType(), iEntity.id));
            index++;
        }
        doDML(pListEntity, pCallback, builder);
    }

    public void query(QueryCallback pCallback) {
        query("", pCallback);
    }

    public void query(String pCondition, final QueryCallback pCallback) {
        try {
            RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(NULL_CONTEXT), getQuery(pCondition));
            client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
                @Override
                public void onSuccess(RestRequest request, final RestResponse result) {
                    result.consumeQuietly(); // consume before going back to main thread
                    try {
                        ArrayList<BaseEntity> lstResult = new ArrayList<BaseEntity>();
                        JSONArray records = result.asJSONObject().getJSONArray("records");
                        for (int i = 0; i < records.length(); i++) {
                            lstResult.add(getEntity(records.getJSONObject(i)));
                        }
                        pCallback.onSuccess(lstResult);
                    } catch (Exception e) {
                        onError(e);
                    }
                }

                @Override
                public void onError(final Exception exception) {
                    pCallback.onError(exception);
                }
            });
        } catch(Exception e) {
            pCallback.onError(e);
        }
    }

    private void doDML(final ArrayList<? extends BaseEntity> pListEntity, final DMLCallback pCallback, CompositeRequest.CompositeRequestBuilder pBuilder) {
        try {
            CompositeRequest cr = pBuilder.build(ApiVersionStrings.getVersionNumber(NULL_CONTEXT));
            client.sendAsync(cr, new RestClient.AsyncRequestCallback() {
                @Override
                public void onSuccess(RestRequest request, RestResponse response) {
                    handleDMLResponse(pListEntity, pCallback, response);
                }

                @Override
                public void onError(Exception exception) {
                    pCallback.onError(new ArrayList<Exception>(Arrays.asList(exception)));
                }
            });
        } catch(Exception e) {
            pCallback.onError(new ArrayList<Exception>(Arrays.asList(e)));
        }
    }

    private void handleDMLResponse(ArrayList<? extends BaseEntity> pListEntity, DMLCallback pCallback, RestResponse pResponse) {
        pResponse.consumeQuietly(); // consume before going back to main thread
        JSONObject responseJson;
        try {
            ArrayList<BaseEntity> listSuccess = new ArrayList<BaseEntity>();
            ArrayList<Exception> listDmlException = new ArrayList<Exception>();
            responseJson = pResponse.asJSONObject();
            JSONArray responses = responseJson.getJSONArray("compositeResponse");
            for (int i = 0; i < responses.length(); i++) {
                JSONObject iResponse = responses.getJSONObject(i);
                if (iResponse.getInt("httpStatusCode") < 200 || iResponse.getInt("httpStatusCode") > 205 ) {
                    listDmlException.add(getDMLException(iResponse));
                } else {
                    BaseEntity entitySuccess = pListEntity.get(i);
                    String responseId = getId(iResponse);
                    entitySuccess.id = TextUtils.isEmpty(responseId) ? entitySuccess.id : responseId;
                    listSuccess.add(entitySuccess);
                }
            }
            if (!listDmlException.isEmpty()) {
                pCallback.onError(listDmlException);
            }
            if (!listSuccess.isEmpty()) {
                pCallback.onSuccess(listSuccess);
            }
        } catch (Exception e) {
            pCallback.onError(new ArrayList<Exception>(Arrays.asList(e)));
        }
    }

    private String getId(JSONObject pResponse) {
        String result = null;
        try {
            result = pResponse.getJSONObject("body").getString("id");
        } catch (JSONException e) {
            // e.printStackTrace();
        }
        return result;
    }

    private DMLException getDMLException(JSONObject pResponse) throws JSONException {
        DMLException result = new DMLException("Unknown", "Unknown");
        if (pResponse.has("body")) {
            JSONObject iResponseBody;
            if (pResponse.get("body") instanceof JSONArray) {
                iResponseBody = pResponse.getJSONArray("body").getJSONObject(0);
            } else {
                iResponseBody = pResponse.getJSONObject("body");
            }
            if (iResponseBody != null) {
                result = new DMLException(iResponseBody.getString("message"), iResponseBody.getString("errorCode"));
            }
        }
        return result;
    }

    protected abstract BaseEntity getEntity(JSONObject pJsonObject);

    protected abstract String getQuery(String pValue);

    protected abstract String getSObjectType();

    public interface DMLCallback {
        void onSuccess(ArrayList<? extends BaseEntity> pListData);
        void onError(ArrayList<Exception> pListException);
    }

    public interface QueryCallback{
        void onSuccess(ArrayList<? extends BaseEntity> pListData);
        void onError(Exception pListException);
    }

    public class DMLException extends Exception {
        public String message;
        public String errorCode;

        public DMLException(String pMessage, String pErrorCode) {
            message = pMessage;
            errorCode = pErrorCode;
        }

        @Nullable
        @Override
        public String getMessage() {
            return message;
        }
    }

}
