/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

/**
 *
 * @author Utilizador
 */
import AtlasClient.AtlasConsumer;
import com.hortonworks.hwc.Connections;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonControler {

    public JsonControler() {
    }
    /*
     This function has the purpose of creating json to create an entity in the Atlas.
     It is invoked in the ColumnProfiler Class (package basicProfiler)
     All parameters are related with statistics of one column. 
            
     */

    public void updateNumAudits(String tableName, String dbName) throws JSONException, IOException {

        AtlasConsumer consumer = new AtlasConsumer();
        JSONObject jsonTable = consumer.getGuidHiveTable(tableName, "hive_table");
        JSONObject jsonDb = consumer.getGuidHiveTable(dbName, "hive_db");
        String guidDataBase = jsonDb.getJSONArray("results").getJSONObject(0).getJSONObject("$id$").getString("id");
        String guidTable = null;

        for (int i = 0; i < jsonDb.getJSONArray("results").length(); i++) {

            if (jsonTable.getJSONArray("results").getJSONObject(i).getJSONObject("db").getString("id").equals(guidDataBase)) {
                guidTable = jsonTable.getJSONArray("results").getJSONObject(i).getJSONObject("$id$").getString("id");

            }

        }

        System.out.println("O GUID da tabela � " + guidTable);
        int jsonAudtis = consumer.getAudtisTable(guidTable).length();

        System.out.println("Audtis �  " + jsonAudtis);
        consumer.updateAuditsTable(jsonAudtis, guidTable);
    }

    public JSONObject createEntityColumnProfiler(String columnName, String datatype, String database, String tablename,
            String comment, String min, String max, long recordCount, long uniqueValues, long emptyValues, long nullValues, String maxFieldLength, String minFieldLength, long percentFillRecords, long percentUniqueValues, long numTrueValues, long numFalseValues, Dataset<Row> frequencyValuesDS) {
        AtlasConsumer getTableName = new AtlasConsumer();
        JSONObject jsonfinal = null;
        try {
            String idTableName = getTableName.getIDAtlasTableACTIVE(tablename, database);
            String idColumn = getTableName.getIDAtlasColumnACTIVE(columnName, tablename, database);

            jsonfinal = new JSONObject();
            jsonfinal.put("jsonClass", "org.apache.atlas.typesystem.json.InstanceSerialization$_Reference");

            JSONObject id = new JSONObject();
            id.put("jsonClass", "org.apache.atlas.typesystem.json.InstanceSerialization$_Id");
            id.put("id", "-1466683608564093000");
            id.put("version", 0);
            id.put("typeName", "ColumnStatistics");
            jsonfinal.put("id", id);

            jsonfinal.put("typeName", "ColumnStatistics");

            JSONObject values = new JSONObject();
            values.put("name", columnName);
            values.put("dataTypeValue", datatype);

            JSONObject tablenameEntity = new JSONObject();
            tablenameEntity.put("jsonClass", "org.apache.atlas.typesystem.json.InstanceSerialization$_Id");
            tablenameEntity.put("id", idTableName);
            tablenameEntity.put("version", 0);
            tablenameEntity.put("typeName", "hive_table");
            tablenameEntity.put("state", "ACTIVE");
            values.put("tableName", tablenameEntity);
            JSONObject frequencyValues = new JSONObject();
            for (int i = 0; i < frequencyValuesDS.collectAsList().size(); i++) {
                frequencyValues.put(frequencyValuesDS.select(columnName).collectAsList().get(i).mkString(), frequencyValuesDS.select("count").collectAsList().get(i).mkString());
            }
            values.put("FrequencyValues", frequencyValues);
            JSONObject columnEntity = new JSONObject();
            columnEntity.put("jsonClass", "org.apache.atlas.typesystem.json.InstanceSerialization$_Id");
            columnEntity.put("id", idColumn);
            columnEntity.put("version", 0);
            columnEntity.put("typeName", "hive_column");
            columnEntity.put("state", "ACTIVE");
            values.put("columnReference", columnEntity);
            Instant instant = Instant.now();
            values.put("createTime", instant.toString());
            values.put("comment", comment);
            values.put("owner", "adminLiD4");
            values.put("description", "Profiling attribute " + columnName + " from " + tablename + ". DB: " + database);
            values.put("qualifiedName", "st." + database + "." + tablename + "." + columnName);
            values.put("description", "Profiling attribute " + columnName + " from " + tablename + ". DB: " + database);
            values.put("numEmptyValues", (int) emptyValues);
            values.put("maxValue", max);
            values.put("minValue", min);
            values.put("maxFieldLenght", Integer.parseInt(maxFieldLength));
            values.put("minFieldLenght", Integer.parseInt(minFieldLength));
            values.put("numNullValues", (int) nullValues);
            values.put("numFalseValues", (int) numFalseValues);
            values.put("numTrueValues", (int) numTrueValues);
            values.put("PercentFillRecords", (int) percentFillRecords);
            values.put("PercentUniqueValues", (int) percentUniqueValues);
            values.put("numUniqueValues", (int) uniqueValues);
            values.put("numRecords", (int) recordCount);
            jsonfinal.put("values", values);
            JSONArray traitNames = new JSONArray();
            jsonfinal.put("traitNames", traitNames);
            JSONObject traits = new JSONObject();
            jsonfinal.put("traits", traits);
        } catch (Exception e) {
            e.getMessage();
        }
        return jsonfinal;
    }

    /*
     This function has the purpose of creating json to create a Process Entity in the Atlas.
     It is invoked in  Profiler Class (package basicProfiler)
     TableName - TableName 
     Database - database related to Table 
     StartDate - Json read as String but the format is Date . Date when the process started
     EndDate - Date when the process was finished; 
            
     */
    public JSONObject createEntityProcess(String tableName, String database, String startdate, String endDate) {

        AtlasConsumer restConsumer = new AtlasConsumer();
        JSONObject jsonfinal = null;
        try {
            String idTableName = restConsumer.getIDAtlasTableACTIVE(tableName, database);
            String idTableStatistics = restConsumer.getIDTableStatistics(idTableName);

            jsonfinal = new JSONObject();
            jsonfinal.put("jsonClass", "org.apache.atlas.typesystem.json.InstanceSerialization$_Reference");
            JSONObject id = new JSONObject();
            id.put("jsonClass", "org.apache.atlas.typesystem.json.InstanceSerialization$_Id");
            id.put("id", "-1466683608564093000");
            id.put("version", 0);
            id.put("typeName", "Process");
            jsonfinal.put("id", id);

            jsonfinal.put("typeName", "ProfilerProcesses");
            JSONObject values = new JSONObject();

            values.put("startTime", startdate);
            values.put("endTime", endDate);

            values.put("userName", "rajops");
            values.put("operationType", "Profiler Quality Operation");
            values.put("clusterName", "lid4.dsi.uminho.pt");
            values.put("query", "");

            JSONArray inputs = new JSONArray();
            JSONObject inputEntity = new JSONObject();
            inputEntity.put("jsonClass", "org.apache.atlas.typesystem.json.InstanceSerialization$_Id");
            inputEntity.put("id", idTableName);
            inputEntity.put("version", 0);
            inputEntity.put("typeName", "hive_table");
            inputEntity.put("state", "ACTIVE");
            inputs.put(inputEntity);
            values.put("inputs", inputs);

            JSONArray outputs = new JSONArray();
            JSONObject outputEntity = new JSONObject();
            outputEntity.put("jsonClass", "org.apache.atlas.typesystem.json.InstanceSerialization$_Id");
            outputEntity.put("id", idTableStatistics);
            outputEntity.put("version", 0);
            outputEntity.put("typeName", "TableStatistics");
            outputEntity.put("state", "ACTIVE");
            outputs.put(outputEntity);
            values.put("outputs", outputs);

            values.put("qualifiedName", "prc." + database + "." + tableName);
            values.put("name", "Profiler Table " + tableName);
            values.put("description", "Profiler Quality Process ");
            values.put("owner", "EDM_RANDD");

            jsonfinal.put("values", values);
            JSONArray traitNames = new JSONArray();
            jsonfinal.put("traitNames", traitNames);
            JSONObject traits = new JSONObject();
            jsonfinal.put("traits", traits);

        } catch (Exception e) {
            e.getMessage();
        }
        return jsonfinal;
    }

    /*
     This function has the purpose of creating json to create a Process Entity in the Atlas ( Profiler Column  Process).
     It is invoked in  Profiler Class (package basicProfiler)
     TableName - TableName 
     Database - database related to Table 
     StartDate - Json read as String but the format is Date . Date when the process started
     EndDate - Date when the process was finished; 
     OutputCols - All columns are related with the profiler processes ( 
            
     */
    public JSONObject createEntityProcessColumnProfiler(String tableName, String database, String startdate, String endDate, String[] outputcols) {
        try {
            AtlasConsumer restConsumer = new AtlasConsumer();
            String idTableName = restConsumer.getIDAtlasTableACTIVE(tableName, database);

            JSONObject jsonfinal = new JSONObject();
            jsonfinal.put("jsonClass", "org.apache.atlas.typesystem.json.InstanceSerialization$_Reference");
            JSONObject id = new JSONObject();
            id.put("jsonClass", "org.apache.atlas.typesystem.json.InstanceSerialization$_Id");
            id.put("id", "-1466683608564093000");
            id.put("version", 0);
            id.put("typeName", "Process");
            jsonfinal.put("id", id);

            jsonfinal.put("typeName", "ProfilerProcesses");
            JSONObject values = new JSONObject();

            values.put("startTime", startdate);
            values.put("endTime", endDate);

            values.put("userName", "rajops");
            values.put("operationType", "Column Profiler Quality Task");
            values.put("clusterName", "lid4.dsi.uminho.pt");
            values.put("query", "");

            JSONArray inputs = new JSONArray();
            JSONObject inputEntity = new JSONObject();
            inputEntity.put("jsonClass", "org.apache.atlas.typesystem.json.InstanceSerialization$_Id");
            inputEntity.put("id", idTableName);
            inputEntity.put("version", 0);
            inputEntity.put("typeName", "hive_table");
            inputEntity.put("state", "ACTIVE");
            inputs.put(inputEntity);
            values.put("inputs", inputs);

            JSONArray outputs = new JSONArray();
            for (String columnName : outputcols) {
                String idColumnStatistics = restConsumer.getIDColumnStatistics(idTableName, columnName);
                JSONObject outputEntity = new JSONObject();
                outputEntity.put("jsonClass", "org.apache.atlas.typesystem.json.InstanceSerialization$_Id");
                outputEntity.put("id", idColumnStatistics);
                outputEntity.put("version", 0);
                outputEntity.put("typeName", "ColumnStatistics");
                outputEntity.put("state", "ACTIVE");
                outputs.put(outputEntity);
            }

            values.put("outputs", outputs);
            values.put("qualifiedName", "prc." + tableName);
            values.put("name", "Profiler Column From Table " + tableName);
            values.put("description", "Profiler Column  Process");
            values.put("owner", "EDM_RANDD");

            jsonfinal.put("values", values);
            JSONArray traitNames = new JSONArray();
            jsonfinal.put("traitNames", traitNames);
            JSONObject traits = new JSONObject();
            jsonfinal.put("traits", traits);
            return jsonfinal;

        } catch (Exception e) {
            e.getMessage();
        }
        return null;
    }

    /*
     This function has the purpose of creating json to create a TableStatistics Entity in the Atlas ( Profiler Table  Process).
     It is invoked in  Profiler Class (package basicProfiler)
     TableName - TableName 
     Database - database related to Table 
     StartDate - Json read as String but the format is Date . Date when the process started
     EndDate - Date when the process was finished; 
     OutputCols - All columns are related with the profiler processes (             
     */
    public JSONObject createEntityTableProfiler(String database, String tablename, int numCategoricalColumns, int numDateColumns, int numObservations,
            int numVariable, int numNumericalColumns, int numOtherTypesColumns, int dataSetSize) throws JSONException {

        JSONObject jsonfinal = null;
        AtlasConsumer restConsumer = new AtlasConsumer();
        String idTableName = restConsumer.getIDAtlasTableACTIVE(tablename, database);
        String idDB = restConsumer.getDBID(idTableName);
        ArrayList<String> arrayColumnStats = restConsumer.getColumnStatsID(idTableName);
        jsonfinal = new JSONObject();
        jsonfinal.put("jsonClass", "org.apache.atlas.typesystem.json.InstanceSerialization$_Reference");

        JSONObject id = new JSONObject();
        id.put("jsonClass", "org.apache.atlas.typesystem.json.InstanceSerialization$_Id");
        id.put("id", "-2521");
        id.put("version", 0);
        id.put("typeName", "TableStatistics");
        jsonfinal.put("id", id);

        jsonfinal.put("typeName", "TableStatistics");

        JSONObject values = new JSONObject();
        values.put("name", tablename);
        values.put("owner", "admin");
        values.put("description", "Profiling table " + tablename + " from " + database + " database");
        values.put("qualifiedName", "st." + database + "." + tablename);

        JSONObject tableEntity = new JSONObject();
        tableEntity.put("jsonClass", "org.apache.atlas.typesystem.json.InstanceSerialization$_Id");
        tableEntity.put("id", idTableName);
        tableEntity.put("version", 0);
        tableEntity.put("typeName", "hive_table");
        tableEntity.put("state", "ACTIVE");
        values.put("table", tableEntity);
        values.put("dataSetSize", dataSetSize);

        JSONObject dbObject = new JSONObject();
        dbObject.put("jsonClass", "org.apache.atlas.typesystem.json.InstanceSerialization$_Id");
        dbObject.put("id", idDB);
        dbObject.put("version", 0);
        dbObject.put("typeName", "hive_db");
        dbObject.put("state", "ACTIVE");
        values.put("db", dbObject);

        JSONArray columnsStats = new JSONArray();
        for (String columnSts : arrayColumnStats) {
            JSONObject columnStatsEntity = new JSONObject();
            columnStatsEntity.put("jsonClass", "org.apache.atlas.typesystem.json.InstanceSerialization$_Id");
            columnStatsEntity.put("id", columnSts);
            columnStatsEntity.put("version", 0);
            columnStatsEntity.put("typeName", "ColumnStatistics");
            columnStatsEntity.put("state", "ACTIVE");
            columnsStats.put(columnStatsEntity);
        }
        values.put("columnStatistics", columnsStats); //array

        values.put("numCategoricalColumns", numCategoricalColumns);
        values.put("numDateColumns", numDateColumns);
        values.put("numObservations", numObservations);
        values.put("numVariables", numVariable);
        values.put("numNumericalColumns", numNumericalColumns);
        values.put("numOtherTypesColumns", numOtherTypesColumns);

        jsonfinal.put("values", values);

        JSONArray traitNames = new JSONArray();
        jsonfinal.put("traitNames", traitNames);
        JSONObject traits = new JSONObject();
        jsonfinal.put("traits", traits);

        return jsonfinal;
    }

    public JSONObject createEntityIntraStatistics(String tablename, String database, String columnMain, String columnToCompare, double correlationValue) throws JSONException {

        JSONObject jsonfinal = null;
        AtlasConsumer restConsumer = new AtlasConsumer();
        String idTableName = restConsumer.getIDAtlasTableACTIVE(tablename, database);
        System.out.println("passou aqui id tabe");
        String idColumnMain = restConsumer.getIDAtlasColumnACTIVE(columnMain, tablename, database);
        System.out.println("id columnmain");
        String idColumnToCompare = restConsumer.getIDAtlasColumnACTIVE(columnToCompare, tablename, database);
        System.out.println("idcolumnto Campare");
        jsonfinal = new JSONObject();
        jsonfinal.put("jsonClass", "org.apache.atlas.typesystem.json.InstanceSerialization$_Reference");

        JSONObject id = new JSONObject();
        id.put("jsonClass", "org.apache.atlas.typesystem.json.InstanceSerialization$_Id");
        id.put("id", "-2521");
        id.put("version", 0);
        id.put("typeName", "IntraStatistics");
        jsonfinal.put("id", id);

        jsonfinal.put("typeName", "IntraStatistics");

        JSONObject values = new JSONObject();
        values.put("name", columnMain + "****" + columnToCompare + "-  " + correlationValue);
        values.put("owner", "admin");
        values.put("description", "Correlation Analysis Between " + columnMain + " and " + columnToCompare);
        values.put("qualifiedName", "instrast." + database + "." + tablename + "." + columnMain + "." + columnToCompare);

        JSONObject columnMainJ = new JSONObject();
        columnMainJ.put("jsonClass", "org.apache.atlas.typesystem.json.InstanceSerialization$_Id");
        columnMainJ.put("id", idColumnMain);
        columnMainJ.put("version", 0);
        columnMainJ.put("typeName", "hive_column");
        columnMainJ.put("state", "ACTIVE");
        values.put("columnMain", columnMainJ);

        JSONObject columnToCompareJ = new JSONObject();
        columnToCompareJ.put("jsonClass", "org.apache.atlas.typesystem.json.InstanceSerialization$_Id");
        columnToCompareJ.put("id", idColumnToCompare);
        columnToCompareJ.put("version", 0);
        columnToCompareJ.put("typeName", "hive_column");
        columnToCompareJ.put("state", "ACTIVE");
        values.put("columnToCompare", columnToCompareJ);

        JSONObject tableEntity = new JSONObject();
        tableEntity.put("jsonClass", "org.apache.atlas.typesystem.json.InstanceSerialization$_Id");
        tableEntity.put("id", idTableName);
        tableEntity.put("version", 0);
        tableEntity.put("typeName", "hive_table");
        tableEntity.put("state", "ACTIVE");
        values.put("table", tableEntity);

        values.put("correlationValue", correlationValue); //array

        JSONArray similarityObjects = new JSONArray();
        values.put("similarityObjects", similarityObjects); //array

        jsonfinal.put("values", values);

        JSONArray traitNames = new JSONArray();
        jsonfinal.put("traitNames", traitNames);
        JSONObject traits = new JSONObject();
        jsonfinal.put("traits", traits);

        return jsonfinal;
    }

}
