package dev.mppviewer.parser;

import dev.mppviewer.parser.config.BodySizeInterceptor;
import dev.mppviewer.parser.exception.BodyTooLargeException;
import dev.mppviewer.parser.exception.LengthRequiredException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class BodySizeInterceptorTest {

    private final BodySizeInterceptor interceptor = new BodySizeInterceptor(1000);

    @Test
    void rejectsBodyWithoutDeclaredLength() {
        assertThatThrownBy(() -> interceptor.preHandle(request(null), new MockHttpServletResponse(), new Object()))
                .isInstanceOf(LengthRequiredException.class);
    }

    @Test
    void rejectsOversizedBody() {
        assertThatThrownBy(() -> interceptor.preHandle(request(1001), new MockHttpServletResponse(), new Object()))
                .isInstanceOf(BodyTooLargeException.class);
    }

    @Test
    void acceptsBodyWithinLimit() {
        assertThat(interceptor.preHandle(request(1000), new MockHttpServletResponse(), new Object())).isTrue();
    }

    private static MockHttpServletRequest request(Integer length) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/parse");
        if (length != null) {
            request.setContent(new byte[length]);
        }
        return request;
    }
}
