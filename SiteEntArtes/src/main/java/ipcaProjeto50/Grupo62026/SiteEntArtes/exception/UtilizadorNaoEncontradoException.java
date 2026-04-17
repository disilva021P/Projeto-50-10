package ipcaProjeto50.Grupo62026.SiteEntArtes.exception;

public class UtilizadorNaoEncontradoException extends RuntimeException {
    public UtilizadorNaoEncontradoException(Integer id) {
        super("Utilizador com ID " + id + " não encontrado.");
    }
    public UtilizadorNaoEncontradoException(String email) {
        super("Utilizador com email '" + email + "' não encontrado.");
    }
}