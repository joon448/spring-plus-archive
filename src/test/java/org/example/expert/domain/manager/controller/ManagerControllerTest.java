package org.example.expert.domain.manager.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.support.ControllerTestSupport;
import org.example.expert.support.WithMockAuthUser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

@WebMvcTest(ManagerController.class)
public class ManagerControllerTest extends ControllerTestSupport {

	@MockBean
	private ManagerService managerService;

	@Test
	@WithMockAuthUser(userId = 1L, email = "admin@example.com", nickname = "admin", userRole = UserRole.ADMIN)
	void manager_생성에_성공한다() throws Exception {
		// given
		long managerUserId = 2L;
		long managerId = 3L;
		long todoId = 1L;

		UserResponse managerUser = new UserResponse(managerUserId, "managerUser@example.com");
		ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);
		ManagerSaveResponse managerSaveResponse = new ManagerSaveResponse(managerId, managerUser);

		given(managerService.saveManager(any(AuthUser.class), eq(todoId), any(ManagerSaveRequest.class)))
			.willReturn(managerSaveResponse);

		// when & then
		mockMvc.perform(post("/todos/{todoId}/managers", todoId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(managerSaveRequest))
				.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(managerId))
			.andExpect(jsonPath("$.user.id").value(managerUserId));
	}

	@Test
	@WithMockAuthUser(userId = 1L, email = "admin@example.com", nickname = "admin", userRole = UserRole.ADMIN)
	void getManagers_성공_시_List_DTO_반환() throws Exception {
		// given
		long todoId = 1L;

		long managerId1 = 3L;
		long managerUserId1 = 2L;
		long managerId2 = 4L;
		long managerUserId2 = 5L;

		ManagerResponse manager1 = new ManagerResponse(
			managerId1,
			new UserResponse(managerUserId1, "user1@test.com")
		);
		ManagerResponse manager2 = new ManagerResponse(
			managerId2,
			new UserResponse(managerUserId2, "user2@test.com")
		);

		given(managerService.getManagers(eq(todoId))).willReturn(List.of(manager1, manager2));

		// when & then
		mockMvc.perform(get("/todos/{todoId}/managers", todoId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].id").value(managerId1))
			.andExpect(jsonPath("$[0].user.id").value(managerUserId1))
			.andExpect(jsonPath("$[0].user.email").value("user1@test.com"))
			.andExpect(jsonPath("$[1].id").value(managerId2))
			.andExpect(jsonPath("$[1].user.id").value(managerUserId2))
			.andExpect(jsonPath("$[1].user.email").value("user2@test.com"));
	}

	@Test
	@WithMockAuthUser(userId = 1L, email = "admin@example.com", nickname = "admin", userRole = UserRole.ADMIN)
	void manager_삭제에_성공한다() throws Exception {
		// given
		long todoId = 1L;
		long managerId = 2L;

		willDoNothing().given(managerService)
			.deleteManager(any(AuthUser.class), eq(todoId), eq(managerId));

		// when & then
		mockMvc.perform(delete("/todos/{todoId}/managers/{managerId}", todoId, managerId)
				.contentType(MediaType.APPLICATION_JSON)
				.with(csrf()))
			.andExpect(status().isOk());
	}
}
