package com.cibertec.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cibertec.models.Transacciones;

public interface TransaccionesRepository extends JpaRepository<Transacciones, Integer> {
	List<Transacciones> findByUsuarioId(Integer usuarioId);

	Optional<Transacciones> findByIdAndUsuarioId(Integer id, Integer usuarioId);
}
