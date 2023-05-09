package ru.dartanum.stock_analyzer.domain.news;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;


@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "name")
public class Channel {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "channel_sq")
    @SequenceGenerator(name = "channel_sq", sequenceName = "channel_sq", allocationSize = 1)
    private int id;

    @NotNull
    private String name;

    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Post> posts;

    public Channel(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
