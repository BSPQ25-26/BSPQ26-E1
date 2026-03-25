package com.mycompany.app.service;

import com.mycompany.app.dto.AddUserToGroupDTO;
import com.mycompany.app.dto.DeleteGroupDTO;
import com.mycompany.app.dto.GroupCreationDTO;
import com.mycompany.app.dto.GroupInfoDTO;
import com.mycompany.app.dto.RemoveUserFromGroupDTO;
import com.mycompany.app.dto.UpdateGroupDTO;
import com.mycompany.app.exception.AuthException;
import com.mycompany.app.model.Group;
import com.mycompany.app.model.Transaction;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.GroupRepository;
import com.mycompany.app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuthService authService;

    /**
     * Creates a new group
     */
    @Transactional
    public GroupInfoDTO createGroup(GroupCreationDTO dto) {
        // Validate token
        String email = authService.getEmailFromToken(dto.getAccessToken());
        if (email == null) {
            throw new AuthException("Invalid token", HttpStatus.UNAUTHORIZED);
        }

        // Find the creator user
        Usuario creator = usuarioRepository.findByEmail(email);
        if (creator == null) {
            throw new AuthException("User not found", HttpStatus.NOT_FOUND);
        }

        // Create new group
        Group group = new Group(dto.getNombre(), dto.getDescripcion());

        // Add creator as first member
        group.addMiembro(creator);

        // Save group
        Group savedGroup = groupRepository.save(group);

        return convertToInfoDTO(savedGroup);
    }

    /**
     * Gets group information by ID
     */
    @Transactional(readOnly = true)
    public GroupInfoDTO getGroupInfo(Integer groupId, String token) {
        // Validate token
        String email = authService.getEmailFromToken(token);
        if (email == null) {
            throw new AuthException("Invalid token", HttpStatus.UNAUTHORIZED);
        }

        // Find group
        Optional<Group> groupOpt = groupRepository.findByIdWithMiembros(groupId);
        if (groupOpt.isEmpty()) {
            throw new AuthException("Group not found", HttpStatus.NOT_FOUND);
        }

        Group group = groupOpt.get();

        // Check if user is a member of the group
        boolean isMember = group.getMiembros().stream()
                .anyMatch(u -> u.getEmail().equals(email));

        if (!isMember) {
            throw new AuthException("User is not a member of this group", HttpStatus.FORBIDDEN);
        }

        return convertToInfoDTO(group);
    }

    /**
     * Gets all groups for a user
     */
    @Transactional(readOnly = true)
    public List<GroupInfoDTO> getUserGroups(String token) {
        // Validate token
        String email = authService.getEmailFromToken(token);
        if (email == null) {
            throw new AuthException("Invalid token", HttpStatus.UNAUTHORIZED);
        }

        List<Group> groups = groupRepository.findByMiembrosEmail(email);

        return groups.stream()
                .map(this::convertToInfoDTO)
                .collect(Collectors.toList());
    }

    /**
     * Adds a user to a group
     */
    @Transactional
    public boolean addUserToGroup(AddUserToGroupDTO dto) {
        // Validate token
        String requestorEmail = authService.getEmailFromToken(dto.getAccessToken());
        if (requestorEmail == null) {
            throw new AuthException("Invalid token", HttpStatus.UNAUTHORIZED);
        }

        // Find group
        Optional<Group> groupOpt = groupRepository.findByIdWithMiembros(dto.getGroupId());
        if (groupOpt.isEmpty()) {
            throw new AuthException("Group not found", HttpStatus.NOT_FOUND);
        }

        Group group = groupOpt.get();

        // Check if requestor is a member of the group
        boolean isRequestorMember = group.getMiembros().stream()
                .anyMatch(u -> u.getEmail().equals(requestorEmail));

        if (!isRequestorMember) {
            throw new AuthException("Only group members can add users", HttpStatus.FORBIDDEN);
        }

        // Find user to add
        Usuario userToAdd = usuarioRepository.findByEmail(dto.getUserEmail());
        if (userToAdd == null) {
            throw new AuthException("User to add not found", HttpStatus.NOT_FOUND);
        }

        // Check if user is already a member
        boolean alreadyMember = group.getMiembros().contains(userToAdd);
        if (alreadyMember) {
            return false; // User already in group
        }

        // Add user to group
        group.addMiembro(userToAdd);
        groupRepository.save(group);

        return true;
    }

    /**
     * Removes a user from a group
     */
    @Transactional
    public boolean removeUserFromGroup(RemoveUserFromGroupDTO dto) {
        // Validate token
        String requestorEmail = authService.getEmailFromToken(dto.getAccessToken());
        if (requestorEmail == null) {
            throw new AuthException("Invalid token", HttpStatus.UNAUTHORIZED);
        }

        // Find group
        Optional<Group> groupOpt = groupRepository.findByIdWithMiembros(dto.getGroupId());
        if (groupOpt.isEmpty()) {
            throw new AuthException("Group not found", HttpStatus.NOT_FOUND);
        }

        Group group = groupOpt.get();

        // Check if requestor is a member of the group
        boolean isRequestorMember = group.getMiembros().stream()
                .anyMatch(u -> u.getEmail().equals(requestorEmail));

        if (!isRequestorMember) {
            throw new AuthException("Only group members can remove users", HttpStatus.FORBIDDEN);
        }

        // Find user to remove
        Usuario userToRemove = usuarioRepository.findByEmail(dto.getUserEmail());
        if (userToRemove == null) {
            throw new AuthException("User to remove not found", HttpStatus.NOT_FOUND);
        }

        // Check if user is a member
        boolean isMember = group.getMiembros().contains(userToRemove);
        if (!isMember) {
            return false; // User not in group
        }

        // Prevent removing last member
        if (group.getMiembros().size() == 1) {
            throw new AuthException("Cannot remove the last member from a group", HttpStatus.BAD_REQUEST);
        }

        // Remove user from group
        group.removeMiembro(userToRemove);
        groupRepository.save(group);

        return true;
    }

    /**
     * Updates a group's information
     */
    @Transactional
    public GroupInfoDTO updateGroup(UpdateGroupDTO dto) {
        // Validate token
        String requestorEmail = authService.getEmailFromToken(dto.getAccessToken());
        if (requestorEmail == null) {
            throw new AuthException("Invalid token", HttpStatus.UNAUTHORIZED);
        }

        // Find group
        Optional<Group> groupOpt = groupRepository.findByIdWithMiembros(dto.getGroupId());
        if (groupOpt.isEmpty()) {
            throw new AuthException("Group not found", HttpStatus.NOT_FOUND);
        }

        Group group = groupOpt.get();

        // Check if requestor is a member of the group
        boolean isRequestorMember = group.getMiembros().stream()
                .anyMatch(u -> u.getEmail().equals(requestorEmail));

        if (!isRequestorMember) {
            throw new AuthException("Only group members can update the group", HttpStatus.FORBIDDEN);
        }

        // Update group fields
        if (dto.getNombre() != null && !dto.getNombre().trim().isEmpty()) {
            group.setNombre(dto.getNombre());
        }
        if (dto.getDescripcion() != null) {
            group.setDescripcion(dto.getDescripcion());
        }

        // Save updated group
        Group updatedGroup = groupRepository.save(group);

        return convertToInfoDTO(updatedGroup);
    }

    /**
     * Deletes a group
     */
    @Transactional
    public boolean deleteGroup(DeleteGroupDTO dto) {
        // Validate token
        String requestorEmail = authService.getEmailFromToken(dto.getAccessToken());
        if (requestorEmail == null) {
            throw new AuthException("Invalid token", HttpStatus.UNAUTHORIZED);
        }

        // Find group
        Optional<Group> groupOpt = groupRepository.findByIdWithMiembros(dto.getGroupId());
        if (groupOpt.isEmpty()) {
            throw new AuthException("Group not found", HttpStatus.NOT_FOUND);
        }

        Group group = groupOpt.get();

        // Check if requestor is a member of the group
        boolean isRequestorMember = group.getMiembros().stream()
                .anyMatch(u -> u.getEmail().equals(requestorEmail));

        if (!isRequestorMember) {
            throw new AuthException("Only group members can delete the group", HttpStatus.FORBIDDEN);
        }

        // Delete the group
        groupRepository.delete(group);

        return true;
    }

    /**
     * Converts Group entity to GroupInfoDTO
     */
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
