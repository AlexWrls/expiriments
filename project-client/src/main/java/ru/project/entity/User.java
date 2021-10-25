package ru.project.entity;

import lombok.Data;
import ru.factory.annotation.Column;
import ru.factory.annotation.Table;
@Data
@Table(value = "users")
public class User  {
    @Column
    private Long id;
    @Column(value = "first_name",length = "100")
    private String firstName;
    @Column(value = "last_name")
    private String lastName;
    @Column
    private Integer age;
}
