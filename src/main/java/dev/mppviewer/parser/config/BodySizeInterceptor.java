package dev.mppviewer.parser.config;

import dev.mppviewer.parser.exception.BodyTooLargeException;
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

    /**
     * При chunked-кодировании Content-Length отсутствует и getContentLengthLong()
     * вернёт -1 — проверка молча пропустит запрос любого размера. Это приемлемо:
     * настоящий барьер стоит в Go, а сайдкар в интернет не смотрит. Здесь ловится
     * честная ошибка, а не атака.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        long declared = request.getContentLengthLong();
        if (declared > maxBytes) {
            throw new BodyTooLargeException(declared, maxBytes);
        }
        return true;
    }
}
