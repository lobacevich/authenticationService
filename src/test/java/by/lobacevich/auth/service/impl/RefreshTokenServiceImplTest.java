package by.lobacevich.auth.service.impl;

import by.lobacevich.auth.entity.Credential;
import by.lobacevich.auth.entity.RefreshToken;
import by.lobacevich.auth.exception.RefreshTokenException;
import by.lobacevich.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository repository;

    @Mock
    Credential credential;

    @Mock
    RefreshToken refreshTokenEntity;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private static final String TOKEN = "refreshToken";
    private static final String NEW_TOKEN = "newRefreshToken";

    @Test
    void save_ShouldCreateNewRefreshToken() {
        when(repository.findByCredential(credential)).thenReturn(Optional.empty());

        refreshTokenService.save(TOKEN, credential);

        verify(repository).save(argThat(refreshToken ->
                refreshToken.getToken().equals(TOKEN) &&
                        refreshToken.getCredential().equals(credential)
        ));
    }

    @Test
    void save_ShouldUpdateToken() {
        when(repository.findByCredential(credential)).thenReturn(Optional.of(refreshTokenEntity));

        refreshTokenService.save(TOKEN, credential);

        verify(repository, times(1)).save(refreshTokenEntity);
    }

    @Test
    void refresh_ShouldCallSaveMethodOfRepository() {
        when(repository.findByToken(TOKEN)).thenReturn(Optional.of(refreshTokenEntity));

        refreshTokenService.refresh(TOKEN, NEW_TOKEN);

        verify(repository, times(1)).save(refreshTokenEntity);
    }

    @Test
    void refresh_ShouldThrowRefreshTokenException() {
        when(repository.findByToken(TOKEN)).thenReturn(Optional.empty());

        assertThrows(RefreshTokenException.class, () -> refreshTokenService.refresh(TOKEN, NEW_TOKEN));
    }

    @Test
    void delete_ShouldCallDeleteByTokenMethodOfRepository() {
        refreshTokenService.delete(TOKEN);

        verify(repository, times(1)).deleteByToken(TOKEN);
    }
}