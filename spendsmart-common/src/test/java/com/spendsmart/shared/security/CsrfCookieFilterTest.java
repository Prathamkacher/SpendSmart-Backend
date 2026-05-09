package com.spendsmart.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.web.csrf.CsrfToken;

import java.io.IOException;

import static org.mockito.Mockito.*;

class CsrfCookieFilterTest {

    @Test
    void testDoFilterInternalWithCsrfToken() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        CsrfToken csrfToken = mock(CsrfToken.class);

        when(request.getAttribute(CsrfToken.class.getName())).thenReturn(csrfToken);

        CsrfCookieFilter filter = new CsrfCookieFilter();
        filter.doFilter(request, response, filterChain);

        verify(csrfToken, times(1)).getToken();
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalWithoutCsrfToken() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getAttribute(CsrfToken.class.getName())).thenReturn(null);

        CsrfCookieFilter filter = new CsrfCookieFilter();
        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }
}
