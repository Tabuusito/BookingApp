package infrastructure.adapter.in.web.controller;

import domain.model.OfferedService;
import domain.port.in.OfferedServiceService;
import infrastructure.adapter.in.web.dto.OfferedServiceResponseDTO;
import infrastructure.adapter.in.web.mapper.OfferedServiceDTOMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceDiscoveryController {

    private final OfferedServiceService offeredServiceService;
    private final OfferedServiceDTOMapper offeredServiceMapper;

    /**
     * Busca servicios por diferentes criterios como categoría, etc.
     * En esta versión inicial, solo mostramos un placeholder.
     * @param category La categoría por la cual filtrar (ej. "matematicas", "musica").
     * @return Una lista de servicios que coinciden con los filtros.
     */
    @GetMapping
    public ResponseEntity<List<OfferedServiceResponseDTO>> findServices(
            @RequestParam(required = false) String category) {

        List<OfferedService> services;

        // Lógica a implementar en la Fase 2
        if (category != null && !category.isBlank()) {
            // services = offeredServiceService.findAllActiveServicesByCategory(category);
            // Esto requeriría añadir el método `findAllActiveServicesByCategory` al servicio.
            // Por ahora, devolvemos no implementado.
            return ResponseEntity.status(501).build();
        } else {
            // Si no hay filtro, podríamos devolver los servicios más populares, o nada.
            // services = offeredServiceService.findPopularServices();
            return ResponseEntity.status(501).build();
        }

        /*
        List<OfferedServiceResponseDTO> response = services.stream()
                .map(offeredServiceMapper::toResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
        */
    }
}