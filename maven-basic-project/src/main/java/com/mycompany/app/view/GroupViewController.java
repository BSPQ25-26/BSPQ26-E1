package com.mycompany.app.view;

import java.util.List;

import com.mycompany.app.dto.AddUserToGroupDTO;
import com.mycompany.app.dto.DeleteGroupDTO;
import com.mycompany.app.dto.GroupCreationDTO;
import com.mycompany.app.dto.GroupInfoDTO;
import com.mycompany.app.dto.RemoveUserFromGroupDTO;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.UsuarioRepository;
import com.mycompany.app.service.AuthService;
import com.mycompany.app.service.GroupService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/groups")
public class GroupViewController {

    private final GroupService groupService;
    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;

    public GroupViewController(GroupService groupService,
                                UsuarioRepository usuarioRepository,
                                AuthService authService) {
        this.groupService = groupService;
        this.usuarioRepository = usuarioRepository;
        this.authService = authService;
    }

    @GetMapping
    public String groups(@RequestParam(required = false) String token,
                         @RequestParam(required = false) Boolean showPanel,
                         HttpSession session, Model model) {
        if (token != null) {
            session.setAttribute("token", token);
        }
        String storedToken = (String) session.getAttribute("token");
        if (storedToken == null || !authService.isValidToken(storedToken)) {
            return "redirect:/web/auth/login";
        }

        String email = authService.getEmailFromToken(storedToken);
        session.setAttribute("userEmail", email);

        Usuario user = usuarioRepository.findByEmail(email);
        if (user == null) return "redirect:/web/auth/login";
        session.setAttribute("userId", user.getId());

        List<GroupInfoDTO> listaGrupos = groupService.getUserGroups(user.getId(), storedToken);
        model.addAttribute("listaGrupos", listaGrupos);
        model.addAttribute("showPanel", Boolean.TRUE.equals(showPanel));

        return "groups";
    }

    @PostMapping("/create")
    public String createGroup(
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String storedToken = (String) session.getAttribute("token");
        if (storedToken == null) return "redirect:/web/auth/login";

        if (nombre == null || nombre.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "El nombre del grupo no puede estar vacío.");
            return "redirect:/web/groups";
        }

        try {
            GroupCreationDTO dto = new GroupCreationDTO(storedToken, nombre.trim(),
                    descripcion != null ? descripcion.trim() : "");
            groupService.createGroup(dto);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo crear el grupo: " + e.getMessage());
        }
        return "redirect:/web/groups";
    }

    @PostMapping("/addMember")
    public String addMember(
            @RequestParam Integer groupId,
            @RequestParam String userEmail,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String storedToken = (String) session.getAttribute("token");
        if (storedToken == null) return "redirect:/web/auth/login";

        try {
            AddUserToGroupDTO dto = new AddUserToGroupDTO(storedToken, groupId, userEmail.trim());
            boolean added = groupService.addUserToGroup(dto);
            if (!added) {
                redirectAttributes.addFlashAttribute("error", "'" + userEmail + "' ya es miembro del grupo.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo añadir al usuario: " + e.getMessage());
        }
        return "redirect:/web/groups";
    }

    @PostMapping("/removeMember")
    public String removeMember(
            @RequestParam Integer groupId,
            @RequestParam String userEmail,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String storedToken = (String) session.getAttribute("token");
        if (storedToken == null) return "redirect:/web/auth/login";

        try {
            RemoveUserFromGroupDTO dto = new RemoveUserFromGroupDTO(storedToken, groupId, userEmail);
            groupService.removeUserFromGroup(dto);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo expulsar al usuario: " + e.getMessage());
        }
        return "redirect:/web/groups";
    }

    @PostMapping("/delete")
    public String deleteGroup(
            @RequestParam Integer groupId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String storedToken = (String) session.getAttribute("token");
        if (storedToken == null) return "redirect:/web/auth/login";

        try {
            DeleteGroupDTO dto = new DeleteGroupDTO(storedToken, groupId);
            groupService.deleteGroup(dto);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el grupo: " + e.getMessage());
        }
        return "redirect:/web/groups";
    }
}
