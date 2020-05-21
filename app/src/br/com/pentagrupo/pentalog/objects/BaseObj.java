package br.com.pentagrupo.pentalog.objects;

public abstract class BaseObj {

    public static final String ID = "Id";
    public static final String NAME = "Name";

    protected String objectName;
    protected String[] columns;

    public BaseObj(String pObjectName, String[] pColumns) {
        objectName = pObjectName;
        columns = pColumns;
    }

    public String getSampleQuery() {
        return getQuery("");
    }

    public String getQuery(String pCondition) {
        return "SELECT " + parseColumns(columns) + " FROM " + objectName + " " + pCondition;
    }

    protected static String parseColumns(String[] pColumns) {
        String result = "";
        for (String iColumn : pColumns)
            result += iColumn + ", ";
        result = result.substring(0, result.length() - 2);
        return result;
    }

}
