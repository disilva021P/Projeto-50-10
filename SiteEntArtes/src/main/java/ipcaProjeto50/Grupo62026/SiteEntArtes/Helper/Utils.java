package ipcaProjeto50.Grupo62026.SiteEntArtes.Helper;

import org.springframework.security.core.context.SecurityContextHolder;

public class Utils {
    public static String getAuthenticatedUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return principal.toString();
    }
}