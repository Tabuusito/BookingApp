package infrastructure.config;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class DataSourceConfig {

    @Autowired
    private Environment env;

    //Sin anotaciones para que no sobreescriba la configuración automática, clase provisional
    public DataSource customDataSource() {
        String dbUrl = "jdbc:mysql://db:3306/reservas?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String dbUsername = "myuser";
        String dbPassword = "secret";
        String dbDriverClassName = env.getProperty("SPRING_DATASOURCE_DRIVER_CLASS_NAME", "com.mysql.cj.jdbc.Driver"); // Valor por defecto

        System.out.println(">>>>>>>>>> CONFIGURANDO customDataSource <<<<<<<<<<");
        System.out.println("DB URL: (hardcoded)" + dbUrl);
        System.out.println("DB Username (hardcoded): " + dbUsername);
        System.out.println("DB Driver (from env): " + dbDriverClassName);

        if (dbUrl == null || dbUsername == null || dbPassword == null) {
            System.err.println("ERROR: Faltan una o más propiedades de DataSource en el entorno!");
            throw new IllegalStateException("Configuración de DataSource incompleta. Verifica las variables de entorno.");
        }

        return DataSourceBuilder.create()
                .url(dbUrl)
                .username(dbUsername)
                .password(dbPassword)
                .driverClassName(dbDriverClassName)
                .build();
    }
}
