/*
 * Copyright 2006-2007 the original author or authors.
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

package com.springsource.open.jms.async;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.springsource.open.foo.TransactionalMessageListenerContainer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.jdbc.JdbcTestUtils;

public class AsynchronousMessageTriggerAndRollbackTests  extends AbstractAsynchronousMessageTriggerTests {

	@Autowired
	TransactionalMessageListenerContainer container;

	@Test
	public void testBusinessFailure() throws InterruptedException {
		jmsTemplate.convertAndSend("async", "foo");
		jmsTemplate.convertAndSend("async", "bar.fail");
		Thread.sleep(100);
		container.shutdown();
		Thread.sleep(1000);
	}

	@Override
	protected void checkPostConditions() {

		// One failed and rolled back, the other committed
		assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "T_FOOS"));
		List<String> list = getMessages();
		// One message rolled back and returned to queue
		assertEquals(1, list.size());

	}

}
