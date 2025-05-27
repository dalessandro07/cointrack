package com.cibertec.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cibertec.models.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
	Optional<Usuario> findByUsername(String username);

	boolean existsByUsernameAndPassword(String username, String password);
}
