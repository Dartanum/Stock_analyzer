package ru.dartanum.stock_analyzer.domain.news;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_sq")
    @SequenceGenerator(name = "post_sq", sequenceName = "post_sq", allocationSize = 1)
    private long id;

    @NotNull
    private String content;
    private LocalDateTime creationDate;

    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    private Category category;

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
