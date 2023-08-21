package org.haic.often.springboot;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author haicdust
 * @version 1.0
 * @since 2023/7/4 21:24
 */
public class InputStreamWrapperFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
		var servletRequest = new InputStreamHttpServletRequestWrapper(httpServletRequest);
		filterChain.doFilter(servletRequest, httpServletResponse);
	}
}
