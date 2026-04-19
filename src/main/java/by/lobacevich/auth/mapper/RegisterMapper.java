package by.lobacevich.auth.mapper;

import by.lobacevich.auth.dto.inner.AuthRegisterDto;
import by.lobacevich.auth.dto.inner.AuthRegisteredDto;
import by.lobacevich.auth.dto.request.FullRequestDto;
import by.lobacevich.auth.dto.request.UserCreateRequestDto;
import by.lobacevich.auth.dto.response.FullDtoResponse;
import by.lobacevich.auth.dto.response.UserCreatedResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RegisterMapper {

    UserCreateRequestDto toUserCreateRequest(FullRequestDto requestDto);

    AuthRegisterDto toAuthRegisterDto(FullRequestDto requestDto, Long userId);

    FullDtoResponse toResponseFull(UserCreatedResponseDto userResponseDto,
                                   AuthRegisteredDto authRegisteredDto);
}
