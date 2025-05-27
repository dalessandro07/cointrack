package com.cibertec.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cibertec.models.Usuario;
import com.cibertec.repositories.UsuarioRepository;

@Service
public class UsuarioService implements org.springframework.security.core.userdetails.UserDetailsService {

	@Autowired
	private final UsuarioRepository usuarioRepository;

	public UsuarioService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario usuario = usuarioRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

		// Establece el usuarioId en el contexto de seguridad
		Authentication authentication = new UsernamePasswordAuthenticationToken(usuario, null);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		return User.builder().username(usuario.getUsername()).password(usuario.getPassword()).roles(usuario.getRol())
				.build();
	}

	public Optional<Usuario> findByUsername(String username) {
		return usuarioRepository.findByUsername(username);
	}
}
