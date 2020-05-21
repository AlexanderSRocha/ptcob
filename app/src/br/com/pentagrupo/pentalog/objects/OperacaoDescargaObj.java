package br.com.pentagrupo.pentalog.objects;

public class OperacaoDescargaObj extends BaseObj{

    public static final String OBJECT_NAME = "OperacaoDescarga__c";
    public static final String CLIENTE__NAME = "Cliente__r.Name";
    public static final String PLACA_VEICULO = "PlacaVeiculo__c";
    public static final String FORNECEDOR = "Fornecedor__c";
    public static final String TIPO_PRODUTO = "TipoProduto__c";
    public static final String STATUS = "Status__c";
    private static final String[] COLUMNS = new String[]{
        ID, NAME, CLIENTE__NAME, PLACA_VEICULO, FORNECEDOR, TIPO_PRODUTO, STATUS
    };

    public OperacaoDescargaObj() {
        super(OBJECT_NAME, COLUMNS);
    }

}
