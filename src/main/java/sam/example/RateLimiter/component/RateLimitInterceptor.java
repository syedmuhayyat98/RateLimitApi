package sam.example.RateLimiter.component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int TOO_MANY_REQUESTS = 429; // HTTP 429 status code
    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final ConcurrentHashMap<String, AtomicLong> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lastRequestTime = new ConcurrentHashMap<>();
    private final long RATE_LIMIT = 5; // Max 5 requests per minute
    private final long TIME_WINDOW = TimeUnit.MINUTES.toMillis(1);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = request.getRemoteAddr();
        long currentTime = System.currentTimeMillis();

        lastRequestTime.putIfAbsent(clientIp, currentTime);
        requestCounts.putIfAbsent(clientIp, new AtomicLong(0));

        long elapsedTime = currentTime - lastRequestTime.get(clientIp);

        if (elapsedTime > TIME_WINDOW) {
            lastRequestTime.put(clientIp, currentTime);
            requestCounts.get(clientIp).set(1);
            logger.info("Resetting count for IP {}. New window starts.", clientIp);
        } else {
            long currentCount = requestCounts.get(clientIp).incrementAndGet();
            logger.info("IP {} has made {} requests in the current window.", clientIp, currentCount);
            if (currentCount > RATE_LIMIT) {
                response.setStatus(TOO_MANY_REQUESTS);
                logger.warn("IP {} has exceeded the rate limit.", clientIp);
                return false;
            }
        }

        return true;
    }
}




