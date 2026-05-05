package com.example.store.filter;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RequestTracingFilterTests {

    private RequestTracingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RequestTracingFilter();
        MDC.clear();
    }

    @Test
    void doFilter_generatesTraceIdAndSetsResponseHeader() throws Exception {
        var request = new MockHttpServletRequest("GET", "/order");
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        String traceId = response.getHeader(RequestTracingFilter.TRACE_ID_HEADER);
        assertThat(traceId).isNotNull().hasSize(16);
    }

    @Test
    void doFilter_propagatesIncomingTraceId() throws Exception {
        var request = new MockHttpServletRequest("GET", "/order");
        request.addHeader(RequestTracingFilter.TRACE_ID_HEADER, "abc123xyz789abcd");
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(RequestTracingFilter.TRACE_ID_HEADER)).isEqualTo("abc123xyz789abcd");
    }

    @Test
    void doFilter_clearsMdcAfterRequest() throws Exception {
        var request = new MockHttpServletRequest("GET", "/customer");
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        // traceId must be removed from MDC once the request is complete
        assertThat(MDC.get(RequestTracingFilter.TRACE_ID_MDC_KEY)).isNull();
    }

    @Test
    void doFilter_includesQueryStringInLog() throws Exception {
        var request = new MockHttpServletRequest("GET", "/customer/search");
        request.setQueryString("name=Alice&page=0");
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getHeader(RequestTracingFilter.TRACE_ID_HEADER)).isNotNull();
    }

    @Test
    void doFilter_ignoresBlankIncomingTraceId() throws Exception {
        var request = new MockHttpServletRequest("POST", "/product");
        request.addHeader(RequestTracingFilter.TRACE_ID_HEADER, "   ");
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        // blank header → should generate a new ID, not use the blank one
        String traceId = response.getHeader(RequestTracingFilter.TRACE_ID_HEADER);
        assertThat(traceId).isNotBlank().doesNotContain(" ").hasSize(16);
    }

    @Test
    void doFilter_clearsMdcEvenIfChainThrows() throws Exception {
        var request = new MockHttpServletRequest("GET", "/order/1");
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);
        org.mockito.Mockito.doThrow(new RuntimeException("downstream failure"))
                .when(chain)
                .doFilter(request, response);

        try {
            filter.doFilter(request, response, chain);
        } catch (RuntimeException ignored) {
            // expected
        }

        assertThat(MDC.get(RequestTracingFilter.TRACE_ID_MDC_KEY)).isNull();
    }
}
