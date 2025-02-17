package com.fourseason.delivery.domain.shop.entity;

import com.fourseason.delivery.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_category")
public class Category extends BaseTimeEntity {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String name;

    public Category(String name) {
        this.name = name;
    }

    public Category() {

    }
}
