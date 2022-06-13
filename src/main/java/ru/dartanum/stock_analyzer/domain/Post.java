package ru.dartanum.stock_analyzer.domain;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_sq")
    @SequenceGenerator(name = "post_sq", sequenceName = "post_sq", allocationSize = 1)
    long id;

    @NotNull
    String content;
    LocalDateTime creationDate;

    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    Channel channel;

    @Enumerated(EnumType.STRING)
    Category category;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Post)) return false;
        Post post = (Post) o;
        return Objects.equals(channel, post.channel) && Objects.equals(creationDate, post.creationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel.getId(), creationDate);
    }
}
