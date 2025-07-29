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
@RequestMapping("/api/admin/timeslots")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTimeSlotController extends AbstractBaseController {

    private final TimeSlotService timeSlotService;
    private final TimeSlotDTOMapper timeSlotMapper;
    private final UuidValidator uuidValidator;

    @PostMapping
    public ResponseEntity<TimeSlotResponseDTO> createTimeSlotAsAdmin(@Valid @RequestBody CreateTimeSlotRequestDTO requestDTO) {
        TimeSlot timeSlotToCreate = timeSlotMapper.fromRequestDTO(requestDTO);
        UUID serviceUuid = uuidValidator.UUIDvalidateAndConvert(requestDTO.getServiceUuid());

        // Como es un admin, el @PreAuthorize en el servicio (que comprueba la propiedad)
        // se saltará gracias a la condición "hasRole('ADMIN')".
        TimeSlot createdTimeSlot = timeSlotService.createTimeSlot(timeSlotToCreate, serviceUuid);

        TimeSlotResponseDTO response = timeSlotMapper.toResponseDTO(createdTimeSlot);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{timeSlotUuid}")
    public ResponseEntity<TimeSlotResponseDTO> getTimeSlotByUuidAsAdmin(@PathVariable("timeSlotUuid") String timeSlotUuidStr) {
        UUID timeSlotUuid = uuidValidator.UUIDvalidateAndConvert(timeSlotUuidStr);
        return timeSlotService.findTimeSlotByUuid(timeSlotUuid)
                .map(timeSlotMapper::toResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{timeSlotUuid}")
    public ResponseEntity<TimeSlotResponseDTO> cancelTimeSlotAsAdmin(@PathVariable("timeSlotUuid") String timeSlotUuidStr) {
        UUID timeSlotUuid = uuidValidator.UUIDvalidateAndConvert(timeSlotUuidStr);
        TimeSlot cancelledTimeSlot = timeSlotService.cancelTimeSlot(timeSlotUuid);
        return ResponseEntity.ok(timeSlotMapper.toResponseDTO(cancelledTimeSlot));
    }

    // Un admin podría querer listar todos los TimeSlots del sistema,
    // posiblemente con filtros por proveedor, servicio, etc.
    // Esto requeriría un nuevo método en el servicio, por ejemplo:
    // `findAllTimeSlots(FilterCriteria criteria)`
    // @GetMapping
    // public ResponseEntity<List<TimeSlotResponseDTO>> getAllTimeSlots() { ... }
}