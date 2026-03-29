package by.lobacevich.auth.repository;

import by.lobacevich.auth.entity.Credential;
import by.lobacevich.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByCredential(Credential credential);

    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);
}
