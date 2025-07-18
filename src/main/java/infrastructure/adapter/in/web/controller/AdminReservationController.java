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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/reservations") // Nueva ruta para la administración de reservas
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Todas las operaciones aquí requieren rol ADMIN
public class AdminReservationController extends AbstractBaseController {

    private final ReservationService reservationService;
    private final ReservationDTOMapper reservationDTOMapper;

    @PostMapping
    public ResponseEntity<ReservationResponseDTO> createReservation(
            @Valid @RequestBody CreateReservationRequestDTO requestDTO,
            Authentication authentication) {

        RequesterContext requester = createRequesterContext(authentication);

        // Los admins pueden crear reservas para cualquier ownerId
        Reservation reservationToCreate = reservationDTOMapper.fromRequestDTO(requestDTO);
        Reservation createdReservation = reservationService.createReservation(
                reservationToCreate,
                requestDTO.getOwnerId(), // El admin especifica el ownerId
                requestDTO.getServiceId(),
                requester
        );

        ReservationResponseDTO responseDTO = reservationDTOMapper.toResponseDTO(createdReservation);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> getReservationById(
            @PathVariable Long id,
            Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        // El servicio findReservationById ya tiene la lógica para que el admin pueda ver cualquier reserva
        Optional<Reservation> reservationOpt = reservationService.findReservationById(id, requester);
        return reservationOpt
                .map(reservationDTOMapper::toResponseDTO)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> getAllReservations(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            Authentication authentication) {

        RequesterContext requester = createRequesterContext(authentication);

        List<Reservation> reservations;

        reservations = reservationService.findAllReservationsForAdmin(Optional.of(ownerId), Optional.of(serviceId), startDate, endDate, requester);

        List<ReservationResponseDTO> responseDTOs = reservations.stream()
                .map(reservationDTOMapper::toResponseDTO).toList();
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReservationRequestDTO requestDTO,
            Authentication authentication) {

        RequesterContext requester = createRequesterContext(authentication);
        Reservation updateReservationData = reservationDTOMapper.fromRequestDTO(requestDTO);
        // El servicio updateReservation ya tiene la lógica de autorización para ADMIN
        Optional<Reservation> updatedReservationOpt = reservationService.updateReservation(id, updateReservationData, requester);

        return updatedReservationOpt
                .map(reservationDTOMapper::toResponseDTO)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id, Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        // El servicio deleteReservation ya tiene la lógica de autorización para ADMIN
        boolean deleted = reservationService.deleteReservation(id, requester);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Métodos para Confirmar/Cancelar (siempre bajo control de ADMIN)
    @PostMapping("/{id}/confirm")
    public ResponseEntity<ReservationResponseDTO> confirmReservation(@PathVariable Long id, Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        Reservation confirmedReservation = reservationService.confirmReservation(id, requester);
        return new ResponseEntity<>(reservationDTOMapper.toResponseDTO(confirmedReservation), HttpStatus.OK);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponseDTO> cancelReservation(@PathVariable Long id, Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        Reservation cancelledReservation = reservationService.cancelReservation(id, requester);
        return new ResponseEntity<>(reservationDTOMapper.toResponseDTO(cancelledReservation), HttpStatus.OK);
    }
}
