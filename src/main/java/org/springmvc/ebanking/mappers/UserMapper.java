package org.springmvc.ebanking.mappers;

import org.springmvc.ebanking.dtos.UserProfileDTO;
import org.springmvc.ebanking.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserProfileDTO toProfileDTO(User user) {
        String fullName = (user.getFirstName() != null ? user.getFirstName() : "") +
                (user.getLastName() != null ? " " + user.getLastName() : "");
        return new UserProfileDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                fullName.trim(),
                null,
                null,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public void updateUserFromDTO(UserProfileDTO dto, User user) {
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());

        String[] nameParts = (dto.getName() != null ? dto.getName().trim().split("\\s+", 2) : new String[]{"", ""});
        user.setFirstName(nameParts[0]);
        user.setLastName(nameParts.length > 1 ? nameParts[1] : "");


    }
}