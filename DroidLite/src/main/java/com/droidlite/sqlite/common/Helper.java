package com.droidlite.sqlite.common;

import static com.droidlite.sqlite.enums.ColumnType.*;

import android.os.Build;
import android.util.Log;

import com.droidlite.sqlite.Entity;
import com.droidlite.sqlite.Table;
import com.droidlite.sqlite.TableColumn;
import com.droidlite.sqlite.TableQuery;
import com.droidlite.sqlite.attributes.Column;
import com.droidlite.sqlite.enums.ColumnType;
import com.droidlite.sqlite.enums.Constraint;
import com.droidlite.sqlite.enums.Query;
import com.droidlite.sqlite.interfaces.IEntity;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class Helper {

    public static Table convertEntityClassToTable(Class<?> target, Entity entity) {

        if (target.isAnnotationPresent(com.droidlite.sqlite.attributes.Table.class)) {

            Table table = new Table();

            com.droidlite.sqlite.attributes.Table attrTable = target.getAnnotation(com.droidlite.sqlite.attributes.Table.class);
            table.Name = attrTable.Name();

            Field[] fields = target.getFields();

            for (int i = 0; i < fields.length; i++) {

                try
                {
                    //Below check is to make sure we only process data table field
                    if(!fields[i].isAnnotationPresent(com.droidlite.sqlite.attributes.Column.class))
                        continue;

                    TableColumn tableColumn = new TableColumn();
                    com.droidlite.sqlite.attributes.Column col = fields[i].getAnnotation(com.droidlite.sqlite.attributes.Column.class);

                    tableColumn.Name = col.Name();
                    tableColumn.Constraints = col.Constraint();

                    if(entity != null)
                        tableColumn.Value = fields[i].get(entity);

                    tableColumn.Type = fields[i].getType().getName().toLowerCase();

                    if(tableColumn.Name.isEmpty())
                        tableColumn.Name = fields[i].getName();

                    //BA: 24092023
                    //Checking if there is alter attribute on on the field
                    if(fields[i].isAnnotationPresent(com.droidlite.sqlite.attributes.AlterColumn.class))
                        tableColumn.Alter = fields[i].getAnnotation(com.droidlite.sqlite.attributes.AlterColumn.class);

                    table.Columns.add(tableColumn);

                }catch (Exception ex) {
                    Helper.log("convertEntityClassToTable|error|0|" + ex.getMessage());
                }

            }

            return table;
        }

        return null;
    }

    public static ArrayList<IEntity> mapResultSet(Class<?> entityClass, ArrayList<HashMap<String, String>> resultSet) {

        ArrayList<IEntity> list = new ArrayList<>();

        for (int i = 0; i < resultSet.size(); i++) {

            IEntity iEntity = hashMapToEntity(entityClass, resultSet.get(i));

            if(iEntity != null)
                list.add(iEntity);

        }

        return list;
    }

    public static Entity hashMapToEntity(Class<?> entityClass, HashMap<String, String> hashMap) {

        if(!hashMap.isEmpty()) {

            try {

                Entity entity = (Entity) entityClass.newInstance();
                Field[] fields = entityClass.getFields();

                for (int i = 0; i < fields.length; i++) {

                    try {

                        if (!fields[i].isAnnotationPresent(Column.class))
                            continue;

                        if (hashMap.containsKey(fields[i].getName())){

                            ColumnType columnType = TableColumn.getColumnTypeCode(fields[i].getType().getName().toLowerCase());
                            String value = hashMap.get(fields[i].getName());

                            if(ColumnType.TextType == columnType)
                                fields[i].set(entity, value);
                            else if(ColumnType.DoubleType == columnType)
                                fields[i].set(entity, Double.valueOf(value));
                            else if(ColumnType.FloatType == columnType)
                                fields[i].set(entity, Float.valueOf(value));
                            else if(ColumnType.IntegerType == columnType)
                                fields[i].set(entity, Integer.valueOf(value));
                            else if(ColumnType.DateType == columnType) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    fields[i].set(entity, LocalDateTime.parse(value));
                                }
                            }
                            else if(ColumnType.Boolean == columnType) {
                                fields[i].set(entity, (Integer.valueOf(value) == 1 ? true : false));
                            }

                        }

                    } catch (Exception ex) {
                        Helper.log("hashMapToEntity|error|0|" + ex.getMessage());
                    }
                }

                return entity;

            } catch (Exception ex) {
                Helper.log("hashMapToEntity|error|1|" + ex.getMessage());
            }
        }

        return null;
    }

    public static void log(String message) {
        Log.e("DroidLite~Log",message);
    }
}
