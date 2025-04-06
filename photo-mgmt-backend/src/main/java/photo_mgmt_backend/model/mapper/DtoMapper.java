package photo_mgmt_backend.model.mapper;

import org.mapstruct.Mapping;

import java.util.List;

public interface DtoMapper<Entity, RequestDto, ResponseDto> {

    @Mapping(target = "id", ignore = true)
    Entity convertRequestDtoToEntity(RequestDto requestDto);

    ResponseDto convertEntityToResponseDto(Entity entity);

    List<ResponseDto> convertEntitiesToResponseDtos(List<Entity> entities);
}
