package com.cibertec.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cibertec.models.Usuario;
import com.cibertec.repositories.UsuarioRepository;

@Controller
public class AuthController {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@PostMapping("/login")
	public String postLogin(@RequestParam("username") String username, @RequestParam("password") String password,
			Model model) {
		boolean loginSuccessful = usuarioRepository.existsByUsernameAndPassword(username, password);

		if (!loginSuccessful) {
			model.addAttribute("error", "Usuario o contraseña incorrectos.");
			return "login";
		}

		return "redirect:/";
	}

	// Mostrar el formulario de registro
	@GetMapping("/register")
	public String showRegistrationForm(Model model) {
		model.addAttribute("usuario", new Usuario());
		return "register";
	}

	// Procesar el registro de un nuevo usuario
	@PostMapping("/register")
	public String registerUser(Usuario usuario, Model model) {
		// Verificar que las contraseñas coinciden
		if (!usuario.getPassword().equals(usuario.getConfirmPassword())) {
			model.addAttribute("error", "Las contraseñas no coinciden.");
			return "register";
		}

		// Si las contraseñas coinciden, encriptamos la contraseña y guardamos en la BD
		usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
		usuario.setRol("USER");
		usuarioRepository.save(usuario);
		return "redirect:/login";
	}
}
