package no.kristiania.database;

import no.kristiania.httpclient.WorkerOptionsController;
import no.kristiania.httpclient.WorkerTaskOptionsController;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskDaoTest {

    private WorkerTaskDao taskDao;
    private static Random random = new Random();


    @BeforeEach
    void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:testdatabase;DB_CLOSE_DELAY=-1");

        Flyway.configure().dataSource(dataSource).load().migrate();
        taskDao = new WorkerTaskDao(dataSource);
    }

    @Test
    void shouldListAllTasks() throws SQLException {
        WorkerTask task1 = exampleTask();
        WorkerTask task2 = exampleTask();
        taskDao.insert(task1);
        taskDao.insert(task2);
        assertThat(taskDao.list())
                .extracting(WorkerTask::getName)
                .contains(task1.getName(), task2.getName());
    }


    @Test
    void shouldRetrieveAllTaskProperties() throws SQLException {
        taskDao.insert(exampleTask());
        taskDao.insert(exampleTask());
        WorkerTask task = exampleTask();
        taskDao.insert(task);
        assertThat(task).hasNoNullFieldsOrProperties();

        assertThat(taskDao.retrieve(task.getId()))
                .usingRecursiveComparison()
                .isEqualTo(task);
    }

    @Test
    void shouldReturnTasksAsOptions() throws SQLException {
        WorkerTaskOptionsController controller = new WorkerTaskOptionsController(taskDao);
        WorkerTask workerTask = exampleTask();
        taskDao.insert(workerTask);

        assertThat(controller.getBody())
                .contains("<option value=" + workerTask.getId() + ">" + workerTask.getName() + "</option>");
    }

    public static WorkerTask exampleTask() {
        WorkerTask task = new WorkerTask();
        task.setName(exampleTaskName());
        return task;
    }

    private static String exampleTaskName() {
        String[] options = {"Desk cleaning", "Code-review", "Database structure", "Office-backflip", "Wine lottery"};
        return options[random.nextInt(options.length)];
    }
}
