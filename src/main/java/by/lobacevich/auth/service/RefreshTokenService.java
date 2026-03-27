package by.lobacevich.auth.service;

import by.lobacevich.auth.entity.Credential;

public interface RefreshTokenService {

    void save(String token, Credential credential);

    void refresh(String oldToken, String newToken);

    void delete(String token);
}
