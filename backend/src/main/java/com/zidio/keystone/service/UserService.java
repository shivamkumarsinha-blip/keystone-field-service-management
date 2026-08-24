package com.zidio.keystone.service;

import com.zidio.keystone.dto.PageResponse;
import com.zidio.keystone.dto.UserCreateRequest;
import com.zidio.keystone.dto.UserDto;
import com.zidio.keystone.entity.User;
import com.zidio.keystone.exception.BadRequestException;
import com.zidio.keystone.exception.ResourceNotFoundException;
import com.zidio.keystone.mapper.UserMapper;
import com.zidio.keystone.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @PreAuthorize("hasRole('MANAGER')")
    public PageResponse<UserDto> list(Pageable pageable) {
        return PageResponse.from(userRepository.findAll(pageable).map(userMapper::toDto));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public UserDto create(UserCreateRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("A user with this email already exists");
        }
        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .active(true)
                .build();
        return userMapper.toDto(userRepository.save(user));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public UserDto setActive(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User " + id + " not found"));
        user.setActive(active);
        return userMapper.toDto(user);
    }
}
