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
import org.springframework.security.access.AccessDeniedException; // Para excepciones lanzadas por el servicio
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/me/services")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MyOfferedServiceController extends AbstractBaseController {

    private final OfferedServiceService offeredServiceService;
    private final OfferedServiceDTOMapper offeredServiceDTOMapper;
    private final UuidValidator uuidValidator;

    @PostMapping
    public ResponseEntity<OfferedServiceResponseDTO> createMyOfferedService(
            @Valid @RequestBody CreateOfferedServiceRequestDTO requestDTO,
            Authentication authentication) {

        RequesterContext requester = createRequesterContext(authentication);

        OfferedService serviceToCreate = offeredServiceDTOMapper.fromRequestDTO(requestDTO);
        OfferedService createdService = offeredServiceService.createOfferedService(
                serviceToCreate, requester.userUuid().orElseThrow(() -> new AccessDeniedException("User UUID is missing from authentication context"))
        );
        OfferedServiceResponseDTO responseDTO = offeredServiceDTOMapper.toResponseDTO(createdService);

        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<OfferedServiceResponseDTO> getMyOfferedServiceByUuid(@PathVariable ("uuid") String serviceUuid) {
        UUID uuid = uuidValidator.UUIDvalidateAndConvert(serviceUuid);
        Optional<OfferedService> serviceOpt = offeredServiceService.findOfferedServiceByUuid(uuid);

        return serviceOpt
                .map(offeredServiceDTOMapper::toResponseDTO)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<OfferedServiceResponseDTO>> getAllMyOfferedServices(
            @RequestParam(name = "activeOnly", defaultValue = "true") boolean activeOnly,
            @RequestParam(name = "nameContains", required = false) String nameContains) {

        List<OfferedService> services = offeredServiceService.findMyServices(
                nameContains, activeOnly
        );

        List<OfferedServiceResponseDTO> responseDTOs = services.stream().map(offeredServiceDTOMapper::toResponseDTO).toList();

        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<OfferedServiceResponseDTO> updateMyOfferedService(
            @PathVariable ("uuid") String serviceUuid,
            @Valid @RequestBody UpdateOfferedServiceRequestDTO requestDTO) {

        UUID uuid = uuidValidator.UUIDvalidateAndConvert(serviceUuid);

        OfferedService updateData = offeredServiceDTOMapper.fromRequestDTO(requestDTO);
        Optional<OfferedService> updatedServiceOpt = offeredServiceService.updateOfferedService(uuid, updateData);

        return updatedServiceOpt
                .map(offeredServiceDTOMapper::toResponseDTO)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteMyOfferedService(@PathVariable ("uuid") String serviceUuid) {
        UUID uuid = uuidValidator.UUIDvalidateAndConvert(serviceUuid);
        offeredServiceService.deleteOfferedService(uuid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
