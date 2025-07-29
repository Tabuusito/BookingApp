DROP DATABASE IF EXISTS reservas;
CREATE DATABASE reservas;
USE reservas;

-- -----------------------------------------------------
-- Tabla `usuarios`
-- -----------------------------------------------------
CREATE TABLE usuarios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    public_uuid BINARY(16) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_users_uuid (public_uuid),
    INDEX idx_users_username (username)
);

-- -----------------------------------------------------
-- Tabla `user_roles`
-- -----------------------------------------------------
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role_name),

    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- -----------------------------------------------------
-- Tabla `offered_services`
-- -----------------------------------------------------
CREATE TABLE offered_services (
    service_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    public_uuid BINARY(16) NOT NULL UNIQUE,
    owner_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    default_duration_seconds BIGINT NOT NULL,
    price_per_reservation DECIMAL(10, 2),
    capacity INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_service_owner FOREIGN KEY (owner_id) REFERENCES usuarios(id) ON DELETE CASCADE,

    INDEX idx_offered_service_public_uuid (public_uuid),
    INDEX idx_offered_service_name (name)
);

-- -----------------------------------------------------
-- Tabla `time_slots`
-- Representa los eventos/slots disponibles en el calendario.
-- -----------------------------------------------------
CREATE TABLE time_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    public_uuid BINARY(16) NOT NULL UNIQUE,
    service_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    capacity INT NOT NULL,
    price DECIMAL(10, 2),
    status VARCHAR(50) NOT NULL, -- e.g., AVAILABLE, FULL, CANCELLED

    CONSTRAINT fk_timeslot_service FOREIGN KEY (service_id) REFERENCES offered_services(service_id) ON DELETE CASCADE,

    INDEX idx_timeslot_public_uuid (public_uuid),
    INDEX idx_timeslot_service_id (service_id),
    INDEX idx_timeslot_start_time (start_time)
);


-- -----------------------------------------------------
-- Tabla `bookings`
-- -----------------------------------------------------
CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    public_uuid BINARY(16) NOT NULL UNIQUE,
    timeslot_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    price_paid DECIMAL(10, 2),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_booking_timeslot FOREIGN KEY (timeslot_id) REFERENCES time_slots(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_client FOREIGN KEY (client_id) REFERENCES usuarios(id) ON DELETE CASCADE,

    -- Un cliente no puede reservar dos veces el mismo slot.
    UNIQUE KEY uq_client_timeslot (client_id, timeslot_id),

    INDEX idx_booking_public_uuid (public_uuid),
    INDEX idx_booking_client_id (client_id)
);