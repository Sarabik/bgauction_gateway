package com.bgauction.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth", r -> r.path("/auth/**")
                        .uri("lb://USERSERVICE"))
                .route("user_service", r -> r.path("/user/**")
                        .uri("lb://USERSERVICE"))
                .route("game_service", r -> r.path("/game/**")
                        .uri("lb://GAMESERVICE"))
                .route("auction_service", r -> r.path("/auction/**")
                        .uri("lb://AUCTIONSERVICE"))
                .route("bid_service", r -> r.path("/bid/**")
                        .uri("lb://BIDSERVICE"))
                .route("notification_service", r -> r.path("/notification/**")
                        .uri("lb://NOTIFICATIONSERVICE"))
                .build();
    }

}
