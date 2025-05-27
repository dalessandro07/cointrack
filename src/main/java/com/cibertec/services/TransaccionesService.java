package com.cibertec.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cibertec.models.Transacciones;
import com.cibertec.repositories.TransaccionesRepository;

@Service
public class TransaccionesService {

	private final TransaccionesRepository transaccionesRepository;

	public TransaccionesService(TransaccionesRepository transaccionesRepository) {
		this.transaccionesRepository = transaccionesRepository;
	}

	public List<Transacciones> getAllTransacciones(Integer usuarioId) {
		return transaccionesRepository.findByUsuarioId(usuarioId);
	}

	public Optional<Transacciones> getTransaccionById(Integer id, Integer usuarioId) {
		return transaccionesRepository.findByIdAndUsuarioId(id, usuarioId);
	}

	public Transacciones createTransaccion(Transacciones transaccion) {
		if (transaccion.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("El monto debe ser mayor que cero");
		}

		transaccion.setFechaCreacion(LocalDateTime.now());
		transaccion.setFechaActualizacion(LocalDateTime.now());
		return transaccionesRepository.save(transaccion);
	}

	public Transacciones updateTransaccion(Transacciones transaccion) {
		if (transaccion.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("El monto debe ser mayor que cero");
		}

		transaccion.setFechaActualizacion(LocalDateTime.now());
		return transaccionesRepository.save(transaccion);
	}

	public void deleteTransaccion(Integer id) {
		transaccionesRepository.deleteById(id);
	}

	public BigDecimal getTotalIngresos(Integer usuarioId) {
		return transaccionesRepository.findByUsuarioId(usuarioId).stream()
				.filter(t -> t.getTipo() == Transacciones.TipoTransaccion.INGRESO).map(Transacciones::getMonto)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal getTotalGastos(Integer usuarioId) {
		return transaccionesRepository.findByUsuarioId(usuarioId).stream()
				.filter(t -> t.getTipo() == Transacciones.TipoTransaccion.GASTO).map(Transacciones::getMonto)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}
