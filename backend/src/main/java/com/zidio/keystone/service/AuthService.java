package com.zidio.keystone.service;

import com.zidio.keystone.dto.LoginRequest;
import com.zidio.keystone.dto.LoginResponse;
import com.zidio.keystone.entity.User;
import com.zidio.keystone.mapper.UserMapper;
import com.zidio.keystone.repository.UserRepository;
import com.zidio.keystone.security.CustomUserDetails;
import com.zidio.keystone.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
                        UserRepository userRepository, UserMapper userMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public LoginResponse login(LoginRequest request) {
        // Throws BadCredentialsException (mapped to 401) on bad email/password or disabled account.
        var authToken = new UsernamePasswordAuthenticationToken(request.email(), request.password());
        var authentication = authenticationManager.authenticate(authToken);
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        User user = principal.getUser();

        String token = jwtService.generateToken(principal, user.getId(), user.getRole().name());
        return new LoginResponse(token, userMapper.toDto(user));
    }
}
