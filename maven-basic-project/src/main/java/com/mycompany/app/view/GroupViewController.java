package com.mycompany.app.view;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mycompany.app.dto.AddUserToGroupDTO;
import com.mycompany.app.dto.DeleteGroupDTO;
import com.mycompany.app.dto.GroupCreationDTO;
import com.mycompany.app.dto.GroupInfoDTO;
import com.mycompany.app.dto.RemoveUserFromGroupDTO;
import com.mycompany.app.model.Deuda;
import com.mycompany.app.model.EstadoDeuda;
import com.mycompany.app.model.Transaction;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.DeudaRepository;
import com.mycompany.app.repository.TransactionRepository;
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
    private final TransactionRepository transactionRepository;
    private final DeudaRepository deudaRepository;

    public GroupViewController(GroupService groupService,
                                UsuarioRepository usuarioRepository,
                                AuthService authService,
                                TransactionRepository transactionRepository,
                                DeudaRepository deudaRepository) {
        this.groupService = groupService;
        this.usuarioRepository = usuarioRepository;
        this.authService = authService;
        this.transactionRepository = transactionRepository;
        this.deudaRepository = deudaRepository;
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

        Map<Integer, List<Transaction>> groupTransactions = new HashMap<>();
        Map<Integer, List<Deuda>>      txDebts            = new HashMap<>();
        Map<Integer, Double>           groupNetBalance     = new HashMap<>();

        for (GroupInfoDTO grupo : listaGrupos) {
            List<Transaction> allGastos = transactionRepository.findByGrupoId(grupo.getId())
                    .stream()
                    .filter(t -> "GASTO".equals(t.getTipoTransaccion()))
                    .sorted(Comparator.comparing(Transaction::getFecha,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());

            List<Transaction> recent = allGastos.stream().limit(5).collect(Collectors.toList());
            Set<Integer> recentIds = recent.stream().map(Transaction::getId).collect(Collectors.toSet());
            groupTransactions.put(grupo.getId(), recent);

            double net = 0.0;
            for (Transaction tx : allGastos) {
                List<Deuda> debts = deudaRepository.findByTransaccionOriginalId(tx.getId());
                for (Deuda d : debts) {
                    if (EstadoDeuda.PENDIENTE.equals(d.getEstado())) {
                        if (d.getAcreedor().getId().equals(user.getId())) net += d.getImporte();
                        else if (d.getDeudor().getId().equals(user.getId())) net -= d.getImporte();
                    }
                }
                if (recentIds.contains(tx.getId())) txDebts.put(tx.getId(), debts);
            }
            groupNetBalance.put(grupo.getId(), Math.round(net * 100.0) / 100.0);
        }

        model.addAttribute("listaGrupos", listaGrupos);
        model.addAttribute("showPanel", Boolean.TRUE.equals(showPanel));
        model.addAttribute("groupTransactions", groupTransactions);
        model.addAttribute("txDebts", txDebts);
        model.addAttribute("groupNetBalance", groupNetBalance);
        model.addAttribute("currentUserId", user.getId());

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