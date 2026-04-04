package kz.ktj.digitaltwin.gateway.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler: bridges Redis pub/sub → WebSocket → frontend.
 *
 * Frontend connects:
 *   ws://gateway:8080/ws/telemetry?locomotiveId=KZ8A-0042
 *
 * On connect:
 *   1. Send initial snapshot (GET last_state:{locoId} from Redis)
 *   2. Subscribe to Redis channels: telemetry:{locoId}, health:{locoId}, alerts:{locoId}
 *   3. Forward every Redis message as WS text frame
 *
 * On disconnect:
 *   Unsubscribe from Redis channels, cleanup.
 *
 * Message format to client:
 *   { "type": "TELEMETRY|HEALTH_INDEX|ALERT", "data": {...} }
 */
@Component
public class TelemetryWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(TelemetryWebSocketHandler.class);

    private final StringRedisTemplate redis;
    private final RedisMessageListenerContainer listenerContainer;

    /** session → list of Redis subscriptions (for cleanup) */
    private final Map<String, SessionSubscriptions> sessions = new ConcurrentHashMap<>();

    public TelemetryWebSocketHandler(StringRedisTemplate redis,
                                      RedisMessageListenerContainer listenerContainer) {
        this.redis = redis;
        this.listenerContainer = listenerContainer;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String locomotiveId = extractLocomotiveId(session);
        if (locomotiveId == null) {
            session.close(CloseStatus.BAD_DATA.withReason("Missing locomotiveId parameter"));
            return;
        }

        log.info("WS connected: session={} locomotive={}", session.getId(), locomotiveId);

        // 1. Send initial snapshot
        sendSnapshot(session, locomotiveId);

        // 2. Subscribe to Redis channels and bridge to WS
        SessionSubscriptions subs = new SessionSubscriptions(locomotiveId);

        subs.telemetryListener = createBridgeListener(session, "TELEMETRY");
        subs.healthListener = createBridgeListener(session, "HEALTH_INDEX");
        subs.alertListener = createBridgeListener(session, "ALERT");

        listenerContainer.addMessageListener(subs.telemetryListener,
            new ChannelTopic("telemetry:" + locomotiveId));
        listenerContainer.addMessageListener(subs.healthListener,
            new ChannelTopic("health:" + locomotiveId));
        listenerContainer.addMessageListener(subs.alertListener,
            new ChannelTopic("alerts:" + locomotiveId));

        sessions.put(session.getId(), subs);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        SessionSubscriptions subs = sessions.remove(session.getId());
        if (subs != null) {
            listenerContainer.removeMessageListener(subs.telemetryListener);
            listenerContainer.removeMessageListener(subs.healthListener);
            listenerContainer.removeMessageListener(subs.alertListener);
            log.info("WS disconnected: session={} locomotive={}", session.getId(), subs.locomotiveId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Client can send: { "action": "ping" } for keepalive
        // or { "action": "subscribe", "locomotiveId": "..." } to switch locomotive
        log.trace("WS received from {}: {}", session.getId(), message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("WS transport error: session={} error={}", session.getId(), exception.getMessage());
    }

    // ─── Helpers ───

    private void sendSnapshot(WebSocketSession session, String locomotiveId) throws IOException {
        // Send last telemetry state
        String telemetryState = redis.opsForValue().get("last_state:" + locomotiveId);
        if (telemetryState != null) {
            sendWrapped(session, "TELEMETRY_SNAPSHOT", telemetryState);
        }

        // Send last health index
        String healthState = redis.opsForValue().get("health_index:" + locomotiveId);
        if (healthState != null) {
            sendWrapped(session, "HEALTH_INDEX_SNAPSHOT", healthState);
        }
    }

    private MessageListener createBridgeListener(WebSocketSession session, String type) {
        return (message, pattern) -> {
            if (!session.isOpen()) return;
            try {
                String payload = new String(message.getBody());
                sendWrapped(session, type, payload);
            } catch (IOException e) {
                log.warn("Failed to send WS message: {}", e.getMessage());
            }
        };
    }

    private synchronized void sendWrapped(WebSocketSession session, String type, String data)
            throws IOException {
        if (!session.isOpen()) return;
        String wrapped = "{\"type\":\"" + type + "\",\"data\":" + data + "}";
        session.sendMessage(new TextMessage(wrapped));
    }

    private String extractLocomotiveId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return null;
        Map<String, String> params = UriComponentsBuilder.fromUri(uri).build()
            .getQueryParams().toSingleValueMap();
        return params.get("locomotiveId");
    }

    private static class SessionSubscriptions {
        final String locomotiveId;
        MessageListener telemetryListener;
        MessageListener healthListener;
        MessageListener alertListener;

        SessionSubscriptions(String locomotiveId) {
            this.locomotiveId = locomotiveId;
        }
    }
}
