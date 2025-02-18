package com.fourseason.delivery.domain.menu.entity;

import com.fourseason.delivery.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_menu_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuImage extends BaseTimeEntity {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;
}
