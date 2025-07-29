package infrastructure.adapter.in.web.controller;

import domain.model.TimeSlot;
import domain.port.in.TimeSlotService;
import infrastructure.adapter.in.web.dto.CreateTimeSlotRequestDTO;
import infrastructure.adapter.in.web.dto.TimeSlotResponseDTO;
import infrastructure.adapter.in.web.mapper.TimeSlotDTOMapper;
import infrastructure.adapter.in.web.util.UuidValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/me/provider/timeslots")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PROVIDER')")
public class ProviderTimeSlotController extends AbstractBaseController {

    private final TimeSlotService timeSlotService;
    private final TimeSlotDTOMapper timeSlotMapper;
    private final UuidValidator uuidValidator;

    @PostMapping
    public ResponseEntity<TimeSlotResponseDTO> createTimeSlot(@Valid @RequestBody CreateTimeSlotRequestDTO requestDTO) {

        TimeSlot timeSlotToCreate = timeSlotMapper.fromRequestDTO(requestDTO);
        UUID serviceUuid = uuidValidator.UUIDvalidateAndConvert(requestDTO.getServiceUuid());

        TimeSlot createdTimeSlot = timeSlotService.createTimeSlot(timeSlotToCreate, serviceUuid);

        TimeSlotResponseDTO response = timeSlotMapper.toResponseDTO(createdTimeSlot);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{timeSlotUuid}")
    public ResponseEntity<TimeSlotResponseDTO> getTimeSlotByUuid(@PathVariable("timeSlotUuid") String timeSlotUuidStr) {
        UUID timeSlotUuid = uuidValidator.UUIDvalidateAndConvert(timeSlotUuidStr);

        // Un proveedor puede querer ver los detalles de su propio slot.
        // La autorización debería estar en el servicio para verificar la propiedad.
        // @PreAuthorize("@customSecurity.isTimeSlotProvider(#timeSlotUuid)")
        return timeSlotService.findTimeSlotByUuid(timeSlotUuid)
                .map(timeSlotMapper::toResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{timeSlotUuid}")
    public ResponseEntity<TimeSlotResponseDTO> cancelTimeSlot(@PathVariable("timeSlotUuid") String timeSlotUuidStr) {
        UUID timeSlotUuid = uuidValidator.UUIDvalidateAndConvert(timeSlotUuidStr);

        // La autorización (@PreAuthorize en el servicio) verificará que el proveedor
        // es el dueño de este TimeSlot antes de permitir la cancelación.
        TimeSlot cancelledTimeSlot = timeSlotService.cancelTimeSlot(timeSlotUuid);

        return ResponseEntity.ok(timeSlotMapper.toResponseDTO(cancelledTimeSlot));
    }

    // Un proveedor también podría querer ver una lista de sus propios TimeSlots,
    // lo cual requeriría un nuevo método en el servicio como `findTimeSlotsByProvider`.
    // @GetMapping
    // public ResponseEntity<List<TimeSlotResponseDTO>> getMyTimeSlots() { ... }
}