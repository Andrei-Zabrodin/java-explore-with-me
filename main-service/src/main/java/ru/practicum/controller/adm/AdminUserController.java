package ru.practicum.controller.adm;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserDto;
import ru.practicum.service.adm.AdminUserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
@Slf4j
public class AdminUserController {
    private final AdminUserService adminUserService;

    @GetMapping
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                            @RequestParam(defaultValue = "0") int from,
                            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /admin/users - получение пользователей c параметрами ids={}, from={}, size={}", ids, from, size);
        return adminUserService.getUsers(ids, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody NewUserRequest request) {
        log.info("POST /admin/users - создание пользователя с email: {}", request.getEmail());
        return adminUserService.createUser(request);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("DELETE /admin/users/{} - удаление пользователя", userId);
        adminUserService.deleteUser(userId);
    }
}
