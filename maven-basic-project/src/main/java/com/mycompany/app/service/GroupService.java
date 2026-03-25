package com.mycompany.app.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuthService authService;

    @Transactional
    public GroupInfoDTO createGroup(GroupCreationDTO dto) {
        String email = authService.getEmailFromToken(dto.getAccessToken());
        if (email == null) {
            throw new AuthException("Invalid token", HttpStatus.UNAUTHORIZED);
        }

        Usuario creator = usuarioRepository.findByEmail(email);
        if (creator == null) {
            throw new AuthException("User not found", HttpStatus.NOT_FOUND);
        }

        Group group = new Group(dto.getNombre(), dto.getDescripcion());
        group.addMiembro(creator);

        Group savedGroup = groupRepository.save(group);

        return convertToInfoDTO(savedGroup);
    }

    @Transactional(readOnly = true)
    public GroupInfoDTO getGroupInfo(Integer groupId, String token) {
        String email = authService.getEmailFromToken(token);
        if (email == null) {
            throw new AuthException("Invalid token", HttpStatus.UNAUTHORIZED);
        }

        Optional<Group> groupOpt = groupRepository.findByIdWithMiembros(groupId);
        if (groupOpt.isEmpty()) {
            throw new AuthException("Group not found", HttpStatus.NOT_FOUND);
        }

        return convertToInfoDTO(groupOpt.get());
    }

    @Transactional(readOnly = true)
    public List<GroupInfoDTO> getUserGroups(Integer userId, String token) {
        String tokenEmail = authService.getEmailFromToken(token);
        if (tokenEmail == null) {
            throw new AuthException("Invalid token", HttpStatus.UNAUTHORIZED);
        }

        Optional<Usuario> userOpt = usuarioRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new AuthException("User not found", HttpStatus.NOT_FOUND);
        }

        String targetEmail = userOpt.get().getEmail();
        List<Group> groups = groupRepository.findByMiembrosEmail(targetEmail);

        return groups.stream()
                .map(this::convertToInfoDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean addUserToGroup(AddUserToGroupDTO dto) {
        String requestorEmail = authService.getEmailFromToken(dto.getAccessToken());
        if (requestorEmail == null) {
            throw new AuthException("Invalid token", HttpStatus.UNAUTHORIZED);
        }

        Optional<Group> groupOpt = groupRepository.findByIdWithMiembros(dto.getGroupId());
        if (groupOpt.isEmpty()) {
            throw new AuthException("Group not found", HttpStatus.NOT_FOUND);
        }

        Group group = groupOpt.get();

        Usuario userToAdd = usuarioRepository.findByEmail(dto.getUserEmail());
        if (userToAdd == null) {
            throw new AuthException("User to add not found", HttpStatus.NOT_FOUND);
        }

        boolean alreadyMember = group.getMiembros().contains(userToAdd);
        if (alreadyMember) {
            return false;
        }

        group.addMiembro(userToAdd);
        groupRepository.save(group);

        return true;
    }

    @Transactional
    public boolean removeUserFromGroup(RemoveUserFromGroupDTO dto) {
        String requestorEmail = authService.getEmailFromToken(dto.getAccessToken());
        if (requestorEmail == null) {
            throw new AuthException("Invalid token", HttpStatus.UNAUTHORIZED);
        }

        Optional<Group> groupOpt = groupRepository.findByIdWithMiembros(dto.getGroupId());
        if (groupOpt.isEmpty()) {
            throw new AuthException("Group not found", HttpStatus.NOT_FOUND);
        }

        Group group = groupOpt.get();

        Usuario userToRemove = usuarioRepository.findByEmail(dto.getUserEmail());
        if (userToRemove == null) {
            throw new AuthException("User to remove not found", HttpStatus.NOT_FOUND);
        }

        boolean isMember = group.getMiembros().contains(userToRemove);
        if (!isMember) {
            return false; 
        }

        if (group.getMiembros().size() == 1) {
            throw new AuthException("Cannot remove the last member from a group", HttpStatus.BAD_REQUEST);
        }

        group.removeMiembro(userToRemove);
        groupRepository.save(group);

        return true;
    }

    @Transactional
    public GroupInfoDTO updateGroup(UpdateGroupDTO dto) {
        String requestorEmail = authService.getEmailFromToken(dto.getAccessToken());
        if (requestorEmail == null) {
            throw new AuthException("Invalid token", HttpStatus.UNAUTHORIZED);
        }

        Optional<Group> groupOpt = groupRepository.findByIdWithMiembros(dto.getGroupId());
        if (groupOpt.isEmpty()) {
            throw new AuthException("Group not found", HttpStatus.NOT_FOUND);
        }

        Group group = groupOpt.get();

        if (dto.getNombre() != null && !dto.getNombre().trim().isEmpty()) {
            group.setNombre(dto.getNombre());
        }
        if (dto.getDescripcion() != null) {
            group.setDescripcion(dto.getDescripcion());
        }

        Group updatedGroup = groupRepository.save(group);

        return convertToInfoDTO(updatedGroup);
    }

    @Transactional
    public boolean deleteGroup(DeleteGroupDTO dto) {
        String requestorEmail = authService.getEmailFromToken(dto.getAccessToken());
        if (requestorEmail == null) {
            throw new AuthException("Invalid token", HttpStatus.UNAUTHORIZED);
        }

        Optional<Group> groupOpt = groupRepository.findByIdWithMiembros(dto.getGroupId());
        if (groupOpt.isEmpty()) {
            throw new AuthException("Group not found", HttpStatus.NOT_FOUND);
        }

        groupRepository.delete(groupOpt.get());

        return true;
    }

    private GroupInfoDTO convertToInfoDTO(Group group) {
        List<String> memberEmails = group.getMiembros().stream()
                .map(Usuario::getEmail)
                .collect(Collectors.toList());

        return new GroupInfoDTO(
                group.getId(),
                group.getNombre(),
                group.getDescripcion(),
                group.getFechaCreacion(),
                memberEmails,
                group.getMiembros().size()
        );
    }
}