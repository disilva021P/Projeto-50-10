package ipcaProjeto50.Grupo62026.SiteEntArtes.Helper;

import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class IdHasher {
    private Hashids hashids;

    @Value("${HASH_SECRET}")
    private String hashSecret;

    @PostConstruct
    public void init() {
        this.hashids = new Hashids(hashSecret, 15);
    }

    public String encode(Integer id) {
        return hashids.encode(id);
    }

    public Integer decode(String hash) {
        long[] res = hashids.decode(hash);
        return res.length > 0 ? (int) res[0] : null;
    }
}