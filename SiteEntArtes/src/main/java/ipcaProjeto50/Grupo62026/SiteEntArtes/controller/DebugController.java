package ipcaProjeto50.Grupo62026.SiteEntArtes.controller;

import ipcaProjeto50.Grupo62026.SiteEntArtes.Helper.IdHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private IdHasher idHasher;

    @GetMapping("/encode/{id}")
    public String testEncode(@PathVariable Integer id) {
        return idHasher.encode(id);
    }

    @GetMapping("/decode/{hash}")
    public Integer testDecode(@PathVariable String hash) {
        return idHasher.decode(hash);
    }
}
