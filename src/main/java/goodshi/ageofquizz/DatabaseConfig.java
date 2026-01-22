package goodshi.ageofquizz;

import java.util.Objects;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "goodshi.ageofquizz.repository", //
		entityManagerFactoryRef = "jpaEntityManagerFactory", transactionManagerRef = "jpaTransactionManager")
public class DatabaseConfig {

	@Value("${spring.datasource.adresse-ip}")
	private String adresseIP;

	@Value("${spring.datasource.port}")
	private String port;

	@Value("${spring.datasource.database}")
	private String database;

	@Value("${spring.datasource.username}")
	private String username;

	@Value("${spring.datasource.password}")
	private String password;

	@Value("${spring.datasource.hikari.maximum-pool-size}")
	private int maximumPoolSize;

	@Value("${spring.datasource.hikari.minimum-idle}")
	private int minimumIdle;

	@Primary
	@Bean
	public DataSource dataSourceCheminDeFer() {

		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl(
				"jdbc:mysql://" + adresseIP + ":" + port + "/" + database + "?allowPublicKeyRetrieval=true");
		hikariConfig.setUsername(username);
		hikariConfig.setPassword(password);
		hikariConfig.setMaximumPoolSize(maximumPoolSize);
		hikariConfig.setMinimumIdle(minimumIdle);

		return new HikariDataSource(hikariConfig);
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean jpaEntityManagerFactory(
			@Qualifier("dataSourceCheminDeFer") DataSource dataSource, EntityManagerFactoryBuilder builder) {
		return builder.dataSource(dataSource).packages("goodshi.ageofquizz.entity").persistenceUnit("jpa").build();
	}

	@Bean
	public PlatformTransactionManager jpaTransactionManager(
			@Qualifier("jpaEntityManagerFactory") LocalContainerEntityManagerFactoryBean jpaEntityManagerFactory) {
		return new JpaTransactionManager(Objects.requireNonNull(jpaEntityManagerFactory.getObject()));
	}
}
