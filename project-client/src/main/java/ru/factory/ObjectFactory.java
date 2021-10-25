package ru.factory;

import lombok.Getter;
import ru.factory.annotation.Column;
import ru.factory.annotation.Table;
import ru.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ObjectFactory {

    private Map<String, String> propertiesMap;

    private static ObjectFactory objectFactory;
    @Getter
    private static DataBase dataBase;

    private ObjectFactory() {
    }

    public static ObjectFactory getInstance() {
        if (objectFactory == null) {
            objectFactory = new ObjectFactory();
            dataBase = new DataBase();
            objectFactory.instance();
        }
        return objectFactory;

    }

    private void instance() {
        String path = ClassLoader.getSystemClassLoader().getResource("application.properties").getPath();
        try (Stream<String> lines = new BufferedReader(new FileReader(path)).lines();) {
            propertiesMap = lines.map(line -> line.split("=")).collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));
            List<Class<?>> classes = ScannerClassProject.find("ru.project");
            setClassValue(dataBase);
            for (Class<?> c : classes) {
                Table annotation = c.getAnnotation(Table.class);
                if (annotation != null) {
                    Object o = c.newInstance();
                    dataBase.execute(String.valueOf(createTable(o)));
                }
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

    }

    private StringBuilder createTable(Object o) {
        Class<?> aClass = o.getClass();
        StringBuilder builder = new StringBuilder();

        builder.append("create table public.");
        Table table = aClass.getAnnotation(Table.class);
        if (table != null) {
            String value;
            if (table.value().isEmpty()) {
                value = aClass.getSimpleName().toLowerCase(Locale.ENGLISH);
            } else {
                value = table.value();
            }
            builder.append(value).append(" (");
        }

        for (Field field : aClass.getDeclaredFields()) {
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                String name;
                if (column.value().isEmpty()) {
                    name = field.getName();
                } else {
                    name = column.value();
                }
                toStr(field.getType(), builder, name, column.length());
                builder.append(",");
            }
        }
        builder.deleteCharAt(builder.lastIndexOf(","));
        builder.append(" );");
        return builder;
    }

    private void setClassValue(Object t) {
        Class<?> aClass = t.getClass();
        for (Field field : aClass.getDeclaredFields()) {
            Value annotation = field.getAnnotation(Value.class);
            if (annotation != null) {
                String value;
                if (annotation.value().isEmpty()) {
                    value = propertiesMap.get(field.getName());
                } else {
                    value = propertiesMap.get(annotation.value());
                }
                field.setAccessible(true);
                try {
                    field.set(t, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    private void toStr(Type type, StringBuilder builder, String value, String length) {
        if (type.equals(String.class)) {
            builder.append(String.format("%s character varying(%s) ", value, length));
        } else if (type.equals(int.class) || type.equals(Integer.class)) {
            builder.append(String.format("%s integer ", value));
        } else if (type.equals(long.class) || type.equals(Long.class)) {
            builder.append(String.format("%s bigint ", value));
        } else if (type.equals(short.class) || type.equals(Short.class)) {
            builder.append(String.format("%s integer ", value));
        } else if (type.equals(char.class) || type.equals(Character.class)) {
            builder.append(String.format("%s character varying(2) ", value));
        } else if (type.equals(double.class) || type.equals(Double.class)) {
            builder.append(String.format("%s double precision ", value));
        } else if (type.equals(byte.class) || type.equals(Byte.class)) {
            builder.append(String.format("%s bytea ", value));
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            builder.append(String.format("%s boolean ", value));
        } else if (type.equals(float.class) || type.equals(Float.class)) {
            builder.append(String.format("%s float ", value));
        } else if (type.equals(LocalDate.class)) {
            builder.append(String.format("%s timestamp ", value));
        }

    }

}
