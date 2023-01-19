package at.ac.uibk.swa.config.exception_handling;

import at.ac.uibk.swa.controllers.error_controllers.SwaErrorController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.stereotype.Component;

import java.io.IOException;


/**
 * Handler for catching {@link AccessDeniedException}s and returning an appropriate Response.
 *
 * @author David Rieser
 * @see AccessDeniedException
 * @see AccessDeniedHandlerImpl
 */
@Component
public class RestAccessDeniedHandler extends AccessDeniedHandlerImpl {

    @Autowired
    private SwaErrorController errorController;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        errorController.handleErrorManual(request, response, accessDeniedException);
    }
}
