package p466.studygroupfinder;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
// load the full application context
@SpringBootTest
class StudyGroupFinderApplicationTests {
	// test passes only if startup configuration succeeds
	// test application.properties file prevents test from touching file based demo db
	@Test
	void contextLoads() {
	}

}
