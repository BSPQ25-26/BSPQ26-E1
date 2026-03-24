package com.mycompany.app.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.mycompany.app.dto.GrupoCreationDTO;
import com.mycompany.app.dto.GrupoMemberDTO;
import com.mycompany.app.model.Grupo;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.GrupoRepository;
import com.mycompany.app.repository.UsuarioRepository;

@Service
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;

    public GrupoService(GrupoRepository grupoRepository, UsuarioRepository usuarioRepository) {
        this.grupoRepository = grupoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public boolean createGrupo(GrupoCreationDTO dto) {
        try {
            Optional<Usuario> creadorOpt = usuarioRepository.findById(dto.getCreadorId());
            if (creadorOpt.isEmpty()) {
                return false;
            }
            Grupo grupo = new Grupo(dto.getNombre(), creadorOpt.get());
            grupoRepository.save(grupo);
            return true;
        } catch (Exception e) {
            System.err.println("Error creating group: " + e.getMessage());
            return false;
        }
    }

    public boolean addMember(GrupoMemberDTO dto) {
        try {
            Optional<Grupo> grupoOpt = grupoRepository.findById(dto.getGrupoId());
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(dto.getUsuarioId());
            if (grupoOpt.isEmpty() || usuarioOpt.isEmpty()) {
                return false;
            }
            Grupo grupo = grupoOpt.get();
            grupo.getMiembros().add(usuarioOpt.get());
            grupoRepository.save(grupo);
            return true;
        } catch (Exception e) {
            System.err.println("Error adding member to group: " + e.getMessage());
            return false;
        }
    }

    public boolean removeMember(GrupoMemberDTO dto) {
        try {
            Optional<Grupo> grupoOpt = grupoRepository.findById(dto.getGrupoId());
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(dto.getUsuarioId());
            if (grupoOpt.isEmpty() || usuarioOpt.isEmpty()) {
                return false;
            }
            Grupo grupo = grupoOpt.get();
            grupo.getMiembros().remove(usuarioOpt.get());
            grupoRepository.save(grupo);
            return true;
        } catch (Exception e) {
            System.err.println("Error removing member from group: " + e.getMessage());
            return false;
        }
    }

    public Set<Usuario> getGroupMembers(Integer grupoId) {
        return grupoRepository.findById(grupoId)
                .map(Grupo::getMiembros)
                .orElse(null);
    }

    public List<Grupo> getUserGroups(Integer usuarioId) {
        return grupoRepository.findByMiembrosId(usuarioId);
    }
}
