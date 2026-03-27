package by.lobacevich.auth.security;

import by.lobacevich.auth.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String REQUEST_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtTokenService tokenService;
    private final JwtAuthenticationEntryPoint entryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String tokenFromHeader = request.getHeader(REQUEST_HEADER);
            if (!StringUtils.hasText(tokenFromHeader) || !tokenFromHeader.startsWith(TOKEN_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }
            String token = tokenFromHeader.substring(TOKEN_PREFIX.length());
            Claims claims = tokenService.parse(token);

            SecurityContextHolder.getContext().setAuthentication(buildAuth(claims));

            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
            entryPoint.commence(request, response,
                    new AuthenticationCredentialsNotFoundException("Invalid or expired token", e));
        }
    }

    private UsernamePasswordAuthenticationToken buildAuth(Claims claims) {
        UserPrincipal principal = new UserPrincipal(Long.parseLong(claims.getSubject()));
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(claims.get("role", String.class)));

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                authorities
        );
    }
}
