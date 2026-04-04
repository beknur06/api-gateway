package kz.ktj.digitaltwin.gateway.config;

import kz.ktj.digitaltwin.gateway.websocket.TelemetryWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final TelemetryWebSocketHandler handler;

    @Value("${gateway.cors.allowed-origins:*}")
    private String allowedOrigins;

    public WebSocketConfig(TelemetryWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/telemetry")
            .setAllowedOrigins(allowedOrigins.split(","));
    }
}
