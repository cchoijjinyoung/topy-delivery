package com.fourseason.delivery;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.entity.Role;
import com.fourseason.delivery.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (memberRepository.findByUsername("admin").isEmpty()) {
            Member admin = new Member(
                    "admin",
                    "admin@delivery.com",
                    passwordEncoder.encode("1234"),
                    "010-1234-5678",
                    Role.MASTER
            );
            memberRepository.save(admin);
            System.out.println("Admin account created.");
        }
    }
}
