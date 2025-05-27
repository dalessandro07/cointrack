package com.cibertec.controllers;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cibertec.models.Transacciones;
import com.cibertec.models.Usuario;
import com.cibertec.services.ExportarExcelService;
import com.cibertec.services.TransaccionesService;
import com.cibertec.services.UsuarioService;

@Controller
@RequestMapping("/transacciones")
public class TransaccionesController {

	private final TransaccionesService transaccionesService;
	private final UsuarioService usuarioService;
	private final ExportarExcelService excelExportService;

	public TransaccionesController(TransaccionesService transaccionesService, UsuarioService usuarioService,
			ExportarExcelService excelExportService) {
		this.transaccionesService = transaccionesService;
		this.usuarioService = usuarioService;
		this.excelExportService = excelExportService;
	}

	@GetMapping
	public String getAllTransacciones(Model model) {
		try {
			Usuario usuario = obtenerUsuarioAutenticado();
			Integer usuarioId = usuario.getId();

			List<Transacciones> transacciones = transaccionesService.getAllTransacciones(usuarioId);

			double totalIngresos = transacciones.stream().filter(t -> t.getTipo().toString().equals("INGRESO"))
					.mapToDouble(t -> t.getMonto().doubleValue()).sum();

			double totalGastos = transacciones.stream().filter(t -> t.getTipo().toString().equals("GASTO"))
					.mapToDouble(t -> t.getMonto().doubleValue()).sum();

			double balance = totalIngresos - totalGastos;

			model.addAttribute("usuarioId", usuarioId);
			model.addAttribute("transacciones", transacciones);
			model.addAttribute("totalIngresos", totalIngresos);
			model.addAttribute("totalGastos", totalGastos);
			model.addAttribute("balance", balance);

			return "transacciones/list";
		} catch (Exception e) {
			model.addAttribute("error", "Error al obtener las transacciones: " + e.getMessage());
			return "transacciones/list";
		}
	}

	// Exportar transacciones en excel
	@GetMapping("/export")
	public ResponseEntity<byte[]> exportTransacciones(@RequestParam Integer usuarioId, Model model) {
		try {
			List<Transacciones> transacciones = transaccionesService.getAllTransacciones(usuarioId);
			ByteArrayOutputStream excelFile = excelExportService.exportTransaccionesToExcel(transacciones);

			// Configurar los encabezados para la descarga del archivo
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Disposition", "attachment; filename=CoinTrack-Transacciones.xlsx");

			return ResponseEntity.ok().headers(headers).body(excelFile.toByteArray());
		} catch (Exception e) {
			System.out.println("Error al exportar las transacciones: " + e.getMessage());
			model.addAttribute("error", "Error al exportar las transacciones: " + e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/new")
	public String newTransaccionForm(Model model) {
		try {
			Transacciones transaccion = new Transacciones();
			transaccion.setFecha(LocalDate.now());

			model.addAttribute("actionUrl", "/transacciones/new");
			model.addAttribute("formTitle", "Nueva transacción");
			model.addAttribute("transaccion", transaccion);
			return "transacciones/form";
		} catch (Exception e) {
			model.addAttribute("error", "Error al mostrar el formulario: " + e.getMessage());
			return "transacciones/list";
		}
	}

	@PostMapping("/new")
	public String createTransaccion(@ModelAttribute Transacciones transaccion, Model model) {
		try {
			Usuario usuario = obtenerUsuarioAutenticado();
			transaccion.setUsuario(usuario);

			transaccionesService.createTransaccion(transaccion);
			return "redirect:/transacciones";
		} catch (Exception e) {
			model.addAttribute("error", "Error al crear la transacción: " + e.getMessage());
			model.addAttribute("transaccion", transaccion);
			return "transacciones/form";
		}
	}

	@GetMapping("/{id}/edit")
	public String editTransaccionForm(@PathVariable Integer id, Model model) {
		try {
			Usuario usuario = obtenerUsuarioAutenticado();
			int usuarioId = usuario.getId();

			Transacciones transaccion = transaccionesService.getTransaccionById(id, usuarioId)
					.orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

			model.addAttribute("actionUrl", "/transacciones/" + transaccion.getId() + "/edit");
			model.addAttribute("formTitle", "Editar transacción");
			model.addAttribute("transaccion", transaccion);
			return "transacciones/form";
		} catch (Exception e) {
			model.addAttribute("error", "Error al cargar la transacción para edición: " + e.getMessage());
			return "transacciones/list";
		}
	}

	@PostMapping("/{id}/edit")
	public String updateTransaccion(@PathVariable Integer id, @ModelAttribute Transacciones transaccion, Model model) {
		try {
			System.out.println("ID TRANSACCION: " + transaccion.getId());

			Usuario usuario = obtenerUsuarioAutenticado();
			transaccion.setUsuario(usuario);

			transaccion.setId(id);
			transaccionesService.updateTransaccion(transaccion);
			return "redirect:/transacciones";
		} catch (Exception e) {
			model.addAttribute("error", "Error al actualizar la transacción: " + e.getMessage());
			model.addAttribute("transaccion", transaccion);
			return "transacciones/form";
		}
	}

	@GetMapping("/{id}/delete")
	public String deleteTransaccion(@PathVariable Integer id, Model model) {
		try {
			transaccionesService.deleteTransaccion(id);
			return "redirect:/transacciones";
		} catch (Exception e) {
			model.addAttribute("error", "Error al eliminar la transacción: " + e.getMessage());
			return "transacciones/list";
		}
	}

	// Helpers personalizados
	private Usuario obtenerUsuarioAutenticado() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.getPrincipal() instanceof User) {
			User user = (User) authentication.getPrincipal();
			String username = user.getUsername();

			return usuarioService.findByUsername(username).orElseThrow(
					() -> new IllegalStateException("Usuario no encontrado para el username: " + username));
		}
		throw new IllegalStateException("No se encontró un usuario autenticado.");
	}
}
