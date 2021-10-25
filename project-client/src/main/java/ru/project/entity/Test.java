package ru.project.entity;

import ru.factory.annotation.Column;
import ru.factory.annotation.Table;

@Table
public class Test {
    @Column
    private Integer id;
    @Column
    private String description;
}
