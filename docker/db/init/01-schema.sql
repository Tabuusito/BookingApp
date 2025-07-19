DROP DATABASE reservas;
CREATE DATABASE reservas;
USE reservas;

CREATE TABLE usuarios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    public_uuid BINARY(16) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_name VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_users_uuid (public_uuid),
	INDEX idx_users_username (username)

);

CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500)
);

CREATE TABLE usuario_rol (
    usuario_id BIGINT NOT NULL,
    rol_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),

    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE offered_services (
    service_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    public_uuid BINARY(16) NOT NULL UNIQUE,
    owner_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    default_duration_seconds BIGINT NOT NULL,
    price_per_reservation DECIMAL(10, 2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_service_owner FOREIGN KEY (owner_id) REFERENCES usuarios(id),

	INDEX idx_offered_service_public_uuid (public_uuid),
    INDEX idx_offered_service_name (name)
);

CREATE TABLE reservations (
    reservation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    public_uuid BINARY(16) NOT NULL UNIQUE,
    owner_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_reservation_owner FOREIGN KEY (owner_id) REFERENCES usuarios(id),
    CONSTRAINT fk_reservation_service FOREIGN KEY (service_id) REFERENCES offered_services(service_id),

    INDEX idx_reservation_owner_id (owner_id),
    INDEX idx_reservation_service_id (service_id),
    INDEX idx_reservation_start_time (start_time),
    INDEX idx_reservation_end_time (end_time)



);
    INSERT INTO roles (role_name, description) VALUES ('ROLE_USER', 'Rol para usuarios estándar con acceso básico.');

    INSERT INTO roles (role_name, description) VALUES ('ROLE_ADMIN', 'Rol para administradores con acceso total y privilegios de gestión.');


