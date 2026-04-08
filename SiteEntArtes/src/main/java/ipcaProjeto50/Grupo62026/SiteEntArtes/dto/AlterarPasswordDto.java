package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

public record AlterarPasswordDto(

        String confirmaNovaPassword,
        String novaPassword,
        String passwordAtual,
        String confirmarNovaPassword

) {
}
