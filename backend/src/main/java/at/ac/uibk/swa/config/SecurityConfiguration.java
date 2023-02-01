package at.ac.uibk.swa.config;

import at.ac.uibk.swa.config.exception_handling.RestAccessDeniedHandler;
import at.ac.uibk.swa.config.filters.HeaderTokenAuthenticationFilter;
import at.ac.uibk.swa.models.annotations.PublicEndpoint;
import at.ac.uibk.swa.util.EndpointMatcherUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.util.*;
import java.util.stream.Stream;

/**
 * Class for configuring the Authentication Process of the Web-Server.
 * <br/>
 * All API-Paths (except for Exceptions specified in {@link EndpointMatcherUtil}) are secured using an Authentication Token.
 * <br/>
 * The Front-End can fetch a Token from "/api/login" using a User's username and password.
 * <br/>
 * This Token can then be used in the Authentication Header as a Bearer Token to authenticate
 * the user for the API.
 *
 * @author David Rieser
 * @see at.ac.uibk.swa.util.EndpointMatcherUtil
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {
    //region Autowired Components
    @Autowired
    private AuthenticationProvider provider;

    @Autowired
    private AuthenticationFailureHandler failureHandler;

    @Autowired
    private AuthenticationEntryPoint entryPoint;

    @Autowired
    private RestAccessDeniedHandler accessDeniedHandler;

    @Autowired
    private StartupConfig.Profile profile;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;
    //endregion

    //region Authentication Manager Bean
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        return http
                .getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(provider)
                .build();
    }
    //endregion

    //region Custom Authentication Filter Beans
    AbstractAuthenticationProcessingFilter bearerAuthenticationFilter(
            HttpSecurity http, final RequestMatcher requiresAuth
    ) throws Exception {
        final AbstractAuthenticationProcessingFilter filter = new HeaderTokenAuthenticationFilter(requiresAuth);

        filter.setAuthenticationManager(authManager(http));
        filter.setAuthenticationFailureHandler(failureHandler);

        return filter;
    }
    //endregion

    //region Endpoint Matchers
    private RequestMatcher getPublicEndpointMatcher() {
        return new OrRequestMatcher(getPublicEndpointStream().toArray(RequestMatcher[]::new));
    }

    private Stream<RequestMatcher> getPublicEndpointStream() {
        return getPublicEndpointStrings().map(AntPathRequestMatcher::new);
    }

    private Stream<String> getPublicEndpointStrings() {
        // NOTE: This is a workaround because Spring Security does not define an Annotation
        //       that lets you exempt an Endpoint from the FilterChain.
        // Get all Controller defined Endpoints that Spring knows about.
        Map<RequestMappingInfo, HandlerMethod> methods = handlerMapping.getHandlerMethods();
        return methods.entrySet().stream()
                // Filter for Endpoints annotated with the PublicEndpoint Annotation
                .filter(h -> h.getValue().hasMethodAnnotation(PublicEndpoint.class))
                // Get the full Endpoint String (including any Prefixes from @RequestMapping's on the Controller)
                .flatMap(h -> Stream.of(h.getKey()
                        .getPathPatternsCondition()
                        .getPatterns().stream()
                        .map(PathPattern::getPatternString)
                        .toArray(String[]::new)))
                // Ensure that no one accidentally unlocks multiple Endpoints at once
                .filter(p -> !p.endsWith("*"));
    }

    private RequestMatcher getActuatorEndpointMatcher() {
        // Enable the Actuator Endpoints in the Development Environment
        if (this.profile == StartupConfig.Profile.DEV) {
            return new OrRequestMatcher(getActuatorEndpointStream().toArray(RequestMatcher[]::new));
        } else {
            // NOTE: Creating an OrRequest using no Values results in a Runtime Error
            //       WORKAROUND: Create a negated AnyRequestMatcher
            return new NegatedRequestMatcher(AnyRequestMatcher.INSTANCE);
        }
    }

    private Stream<RequestMatcher> getActuatorEndpointStream() {
        return getActuatorEndpointStrings().map(AntPathRequestMatcher::new);
    }

    private Stream<String> getActuatorEndpointStrings() {
        // Enable the Actuator Endpoints in the Development Environment
        if (this.profile == StartupConfig.Profile.DEV) {
            return Stream.of("/actuator/**");
        } else {
            return Stream.of();
        }
    }
    //endregion

    //region Filter Chain Bean
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        RequestMatcher publicMappings = new OrRequestMatcher(getActuatorEndpointMatcher(), getPublicEndpointMatcher());
        RequestMatcher protectedMappings = new NegatedRequestMatcher(publicMappings);

        http
                // Register the custom AuthenticationProvider and AuthenticationFilter
                .authenticationProvider(provider)
                .addFilterBefore(bearerAuthenticationFilter(http, protectedMappings), AnonymousAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                            .requestMatchers(publicMappings).permitAll()
                            .requestMatchers(protectedMappings).authenticated()
                )

                // Disable CORS, CSRF as well as the default Web Security Login and Logout Pages.
                .csrf().disable()
                .cors().disable()
                .formLogin().disable()
                .logout().disable()
                // Disable Anonymous Users from connecting
                .anonymous().disable()
        ;

        // Register the custom Authentication Entry Point and Access Denied Handler.
        http.exceptionHandling()
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(accessDeniedHandler)
        ;

        return http.build();
    }
    //endregion
}

