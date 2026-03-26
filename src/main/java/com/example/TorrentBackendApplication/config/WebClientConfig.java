package com.example.TorrentBackendApplication.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.JacksonJsonEncoder;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient aria2WebClient() {
        WebClient.Builder builder = WebClient.builder(); // Create it manually

        JsonMapper mapper = JsonMapper.builder().build();
        MediaType rpcType = MediaType.parseMediaType("application/json-rpc");

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(c -> {
                    c.defaultCodecs().jacksonJsonDecoder(new JacksonJsonDecoder(mapper, rpcType, MediaType.APPLICATION_JSON));
                    c.defaultCodecs().jacksonJsonEncoder(new JacksonJsonEncoder(mapper, rpcType, MediaType.APPLICATION_JSON));
                    c.defaultCodecs().maxInMemorySize(16 * 1024 * 1024);
                }).build();
        return builder.exchangeStrategies(strategies).build();
    }
}