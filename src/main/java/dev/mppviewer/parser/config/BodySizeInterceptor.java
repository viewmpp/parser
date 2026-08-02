package dev.mppviewer.parser.config;

import dev.mppviewer.parser.exception.BodyTooLargeException;
import dev.mppviewer.parser.exception.LengthRequiredException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class BodySizeInterceptor implements HandlerInterceptor {

    private final long maxBytes;

    public BodySizeInterceptor(@Value("${parser.max-body-bytes}") long maxBytes) {
        this.maxBytes = maxBytes;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        long declared = request.getContentLengthLong();
        if (declared < 0) {
            throw new LengthRequiredException();
        }
        if (declared > maxBytes) {
            throw new BodyTooLargeException(declared, maxBytes);
        }
        return true;
    }
}
