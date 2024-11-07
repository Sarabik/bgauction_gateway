package com.bgauction.gateway.config;

import com.bgauction.gateway.filters.JwtAuthenticationFilter;
import com.bgauction.gateway.filters.LogoutFilter;
import com.bgauction.gateway.filters.ServiceKeyAddingFilter;
import com.bgauction.gateway.filters.ServiceKeyCheckFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ServiceKeyCheckFilter serviceKeyCheckFilter;
    private final ServiceKeyAddingFilter serviceKeyAddingFilter;
    private final LogoutFilter logoutFilter;

    public GatewayConfig(@Value("${service.internal-key}") String serviceInternalKey,
                         JwtAuthenticationFilter jwtAuthenticationFilter,
                         ServiceKeyCheckFilter serviceKeyCheckFilter,
                         ServiceKeyAddingFilter serviceKeyAddingFilter,
                         LogoutFilter logoutFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.serviceKeyCheckFilter = serviceKeyCheckFilter;
        this.serviceKeyAddingFilter = serviceKeyAddingFilter;
        this.logoutFilter = logoutFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                /* logout */
                .route("logout_route", r -> r
                        .path("/auth/logout")
                        .filters(f -> f.filter(logoutFilter))
                        .uri("no://op"))

                /* internal */
                .route("internal_user_route", r -> r.path("/internal/user/**")
                        .filters(f -> f.filter(serviceKeyCheckFilter))
                        .uri("lb://USERSERVICE"))
                .route("internal_game_route", r -> r.path("/internal/game/**")
                        .filters(f -> f.filter(serviceKeyCheckFilter))
                        .uri("lb://GAMESERVICE"))
                .route("internal_auction_route", r -> r.path("/internal/auction/**")
                        .filters(f -> f.filter(serviceKeyCheckFilter))
                        .uri("lb://AUCTIONSERVICE"))
                .route("internal_bid_route", r -> r.path("/internal/bid/**")
                        .filters(f -> f.filter(serviceKeyCheckFilter))
                        .uri("lb://BIDSERVICE"))

                /* external */

                /* USER */
                .route("external_auth_route", r -> r
                        .path("/auth/register")
                        .or().path("/auth/login")
                        .uri("lb://USERSERVICE"))
                .route("external_user_route", r -> r.path("/user/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://USERSERVICE"))

                /* GAME */
                .route("not_secured_external_game_route", r -> r
                        .path("/game/{id}")
                        .and()
                        .method(HttpMethod.GET)
                        .filters(f -> f.filter(serviceKeyAddingFilter))
                        .uri("lb://GAMESERVICE"))
                .route("secured_external_game_route_post_put_delete", r -> r
                        .path("/game/**")
                        .and()
                        .method(HttpMethod.POST, HttpMethod.DELETE, HttpMethod.PUT)
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://GAMESERVICE"))
                .route("secured_external_game_route_get_by_user", r -> r
                        .path("/game/user/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter)) // Применить фильтр
                        .uri("lb://GAMESERVICE"))

                /* AUCTION */
                .route("not_secured_external_auction_route", r -> r
                        .path("/auction/**")
                        .and()
                        .method(HttpMethod.GET)
                        .filters(f -> f.filter(serviceKeyAddingFilter))
                        .uri("lb://AUCTIONSERVICE"))
                .route("secured_external_auction_route", r -> r
                        .path("/auction/**")
                        .and()
                        .method(HttpMethod.GET).negate()
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://AUCTIONSERVICE"))

                /* BID */
                .route("not_secured_external_bid_route", r -> r
                        .path("/bid/auction/**")
                        .filters(f -> f.filter(serviceKeyAddingFilter))
                        .uri("lb://BIDSERVICE"))
                .route("secured_external_bid_route", r -> r
                        .path("/bid")
                        .or()
                        .path("/bid/bidder/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://BIDSERVICE"))

                .build();
    }

}
