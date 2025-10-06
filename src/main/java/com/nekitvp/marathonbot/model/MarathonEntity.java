package com.nekitvp.marathonbot.model;


import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "marathon")
public class MarathonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "is_member")
    private Boolean isMember;

    @Column(name = "date_start")
    private LocalDateTime dateStart;

    @Column(name = "date_end")
    private LocalDateTime dateEnd;

    @Builder.Default
    @Column(name = "select_for_send_message")
    private Boolean select = false;

    @Builder.Default
    @Column(name = "free_fail_count")
    private Integer freeFailCount = 5;

    @ManyToMany(mappedBy = "managedMarathons")
    private Set<UserEntity> managers = new HashSet<>();
}
