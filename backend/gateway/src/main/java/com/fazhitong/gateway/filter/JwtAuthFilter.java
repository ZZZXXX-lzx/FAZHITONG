package com.fazhitong.gateway.filter;

import com.fazhitong.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    /** 免登录接口 */
    private static final List<String> AUTH_WHITE_LIST = List.of(
            "/api/auth/login",
            "/api/auth/register"
    );

    /** 公开只读内容：知识库、案例、文书模板、律师大厅、评价 */
    private static final List<String> PUBLIC_GET_PREFIXES = List.of(
            "/api/case/",
            "/api/document/",
            "/api/consultation/lawyer-service/lawyers",
            "/api/consultation/lawyer-service/prices",
            "/api/user/review/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // CORS 预检直接放行
        if (HttpMethod.OPTIONS.equals(method)) {
            return chain.filter(exchange);
        }

        // 免登录接口放行
        if (AUTH_WHITE_LIST.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        // 公开只读接口放行（仅 GET）
        if (HttpMethod.GET.equals(method)
                && PUBLIC_GET_PREFIXES.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        // 其余接口校验 JWT
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }
        try {
            Claims claims = JwtUtils.parse(authHeader.substring(7));
            ServerHttpRequest mutated = request.mutate()
                    .header("X-User-Id", String.valueOf(claims.get("userId")))
                    .header("X-User-Type", String.valueOf(claims.get("role")))
                    .header("X-Username", String.valueOf(claims.get("username")))
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (Exception e) {
            return unauthorized(exchange);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
