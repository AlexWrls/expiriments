package ru.factory;

import lombok.SneakyThrows;
import ru.factory.annotation.Column;
import ru.factory.annotation.Table;
import ru.factory.annotation.Value;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static ru.factory.DataBaseConnection.getDbConnection;

public class DataBase {
    @Value
    private static String driver;
    @Value
    private static String url;
    @Value
    private static String username;
    @Value
    private static String password;

    public void execute(String query) {
        try (Connection connection = getDbConnection(driver, url, username, password);
             Statement statement = connection.createStatement()) {
            statement.execute(query);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }



    public <T> T getObject(Class<?> clazz) throws IllegalAccessException, InstantiationException {
        Object obj = clazz.newInstance();
        Field[] fields = obj.getClass().getDeclaredFields();
        String nameTable;
        Table table = obj.getClass().getAnnotation(Table.class);
        if (table.value().isEmpty()) {
            nameTable = obj.getClass().getSimpleName();
        } else {
            nameTable = table.value();
        }
        Map<Field, String> fieldClass = new HashMap<>();
        for (Field field : fields) {
            Column column = field.getAnnotation(Column.class);
            if (column.value().isEmpty()) {
                fieldClass.put(field, field.getName());
            } else {
                fieldClass.put(field, column.value());
            }
        }
        try (Connection connection = getDbConnection(driver, url, username, password);
             Statement statement = connection.createStatement()) {
            String query = String.format("select * from public.%s", nameTable);
            final ResultSet resultSet = statement.executeQuery(query);
            while (true) {
                assert resultSet != null;
                if (!resultSet.next()) break;
                fieldClass.forEach((k, v) -> {
                    try {
                        Object object = resultSet.getObject(v);
                        k.setAccessible(true);
                        Object castObject = k.getType().cast(object);
                        k.set(obj, castObject);
                    } catch (IllegalAccessException | SQLException e) {
                        e.printStackTrace();
                    }
                });
            }
            resultSet.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return (T) obj;
    }
}
