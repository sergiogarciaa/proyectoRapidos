package com.biblioteca.controladores;

import java.util.List;

import org.apache.coyote.http11.Http11Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;

import com.biblioteca.dao.Usuario;
import com.biblioteca.dto.UsuarioDTO;
import com.biblioteca.servicios.IUsuarioServicio;

/**
 * Clase que ejerce de controlador de la vista de login/registro para gestionar las
 * solicitudes relacionadas con la autenticación y registro de usuarios.
 */
@Controller
public class LoginControlador {

	@Autowired
	private IUsuarioServicio usuarioServicio;

	/**
	 * Gestiona la solicitud HTTP GET para /auth/login y muestra la página de inicio de sesión
	 * @param model Modelo que se utiliza para enviar un usuarioDTO vacio a la vista.
	 * @return La vista de inicio de sesión (login.html).
	 */
	@GetMapping("/auth/login")
	public String login(Model model) {
		// Se agrega un nuevo objeto UsuarioDTO al modelo para el formulario de login
		model.addAttribute("usuarioDTO", new UsuarioDTO());
		return "login";
	}

	/**
	 * Gestiona la solicitud HTTP GET para mostrar la página de registro.
	 * @param model Modelo que se utiliza para enviar un usuarioDTO vacio a la vista.
	 * @return La vista de registro de usuario (registrar.html).
	 */
	@GetMapping("/auth/registrar")
	public String registrarGet(Model model) {
		model.addAttribute("usuarioDTO", new UsuarioDTO());
		return "registro";
	}

	/**
	 * Procesa la solicitud HTTP POST para registro de un nuevo usuario.
	 * @param  usuarioDTO El objeto UsuarioDTO que recibe en el modelo y contiene los
	 *         datos del nuevo usuario.
	 * @return La vista de inicio de sesión (login.html) si fue exitoso el registro; 
	 * 		   de lo contrario, la vista de registro de usuario (registro.html).
	 */
	@PostMapping("/auth/registrar")
	public String registrarPost(@ModelAttribute UsuarioDTO usuarioDTO, Model model) {

		UsuarioDTO nuevoUsuario = usuarioServicio.registrar(usuarioDTO);
		
		if (nuevoUsuario != null && nuevoUsuario.getDniUsuario() != null) {
			// Si el usuario y el DNI no son null es que el registro se completo correctamente
			model.addAttribute("mensajeRegistroExitoso", "Registro del nuevo usuario OK");
			return "login";
		} else {
			// Se verifica si el DNI ya existe para mostrar error personalizado en la vista
			if (usuarioDTO.getDniUsuario() == null) {
				model.addAttribute("mensajeErrorDni", "Ya existe un usuario con ese DNI");
				return "registro";
			} else {
				model.addAttribute("mensajeErrorMail", "Ya existe un usuario con ese email");
				return "registro";
			}
		}
	}

	/**
	 * Gestiona la solicitud HTTP GET para llevar a la página de home una vez logeado con exito.
	 * @return La vista de home.html
	 */
	@GetMapping("/privada/home")
	public String loginCorrecto(Model model, Authentication authentication) {
		Usuario usuario = usuarioServicio.buscarPorEmail(authentication.getName());
		String nombreUsuario = usuario.getNombreUsuario() + " " + usuario.getApellidosUsuario();
		model.addAttribute("nombreUsuario", nombreUsuario);
		// Agregar información sobre si el usuario es administrador al modelo
	    model.addAttribute("isAdmin", usuario.getAdmin());
		return "home";
	}
	
	/**
	 * Gestiona la solicitud HTTP GET para llevar a la página de administracion una vez logeado con exito.
	 * @return La vista de administracion.html
	 */
	
	@GetMapping("/privada/administracion")
	public String adminCorrecto(Model model, Authentication authentication) {
		Usuario usuario = usuarioServicio.buscarPorEmail(authentication.getName());
		// Sacar nombre del usuario logeado
		String nombreUsuario = usuario.getNombreUsuario();
		model.addAttribute("nombreUsuario", nombreUsuario);
		// Sacar si es administrador
		model.addAttribute("isAdmin", usuario.getAdmin());
	
		// Sacar lista de usuario
		List<UsuarioDTO> usuarios = usuarioServicio.buscarTodos();
        model.addAttribute("usuarios", usuarios);
		return "administracion";
	}
	
	/**
	 * Gestiona la solicitud HTTP Post para actualizar los roles
	 * @return Cambio de rol
	 */
	
	@PostMapping("/privada/administracion/cambiarRol")
	public String cambiarRol(@RequestParam String emailUsuario, Model model, Authentication authentication) {
	    boolean exito = usuarioServicio.cambiarRolAdminPorEmail(emailUsuario);

	    if (exito) {
	        // Actualiza la lista de usuarios después de cambiar el rol de administrador
	        List<UsuarioDTO> usuarios = usuarioServicio.buscarTodos();
	        model.addAttribute("usuarios", usuarios);
	        return "administracion";
	    } else {
	        return "error";
	    }
	}
}
