package com.spendsmart.admin;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class AdminServerApplicationTest {

    @Test
    void main_ShouldDelegateToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            AdminServerApplication.main(new String[]{"--spring.main.web-application-type=none"});

            springApplication.verify(() ->
                    SpringApplication.run(AdminServerApplication.class, new String[]{"--spring.main.web-application-type=none"}));
        }
    }
}
