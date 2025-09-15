package org.example.expert.domain.todo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.comment.entity.QComment.comment;

import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TodoRepositoryImpl implements TodoRepositoryCustom {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Optional<Todo> findByIdWithUser(Long todoId) {
		Todo foundTodo = jpaQueryFactory
			.selectFrom(todo)
			.join(todo.user, user)
			.fetchJoin()
			.where(todo.id.eq(todoId))
			.fetchOne();
		return Optional.ofNullable(foundTodo);
	}

	@Override
	public Page<TodoSearchResponse> searchTodosOrderByCreatedAtDesc(String title, LocalDateTime startTime,
		LocalDateTime endTimeExclusive, String managerNickname, Pageable pageable) {
		List<TodoSearchResponse> todos = jpaQueryFactory.select(Projections.constructor(
				TodoSearchResponse.class,
				todo.title,
				manager.id.countDistinct(),
				comment.id.countDistinct()
			))
			.from(todo)
			.leftJoin(todo.managers, manager)
			.leftJoin(manager.user, user)
			.leftJoin(todo.comments, comment)
			.where(
				containTitle(title),
				afterStartTime(startTime),
				beforeEndTimeExclusive(endTimeExclusive),
				containManagerNickname(managerNickname)
			)
			.groupBy(todo.id, todo.title)
			.orderBy(todo.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = jpaQueryFactory.select(todo.id.countDistinct())
			.from(todo)
			.leftJoin(todo.managers, manager)
			.leftJoin(manager.user, user)
			.where(
				containTitle(title),
				afterStartTime(startTime),
				beforeEndTimeExclusive(endTimeExclusive),
				containManagerNickname(managerNickname)
			)
			.fetchOne();
		return new PageImpl<>(todos, pageable, total == null ? 0: total);
	}

	private BooleanExpression containTitle(String title){
		return (title == null || title.isBlank())? null : todo.title.containsIgnoreCase(title);
	}

	private BooleanExpression afterStartTime(LocalDateTime startTime){
		return (startTime == null) ? null : todo.createdAt.goe(startTime);
	}

	private BooleanExpression beforeEndTimeExclusive(LocalDateTime endTimeExclusive){
		return (endTimeExclusive == null) ? null : todo.createdAt.lt(endTimeExclusive);
	}

	private BooleanExpression containManagerNickname(String managerNickname){
		return (managerNickname == null || managerNickname.isBlank())? null : user.nickname.containsIgnoreCase(managerNickname);
	}

}
