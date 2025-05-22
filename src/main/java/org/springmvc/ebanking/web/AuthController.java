package org.springmvc.ebanking.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springmvc.ebanking.dtos.auth.RegisterDto;
import org.springmvc.ebanking.dtos.auth.LoginDto;
import org.springmvc.ebanking.entities.Role;
import org.springmvc.ebanking.entities.User;
import org.springmvc.ebanking.repositories.RoleRepository;
import org.springmvc.ebanking.repositories.UserRepository;
import org.springmvc.ebanking.security.JwtTokenProvider;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDto registerDto) {
        log.info("Registering user: {}", registerDto.getUsername());

        if (userRepository.findByUsername(registerDto.getUsername()).isPresent()) {
            log.warn("Username already exists: {}", registerDto.getUsername());
            return ResponseEntity.badRequest().body(new ErrorResponse("Username already exists"));
        }
        if (userRepository.findByEmail(registerDto.getEmail()).isPresent()) {
            log.warn("Email already exists: {}", registerDto.getEmail());
            return ResponseEntity.badRequest().body(new ErrorResponse("Email already exists"));
        }

        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setFirstName(registerDto.getFirstName());
        user.setLastName(registerDto.getLastName());

        Set<Role> roles = new HashSet<>();
        for (String roleName : registerDto.getRoles()) {
            Role role = roleRepository.findByName("ROLE_" + roleName)
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setName("ROLE_" + roleName);
                        return roleRepository.save(newRole);
                    });
            roles.add(role);
        }
        user.setRoles(roles);

        userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());
        return ResponseEntity.ok(new SuccessResponse("User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        log.info("Attempting login for username: {}", loginDto.getUsernameOrEmail());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getUsernameOrEmail(),
                            loginDto.getPassword()
                    )
            );
            String token = jwtTokenProvider.generateToken(authentication);
            log.info("Login successful, token generated for: {}", loginDto.getUsernameOrEmail());
            return ResponseEntity.ok(new LoginSuccessResponse(token));
        } catch (AuthenticationException e) {
            log.warn("Login failed for username: {}, reason: {}", loginDto.getUsernameOrEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Invalid credentials"));
        }
    }
}

class SuccessResponse {
    private String message;

    public SuccessResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

class ErrorResponse {
    private String message;

    public ErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

class LoginSuccessResponse {
    private String token;

    public LoginSuccessResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}