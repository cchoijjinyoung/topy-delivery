package com.fourseason.delivery;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.entity.Role;
import com.fourseason.delivery.domain.member.repository.MemberRepository;
import com.fourseason.delivery.domain.menu.entity.Menu;
import com.fourseason.delivery.domain.menu.entity.MenuStatus;
import com.fourseason.delivery.domain.menu.repository.MenuRepository;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.entity.OrderMenu;
import com.fourseason.delivery.domain.order.repository.OrderMenuRepository;
import com.fourseason.delivery.domain.order.repository.OrderRepository;
import com.fourseason.delivery.domain.shop.entity.Category;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.domain.shop.repository.CategoryRepository;
import com.fourseason.delivery.domain.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.fourseason.delivery.domain.order.entity.OrderStatus.PENDING;
import static com.fourseason.delivery.domain.order.entity.OrderType.ONLINE;

@Component
@RequiredArgsConstructor
public class TestInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;
    private final MenuRepository menuRepository;
    private final OrderMenuRepository orderMenuRepository;
    private final OrderRepository orderRepository;

    @Override
    public void run(String... args) {

        if (memberRepository.findByUsername("manager").isEmpty()) {
            Member manager = new Member(
                    "manager",
                    "manager@delivery.com",
                    passwordEncoder.encode("1234"),
                    "010-1234-5678",
                    Role.MANAGER
            );
            memberRepository.save(manager);
            System.out.println("Manager account created.");
        }

        if (memberRepository.findByUsername("owner").isEmpty()) {
            Member owner = new Member(
                    "owner",
                    "owner@delivery.com",
                    passwordEncoder.encode("1234"),
                    "010-1234-5678",
                    Role.OWNER
            );
            memberRepository.save(owner);
            System.out.println("Owner account created.");

            Category category = Category.builder()
                    .name("test")
                    .build();
            categoryRepository.save(category);
            System.out.println("Category created");

            Shop shop = Shop.builder()
                    .name("testShop")
                    .description("testShop")
                    .tel("010-1234-5678")
                    .address("testAddress")
                    .detailAddress("testAddress")
                    .member(owner)
                    .category(category)
                    .build();

            shopRepository.save(shop);
            System.out.println("Shop created");

            Menu menu = Menu.builder()
                    .name("test")
                    .description("test")
                    .price(2000)
                    .menuStatus(MenuStatus.SHOW)
                    .shop(shop)
                    .build();
            menuRepository.save(menu);
            if (memberRepository.findByUsername("customer").isEmpty()) {
                Member customer = new Member(
                        "customer",
                        "customer@delivery.com",
                        passwordEncoder.encode("1234"),
                        "010-1234-5678",
                        Role.CUSTOMER
                );
                memberRepository.save(customer);
                System.out.println("Customer account created.");

                OrderMenu orderMenu = OrderMenu.builder()
                        .menu(menu)
                        .name(menu.getName())
                        .price(menu.getPrice())
                        .quantity(1)
                        .build();

                Order order = Order.builder()
                        .shop(shop)
                        .member(customer)
                        .orderStatus(PENDING)
                        .orderType(ONLINE)
                        .address("testAddress")
                        .instruction("test!")
                        .totalPrice(2000)
                        .orderMenuList(List.of(orderMenu))
                        .build();
                orderRepository.save(order);
                System.out.println("Order created.");
            }
        }
    }
}
