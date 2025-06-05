package infrastructure.adapter.out.persistence.mapper;

import domain.model.User;
import infrastructure.adapter.out.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);


    UserEntity toEntity(User user);

    User toDomain(UserEntity userEntity);

    List<User> toDomainList(List<UserEntity> userEntities);
    List<UserEntity> toEntityList(List<User> users);
}