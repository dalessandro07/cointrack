package com.cibertec.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.cibertec.services.UsuarioService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final UsuarioService usuarioService;

	public SecurityConfig(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// Configura la seguridad HTTP
		http.authorizeHttpRequests(
				auth -> auth.requestMatchers("/transacciones/**").authenticated().anyRequest().permitAll())
				.formLogin(form -> form.loginPage("/login").permitAll().failureUrl("/login?error")
						.defaultSuccessUrl("/").usernameParameter("username").passwordParameter("password"))
				.logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout").permitAll());

		http.authenticationManager(authenticationManager(http));

		return http.build();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder authenticationManagerBuilder = http
				.getSharedObject(AuthenticationManagerBuilder.class);

		authenticationManagerBuilder.userDetailsService(usuarioService).passwordEncoder(passwordEncoder());

		return authenticationManagerBuilder.build();
	}
}
