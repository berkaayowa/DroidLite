package com.droidlite.sqlite;

import com.droidlite.sqlite.attributes.Column;
import com.droidlite.sqlite.common.Database;
import com.droidlite.sqlite.common.Helper;
import com.droidlite.sqlite.common.QueryHelper;
import com.droidlite.sqlite.interfaces.IEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class Entity implements IEntity {

    @Override
    public Boolean save() {

        Table table = Helper.convertEntityClassToTable(this.getClass(), this);
        TableColumn primaryKeyColumn = table.getPrimaryKey();
        TableQuery query = null;

        //BA: 24092023
        //This is new record then generate insert query
        if(primaryKeyColumn.Value == primaryKeyColumn.getDefaultValue())
            query = QueryHelper.generateInsertQuery(table);
        else
            query = QueryHelper.generateUpdateQuery(table);

        //BA: 24092023
        //If there is alter query run them all before the main query
        executeQueries(query.TableQueries);

        return Database.getInstance().run(query.Query);
    }

    @Override
    public ArrayList<IEntity> get(Class<?> target, TableColumn[] columns) {
        return getAll(target, columns);
    }

    public ArrayList<IEntity> get(TableColumn[] columns) {

        return get(this.getClass(), columns);
    }

    public static ArrayList<IEntity> getAll(Class<?> target, TableColumn[] columns) {

        ArrayList<IEntity> records = new ArrayList<>();
        Table table = Helper.convertEntityClassToTable(target, null);

        if(table != null) {

            TableQuery query =  QueryHelper.generateSelectQuery(table, columns);

            //BA: 24092023
            //If there is alter query run them all before the main query
            executeQueries(query.TableQueries);

            records = Helper.mapResultSet(target, Database.getInstance().runSelectQuery(query.Query));

        }

        return records;

    }

    public static IEntity getFirstOrNull(Class<?> target, TableColumn[] columns) {

        ArrayList<IEntity> records = getAll(target, columns);

        if(!records.isEmpty())
            return records.get(0);

        return null;
    }

    public IEntity getFirstOrNull(TableColumn[] columns) {

        return getFirstOrNull(this.getClass(), columns);
    }

    @Override
    public Boolean delete() {

        Table table = Helper.convertEntityClassToTable(this.getClass(), this);
        TableColumn primaryKeyColumn = table.getPrimaryKey();

        return delete(this.getClass(), new TableColumn[]{primaryKeyColumn});
    }

    public static Boolean delete(Class<?> target, TableColumn[] columns) {

        Table table = Helper.convertEntityClassToTable(target, null);

        if(table != null) {

            TableQuery query =  QueryHelper.generateDeleteQuery(table, columns);
            return Database.getInstance().run(query.Query);

        }

        return false;

    }

    protected void bind(IEntity entity) {

        if(entity != null) {

            Field[] fields = entity.getClass().getFields();

            for (int i = 0; i < fields.length; i++) {

                try {

                    if (!fields[i].isAnnotationPresent(Column.class))
                        continue;

                    fields[i].set(this, fields[i].get(entity));
                } catch (Exception ex) {
                    Helper.log("Entity|populate|error|0|" + ex.getMessage());
                }
            }

        }

    }

    private static void executeQueries(ArrayList<TableQuery> queries) {

        for (TableQuery query: queries) {

            try {

                Helper.log("Entity.executeAlterQueries|query|" + query.Query);

                if(Database.getInstance().run(query.Query))
                    Helper.log("Entity.executeAlterQueries|succeed|");
                else
                    Helper.log("Entity.executeAlterQueries|failed|");

            } catch (Exception ex) {
                Helper.log("Entity.executeAlterQueries|error|0|" + ex.getMessage());
            }

        }
    }


}
