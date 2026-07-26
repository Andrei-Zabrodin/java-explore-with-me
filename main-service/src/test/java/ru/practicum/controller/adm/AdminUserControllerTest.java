package ru.practicum.controller.adm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.service.adm.AdminUserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminUserService adminUserService;

    private NewUserRequest newUserRequest;
    private UserDto userDto;
    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;

        newUserRequest = new NewUserRequest();
        newUserRequest.setEmail("test@example.com");
        newUserRequest.setName("Test User");

        userDto = new UserDto();
        userDto.setId(userId);
        userDto.setEmail("test@example.com");
        userDto.setName("Test User");
    }

    // ============ GET /admin/users ============

    @Test
    void getUsersShouldReturnListWhenValidRequest() throws Exception {
        List<UserDto> users = List.of(userDto);

        when(adminUserService.getUsers(anyList(), anyInt(), anyInt()))
                .thenReturn(users);

        mockMvc.perform(get("/admin/users")
                        .param("ids", "1")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(userDto.getId()))
                .andExpect(jsonPath("$[0].email").value(userDto.getEmail()))
                .andExpect(jsonPath("$[0].name").value(userDto.getName()));

        verify(adminUserService, times(1))
                .getUsers(anyList(), anyInt(), anyInt());
    }

    @Test
    void getUsersShouldReturnListWhenNoIdsProvided() throws Exception {
        when(adminUserService.getUsers(isNull(), anyInt(), anyInt()))
                .thenReturn(List.of(userDto));

        mockMvc.perform(get("/admin/users")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(userDto.getId()))
                .andExpect(jsonPath("$[0].email").value(userDto.getEmail()))
                .andExpect(jsonPath("$[0].name").value(userDto.getName()));

        verify(adminUserService, times(1))
                .getUsers(isNull(), anyInt(), anyInt());
    }

    @Test
    void getUsersShouldReturnBadRequestWhenFromIsInvalid() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .param("from", "invalid"))
                .andExpect(status().isBadRequest());

        verify(adminUserService, never())
                .getUsers(anyList(), anyInt(), anyInt());
    }

    // ============ POST /admin/users ============

    @Test
    void createUserShouldReturnCreatedWhenValidRequest() throws Exception {
        when(adminUserService.createUser(any(NewUserRequest.class)))
                .thenReturn(userDto);

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()))
                .andExpect(jsonPath("$.name").value(userDto.getName()));

        verify(adminUserService, times(1))
                .createUser(any(NewUserRequest.class));
    }

    @Test
    void createUserShouldReturnBadRequestWhenEmailIsBlank() throws Exception {
        newUserRequest.setEmail("");

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isBadRequest());

        verify(adminUserService, never())
                .createUser(any(NewUserRequest.class));
    }

    @Test
    void createUserShouldReturnBadRequestWhenEmailIsTooShort() throws Exception {
        newUserRequest.setEmail("a@b.c");

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isBadRequest());

        verify(adminUserService, never())
                .createUser(any(NewUserRequest.class));
    }

    @Test
    void createUserShouldReturnBadRequestWhenEmailIsTooLong() throws Exception {
        String longEmail = "a".repeat(260) + "@example.com";
        newUserRequest.setEmail(longEmail);

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isBadRequest());

        verify(adminUserService, never())
                .createUser(any(NewUserRequest.class));
    }

    @Test
    void createUserShouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        newUserRequest.setEmail("invalid-email");

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isBadRequest());

        verify(adminUserService, never())
                .createUser(any(NewUserRequest.class));
    }

    @Test
    void createUserShouldReturnBadRequestWhenNameIsBlank() throws Exception {
        newUserRequest.setName("");

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isBadRequest());

        verify(adminUserService, never())
                .createUser(any(NewUserRequest.class));
    }

    @Test
    void createUserShouldReturnBadRequestWhenNameIsTooShort() throws Exception {
        newUserRequest.setName("A");

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isBadRequest());

        verify(adminUserService, never())
                .createUser(any(NewUserRequest.class));
    }

    @Test
    void createUserShouldReturnBadRequestWhenNameIsTooLong() throws Exception {
        newUserRequest.setName("A".repeat(260));

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isBadRequest());

        verify(adminUserService, never())
                .createUser(any(NewUserRequest.class));
    }

    // ============ DELETE /admin/users/{userId} ============

    @Test
    void deleteUserShouldReturnNoContentWhenUserExists() throws Exception {
        doNothing().when(adminUserService).deleteUser(userId);

        mockMvc.perform(delete("/admin/users/{userId}", userId))
                .andExpect(status().isNoContent());

        verify(adminUserService, times(1)).deleteUser(userId);
    }
}