package ipcaProjeto50.Grupo62026.SiteEntArtes;

import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.TipoUtilizador;
import ipcaProjeto50.Grupo62026.SiteEntArtes.entity.Utilizadore;
import ipcaProjeto50.Grupo62026.SiteEntArtes.repository.TipoUtilizadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Instant;
import java.time.LocalDate;

@SpringBootApplication
@EnableScheduling
public class SiteEntArtesApplication {

	public static void main(String[] args) {
		SpringApplication.run(SiteEntArtesApplication.class, args);
	}

}
