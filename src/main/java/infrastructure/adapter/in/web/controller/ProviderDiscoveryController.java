package infrastructure.adapter.in.web.controller;

import domain.model.OfferedService;
import domain.model.User;
import domain.port.in.OfferedServiceService;
import domain.port.in.UserService;
import infrastructure.adapter.in.web.dto.OfferedServiceResponseDTO;
import infrastructure.adapter.in.web.dto.UserResponseDTO; // Reutilizamos el DTO existente
import infrastructure.adapter.in.web.mapper.OfferedServiceDTOMapper;
import infrastructure.adapter.in.web.mapper.UserDTOMapper;
import infrastructure.adapter.in.web.util.UuidValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/providers") // Ruta base para la exploración de proveedores
@RequiredArgsConstructor
public class ProviderDiscoveryController {

    private final UserService userService;
    private final OfferedServiceService offeredServiceService;
    private final UserDTOMapper userMapper;
    private final OfferedServiceDTOMapper offeredServiceMapper;
    private final UuidValidator uuidValidator;

    /**
     * Busca perfiles públicos de proveedores por nombre de usuario.
     * Este endpoint es público.
     * @param search El término de búsqueda para el nombre del proveedor.
     * @return Una lista de DTOs de usuario que representan a los proveedores encontrados.
     */
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> findProviders(@RequestParam(required = false, defaultValue = "") String search) {
        List<User> providers = userService.findPublicProviders(search);

        // Mapeamos los usuarios a DTOs. El UserResponseDTO es adecuado porque no expone
        // información sensible como el hash de la contraseña.
        List<UserResponseDTO> response = providers.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene todos los servicios ACTIVOS ofrecidos por un proveedor específico.
     * Este endpoint también es público para que los clientes puedan ver la oferta.
     * @param providerUuidStr El UUID del proveedor cuyos servicios se quieren listar.
     * @return Una lista de DTOs de los servicios ofrecidos.
     */
    @GetMapping("/{providerUuid}/services")
    public ResponseEntity<List<OfferedServiceResponseDTO>> findServicesByProvider(@PathVariable("providerUuid") String providerUuidStr) {
        UUID providerUuid = uuidValidator.UUIDvalidateAndConvert(providerUuidStr);
        List<OfferedService> services = offeredServiceService.findAllActiveServicesByProvider(providerUuid);

        List<OfferedServiceResponseDTO> response = services.stream()
                .map(offeredServiceMapper::toResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
