package ipcaProjeto50.Grupo62026.SiteEntArtes.dto;

public record AlterarPasswordSemLoginDto(

        String confirmaNovaPassword,
        String novaPassword,
        String token,
        String email
) {
}