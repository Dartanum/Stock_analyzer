package ru.dartanum.stock_analyzer.domain;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "name")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Channel {
    @Id
    int id;
    String name;
}
