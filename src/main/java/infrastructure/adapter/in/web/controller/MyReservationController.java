package infrastructure.adapter.in.web.controller;

import domain.model.Reservation;
import domain.port.in.ReservationService;
import infrastructure.adapter.in.web.dto.CreateReservationRequestDTO;
import infrastructure.adapter.in.web.dto.ReservationResponseDTO;
import infrastructure.adapter.in.web.dto.UpdateReservationRequestDTO;
import infrastructure.adapter.in.web.mapper.ReservationDTOMapper;
import infrastructure.adapter.in.web.security.RequesterContext;
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

@RestController
@RequestMapping("/api/me/reservations") // Nueva ruta para las reservas del usuario autenticado
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()") // Solo usuarios autenticados pueden acceder a sus propias reservas
public class MyReservationController extends AbstractBaseController {

    private final ReservationService reservationService;
    private final ReservationDTOMapper reservationDTOMapper;

    @PostMapping
    public ResponseEntity<ReservationResponseDTO> createMyReservation(
            @Valid @RequestBody CreateReservationRequestDTO requestDTO,
            Authentication authentication) {

        RequesterContext requester = createRequesterContext(authentication);
        // Aquí, el ownerId para la creación de la reserva es SIEMPRE el del propio usuario autenticado
        Long myUserId = requester.userId()
                .orElseThrow(() -> new AccessDeniedException("User ID is missing from authentication context. Cannot create reservation."));

        Reservation reservationToCreate = reservationDTOMapper.fromRequestDTO(requestDTO);
        Reservation createdReservation = reservationService.createReservation(
                reservationToCreate,
                myUserId, // Forzamos el ownerId al del usuario autenticado
                requestDTO.getServiceId(),
                requester
        );

        ReservationResponseDTO responseDTO = reservationDTOMapper.toResponseDTO(createdReservation);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> getMyReservationById(
            @PathVariable Long id,
            Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        // El servicio findReservationById ya tiene la lógica de autorización para el propietario
        Optional<Reservation> reservationOpt = reservationService.findReservationById(id, requester);
        return reservationOpt
                .map(reservationDTOMapper::toResponseDTO)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> getAllMyReservations(
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            Authentication authentication) {

        RequesterContext requester = createRequesterContext(authentication);
        Long myUserId = requester.userId()
                .orElseThrow(() -> new AccessDeniedException("User ID is missing from authentication context. Cannot retrieve reservations."));

        List<Reservation> reservations;

        if (serviceId != null) {
            reservations = reservationService.findMyReservationsByServiceId(myUserId, serviceId, requester);
        } else if (startDate != null && endDate != null) {
            reservations = reservationService.findMyReservationsByDateRange(myUserId, startDate, endDate, requester);
        } else {
            // Sin filtros, obtener todas las reservas del usuario autenticado
            reservations = reservationService.findReservationsByOwnerId(myUserId, requester);
        }

        List<ReservationResponseDTO> responseDTOs = reservations.stream()
                .map(reservationDTOMapper::toResponseDTO).toList();
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> updateMyReservation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReservationRequestDTO requestDTO,
            Authentication authentication) {

        RequesterContext requester = createRequesterContext(authentication);
        Reservation updateReservationData = reservationDTOMapper.fromRequestDTO(requestDTO);
        // El servicio updateReservation ya tiene la lógica de autorización para el propietario
        Optional<Reservation> updatedReservationOpt = reservationService.updateReservation(id, updateReservationData, requester);

        return updatedReservationOpt
                .map(reservationDTOMapper::toResponseDTO)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMyReservation(@PathVariable Long id, Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        // El servicio deleteReservation ya tiene la lógica de autorización para el propietario
        boolean deleted = reservationService.deleteReservation(id, requester);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Métodos para Confirmar/Cancelar (generalmente permitidos al dueño)
    @PostMapping("/{id}/confirm")
    public ResponseEntity<ReservationResponseDTO> confirmMyReservation(@PathVariable Long id, Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        Reservation confirmedReservation = reservationService.confirmReservation(id, requester);
        return new ResponseEntity<>(reservationDTOMapper.toResponseDTO(confirmedReservation), HttpStatus.OK);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponseDTO> cancelMyReservation(@PathVariable Long id, Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        Reservation cancelledReservation = reservationService.cancelReservation(id, requester);
        return new ResponseEntity<>(reservationDTOMapper.toResponseDTO(cancelledReservation), HttpStatus.OK);
    }
}
