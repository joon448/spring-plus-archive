package org.example.expert.domain.manager.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.log.service.LogService;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class ManagerServiceTest {
	@Mock
	private ManagerRepository managerRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private TodoRepository todoRepository;

	@Mock
	private LogService logService;

	@InjectMocks
	private ManagerService managerService;

	@Test
	public void saveManager_성공_시_DTO_반환() {
		// given
		long userId = 1L;
		long managerUserId = 2L;
		long todoId = 1L;

		AuthUser authUser = new AuthUser(1L, "user@test.com","user", UserRole.USER);

		User userEntity = User.fromAuthUser(authUser);
		ReflectionTestUtils.setField(userEntity, "id", userId);

		Todo todo = new Todo("title", "contents", "Sunny", userEntity);
		ReflectionTestUtils.setField(todo, "id", todoId);

		User managerUser = new User("managerUser@test.com", "pw", "managerUser", UserRole.USER);
		ReflectionTestUtils.setField(managerUser, "id", managerUserId);

		ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

		given(todoRepository.findById(eq(todoId))).willReturn(Optional.of(todo));
		given(userRepository.findById(eq(managerUserId))).willReturn(Optional.of(managerUser));
		Manager manager = new Manager(managerUser, todo);
		given(managerRepository.save(any(Manager.class))).willReturn(manager);

		// when
		ManagerSaveResponse managerSaveResponse = managerService.saveManager(authUser, todoId, managerSaveRequest);

		// then
		assertThat(managerSaveResponse).isNotNull();
		assertThat(managerSaveResponse.getUser().getId()).isEqualTo(managerUserId);
		assertThat(managerSaveResponse.getUser().getEmail()).isEqualTo("managerUser@test.com");
		verify(logService).logSaveManager(userId, managerUserId, todoId);
	}

	@Test
	public void saveManager_Todo가_없을_시_400_반환() {
		// given
		long todoId = 1L;
		AuthUser authUser = new AuthUser(1L, "user@test.com","user", UserRole.USER);
		ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(2L);

		given(todoRepository.findById(eq(todoId))).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> managerService.saveManager(authUser, todoId, managerSaveRequest))
			.isInstanceOf(InvalidRequestException.class)
			.hasMessage("Todo not found");
	}

	@Test
	public void saveManager_요청자가_작성자가_아닐_시_400_반환() {
		// given
		long userId = 1L;
		long ownerId = 2L;
		long todoId = 1L;

		AuthUser authUser = new AuthUser(userId, "user@test.com","user", UserRole.USER);

		User owner = new User("owner@test.com", "pw", "owner", UserRole.USER);
		ReflectionTestUtils.setField(owner, "id", ownerId);

		Todo todo = new Todo("title", "contents", "Sunny", owner);
		ReflectionTestUtils.setField(todo, "id", todoId);

		given(todoRepository.findById(eq(todoId))).willReturn(Optional.of(todo));

		// when & then
		assertThatThrownBy(() -> managerService.saveManager(authUser, todoId, new ManagerSaveRequest(userId)))
			.isInstanceOf(InvalidRequestException.class)
			.hasMessage("담당자를 등록하려고 하는 유저가 유효하지 않거나, 일정을 만든 유저가 아닙니다.");
	}

	@Test
	public void saveManager_담당자_유저가_없을_시_400_반환() {
		// given
		long ownerId = 1L;
		long todoId = 1L;
		long missingManagerId = 999L;

		AuthUser authUser = new AuthUser(ownerId, "owner@test.com", "owner", UserRole.USER);
		User owner = User.fromAuthUser(authUser);
		ReflectionTestUtils.setField(owner, "id", ownerId);

		Todo todo = new Todo("title", "contents", "Sunny", owner);
		ReflectionTestUtils.setField(todo, "id", todoId);

		given(todoRepository.findById(eq(todoId))).willReturn(Optional.of(todo));
		given(userRepository.findById(eq(missingManagerId))).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> managerService.saveManager(authUser, todoId, new ManagerSaveRequest(missingManagerId)))
			.isInstanceOf(InvalidRequestException.class)
			.hasMessage("등록하려고 하는 담당자 유저가 존재하지 않습니다.");
	}

	@Test
	public void saveManager_담당자로_자신_등록_시_400_반환() {
		// given
		long ownerId = 1L;
		long todoId = 1L;

		AuthUser authUser = new AuthUser(ownerId, "owner@test.com", "owner", UserRole.USER);
		User owner = User.fromAuthUser(authUser);
		ReflectionTestUtils.setField(owner, "id", ownerId);

		Todo todo = new Todo("title", "contents", "Sunny", owner);
		ReflectionTestUtils.setField(todo, "id", todoId);

		given(todoRepository.findById(eq(todoId))).willReturn(Optional.of(todo));
		given(userRepository.findById(eq(ownerId))).willReturn(Optional.of(owner));

		// when & then
		assertThatThrownBy(() -> managerService.saveManager(authUser, todoId, new ManagerSaveRequest(ownerId)))
			.isInstanceOf(InvalidRequestException.class)
			.hasMessage("일정 작성자는 본인을 담당자로 등록할 수 없습니다.");
	}

	@Test
	public void getManagers_성공_시_List_DTO_반환() {
		// given
		long todoId = 1L;

		User owner = new User("owner@test.com", "pw", "owner", UserRole.USER);
		ReflectionTestUtils.setField(owner, "id", 1L);

		Todo todo = new Todo("title", "contents", "Sunny", owner);
		ReflectionTestUtils.setField(todo, "id", todoId);

		User managerUser = new User("managerUser@test.com", "pw", "managerUser", UserRole.USER);
		ReflectionTestUtils.setField(managerUser, "id", 2L);

		Manager manager = new Manager(managerUser, todo);
		ReflectionTestUtils.setField(manager, "id", 3L);

		given(todoRepository.findById(eq(todoId))).willReturn(Optional.of(todo));
		given(managerRepository.findByTodoIdWithUser(eq(todoId))).willReturn(List.of(manager));

		// when
		List<ManagerResponse> list = managerService.getManagers(todoId);

		// then
		assertThat(list).hasSize(1);
		assertThat(list.get(0).getId()).isEqualTo(3L);
		assertThat(list.get(0).getUser().getId()).isEqualTo(2L);
		assertThat(list.get(0).getUser().getEmail()).isEqualTo("managerUser@test.com");
	}

	@Test
	public void getManagers_Todo_없을_시_400_반환 () {
		// given
		long todoId = 999L;
		given(todoRepository.findById(eq(todoId))).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> managerService.getManagers(todoId))
			.isInstanceOf(InvalidRequestException.class)
			.hasMessage("Todo not found");
	}

	@Test
	public void deleteManagers_성공_시_void(){
		// given
		long ownerId = 1L;
		long managerId = 2L;
		long todoId = 1L;

		AuthUser authUser = new AuthUser(ownerId, "owner@test.com", "owner", UserRole.USER);
		User owner = User.fromAuthUser(authUser);
		ReflectionTestUtils.setField(owner, "id", ownerId);

		Todo todo = new Todo("title", "contents", "Sunny", owner);
		ReflectionTestUtils.setField(todo, "id", todoId);

		User managerUser = new User("managerUser@test.com", "pw", "managerUser", UserRole.USER);
		ReflectionTestUtils.setField(managerUser, "id", 20L);

		Manager manager = new Manager(managerUser, todo);
		ReflectionTestUtils.setField(manager, "id", managerId);

		given(todoRepository.findById(eq(todoId))).willReturn(Optional.of(todo));
		given(managerRepository.findById(eq(managerId))).willReturn(Optional.of(manager));

		// when
		managerService.deleteManager(authUser, todoId, managerId);

		// then
		then(managerRepository).should().delete(eq(manager));
	}

	@Test
	public void deleteManagers_Todo_없을_시_400_반환(){
		// given
		long ownerId = 1L;
		long managerId = 2L;
		long todoId = 1L;

		AuthUser authUser = new AuthUser(ownerId, "owner@test.com", "owner", UserRole.USER);

		given(todoRepository.findById(eq(todoId))).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> managerService.deleteManager(authUser, todoId, managerId))
			.isInstanceOf(InvalidRequestException.class)
			.hasMessage("Todo not found");
	}

	@Test
	public void deleteManagers_요청자가_작성자가_아닐_시_400_반환(){
		// given
		long userId = 1L;
		long ownerId = 2L;
		long managerId = 3L;
		long todoId = 1L;

		AuthUser authUser = new AuthUser(userId, "user@test.com","user", UserRole.USER);

		User owner = new User("owner@test.com", "pw", "owner", UserRole.USER);
		ReflectionTestUtils.setField(owner, "id", ownerId);

		Todo todo = new Todo("title", "contents", "Sunny", owner);
		ReflectionTestUtils.setField(todo, "id", todoId);

		given(todoRepository.findById(eq(todoId))).willReturn(Optional.of(todo));

		// when & then
		assertThatThrownBy(() -> managerService.deleteManager(authUser, todoId, managerId))
			.isInstanceOf(InvalidRequestException.class)
			.hasMessage("해당 일정을 만든 유저가 유효하지 않습니다.");
	}

	@Test
	public void deleteManagers_Manager_없을_시_400_반환(){
		// given
		long ownerId = 1L;
		long managerId = 2L;
		long todoId = 1L;

		AuthUser authUser = new AuthUser(ownerId, "owner@test.com", "owner", UserRole.USER);
		User owner = User.fromAuthUser(authUser);
		ReflectionTestUtils.setField(owner, "id", ownerId);

		Todo todo = new Todo("title", "contents", "Sunny", owner);
		ReflectionTestUtils.setField(todo, "id", todoId);

		given(todoRepository.findById(eq(todoId))).willReturn(Optional.of(todo));
		given(managerRepository.findById(eq(managerId))).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> managerService.deleteManager(authUser, todoId, managerId))
			.isInstanceOf(InvalidRequestException.class)
			.hasMessage("Manager not found");
	}

	@Test
	public void deleteManagers_해당_일정의_Manager가_아닐_시_400_반환(){
		// given
		long ownerId = 1L;
		long managerUserId = 2L;
		long managerId = 3L;
		long myTodoId = 1L;
		long otherTodoId = 2L;

		AuthUser authUser = new AuthUser(ownerId, "owner@test.com", "owner", UserRole.USER);
		User owner = User.fromAuthUser(authUser);
		ReflectionTestUtils.setField(owner, "id", ownerId);

		Todo myTodo = new Todo("title", "contents", "Sunny", owner);
		ReflectionTestUtils.setField(myTodo, "id", myTodoId);

		Todo otherTodo = new Todo("title2", "contents2", "Sunny", owner);
		ReflectionTestUtils.setField(otherTodo, "id", otherTodoId);

		User managerUser = new User("managerUser@test.com", "pw", "managerUser", UserRole.USER);
		ReflectionTestUtils.setField(managerUser, "id", managerUserId);

		Manager manager = new Manager(managerUser, otherTodo);
		ReflectionTestUtils.setField(manager, "id", managerId);

		given(todoRepository.findById(eq(myTodoId))).willReturn(Optional.of(myTodo));
		given(managerRepository.findById(eq(managerId))).willReturn(Optional.of(manager));

		// when & then
		assertThatThrownBy(() -> managerService.deleteManager(authUser, myTodoId, managerId))
			.isInstanceOf(InvalidRequestException.class)
			.hasMessage("해당 일정에 등록된 담당자가 아닙니다.");
	}
}
