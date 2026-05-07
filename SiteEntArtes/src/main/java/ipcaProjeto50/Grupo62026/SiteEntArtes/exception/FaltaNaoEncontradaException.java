package ipcaProjeto50.Grupo62026.SiteEntArtes.exception;

public class FaltaNaoEncontradaException extends RuntimeException {
    public FaltaNaoEncontradaException(String id) {
        super("Falta com ID " + id + " não encontrada.");
    }
}
