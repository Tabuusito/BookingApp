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
import org.springframework.security.access.AccessDeniedException; // Para excepciones lanzadas por el servicio
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/me/services")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MyOfferedServiceController extends AbstractBaseController { // Extiende la clase base

    private final OfferedServiceService offeredServiceService;
    private final OfferedServiceDTOMapper offeredServiceDTOMapper;

    @PostMapping
    public ResponseEntity<OfferedServiceResponseDTO> createMyOfferedService(
            @Valid @RequestBody CreateOfferedServiceRequestDTO requestDTO,
            Authentication authentication) {

        RequesterContext requester = createRequesterContext(authentication);

        OfferedService serviceToCreate = offeredServiceDTOMapper.fromRequestDTO(requestDTO);
        OfferedService createdService = offeredServiceService.createOfferedService(
                serviceToCreate, requester.userId().orElseThrow(() -> new AccessDeniedException("User ID is missing from authentication context")), requester // Aquí ownerId es el del requester
        );
        OfferedServiceResponseDTO responseDTO = offeredServiceDTOMapper.toResponseDTO(createdService);

        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfferedServiceResponseDTO> getMyOfferedServiceById(@PathVariable Long id, Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        Optional<OfferedService> serviceOpt = offeredServiceService.findOfferedServiceById(id, requester);

        return serviceOpt
                .map(offeredServiceDTOMapper::toResponseDTO)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<OfferedServiceResponseDTO>> getAllMyOfferedServices(
            @RequestParam(name = "activeOnly", defaultValue = "true") boolean activeOnly,
            @RequestParam(name = "nameContains", required = false) String nameContains,
            Authentication authentication) {

        RequesterContext requester = createRequesterContext(authentication);

        List<OfferedService> services = offeredServiceService.findMyServices(
                nameContains, activeOnly, requester
        );

        List<OfferedServiceResponseDTO> responseDTOs = services.stream().map(offeredServiceDTOMapper::toResponseDTO).toList();

        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OfferedServiceResponseDTO> updateMyOfferedService(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOfferedServiceRequestDTO requestDTO,
            Authentication authentication) {

        RequesterContext requester = createRequesterContext(authentication);

        OfferedService updateData = offeredServiceDTOMapper.fromRequestDTO(requestDTO);
        Optional<OfferedService> updatedServiceOpt = offeredServiceService.updateOfferedService(id, updateData, requester);

        return updatedServiceOpt
                .map(offeredServiceDTOMapper::toResponseDTO)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMyOfferedService(@PathVariable Long id, Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        boolean deleted = offeredServiceService.deleteOfferedService(id, requester);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }
}
