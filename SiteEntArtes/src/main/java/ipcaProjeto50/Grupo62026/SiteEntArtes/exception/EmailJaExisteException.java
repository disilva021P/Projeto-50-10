package ipcaProjeto50.Grupo62026.SiteEntArtes.exception;

public class EmailJaExisteException extends RuntimeException {
    public EmailJaExisteException(String email) {
        super("O email '" + email + "' já está registado no sistema.");
    }
}