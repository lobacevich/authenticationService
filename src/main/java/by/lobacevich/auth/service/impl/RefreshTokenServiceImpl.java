package by.lobacevich.auth.service.impl;

import by.lobacevich.auth.entity.Credential;
import by.lobacevich.auth.entity.RefreshToken;
import by.lobacevich.auth.exception.RefreshTokenException;
import by.lobacevich.auth.repository.RefreshTokenRepository;
import by.lobacevich.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository repository;

    @Transactional
    @Override
    public void save(String token, Credential credential) {
        Optional<RefreshToken> refreshTokenOptional = repository.findByCredential(credential);
        if (refreshTokenOptional.isEmpty()) {
            repository.save(RefreshToken.builder()
                    .token(token)
                    .credential(credential)
                    .build());
        } else {
            RefreshToken refreshToken = refreshTokenOptional.get();
            refreshToken.setToken(token);
            repository.save(refreshToken);
        }
    }

    @Transactional
    @Override
    public void refresh(String oldToken, String newToken) {
        RefreshToken refreshToken = repository.findByToken(oldToken).orElseThrow(() ->
                new RefreshTokenException("Refresh token is not exists"));
        refreshToken.setToken(newToken);
        repository.save(refreshToken);
    }

    @Transactional
    @Override
    public void delete(String token) {
        repository.deleteByToken(token);
    }
}
