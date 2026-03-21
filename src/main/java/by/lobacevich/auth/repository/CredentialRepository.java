package by.lobacevich.auth.repository;

import by.lobacevich.auth.entity.Credential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredentialRepository extends JpaRepository<Credential, Long> {

    boolean existsByLogin(String login);

    Optional<Credential> findByLogin(String login);
}
