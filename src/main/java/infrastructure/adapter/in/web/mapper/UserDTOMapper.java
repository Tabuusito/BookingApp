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
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserDTOMapper {

    UserDTOMapper INSTANCE = Mappers.getMapper(UserDTOMapper.class);

    // El mapeo de 'roles' ahora usa un método custom
    @Mapping(source = "roles", target = "roles", qualifiedByName = "rolesToRoleNames")
    UserResponseDTO toDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(source = "password", target = "passwordHash")
    @Mapping(source = "roles", target = "roles", qualifiedByName = "roleNamesToRoles")
    User toDomain(AdminUserCreationDTO adminUserCreationDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(source = "password", target = "passwordHash")
    @Mapping(source = "roles", target = "roles", qualifiedByName = "roleNamesToRoles")
    User toDomain(UserUpdateDTO userUpdateDTO);

    // --- NUEVOS MÉTODOS DE AYUDA PARA ROLES ---

    @Named("roleNamesToRoles")
    default Set<Role> roleNamesToRoles(Set<String> roleNames) {
        if (roleNames == null) {
            return null;
        }
        return roleNames.stream()
                .map(roleName -> {
                    try {
                        return Role.valueOf(roleName.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid role: " + roleName + ". Valid roles are: " + Arrays.toString(Role.values()));
                    }
                })
                .collect(Collectors.toSet());
    }

    @Named("rolesToRoleNames")
    default Set<String> rolesToRoleNames(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::name)
                .collect(Collectors.toSet());
    }
}