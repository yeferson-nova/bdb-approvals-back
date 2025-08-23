package co.com.bancodebogota.bdbapprovals.infrastructure.config;

import co.com.bancodebogota.bdbapprovals.application.port.out.ClockPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

@Configuration
public class ClockConfig {
    @Bean
    ClockPort clockPort() { return Instant::now; }
}
