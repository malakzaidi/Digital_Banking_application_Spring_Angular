package org.springmvc.ebanking.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springmvc.ebanking.dtos.UserProfileDTO;
import org.springmvc.ebanking.entities.User;
import org.springmvc.ebanking.mappers.UserMapper;
import org.springmvc.ebanking.repositories.UserRepository;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(@AuthenticationPrincipal Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        UserProfileDTO profileDTO = userMapper.toProfileDTO(user);
        return ResponseEntity.ok(profileDTO);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateUserProfile(
            @AuthenticationPrincipal Principal principal,
            @RequestBody UserProfileDTO profileDTO
    ) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        userMapper.updateUserFromDTO(profileDTO, user);
        userRepository.save(user);
        UserProfileDTO updatedProfile = userMapper.toProfileDTO(user);
        return ResponseEntity.ok(updatedProfile);
    }
}