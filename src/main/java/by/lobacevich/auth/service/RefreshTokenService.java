package by.lobacevich.auth.service;

import by.lobacevich.auth.entity.Credential;

/**
 * Service for managing refresh tokens.
 */
public interface RefreshTokenService {

    /**
     * Saves a refresh token for a user (creates new or updates existing).
     *
     * @param token      new refresh token
     * @param credential user credential entity
     */
    void save(String token, Credential credential);

    /**
     * Replaces an old refresh token with a new one (rotation).
     *
     * @param oldToken existing refresh token
     * @param newToken new refresh token
     * @throws RefreshTokenException if the old token is not found
     */
    void refresh(String oldToken, String newToken);

    /**
     * Deletes a refresh token by its value.
     *
     * @param token token value
     */
    void delete(String token);
}
