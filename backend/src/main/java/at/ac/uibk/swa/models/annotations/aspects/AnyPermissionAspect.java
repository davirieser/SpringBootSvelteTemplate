package at.ac.uibk.swa.models.annotations.aspects;

import at.ac.uibk.swa.models.Authenticable;
import at.ac.uibk.swa.models.Permission;
import at.ac.uibk.swa.models.annotations.AnyPermission;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Component
public class AnyPermissionAspect {
    @Autowired
    private HttpServletRequest request;

    @Around("@annotation(at.ac.uibk.swa.models.annotations.AnyPermission)")
    public Object doSomething(ProceedingJoinPoint jp) throws Throwable {
        // Get the Permissions that are needed from the Attribute
        Set<Permission> requiredPermission = Arrays.stream(
                ((MethodSignature) jp.getSignature())
                    .getMethod()
                    .getAnnotation(AnyPermission.class)
                        .value()
                ).collect(Collectors.toSet());

        // Try to get the currently logged-in user
        Optional<Set<GrantedAuthority>> maybeUserPermissions =
                Optional.ofNullable((UsernamePasswordAuthenticationToken) request.getUserPrincipal())
                .map(token -> token.getPrincipal() instanceof Authenticable a ? a : null)
                .map(Authenticable::getPermissions);

        // If no user is logged in => No Permissions => Fail
        if (maybeUserPermissions.isPresent()) {
            // Get the logged-in user's Permissions and check if they meet any requirement
            Set<GrantedAuthority> userPermissions = maybeUserPermissions.get();
            for (Permission permission : requiredPermission) {
                // Proceed if any Permissions were met
                if (userPermissions.contains(permission)) {
                    return jp.proceed();
                }
            }
        }

        throw new AccessDeniedException("");
    }
}
