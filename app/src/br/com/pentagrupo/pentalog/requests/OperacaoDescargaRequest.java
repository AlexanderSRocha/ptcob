package br.com.pentagrupo.pentalog.requests;

import com.salesforce.androidsdk.rest.RestClient;

import org.json.JSONObject;

import br.com.pentagrupo.pentalog.entities.BaseEntity;
import br.com.pentagrupo.pentalog.entities.OperacaoDescarga;
import br.com.pentagrupo.pentalog.objects.OperacaoDescargaObj;

public class OperacaoDescargaRequest extends BaseRequest {

    public OperacaoDescargaRequest(RestClient pClient) {
        super(pClient);
    }

    @Override
    protected BaseEntity getEntity(JSONObject pJsonObject) {
        return new OperacaoDescarga(pJsonObject);
    }

    @Override
    protected String getQuery(String pValue) {
        return new OperacaoDescargaObj().getQuery(pValue);
    }

    @Override
    protected String getSObjectType() {
        return OperacaoDescargaObj.OBJECT_NAME;
    }
}
