package com.mycompany.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.mycompany.app.dto.AddUserToGroupDTO;
import com.mycompany.app.dto.DeleteGroupDTO;
import com.mycompany.app.dto.GroupCreationDTO;
import com.mycompany.app.dto.GroupInfoDTO;
import com.mycompany.app.dto.RemoveUserFromGroupDTO;
import com.mycompany.app.dto.UpdateGroupDTO;
import com.mycompany.app.exception.AuthException;
import com.mycompany.app.model.Group;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.GroupRepository;
import com.mycompany.app.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private GroupService groupService;

    @Test
    void createGroup_WithInvalidToken_ThrowsUnauthorized() {
        GroupCreationDTO dto = new GroupCreationDTO("bad-token", "Trip", "Trip expenses");
        when(authService.getEmailFromToken("bad-token")).thenReturn(null);

        AuthException ex = assertThrows(AuthException.class, () -> groupService.createGroup(dto));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("Invalid token", ex.getMessage());
    }

    @Test
    void createGroup_WithValidData_ReturnsGroupInfo() {
        GroupCreationDTO dto = new GroupCreationDTO("ok-token", "Trip", "Trip expenses");
        Usuario creator = new Usuario("Ana", "ana@mail.com", "pwd", 100.0);
        creator.setId(1);

        Group saved = new Group("Trip", "Trip expenses");
        saved.setId(10);
        saved.setMiembros(Set.of(creator));

        when(authService.getEmailFromToken("ok-token")).thenReturn("ana@mail.com");
        when(usuarioRepository.findByEmail("ana@mail.com")).thenReturn(creator);
        when(groupRepository.save(org.mockito.ArgumentMatchers.any(Group.class))).thenReturn(saved);

        GroupInfoDTO result = groupService.createGroup(dto);

        assertEquals(10, result.getId());
        assertEquals("Trip", result.getNombre());
        assertEquals(1, result.getNumeroMiembros());
        assertEquals(List.of("ana@mail.com"), result.getMiembrosEmails());
    }

    @Test
    void addUserToGroup_WhenAlreadyMember_ReturnsFalse() {
        AddUserToGroupDTO dto = new AddUserToGroupDTO("ok-token", 3, "bob@mail.com");
        Usuario member = new Usuario("Bob", "bob@mail.com", "pwd", 0.0);
        Group group = new Group("Group", "Desc");
        group.setMiembros(Set.of(member));

        when(authService.getEmailFromToken("ok-token")).thenReturn("owner@mail.com");
        when(groupRepository.findByIdWithMiembros(3)).thenReturn(Optional.of(group));
        when(usuarioRepository.findByEmail("bob@mail.com")).thenReturn(member);

        boolean result = groupService.addUserToGroup(dto);

        assertFalse(result);
    }

    @Test
    void removeUserFromGroup_WhenLastMember_ThrowsBadRequest() {
        RemoveUserFromGroupDTO dto = new RemoveUserFromGroupDTO("ok-token", 3, "bob@mail.com");
        Usuario member = new Usuario("Bob", "bob@mail.com", "pwd", 0.0);
        Group group = new Group("Group", "Desc");
        group.setMiembros(Set.of(member));

        when(authService.getEmailFromToken("ok-token")).thenReturn("owner@mail.com");
        when(groupRepository.findByIdWithMiembros(3)).thenReturn(Optional.of(group));
        when(usuarioRepository.findByEmail("bob@mail.com")).thenReturn(member);

        AuthException ex = assertThrows(AuthException.class, () -> groupService.removeUserFromGroup(dto));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals("Cannot remove the last member from a group", ex.getMessage());
    }

    @Test
    void updateGroup_WithBlankName_KeepsOldNameAndUpdatesDescription() {
        UpdateGroupDTO dto = new UpdateGroupDTO("ok-token", 9, "   ", "Updated");
        Group group = new Group("Original", "Initial");

        when(authService.getEmailFromToken("ok-token")).thenReturn("owner@mail.com");
        when(groupRepository.findByIdWithMiembros(9)).thenReturn(Optional.of(group));
        when(groupRepository.save(group)).thenReturn(group);

        GroupInfoDTO result = groupService.updateGroup(dto);

        assertEquals("Original", result.getNombre());
        assertEquals("Updated", result.getDescripcion());
    }

    @Test
    void deleteGroup_WhenGroupNotFound_ThrowsNotFound() {
        DeleteGroupDTO dto = new DeleteGroupDTO("ok-token", 90);
        when(authService.getEmailFromToken("ok-token")).thenReturn("owner@mail.com");
        when(groupRepository.findByIdWithMiembros(90)).thenReturn(Optional.empty());

        AuthException ex = assertThrows(AuthException.class, () -> groupService.deleteGroup(dto));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("Group not found", ex.getMessage());
    }

    @Test
    void getUserGroups_WithValidTokenAndExistingUser_ReturnsGroups() {
        Usuario user = new Usuario("Ana", "ana@mail.com", "pwd", 0.0);
        Group group = new Group("Trip", "desc");
        group.setId(1);
        group.setMiembros(Set.of(user));

        when(authService.getEmailFromToken("ok-token")).thenReturn("ana@mail.com");
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(user));
        when(groupRepository.findByMiembrosEmail("ana@mail.com")).thenReturn(List.of(group));

        List<GroupInfoDTO> result = groupService.getUserGroups(1, "ok-token");

        assertEquals(1, result.size());
        assertEquals("Trip", result.get(0).getNombre());
    }
}
