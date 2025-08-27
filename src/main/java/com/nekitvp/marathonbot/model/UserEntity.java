package com.nekitvp.marathonbot.model;

import com.nekitvp.marathonbot.enumBot.StateBot;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@Entity
@Table(name = "users", schema = "public")
public class UserEntity {

    @Id
    private Long telegramId;

    private String telegramUserName;

    private String telegramFirstName;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private StateBot state;

    @Builder.Default
    private Boolean admin = false;

    @Builder.Default
    private Boolean manager = false;

    @Builder.Default
    private Long countChangeState = 0L;

    @Builder.Default
    private Long countChangeStateAll = 0L;

    @Column(name = "marathon_id", updatable = false, insertable = false)
    private Long marathonId;

    @ManyToOne
    @JoinColumn(name = "marathon_id", referencedColumnName = "id")
    private MarathonEntity marathon;
}
