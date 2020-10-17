/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.actuator.log4j2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link SampleActuatorLog4J2Application}.
 *
 * @author Dave Syer
 * @author Stephane Nicoll
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SampleActuatorLog4J2ApplicationTests {

	/**
	 * 日志记录器的小写变量名
	 */
	private static final Logger logger = LogManager
			.getLogger(SampleActuatorLog4J2ApplicationTests.class);

	/**
	 * 日志输出捕获
	 */
	@Rule
	public OutputCapture output = new OutputCapture();

	/**
	 * 模拟的MVC
	 */
	@Autowired
	private MockMvc mvc;

	@Test
	public void testLogger() {
		// 验证日志信息
		logger.info("Hello World");
		this.output.expect(containsString("Hello World"));
	}

	@Test
	public void validateLoggersEndpoint() throws Exception {
		// 校验日志记录器的端点配置
		this.mvc.perform(get("/loggers/org.apache.coyote.http11.Http11NioProtocol"))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("{\"configuredLevel\":\"WARN\","
						+ "\"effectiveLevel\":\"WARN\"}")));
	}

}
