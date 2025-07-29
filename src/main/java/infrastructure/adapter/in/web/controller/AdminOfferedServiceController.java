package infrastructure.adapter.in.web.controller;

import domain.model.OfferedService;
import domain.port.in.OfferedServiceService;
import infrastructure.adapter.in.web.dto.CreateOfferedServiceRequestDTO;
import infrastructure.adapter.in.web.dto.OfferedServiceResponseDTO;
import infrastructure.adapter.in.web.dto.UpdateOfferedServiceRequestDTO;
import infrastructure.adapter.in.web.mapper.OfferedServiceDTOMapper;
import infrastructure.adapter.in.web.security.RequesterContext;
import infrastructure.adapter.in.web.util.UuidValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/services")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOfferedServiceController extends AbstractBaseController {

    private final OfferedServiceService offeredServiceService;
    private final OfferedServiceDTOMapper offeredServiceDTOMapper;
    private final UuidValidator uuidValidator;

    @PostMapping
    public ResponseEntity<OfferedServiceResponseDTO> createOfferedService(
            @Valid @RequestBody CreateOfferedServiceRequestDTO requestDTO) {

        UUID uuid = uuidValidator.UUIDvalidateAndConvert(requestDTO.getOwnerUuid());

        OfferedService serviceToCreate = offeredServiceDTOMapper.fromRequestDTO(requestDTO);
        OfferedService createdService = offeredServiceService.createOfferedService(
                serviceToCreate, uuid// Aquí ownerId puede ser de otro usuario si el admin lo especifica
        );
        OfferedServiceResponseDTO responseDTO = offeredServiceDTOMapper.toResponseDTO(createdService);

        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<OfferedServiceResponseDTO> getOfferedServiceByUuid(@PathVariable ("uuid") String serviceUuid) {
        UUID uuid = uuidValidator.UUIDvalidateAndConvert(serviceUuid);
        Optional<OfferedService> serviceOpt = offeredServiceService.findOfferedServiceByUuid(uuid); // El servicio verifica que es admin

        return serviceOpt
                .map(offeredServiceDTOMapper::toResponseDTO)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<OfferedServiceResponseDTO>> getAllOfferedServices(
            @RequestParam(name = "activeOnly", defaultValue = "false") boolean activeOnly, // Admin puede ver inactivos por defecto
            @RequestParam(name = "nameContains", required = false) String nameContains,
            @RequestParam(name = "ownerUuid", required = false) String ownerUuid // Admin puede filtrar por ownerId
            ) {

        List<OfferedService> services;
        UUID uuid = uuidValidator.UUIDvalidateAndConvert(ownerUuid);

        services = offeredServiceService.findAllServicesForAdmin(nameContains, Optional.ofNullable(uuid), activeOnly);

        List<OfferedServiceResponseDTO> responseDTOs = services.stream().map(offeredServiceDTOMapper::toResponseDTO).toList();

        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<OfferedServiceResponseDTO> updateOfferedService(
            @PathVariable ("uuid") String serviceUuid,
            @Valid @RequestBody UpdateOfferedServiceRequestDTO requestDTO) {
        UUID uuid = uuidValidator.UUIDvalidateAndConvert(serviceUuid);

        OfferedService updateData = offeredServiceDTOMapper.fromRequestDTO(requestDTO);
        Optional<OfferedService> updatedServiceOpt = offeredServiceService.updateOfferedService(uuid, updateData); // El servicio verifica que es admin

        return updatedServiceOpt
                .map(offeredServiceDTOMapper::toResponseDTO)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteOfferedService(@PathVariable ("uuid") String serviceUuid) {
        UUID uuid =uuidValidator.UUIDvalidateAndConvert(serviceUuid);
        // La excepción ServiceInUseException será manejada por el GlobalExceptionHandler
        offeredServiceService.deleteOfferedService(uuid); // El servicio verifica que es admin

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
