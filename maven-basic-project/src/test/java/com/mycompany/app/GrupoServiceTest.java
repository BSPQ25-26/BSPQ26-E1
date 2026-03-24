package com.mycompany.app;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.mycompany.app.dto.GrupoCreationDTO;
import com.mycompany.app.dto.GrupoMemberDTO;
import com.mycompany.app.model.Grupo;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.GrupoRepository;
import com.mycompany.app.repository.UsuarioRepository;
import com.mycompany.app.service.GrupoService;

public class GrupoServiceTest {

    private GrupoRepository grupoRepository;
    private UsuarioRepository usuarioRepository;
    private GrupoService grupoService;

    private Usuario usuario;
    private Usuario otroUsuario;
    private Grupo grupo;

    @Before
    public void setUp() {
        grupoRepository = mock(GrupoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        grupoService = new GrupoService(grupoRepository, usuarioRepository);

        usuario = new Usuario("Alice", "alice@example.com", "pass", 0.0);
        usuario.setId(1);

        otroUsuario = new Usuario("Bob", "bob@example.com", "pass", 0.0);
        otroUsuario.setId(2);

        grupo = new Grupo("Amigos", usuario);
        grupo.setId(10);
    }

    @Test
    public void createGrupo_withValidData_returnsTrue() {
        GrupoCreationDTO dto = new GrupoCreationDTO("token", "Amigos", 1);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(grupoRepository.save(any(Grupo.class))).thenReturn(grupo);

        boolean result = grupoService.createGrupo(dto);

        assertTrue(result);
        verify(grupoRepository).save(any(Grupo.class));
    }

    @Test
    public void createGrupo_withUnknownCreator_returnsFalse() {
        GrupoCreationDTO dto = new GrupoCreationDTO("token", "Amigos", 99);
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        boolean result = grupoService.createGrupo(dto);

        assertFalse(result);
        verify(grupoRepository, never()).save(any());
    }

    @Test
    public void addMember_withValidData_returnsTrue() {
        GrupoMemberDTO dto = new GrupoMemberDTO("token", 10, 2);
        when(grupoRepository.findById(10)).thenReturn(Optional.of(grupo));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(otroUsuario));
        when(grupoRepository.save(any(Grupo.class))).thenReturn(grupo);

        boolean result = grupoService.addMember(dto);

        assertTrue(result);
        assertTrue(grupo.getMiembros().contains(otroUsuario));
    }

    @Test
    public void addMember_withUnknownGroup_returnsFalse() {
        GrupoMemberDTO dto = new GrupoMemberDTO("token", 99, 2);
        when(grupoRepository.findById(99)).thenReturn(Optional.empty());
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(otroUsuario));

        boolean result = grupoService.addMember(dto);

        assertFalse(result);
    }

    @Test
    public void addMember_withUnknownUser_returnsFalse() {
        GrupoMemberDTO dto = new GrupoMemberDTO("token", 10, 99);
        when(grupoRepository.findById(10)).thenReturn(Optional.of(grupo));
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        boolean result = grupoService.addMember(dto);

        assertFalse(result);
    }

    @Test
    public void removeMember_withValidData_returnsTrue() {
        grupo.getMiembros().add(otroUsuario);
        GrupoMemberDTO dto = new GrupoMemberDTO("token", 10, 2);
        when(grupoRepository.findById(10)).thenReturn(Optional.of(grupo));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(otroUsuario));
        when(grupoRepository.save(any(Grupo.class))).thenReturn(grupo);

        boolean result = grupoService.removeMember(dto);

        assertTrue(result);
        assertFalse(grupo.getMiembros().contains(otroUsuario));
    }

    @Test
    public void removeMember_withUnknownGroup_returnsFalse() {
        GrupoMemberDTO dto = new GrupoMemberDTO("token", 99, 2);
        when(grupoRepository.findById(99)).thenReturn(Optional.empty());
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(otroUsuario));

        boolean result = grupoService.removeMember(dto);

        assertFalse(result);
    }

    @Test
    public void getGroupMembers_withExistingGroup_returnsMembers() {
        when(grupoRepository.findById(10)).thenReturn(Optional.of(grupo));

        Set<Usuario> members = grupoService.getGroupMembers(10);

        assertNotNull(members);
        assertTrue(members.contains(usuario));
    }

    @Test
    public void getGroupMembers_withUnknownGroup_returnsNull() {
        when(grupoRepository.findById(99)).thenReturn(Optional.empty());

        Set<Usuario> members = grupoService.getGroupMembers(99);

        assertNull(members);
    }

    @Test
    public void getUserGroups_returnsGroupsForUser() {
        when(grupoRepository.findByMiembrosId(1)).thenReturn(List.of(grupo));

        List<Grupo> groups = grupoService.getUserGroups(1);

        assertNotNull(groups);
        assertFalse(groups.isEmpty());
        assertTrue(groups.contains(grupo));
    }
}
