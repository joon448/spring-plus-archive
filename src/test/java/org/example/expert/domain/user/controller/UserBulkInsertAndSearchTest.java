package org.example.expert.domain.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.DataSource;

import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.support.IntegrationTestSupport;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("bulk")
public class UserBulkInsertAndSearchTest extends IntegrationTestSupport {
	@Autowired
	DataSource dataSource;
	@Autowired
	PasswordEncoder passwordEncoder;

	private static final char[] CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
	private static final int BASE = CHARS.length;
	private static final int ROT = 27;
	static final long RUN_SEED = System.currentTimeMillis() >> 8;



	@Test
	@Disabled
	void insertMassiveUsers() throws Exception{
		final long TOTAL = 5_000_000L;
		final int BATCH = 10_000;
		final String password = passwordEncoder.encode("Password123!");

		try(Connection connection = dataSource.getConnection()){
			boolean prevAutoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			String query = "INSERT INTO users (email, password, nickname, user_role) VALUES (?, ?, ?, ?)";
			try(PreparedStatement psmt = connection.prepareStatement(query)){
				for(long i = 1; i <= TOTAL; i++){
					String nickname = generateNicknameWithTimeSeed(i);
					psmt.setString(1, nickname+"@test.com");
					psmt.setString(2, password);
					psmt.setString(3, nickname);
					psmt.setString(4, "USER");
					psmt.addBatch();

					if(i % BATCH==0){
						psmt.executeBatch();
						connection.commit();
					}
				}
				psmt.executeBatch();
				connection.commit();
			} catch (Exception e) {
				connection.rollback();
				throw e;
			} finally {
				connection.setAutoCommit(prevAutoCommit);
			}
		}
	}

	String generateNicknameWithTimeSeed(long n){
		long v = Long.rotateLeft(n, ROT);
		v = RUN_SEED ^ v;
		if (v == 0L) return "0";

		char[] buf = new char[12];
		int i = buf.length;

		while (Long.compareUnsigned(v, 0L) > 0) {
			long q = Long.divideUnsigned(v, BASE);
			int  r = (int) Long.remainderUnsigned(v, BASE);
			buf[--i] = CHARS[r];
			v = q;
		}
		return new String(buf, i, buf.length - i);
	}

	@Test
	void searchUsersByNickname() throws Exception{
		// given
		String nickname = "hello";
		User authUser = saveUser(nickname+"@test.com", "pw", nickname, UserRole.USER);

		// when & then
		mockMvc.perform(get("/users/search")
				.param("nickname", nickname)
				.with(addHeatherBearerToken(authUser)))
			.andExpect(status().isOk());
	}
}
