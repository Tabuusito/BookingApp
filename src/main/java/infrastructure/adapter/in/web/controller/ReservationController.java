package infrastructure.adapter.in.web.controller;

import domain.model.Reservation;
import domain.port.in.ReservationService;
import infrastructure.adapter.in.web.dto.CreateReservationRequestDTO;
import infrastructure.adapter.in.web.dto.ReservationResponseDTO;
import infrastructure.adapter.in.web.dto.UpdateReservationRequestDTO;
import infrastructure.adapter.in.web.mapper.ReservationDTOMapper;
import infrastructure.adapter.in.web.security.RequesterContext;
import infrastructure.adapter.in.web.security.SpringSecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController extends AbstractBaseController {

    private final ReservationService reservationService;
    private final ReservationDTOMapper reservationDTOMapper;


    // --- Endpoints ---

    @PostMapping
    public ResponseEntity<ReservationResponseDTO> createReservation(
            @Valid @RequestBody CreateReservationRequestDTO requestDTO,
            Authentication authentication) {

        RequesterContext requester = createRequesterContext(authentication);

        Reservation reservationToCreate = reservationDTOMapper.fromRequestDTO(requestDTO);
        Reservation createdReservation = reservationService.createReservation(
                reservationToCreate,
                requestDTO.getOwnerId(),
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
        Optional<Reservation> reservationOpt = reservationService.findReservationById(id, requester);
        return reservationOpt
                .map(reservationDTOMapper::toResponseDTO)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> getAllReservations(
            @RequestParam(required = false) Long ownerIdParam,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {

        RequesterContext requester = createRequesterContext(authentication);

        List<Reservation> reservations;

        if (ownerIdParam != null) {
            if (!requester.isAdmin() && !requester.isOwner(ownerIdParam)) {
                return new ResponseEntity<>(Collections.emptyList(), HttpStatus.FORBIDDEN);
            }
            reservations = reservationService.findReservationsByOwnerId(ownerIdParam, requester);
        } else if (serviceId != null) {
            reservations = reservationService.findReservationsByServiceId(serviceId, requester);
        } else if (startDate != null && endDate != null) {
            reservations = reservationService.findReservationsByDateRange(startDate, endDate, requester);
        } else {
            if (requester.isAdmin()) {
                reservations = reservationService.findAllReservations(requester);
            } else if (requester.userId().isPresent()) {
                reservations = reservationService.findReservationsByOwnerId(requester.userId().get(), requester);
            } else {
                return new ResponseEntity<>(Collections.emptyList(), HttpStatus.UNAUTHORIZED);
            }
        }

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
        Optional<Reservation> updatedReservationOpt = reservationService.updateReservation(id, updateReservationData, requester);

        return updatedReservationOpt
                .map(reservationDTOMapper::toResponseDTO)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id, Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        boolean deleted = reservationService.deleteReservation(id, requester);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }
}
