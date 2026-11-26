package com.innowise.userservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class HeaderAuthFilter extends OncePerRequestFilter {

  @Value("${internal.secret}")
  private String internalSecret;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String secret = request.getHeader("X-Internal-Secret");

    if (internalSecret.equals(secret)) {
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
              0L,
              null,
              List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))
          );

      SecurityContextHolder.getContext().setAuthentication(authentication);
      filterChain.doFilter(request, response);
      return;
    }

    String userId = request.getHeader("X-User-Id");
    String role = request.getHeader("X-User-Role");

    if (userId != null && role != null) {
      try {
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                Long.parseLong(userId),
                null,
                List.of(new SimpleGrantedAuthority(role))
            );

        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (NumberFormatException ex) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
    }

    filterChain.doFilter(request, response);
  }
}
