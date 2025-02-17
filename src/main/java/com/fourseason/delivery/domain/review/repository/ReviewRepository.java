package com.fourseason.delivery.domain.review.repository;

import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.review.entity.Review;
import com.fourseason.delivery.domain.shop.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Optional<Review> findByIdAndOrderId(UUID review_id, String order_id);

    List<Review> findByShop(Shop shop);

}
