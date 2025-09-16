package org.example.expert.domain.todo.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {
	@Mock
	private TodoRepository todoRepository;

	@Mock
	private WeatherClient weatherClient;

	@InjectMocks
	private TodoService todoService;

	@Test
	public void saveTodo_성공_시_DTO_반환() {
		//given
		AuthUser authUser = new AuthUser(1L, "user@test.com","user",UserRole.USER);
		TodoSaveRequest todoSaveRequest = new TodoSaveRequest("title", "content");

		given(weatherClient.getTodayWeather()).willReturn("Sunny");

		User userEntity = User.fromAuthUser(authUser);
		Todo saved = new Todo("title", "contents", "Sunny", userEntity);
		ReflectionTestUtils.setField(saved, "id", 1L);
		given(todoRepository.save(any(Todo.class))).willReturn(saved);

		//when
		TodoSaveResponse todoSaveResponse = todoService.saveTodo(authUser, todoSaveRequest);

		//then
		assertThat(todoSaveResponse).isNotNull();
		assertThat(todoSaveResponse.getId()).isEqualTo(1L);
		assertThat(todoSaveResponse.getTitle()).isEqualTo("title");
		assertThat(todoSaveResponse.getContents()).isEqualTo("contents");
		assertThat(todoSaveResponse.getWeather()).isEqualTo("Sunny");
		assertThat(todoSaveResponse.getUser().getEmail()).isEqualTo(userEntity.getEmail());
		verify(weatherClient).getTodayWeather();
		verify(todoRepository).save(any(Todo.class));
	}

	@Test
	public void getTodos_성공_시_Page_Dto_반환() {
		//given
		int page = 1, size = 5;
		String weather = "Sunny";
		LocalDate start = LocalDate.of(2025, 9, 1);
		LocalDate end   = LocalDate.of(2025, 9, 20);
		LocalDateTime expectedStart = start.atStartOfDay();
		LocalDateTime expectedEndEx = end.plusDays(1).atStartOfDay();
		User user = new User("user@test.com", "pw", "user", UserRole.USER);
		Todo todo1 = new Todo("title", "contents", "Sunny", user);
		Todo todo2 = new Todo("title2", "contents2", "Sunny", user);

		Page<Todo> todoResponsePage = new PageImpl<>(List.of(todo1, todo2), PageRequest.of(page - 1, size), 2);
		given(todoRepository.findByConditionOrderByModifiedAtDesc(eq(weather), eq(expectedStart), eq(expectedEndEx), any(Pageable.class))).willReturn(todoResponsePage);

		//when
		Page<TodoResponse> result = todoService.getTodos(page, size, start, end, weather);

		//then
		assertThat(result).isNotNull();
		assertThat(result.getTotalElements()).isEqualTo(2);
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getContent().get(0).getTitle()).isEqualTo("title");
		assertThat(result.getContent().get(0).getContents()).isEqualTo("contents");
		assertThat(result.getContent().get(0).getWeather()).isEqualTo("Sunny");
		assertThat(result.getContent().get(0).getUser().getEmail()).isEqualTo(user.getEmail());
		assertThat(result.getContent().get(1).getTitle()).isEqualTo("title2");
		assertThat(result.getContent().get(1).getContents()).isEqualTo("contents2");
		assertThat(result.getContent().get(1).getWeather()).isEqualTo("Sunny");
		assertThat(result.getContent().get(1).getUser().getEmail()).isEqualTo(user.getEmail());
	}

	@Test
	public void getTodo_성공_시_DTO_반환() {
		//given
		User user = new User("user@test.com", "pw", "user", UserRole.USER);
		Todo todo = new Todo("title", "contents", "Sunny", user);
		Long todoId = 1L;
		ReflectionTestUtils.setField(todo, "id", todoId);

		given(todoRepository.findByIdWithUser(todoId)).willReturn(Optional.of(todo));

		// when
		TodoResponse todoResponse = todoService.getTodo(todoId);

		// then
		assertThat(todoResponse).isNotNull();
		assertThat(todoResponse.getId()).isEqualTo(1L);
		assertThat(todoResponse.getTitle()).isEqualTo("title");
		assertThat(todoResponse.getContents()).isEqualTo("contents");
		assertThat(todoResponse.getWeather()).isEqualTo("Sunny");
		assertThat(todoResponse.getUser().getEmail()).isEqualTo(user.getEmail());
	}

	@Test
	public void getTodo_Todo_없을_시_400_반환() {
		//given
		User user = new User("user@test.com", "pw", "user", UserRole.USER);
		Todo todo = new Todo("title", "contents", "Sunny", user);
		Long todoId = 1L;
		ReflectionTestUtils.setField(todo, "id", todoId);

		// when & then
		assertThatThrownBy(() -> todoService.getTodo(todoId))
			.isInstanceOf(InvalidRequestException.class)
			.hasMessage("Todo not found");
	}

	@Test
	public void searchTodos_성공_시_Page_Dto_반환() {
		//given
		int page = 1, size = 5;
		String title = "title";
		String manager = "manager";
		LocalDate start = LocalDate.of(2025, 9, 1);
		LocalDate end   = LocalDate.of(2025, 9, 20);
		LocalDateTime expectedStart = start.atStartOfDay();
		LocalDateTime expectedEndEx = end.plusDays(1).atStartOfDay();

		given(todoRepository.searchTodosOrderByCreatedAtDesc(
			eq(title), eq(expectedStart), eq(expectedEndEx), eq(manager), any(Pageable.class)
		)).willReturn(Page.empty());

		//when
		Page<TodoSearchResponse> result = todoService.searchTodos(page, size, title, start, end, manager);

		//then
		assertThat(result).isNotNull();
		assertThat(result.getTotalElements()).isZero();
		assertThat(result.getTotalPages()).isEqualTo(1);
	}
}
