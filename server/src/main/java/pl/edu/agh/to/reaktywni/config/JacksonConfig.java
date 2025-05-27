package pl.edu.agh.to.reaktywni.config;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        StreamReadConstraints constraints = StreamReadConstraints.builder()
                .maxStringLength(200_000_000)
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.getFactory().setStreamReadConstraints(constraints);
        return objectMapper;
    }
}
