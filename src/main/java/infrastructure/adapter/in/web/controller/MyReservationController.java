package infrastructure.adapter.in.web.controller;

import domain.exception.InvalidUuidFormatException;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/me/reservations")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MyReservationController extends AbstractBaseController {

    private final ReservationService reservationService;
    private final ReservationDTOMapper reservationDTOMapper;
    private final UuidValidator uuidValidator;

    @PostMapping
    public ResponseEntity<ReservationResponseDTO> createMyReservation(
            @Valid @RequestBody CreateReservationRequestDTO requestDTO,
            Authentication authentication) {

        RequesterContext requester = createRequesterContext(authentication);
        UUID uuid = uuidValidator.UUIDvalidateAndConvert(requestDTO.getServiceUuid());
        // Aquí, el ownerId para la creación de la reserva es SIEMPRE el del propio usuario autenticado
        Long myUserId = requester.userId()
                .orElseThrow(() -> new AccessDeniedException("User ID is missing from authentication context. Cannot create reservation."));

        Reservation reservationToCreate = reservationDTOMapper.fromRequestDTO(requestDTO);
        Reservation createdReservation = reservationService.createReservation(
                reservationToCreate,
                myUserId, // Forzamos el ownerId al del usuario autenticado
                uuid,
                requester
        );

        ReservationResponseDTO responseDTO = reservationDTOMapper.toResponseDTO(createdReservation);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ReservationResponseDTO> getMyReservationByUuid(
            @PathVariable ("uuid") String reservationUuid,
            Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        UUID uuid = uuidValidator.UUIDvalidateAndConvert(reservationUuid);

        Optional<Reservation> reservationOpt = reservationService.findReservationByUuid(uuid, requester);
        return reservationOpt
                .map(reservationDTOMapper::toResponseDTO)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> getAllMyReservations(
            @RequestParam(required = false) String serviceUuid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            Authentication authentication) {

        RequesterContext requester = createRequesterContext(authentication);
        Long myUserId = requester.userId()
                .orElseThrow(() -> new AccessDeniedException("User ID is missing from authentication context. Cannot retrieve reservations."));

        List<Reservation> reservations;

        UUID uuid = uuidValidator.validateAndConvertOptional(serviceUuid);

        if (uuid != null) {
            reservations = reservationService.findMyReservationsByServiceUuid(myUserId, uuid, requester);
        } else if (startDate != null && endDate != null) {
            reservations = reservationService.findMyReservationsByDateRange(myUserId, startDate, endDate, requester);
        } else {
            reservations = reservationService.findReservationsByOwnerId(myUserId, requester);
        }

        List<ReservationResponseDTO> responseDTOs = reservations.stream()
                .map(reservationDTOMapper::toResponseDTO).toList();

        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ReservationResponseDTO> updateMyReservation(
            @PathVariable ("uuid") String reservationUuid,
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
    public ResponseEntity<Void> deleteMyReservation(@PathVariable ("uuid") String reservationUuid, Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        UUID uuid = uuidValidator.UUIDvalidateAndConvert(reservationUuid);
        boolean deleted = reservationService.deleteReservation(uuid, requester);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Métodos para Confirmar/Cancelar (permitidos al dueño)
    @PostMapping("/{uuid}/confirm")
    public ResponseEntity<ReservationResponseDTO> confirmMyReservation(@PathVariable ("uuid") String reservationUuid, Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        UUID uuid = uuidValidator.UUIDvalidateAndConvert(reservationUuid);
        Reservation confirmedReservation = reservationService.confirmReservation(uuid, requester);
        return new ResponseEntity<>(reservationDTOMapper.toResponseDTO(confirmedReservation), HttpStatus.OK);
    }

    @PostMapping("/{uuid}/cancel")
    public ResponseEntity<ReservationResponseDTO> cancelMyReservation(@PathVariable ("uuid") String reservationUuid, Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        UUID uuid = uuidValidator.UUIDvalidateAndConvert(reservationUuid);
        Reservation cancelledReservation = reservationService.cancelReservation(uuid, requester);
        return new ResponseEntity<>(reservationDTOMapper.toResponseDTO(cancelledReservation), HttpStatus.OK);
    }
}
