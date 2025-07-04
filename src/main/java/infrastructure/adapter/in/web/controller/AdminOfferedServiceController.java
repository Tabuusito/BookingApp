package infrastructure.adapter.in.web.controller;

import domain.model.OfferedService;
import domain.port.in.OfferedServiceService;
import infrastructure.adapter.in.web.dto.CreateOfferedServiceRequestDTO;
import infrastructure.adapter.in.web.dto.OfferedServiceResponseDTO;
import infrastructure.adapter.in.web.dto.UpdateOfferedServiceRequestDTO;
import infrastructure.adapter.in.web.mapper.OfferedServiceDTOMapper;
import infrastructure.adapter.in.web.security.RequesterContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/services")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOfferedServiceController extends AbstractBaseController {

    private final OfferedServiceService offeredServiceService;
    private final OfferedServiceDTOMapper offeredServiceDTOMapper;

    @PostMapping
    public ResponseEntity<OfferedServiceResponseDTO> createOfferedService(
            @Valid @RequestBody CreateOfferedServiceRequestDTO requestDTO,
            Authentication authentication) {

        RequesterContext requester = createRequesterContext(authentication);

        OfferedService serviceToCreate = offeredServiceDTOMapper.fromRequestDTO(requestDTO);
        OfferedService createdService = offeredServiceService.createOfferedService(
                serviceToCreate, requestDTO.getOwnerId(), requester // Aquí ownerId puede ser de otro usuario si el admin lo especifica
        );
        OfferedServiceResponseDTO responseDTO = offeredServiceDTOMapper.toResponseDTO(createdService);

        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfferedServiceResponseDTO> getOfferedServiceById(@PathVariable Long id, Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        Optional<OfferedService> serviceOpt = offeredServiceService.findOfferedServiceById(id, requester); // El servicio verifica que es admin

        return serviceOpt
                .map(offeredServiceDTOMapper::toResponseDTO)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<OfferedServiceResponseDTO>> getAllOfferedServices(
            @RequestParam(name = "activeOnly", defaultValue = "false") boolean activeOnly, // Admin puede ver inactivos por defecto
            @RequestParam(name = "nameContains", required = false) String nameContains,
            @RequestParam(name = "ownerId", required = false) Long ownerId, // Admin puede filtrar por ownerId
            Authentication authentication) {

        RequesterContext requester = createRequesterContext(authentication);

        List<OfferedService> services;

        services = offeredServiceService.findAllServicesForAdmin(nameContains, activeOnly, Optional.ofNullable(ownerId), requester);

        List<OfferedServiceResponseDTO> responseDTOs = services.stream().map(offeredServiceDTOMapper::toResponseDTO).toList();

        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OfferedServiceResponseDTO> updateOfferedService(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOfferedServiceRequestDTO requestDTO,
            Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);

        OfferedService updateData = offeredServiceDTOMapper.fromRequestDTO(requestDTO);
        Optional<OfferedService> updatedServiceOpt = offeredServiceService.updateOfferedService(id, updateData, requester); // El servicio verifica que es admin

        return updatedServiceOpt
                .map(offeredServiceDTOMapper::toResponseDTO)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOfferedService(@PathVariable Long id, Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        // La excepción ServiceInUseException será manejada por el GlobalExceptionHandler
        boolean deleted = offeredServiceService.deleteOfferedService(id, requester); // El servicio verifica que es admin
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
