package com.springsource.open.foo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class FooHandler implements Handler {

	private static final Log log = LogFactory.getLog(FooHandler.class);
	private JdbcTemplate jdbcTemplate;
	private JmsTemplate jmsTemplate;
	private TransactionTemplate transactionTemplate;
	private AtomicInteger count = new AtomicInteger(0);

	@Autowired
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	@Autowired
	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public void handle(final String msg) {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				log.debug("Received message: [" + msg + "]");

				Date date = new Date();
				jdbcTemplate.update(
						"INSERT INTO T_FOOS (ID, name, foo_date) values (?, ?,?)", count.getAndIncrement(), msg, date);

				log.debug(String
						.format("Inserted foo with name=%s, date=%s", msg, date));

				if (msg.contains("next")) {
					jmsTemplate.convertAndSend("async", "bar");
				}
			}
		});
	}

	public void resetItemCount() {
		count.set(0);
	}

	public int getItemCount() {
		return count.get();
	}

}
