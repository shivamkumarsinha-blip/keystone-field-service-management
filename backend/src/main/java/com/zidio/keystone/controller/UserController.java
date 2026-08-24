package com.zidio.keystone.controller;

import com.zidio.keystone.dto.PageResponse;
import com.zidio.keystone.dto.UserCreateRequest;
import com.zidio.keystone.dto.UserDto;
import com.zidio.keystone.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public PageResponse<UserDto> list(Pageable pageable) {
        return userService.list(pageable);
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(201).body(userService.create(request));
    }

    @PutMapping("/{id}/active")
    public UserDto setActive(@PathVariable Long id, @RequestParam boolean active) {
        return userService.setActive(id, active);
    }
}
