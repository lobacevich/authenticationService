package by.lobacevich.auth.config;

import by.lobacevich.auth.mapper.RegisterMapper;
import by.lobacevich.auth.mapper.RegisterMapperImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

    @Bean
    RegisterMapper registerMapper() {
        return new RegisterMapperImpl();
    }
}
