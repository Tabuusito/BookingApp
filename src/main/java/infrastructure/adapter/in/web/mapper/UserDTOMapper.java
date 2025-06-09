package infrastructure.adapter.in.web.mapper;

import domain.model.Role;
import domain.model.User;
import infrastructure.adapter.in.web.dto.AdminUserCreationDTO;
import infrastructure.adapter.in.web.dto.UserResponseDTO;
import infrastructure.adapter.in.web.dto.UserUpdateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserDTOMapper {

    UserDTOMapper INSTANCE = Mappers.getMapper(UserDTOMapper.class);

    @Mapping(source = "role", target = "role", qualifiedByName = "mapRoleEnumToString") // Convierte Role (enum) a String
    UserResponseDTO toDTO(User user);

    List<UserResponseDTO> toDTOList(List<User> users);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "password", target = "passwordHash") // <--- ¡ESTA ES LA LÍNEA CLAVE!
    @Mapping(source = "role", target = "role", qualifiedByName = "mapRoleStringToEnum")
    User toDomain(AdminUserCreationDTO adminUserCreationDTO);

    User toDomain(UserUpdateDTO userUpdateDTO);

    @Named("mapRoleStringToEnum")
    default Role mapRoleStringToEnum(String roleString) {
        if (roleString == null || roleString.isBlank()) {
            throw new IllegalArgumentException("Role cannot be blank or null.");
        }
        try {
            return Role.valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + roleString + ". Valid roles are: " + Arrays.toString(Role.values()));
        }
    }

    @Named("mapRoleEnumToString")
    default String mapRoleEnumToString(Role role) {
        if (role == null) {
            return null;
        }
        return role.name(); // Convierte el enum a su nombre de String (ej. Role.ADMIN -> "ADMIN")
    }

    List<User> toDomainList(List<UserResponseDTO> userResponseDTOs); // Renombré el parámetro para claridad
}