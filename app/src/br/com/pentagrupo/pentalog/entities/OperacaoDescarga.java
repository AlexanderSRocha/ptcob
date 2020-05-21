package br.com.pentagrupo.pentalog.entities;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import br.com.pentagrupo.pentalog.objects.OperacaoDescargaObj;

public class OperacaoDescarga extends BaseEntity {

    public String cliente;
    public String placaVeiculo;
    public String fornecedor;
    public String tipoProduto;
    public String status;

    public OperacaoDescarga() {
        super();
    }

    public OperacaoDescarga(JSONObject o) {
        try {
            id = o.getString("Id");
            name = o.getString("Name");
            cliente = o.getJSONObject("Cliente__r").getString("Name");
            placaVeiculo = o.getString("PlacaVeiculo__c");
            fornecedor = o.getString("Fornecedor__c");
            tipoProduto = o.getString("TipoProduto__c");
            status = o.getString("Status__c");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, Object> getObjectMap() {
        Map<String, Object> result = super.getObjectMap();
        result.put(OperacaoDescargaObj.STATUS, status);
        return result;
    }
}
