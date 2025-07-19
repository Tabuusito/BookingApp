package infrastructure.adapter.in.web.controller;

import domain.model.Reservation;
import domain.port.in.ReservationService;
import infrastructure.adapter.in.web.dto.CreateReservationRequestDTO;
import infrastructure.adapter.in.web.dto.ReservationResponseDTO;
import infrastructure.adapter.in.web.dto.UpdateReservationRequestDTO;
import infrastructure.adapter.in.web.mapper.ReservationDTOMapper;
import infrastructure.adapter.in.web.security.RequesterContext;
import infrastructure.adapter.in.web.util.UuidValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReservationController extends AbstractBaseController {

    private final ReservationService reservationService;
    private final ReservationDTOMapper reservationDTOMapper;
    private final UuidValidator uuidValidator;

    @PostMapping
    public ResponseEntity<ReservationResponseDTO> createReservation(
            @Valid @RequestBody CreateReservationRequestDTO requestDTO,
            Authentication authentication) {

        RequesterContext requester = createRequesterContext(authentication);
        UUID uuid = uuidValidator.UUIDvalidateAndConvert(requestDTO.getServiceUuid());

        // Los admins pueden crear reservas para cualquier ownerId
        Reservation reservationToCreate = reservationDTOMapper.fromRequestDTO(requestDTO);
        Reservation createdReservation = reservationService.createReservation(
                reservationToCreate,
                requestDTO.getOwnerId(),
                uuid,
                requester
        );

        ReservationResponseDTO responseDTO = reservationDTOMapper.toResponseDTO(createdReservation);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ReservationResponseDTO> getReservationById(
            @PathVariable String reservationUuid,
            Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        UUID uuid = uuidValidator.UUIDvalidateAndConvert(reservationUuid);
        // El servicio findReservationById ya tiene la lógica para que el admin pueda ver cualquier reserva
        Optional<Reservation> reservationOpt = reservationService.findReservationByUuid(uuid, requester);
        return reservationOpt
                .map(reservationDTOMapper::toResponseDTO)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> getAllReservations(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) String serviceUuid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            Authentication authentication) {

        RequesterContext requester = createRequesterContext(authentication);

        Optional<UUID> serviceUuidOptional = Optional.ofNullable(uuidValidator
                .validateAndConvertOptional(serviceUuid));

        List<Reservation> reservations = reservationService.findAllReservationsForAdmin(
                Optional.ofNullable(ownerId),
                serviceUuidOptional,
                startDate,
                endDate,
                requester
        );

        List<ReservationResponseDTO> responseDTOs = reservations.stream()
                .map(reservationDTOMapper::toResponseDTO).toList();

        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ReservationResponseDTO> updateReservation(
            @PathVariable String reservationUuid,
            @Valid @RequestBody UpdateReservationRequestDTO requestDTO,
            Authentication authentication) {

        RequesterContext requester = createRequesterContext(authentication);
        UUID uuid = uuidValidator.UUIDvalidateAndConvert(reservationUuid);
        Reservation updateReservationData = reservationDTOMapper.fromRequestDTO(requestDTO);

        Optional<Reservation> updatedReservationOpt = reservationService.updateReservation(uuid, updateReservationData, requester);

        return updatedReservationOpt
                .map(reservationDTOMapper::toResponseDTO)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteReservation(@PathVariable String reservationUuid, Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        UUID uuid = uuidValidator.UUIDvalidateAndConvert(reservationUuid);

        boolean deleted = reservationService.deleteReservation(uuid, requester);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Métodos para Confirmar/Cancelar (siempre bajo control de ADMIN)
    @PostMapping("/{uuid}/confirm")
    public ResponseEntity<ReservationResponseDTO> confirmReservation(@PathVariable String reservationUuid, Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        UUID uuid = uuidValidator.UUIDvalidateAndConvert(reservationUuid);
        Reservation confirmedReservation = reservationService.confirmReservation(uuid, requester);
        return new ResponseEntity<>(reservationDTOMapper.toResponseDTO(confirmedReservation), HttpStatus.OK);
    }

    @PostMapping("/{uuid}/cancel")
    public ResponseEntity<ReservationResponseDTO> cancelReservation(@PathVariable String reservationUuid, Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        UUID uuid = uuidValidator.UUIDvalidateAndConvert(reservationUuid);
        Reservation cancelledReservation = reservationService.cancelReservation(uuid, requester);
        return new ResponseEntity<>(reservationDTOMapper.toResponseDTO(cancelledReservation), HttpStatus.OK);
    }
}
