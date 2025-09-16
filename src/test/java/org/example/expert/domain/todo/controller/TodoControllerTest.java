package org.example.expert.domain.todo.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.support.ControllerTestSupport;
import org.example.expert.support.WithMockAuthUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(TodoController.class)
class TodoControllerTest extends ControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private TodoService todoService;

    @Test
    @WithMockAuthUser(userId = 1L, email = "admin@example.com", nickname = "admin", userRole = UserRole.ADMIN)
    void todo_생성에_성공한다() throws Exception {
        // given
        long todoId = 1L;
        AuthUser authUser = (AuthUser) org.springframework.security.test.context.TestSecurityContextHolder
            .getContext().getAuthentication().getPrincipal();
        User user = User.fromAuthUser(authUser);
        TodoSaveRequest todoSaveRequest = new TodoSaveRequest("title", "contents");
        UserResponse userResponse = new UserResponse(user.getId(), user.getEmail());
        TodoSaveResponse response = new TodoSaveResponse(
            todoId,
            "title",
            "contents",
            "Sunny",
            userResponse
        );
        given(todoService.saveTodo(any(AuthUser.class), any(TodoSaveRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(todoSaveRequest))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(todoId))
            .andExpect(jsonPath("$.title").value("title"));
    }

    @Test
    @WithMockAuthUser(userId = 1L, email = "admin@example.com", nickname = "admin", userRole = UserRole.ADMIN)
    void todo_조회조건이_없을_때_목록조회에_성공한다() throws Exception {
        // given
        int page = 1;
        int size = 10;
        Page<TodoResponse> todoResponsePage = new PageImpl<>(List.of(), PageRequest.of(page - 1, size), 0);
        given(todoService.getTodos(eq(page), eq(size), isNull(LocalDate.class), isNull(LocalDate.class), isNull())).willReturn(todoResponsePage);

        // when & then
        mockMvc.perform(get("/todos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.page.totalElements").value(0))
            .andExpect(jsonPath("$.page.totalPages").value(0));
    }

    @Test
    @WithMockAuthUser(userId = 1L, email = "admin@example.com", nickname = "admin", userRole = UserRole.ADMIN)
    void todo_조회조건이_있을_때_목록조회에_성공한다() throws Exception {
        // given
        int page = 1;
        int size = 10;
        Page<TodoResponse> todoResponsePage = new PageImpl<>(List.of(), PageRequest.of(page - 1, size), 0);
        given(todoService.getTodos(eq(page), eq(size),
            eq(LocalDate.of(2025,9,1)),
            eq(LocalDate.of(2025,9,20)),
            eq("Sunny"))).willReturn(todoResponsePage);

        // when & then
        mockMvc.perform(get("/todos")
                .param("start", "2025-09-01")   // @DateTimeFormat(ISO.DATE)와 맞춰 yyyy-MM-dd
                .param("end",   "2025-09-20")
                .param("weather", "Sunny"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.page.totalElements").value(0))
            .andExpect(jsonPath("$.page.totalPages").value(0));
    }

    @Test
    @WithMockAuthUser(userId = 1L, email = "admin@example.com", nickname = "admin", userRole = UserRole.ADMIN)
    void todo_단건조회에_성공한다() throws Exception {
        // given
        long todoId = 1L;
        String title = "title";
        AuthUser authUser = new AuthUser(1L, "email", "test", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        UserResponse userResponse = new UserResponse(user.getId(), user.getEmail());
        TodoResponse response = new TodoResponse(
                todoId,
                title,
                "contents",
                "Sunny",
                userResponse,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(todoService.getTodo(todoId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/todos/{todoId}", todoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(todoId))
                .andExpect(jsonPath("$.title").value(title));
    }

    @Test
    @WithMockAuthUser(userId = 1L, email = "admin@example.com", nickname = "admin", userRole = UserRole.ADMIN)
    void todo_단건_조회_시_todo가_존재하지_않아_예외가_발생한다() throws Exception {
        // given
        long todoId = 1L;

        given(todoService.getTodo(todoId))
                .willThrow(new InvalidRequestException("Todo not found"));

        // when & then
        mockMvc.perform(get("/todos/{todoId}", todoId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Todo not found"));
    }
}
