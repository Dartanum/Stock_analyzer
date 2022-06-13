package ru.dartanum.stock_analyzer.domain;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "name")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Channel {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "channel_sq")
    @SequenceGenerator(name = "channel_sq", sequenceName = "channel_sq", allocationSize = 1)
    int id;

    @NotNull
    String name;
}
