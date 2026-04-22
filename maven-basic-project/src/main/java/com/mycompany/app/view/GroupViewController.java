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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
    public String groups(
            @CookieValue(value = "token", required = false) String token,
            @RequestParam(required = false) Boolean showPanel,
            Model model
    ) {
        if (token == null || !authService.isValidToken(token)) return "redirect:/web/auth/login";

        String email = authService.getEmailFromToken(token);
        Usuario user = usuarioRepository.findByEmail(email);
        if (user == null) return "redirect:/web/auth/login";

        List<GroupInfoDTO> listaGrupos = groupService.getUserGroups(user.getId(), token);
        model.addAttribute("listaGrupos", listaGrupos);
        model.addAttribute("showPanel", Boolean.TRUE.equals(showPanel));

        return "groups";
    }

    @PostMapping("/create")
    public String createGroup(
            @CookieValue(value = "token", required = false) String token,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            RedirectAttributes redirectAttributes
    ) {
        if (token == null || !authService.isValidToken(token)) return "redirect:/web/auth/login";

        if (nombre == null || nombre.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Group name cannot be empty.");
            return "redirect:/web/groups";
        }

        try {
            GroupCreationDTO dto = new GroupCreationDTO(token, nombre.trim(),
                    descripcion != null ? descripcion.trim() : "");
            groupService.createGroup(dto);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not create group: " + e.getMessage());
        }
        return "redirect:/web/groups";
    }

    @PostMapping("/addMember")
    public String addMember(
            @CookieValue(value = "token", required = false) String token,
            @RequestParam Integer groupId,
            @RequestParam String userEmail,
            RedirectAttributes redirectAttributes
    ) {
        if (token == null || !authService.isValidToken(token)) return "redirect:/web/auth/login";

        try {
            AddUserToGroupDTO dto = new AddUserToGroupDTO(token, groupId, userEmail.trim());
            boolean added = groupService.addUserToGroup(dto);
            if (!added) {
                redirectAttributes.addFlashAttribute("error", "'" + userEmail + "' is already a member of this group.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not add user: " + e.getMessage());
        }
        return "redirect:/web/groups";
    }

    @PostMapping("/removeMember")
    public String removeMember(
            @CookieValue(value = "token", required = false) String token,
            @RequestParam Integer groupId,
            @RequestParam String userEmail,
            RedirectAttributes redirectAttributes
    ) {
        if (token == null || !authService.isValidToken(token)) return "redirect:/web/auth/login";

        try {
            RemoveUserFromGroupDTO dto = new RemoveUserFromGroupDTO(token, groupId, userEmail);
            groupService.removeUserFromGroup(dto);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not remove user: " + e.getMessage());
        }
        return "redirect:/web/groups";
    }

    //HTML forms do not support DELETE functions, so this has to be a POST
    @PostMapping("/delete")
    public String deleteGroup(
            @CookieValue(value = "token", required = false) String token,
            @RequestParam Integer groupId,
            RedirectAttributes redirectAttributes
    ) {
        if (token == null || !authService.isValidToken(token)) return "redirect:/web/auth/login";

        try {
            DeleteGroupDTO dto = new DeleteGroupDTO(token, groupId);
            groupService.deleteGroup(dto);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Could not delete group: " + e.getMessage());
        }
        return "redirect:/web/groups";
    }
}