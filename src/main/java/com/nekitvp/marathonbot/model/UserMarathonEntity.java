package com.nekitvp.marathonbot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
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
@Table(name = "user_marathon")
public class UserMarathonEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "telegram_id")
    private Long telegramId;

    @ManyToOne
    @JoinColumn(name = "marathon_id", referencedColumnName = "id")
    private MarathonEntity marathon;

}
