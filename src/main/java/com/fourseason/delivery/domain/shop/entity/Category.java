package com.fourseason.delivery.domain.shop.entity;

import com.fourseason.delivery.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name = "p_category")
public class Category extends BaseTimeEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;
}
