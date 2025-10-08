package com.nekitvp.marathonbot.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "goal", schema = "public")
public class GoalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "marathon_id", updatable = false, insertable = false)
    private Long marathonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marathon_id", referencedColumnName = "id")
    private MarathonEntity marathon;

    @Builder.Default
    @Column(name = "asked_count")
    private Integer askedCount = 0;
}

