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

package com.springsource.open.jms.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/META-INF/spring/jms-context.xml")
public class DatasourceTests {

	private JdbcTemplate JdbcTemplate;

	@Autowired
	public void setDataSource(@Qualifier("dataSource") DataSource dataSource) {
		this.JdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Transactional @Test
	public void testTemplate() throws Exception {
		JdbcTemplate.execute("delete from T_FOOS");
		int count = JdbcTemplate.queryForObject("select count(*) from T_FOOS", Integer.class);
		assertEquals(0, count);

		JdbcTemplate.update("INSERT into T_FOOS (id,name,foo_date) values (?,?,null)", 0, "foo");
	}
}
