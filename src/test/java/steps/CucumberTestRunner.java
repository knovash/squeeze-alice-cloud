//package steps;

package steps;  // Пакет для тестов

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)  // Указываем JUnit использовать Cucumber runner
@CucumberOptions(
        features = "src/test/resources/features",  // Путь к feature-файлам
        glue = "org.knovash.alicebroker.test",      // Пакет с step definitions
        plugin = {                                  // Плагины для отчётов
                "pretty",                           // Красивое оформление в консоли
                "html:target/cucumber-reports.html",// HTML отчёт
                "json:target/cucumber-report.json"  // JSON отчёт для CI
        },
        tags = "@smoke",                            // Фильтрация тестов по тегам
        monochrome = true                          // Чёрно-белый вывод в консоль
//        strict = true                               // Провал теста при нереализованных шагах
)
public class CucumberTestRunner {
}