package com.desarrolloweb.spring.app.controllers;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.desarrolloweb.spring.app.repositories.ClienteRepository;
import com.desarrolloweb.spring.app.repositories.ProveedorRepository;
import com.desarrolloweb.spring.app.util.PageRender;
import com.desarrolloweb.spring.app.entities.Audit;
import com.desarrolloweb.spring.app.entities.Proveedor;


@Controller
public class Proveedores {

	@Autowired
	private ClienteRepository clienteRepository;

	@RequestMapping(value = "/detalle-cliente/{id}", method = RequestMethod.GET)
	public String detalleCliente(@PathVariable(value = "id") Long id, Model model) {

		Proveedor cliente = Proveedor.findById(id).get();
		if (cliente == null) {
			return "redirect:/listar-clientes";
		}

		model.addAttribute("titulo", "Detalle Cliente: " + cliente.getNombre());
		model.addAttribute("cliente", cliente);
		return "detalle-cliente-form";
	}
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "/listar-clientes", method = RequestMethod.GET)
	public String listarClientes(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {

		Pageable pageRequest = PageRequest.of(page, 4);

		Page<Proveedor> clientes = ProveedorRepository.findAll(pageRequest);

		PageRender<Proveedor> pageRender = new PageRender<Proveedor>("/listar-clientes", clientes);
		model.addAttribute("titulo", "Listado de clientes");
		model.addAttribute("clientes", clientes);
		model.addAttribute("page", pageRender);
		return "clientes";
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "/nuevo-cliente", method = RequestMethod.GET)
	public String nuevoCliente(Model model) {
		Proveedor cliente = new Proveedor();
		model.addAttribute("titulo", "Nuevo Cliente");
		model.addAttribute("cliente", cliente);
		return "form-cliente";
	}

	@RequestMapping(value = "/editar-cliente/{id}", method = RequestMethod.GET)
	public String editarCliente(@PathVariable(value = "id") Long id, Model model) {
		Proveedor cliente = null;
		if (id > 0) {
			Proveedor = ProveedorRepository.findById(id).get();
		} else {
			return "redirect:/listar-clientes";
		}
		model.addAttribute("titulo", "Editar Cliente");
		model.addAttribute("cliente", cliente);
		return "form-cliente";
	}

	@RequestMapping(value = "/eliminar-cliente/{id}", method = RequestMethod.GET)
	public String eliminarCliente(@PathVariable(value = "id") Long id, Model model) {
		Proveedor Proveedor = null;
		if (id > 0) {
			Proveedor = ProveedorRepository.findById(id).get();
			ProveedorRepository.delete(Proveedor);
		} else {
			return "redirect:/listar-clientes";
		}

		return "redirect:/listar-clientes";
	}

	@RequestMapping(value = "/nuevo-cliente", method = RequestMethod.POST)
	public String guardarCliente(@RequestParam("file") MultipartFile foto, Proveedor cliente) {
		Audit audit = null;
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();



		if (!foto.isEmpty()) {

			try {
				
				String path = "/Users/kc/Desktop/tmp/".concat(foto.getOriginalFilename());

				byte[] bytes = foto.getBytes();
				Path rutaCompleta = Paths.get(path);
				Files.write(rutaCompleta, bytes);
				Proveedor.setFoto(foto.getOriginalFilename());

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (Proveedor.getId() != null && Proveedor.getId() > 0) {
			Proveedor proveedor2 = clienteRepository.findById(Proveedor.getId()).get();
			audit = new Audit(auth.getName());
			Proveedor.setAudit(audit);
			Proveedor.setId(proveedor2.getId());
			Proveedor.getAudit().setTsCreated(proveedor2.getAudit().getTsCreated());
			Proveedor.getAudit().setUsuCreated(proveedor2.getAudit().getUsuCreated());
		} else {
			audit = new Audit(auth.getName());
			Proveedor.setAudit(audit);
		}

		clienteRepository.save(Proveedor);
		return "redirect:/listar-clientes";
	}
	
}
