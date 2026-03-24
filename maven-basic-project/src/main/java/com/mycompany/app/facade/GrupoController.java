package com.mycompany.app.facade;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.app.dto.GrupoCreationDTO;
import com.mycompany.app.dto.GrupoMemberDTO;
import com.mycompany.app.model.Grupo;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.service.AuthService;
import com.mycompany.app.service.GrupoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/grupo")
public class GrupoController {

    private final GrupoService grupoService;
    private final AuthService authService;

    public GrupoController(GrupoService grupoService, AuthService authService) {
        this.grupoService = grupoService;
        this.authService = authService;
    }

    @Operation(
        summary = "Create group",
        description = "Creates a new group with the authenticated user as creator and first member",
        responses = {
            @ApiResponse(responseCode = "201", description = "Group created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request: group could not be created"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: invalid token")
        }
    )
    @PostMapping("/create")
    public ResponseEntity<String> createGrupo(@RequestBody GrupoCreationDTO request) {
        if (!authService.isValidToken(request.getToken())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        boolean result = grupoService.createGrupo(request);
        if (result) {
            return new ResponseEntity<>(HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(
        summary = "Add member to group",
        description = "Registers a user into an existing group",
        responses = {
            @ApiResponse(responseCode = "200", description = "Member added successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request: member could not be added"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: invalid token")
        }
    )
    @PostMapping("/addMember")
    public ResponseEntity<String> addMember(@RequestBody GrupoMemberDTO request) {
        if (!authService.isValidToken(request.getToken())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        boolean result = grupoService.addMember(request);
        if (result) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(
        summary = "Remove member from group",
        description = "Removes a user from an existing group",
        responses = {
            @ApiResponse(responseCode = "200", description = "Member removed successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request: member could not be removed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: invalid token")
        }
    )
    @PostMapping("/removeMember")
    public ResponseEntity<String> removeMember(@RequestBody GrupoMemberDTO request) {
        if (!authService.isValidToken(request.getToken())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        boolean result = grupoService.removeMember(request);
        if (result) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(
        summary = "Get group members",
        description = "Retrieves all members of a group",
        responses = {
            @ApiResponse(responseCode = "200", description = "Members retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: invalid token"),
            @ApiResponse(responseCode = "404", description = "Group not found")
        }
    )
    @GetMapping("/{grupoId}/members")
    public ResponseEntity<Set<Usuario>> getGroupMembers(
            @PathVariable("grupoId") Integer grupoId,
            @RequestParam("token") String token) {
        if (!authService.isValidToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Set<Usuario> members = grupoService.getGroupMembers(grupoId);
        if (members == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(members);
    }

    @Operation(
        summary = "Get user groups",
        description = "Retrieves all groups a user belongs to",
        responses = {
            @ApiResponse(responseCode = "200", description = "Groups retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: invalid token")
        }
    )
    @GetMapping("/user/{usuarioId}")
    public ResponseEntity<List<Grupo>> getUserGroups(
            @PathVariable("usuarioId") Integer usuarioId,
            @RequestParam("token") String token) {
        if (!authService.isValidToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<Grupo> groups = grupoService.getUserGroups(usuarioId);
        return ResponseEntity.ok(groups);
    }
}
