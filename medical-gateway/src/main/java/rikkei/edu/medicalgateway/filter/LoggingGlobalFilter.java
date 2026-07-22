package rikkei.edu.medicalgateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
@Slf4j
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        InetSocketAddress remoteAddress = request.getRemoteAddress();
        String clientIp = (remoteAddress != null) ? remoteAddress.getAddress().getHostAddress() : "Unknown IP";

        String method = request.getMethod().name();

        String path = request.getPath().toString();

        log.info("======================= NEW REQUEST =======================");
        log.info("IP Máy khách : {}", clientIp);
        log.info("Phương thức  : {}", method);
        log.info("Đường dẫn    : {}", path);
        log.info("===========================================================");

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
