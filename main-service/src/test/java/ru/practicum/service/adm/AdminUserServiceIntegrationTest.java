package ru.practicum.service.adm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AdminUserServiceIntegrationTest {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private UserRepository userRepository;

    private NewUserRequest newUserRequest;
    private User existingUser1;
    private User existingUser2;

    @BeforeEach
    void setUp() {
        existingUser1 = new User();
        existingUser1.setEmail("user1@example.com");
        existingUser1.setName("User 1");
        existingUser1 = userRepository.save(existingUser1);

        existingUser2 = new User();
        existingUser2.setEmail("user2@example.com");
        existingUser2.setName("User 2");
        existingUser2 = userRepository.save(existingUser2);

        newUserRequest = new NewUserRequest();
        newUserRequest.setEmail("test@example.com");
        newUserRequest.setName("Test User");
    }

    @Test
    void getUsersShouldReturnListWhenIdsProvided() {
        List<UserDto> result = adminUserService.getUsers(List.of(existingUser1.getId(), existingUser2.getId()), 0, 10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(existingUser1.getId());
        assertThat(result.get(0).getEmail()).isEqualTo(existingUser1.getEmail());
        assertThat(result.get(0).getName()).isEqualTo(existingUser1.getName());
        assertThat(result.get(1).getId()).isEqualTo(existingUser2.getId());
        assertThat(result.get(1).getEmail()).isEqualTo(existingUser2.getEmail());
        assertThat(result.get(1).getName()).isEqualTo(existingUser2.getName());
    }

    @Test
    void getUsersShouldReturnAllUsersWhenIdsIsNull() {
        List<UserDto> result = adminUserService.getUsers(null, 0, 10);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserDto::getEmail)
                .containsExactlyInAnyOrder(existingUser1.getEmail(), existingUser2.getEmail());
    }

    @Test
    void getUsersShouldReturnUsersWithPagination() {
        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.setEmail("pageuser" + i + "@example.com");
            user.setName("Page User " + i);
            userRepository.save(user);
        }

        List<UserDto> result = adminUserService.getUsers(null, 0, 3);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(UserDto::getName)
                .containsExactlyInAnyOrder(existingUser1.getName(), existingUser2.getName(), "Page User 0");
    }

    @Test
    void getUsersShouldReturnEmptyListWhenNoUsersFound() {
        Long nonExistentId = 999L;
        List<UserDto> result = adminUserService.getUsers(List.of(nonExistentId), 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void createUserShouldReturnUserDtoWhenValidRequest() {
        UserDto result = adminUserService.createUser(newUserRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getName()).isEqualTo("Test User");

        User saved = userRepository.findById(result.getId()).orElseThrow();
        assertThat(saved.getEmail()).isEqualTo(newUserRequest.getEmail());
        assertThat(saved.getName()).isEqualTo(newUserRequest.getName());
    }

    @Test
    void createUserShouldThrowConflictExceptionWhenEmailAlreadyExists() {
        newUserRequest.setEmail(existingUser1.getEmail());

        assertThatThrownBy(() -> adminUserService.createUser(newUserRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("User with email '" + existingUser1.getEmail() + "' already exists");
    }

    @Test
    void deleteUserShouldDeleteWhenUserExists() {
        Long userId = existingUser1.getId();

        adminUserService.deleteUser(userId);

        assertThat(userRepository.findById(userId)).isEmpty();
        assertThat(userRepository.findAll()).hasSize(1);
        assertThat(userRepository.findAll().get(0).getEmail()).isEqualTo(existingUser2.getEmail());
    }

    @Test
    void deleteUserShouldThrowNotFoundExceptionWhenUserNotFound() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> adminUserService.deleteUser(nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id=" + nonExistentId + " was not found");
    }
}