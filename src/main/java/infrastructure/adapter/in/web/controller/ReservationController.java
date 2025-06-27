package infrastructure.adapter.in.web.controller;

import domain.model.Reservation;
import domain.port.in.ReservationService;
import infrastructure.adapter.in.web.dto.CreateReservationRequestDTO;
import infrastructure.adapter.in.web.dto.ReservationResponseDTO;
import infrastructure.adapter.in.web.dto.UpdateReservationRequestDTO;
import infrastructure.adapter.in.web.mapper.ReservationDTOMapper;
import infrastructure.adapter.in.web.security.SpringSecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationDTOMapper reservationDTOMapper;

    private Optional<Long> getAuthenticatedUserId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof SpringSecurityUser) {
            SpringSecurityUser userDetails = (SpringSecurityUser) authentication.getPrincipal();
            return Optional.ofNullable(userDetails.getId());
        }
        return Optional.empty();
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }

    // --- Endpoints ---

    @PostMapping
    public ResponseEntity<ReservationResponseDTO> createReservation(
            @Valid @RequestBody CreateReservationRequestDTO requestDTO,
            Authentication authentication) {

        Optional<Long> requestingUserIdOpt = getAuthenticatedUserId(authentication);

        Reservation reservationToCreate = reservationDTOMapper.fromRequestDTO(requestDTO);
        Reservation createdReservation = reservationService.createReservation(
                reservationToCreate,
                requestDTO.getUserId(),
                requestDTO.getServiceId(),
                requestingUserIdOpt
        );

        ReservationResponseDTO responseDTO = reservationDTOMapper.toResponseDTO(createdReservation);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> getReservationById(
            @PathVariable Long id,
            Authentication authentication) { // Inyectar Authentication
        Optional<Long> requestingUserIdOpt = getAuthenticatedUserId(authentication);
        Optional<Reservation> reservationOpt = reservationService.findReservationById(id, requestingUserIdOpt); // Pasar el ID del solicitante
        return reservationOpt
                .map(reservationDTOMapper::toResponseDTO)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> getAllReservations(
            @RequestParam(required = false) Long userIdParam,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {

        Optional<Long> authenticatedUserIdOpt = getAuthenticatedUserId(authentication);
        Long authenticatedUserId = authenticatedUserIdOpt.orElse(null);
        boolean userIsAdmin = isAdmin(authentication);

        List<Reservation> reservations;

        if (userIdParam != null) {
            if (!userIsAdmin && (authenticatedUserId == null || !userIdParam.equals(authenticatedUserId))) {
                return new ResponseEntity<>(Collections.emptyList(), HttpStatus.FORBIDDEN);
            }
            reservations = reservationService.findReservationsByUserId(userIdParam, authenticatedUserIdOpt);
        } else if (serviceId != null) {
            reservations = reservationService.findReservationsByServiceId(serviceId, authenticatedUserIdOpt);
        } else if (startDate != null && endDate != null) {
            reservations = reservationService.findReservationsByDateRange(startDate, endDate, authenticatedUserIdOpt);
        } else {
            if (userIsAdmin) {
                reservations = reservationService.findAllReservations(authenticatedUserIdOpt); // Podría necesitar el ID del admin
            } else if (authenticatedUserId != null) {
                reservations = reservationService.findReservationsByUserId(authenticatedUserId, authenticatedUserIdOpt);
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
            Authentication authentication) { // Inyectar Authentication

        Optional<Long> requestingUserIdOpt = getAuthenticatedUserId(authentication);
        Reservation updateReservationData = reservationDTOMapper.fromRequestDTO(requestDTO);
        Optional<Reservation> updatedReservationOpt = reservationService.updateReservation(id, updateReservationData, requestingUserIdOpt);

        return updatedReservationOpt
                .map(reservationDTOMapper::toResponseDTO)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id, Authentication authentication) {
        Optional<Long> requestingUserIdOpt = getAuthenticatedUserId(authentication);
        try {
            boolean deleted = reservationService.deleteReservation(id, requestingUserIdOpt);
            if (deleted) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch(AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
