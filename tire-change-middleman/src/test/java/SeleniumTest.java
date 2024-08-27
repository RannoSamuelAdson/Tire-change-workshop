import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

public class SeleniumTest {

    private WebDriver driver;

    @BeforeEach
    public void setUp() {
        // Setup ChromeDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();

        // Set the implicit wait time to 5 seconds
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void test_submitFilters_anyVehicleAnyWorkshop() {

        driver.get("http://localhost:8080/interface.html"); // Replace with your actual URL

        // Locate the elements and input the values
        WebElement startDateInput = driver.findElement(By.id("free_timeslot_start"));
        startDateInput.sendKeys("01-01-2006");

        WebElement endDateInput = driver.findElement(By.id("free_timeslot_end"));
        endDateInput.sendKeys("01-01-2030");

        WebElement carTypeSelect = driver.findElement(By.id("car_type_filter_pick"));
        carTypeSelect.sendKeys("any");

        WebElement workshopSelect = driver.findElement(By.id("workshop_filter_pick"));
        workshopSelect.sendKeys("any");

        // Click the submit button
        WebElement submitButton = driver.findElement(By.id("submit_filters"));
        submitButton.click();

        WebElement londonCell = driver.findElement(By.xpath("//td[text()='london']"));
        WebElement manchesterCell = driver.findElement(By.xpath("//td[text()='manchester']"));
        assertTrue(londonCell.isDisplayed(), "<td>london</td> is not displayed on the page!");
        assertTrue(manchesterCell.isDisplayed(), "<td>manchester</td> is not displayed on the page!");
    }
    @Test
    public void test_submitFilters_truckVehicleAnyWorkshop() {

        driver.get("http://localhost:8080/interface.html"); // Replace with your actual URL

        // Locate the elements and input the values
        WebElement startDateInput = driver.findElement(By.id("free_timeslot_start"));
        startDateInput.sendKeys("01-01-2006");

        WebElement endDateInput = driver.findElement(By.id("free_timeslot_end"));
        endDateInput.sendKeys("01-01-2030");

        WebElement carTypeSelect = driver.findElement(By.id("car_type_filter_pick"));
        carTypeSelect.sendKeys("truck");

        WebElement workshopSelect = driver.findElement(By.id("workshop_filter_pick"));
        workshopSelect.sendKeys("any");

        // Click the submit button
        WebElement submitButton = driver.findElement(By.id("submit_filters"));
        submitButton.click();

;

        // Check for the car,truck cell
        WebElement carAndTruckCell = driver.findElement(By.xpath("//td[text()='car,truck']"));
        assertTrue(carAndTruckCell.isDisplayed(), "<td>car,truck</td> is not displayed on the page!");

        // Check if <td>car</td> does not exist
        boolean carCellAbsent;
        try {
            driver.findElement(By.xpath("//td[text()='car']"));
            carCellAbsent = false; // If found, set to false
        } catch (NoSuchElementException e) {
            carCellAbsent = true; // If not found, set to true
        }

        // Assert that <td>car</td> is absent
        assertTrue(carCellAbsent, "<td>car</td> is displayed on the page!");
    }
    @Test
    public void test_submitFilters_carVehicleAnyWorkshop() {

        driver.get("http://localhost:8080/interface.html"); // Replace with your actual URL

        // Locate the elements and input the values
        WebElement startDateInput = driver.findElement(By.id("free_timeslot_start"));
        startDateInput.sendKeys("01-01-2006");

        WebElement endDateInput = driver.findElement(By.id("free_timeslot_end"));
        endDateInput.sendKeys("01-01-2030");

        WebElement carTypeSelect = driver.findElement(By.id("car_type_filter_pick"));
        carTypeSelect.sendKeys("any");

        WebElement workshopSelect = driver.findElement(By.id("workshop_filter_pick"));
        workshopSelect.sendKeys("any");

        // Click the submit button
        WebElement submitButton = driver.findElement(By.id("submit_filters"));
        submitButton.click();

        WebElement carCell = driver.findElement(By.xpath("//td[text()='car']"));
        WebElement carAndTruckCell = driver.findElement(By.xpath("//td[text()='car,truck']"));
        assertTrue(carCell.isDisplayed(), "<td>car</td> is not displayed on the page!");
        assertTrue(carAndTruckCell.isDisplayed(), "<td>car,truck</td> is not displayed on the page!");
    }
    @Test
    public void test_submitFilters_carVehicleLondonWorkshop() {

        driver.get("http://localhost:8080/interface.html"); // Replace with your actual URL

        // Locate the elements and input the values
        WebElement startDateInput = driver.findElement(By.id("free_timeslot_start"));
        startDateInput.sendKeys("01-01-2006");

        WebElement endDateInput = driver.findElement(By.id("free_timeslot_end"));
        endDateInput.sendKeys("01-01-2030");

        WebElement carTypeSelect = driver.findElement(By.id("car_type_filter_pick"));
        carTypeSelect.sendKeys("car");

        WebElement workshopSelect = driver.findElement(By.id("workshop_filter_pick"));
        workshopSelect.sendKeys("london");

        // Click the submit button
        WebElement submitButton = driver.findElement(By.id("submit_filters"));
        submitButton.click();

        ;

        // Check for the car,truck cell
        WebElement carCell = driver.findElement(By.xpath("//td[text()='car']"));
        WebElement londonCell = driver.findElement(By.xpath("//td[text()='london']"));
        assertTrue(londonCell.isDisplayed(), "<td>london</td> is not displayed on the page!");
        assertTrue(carCell.isDisplayed(), "<td>car,truck</td> is not displayed on the page!");

        // Check if <td>car</td> does not exist
        boolean manchesterCellAbsent;
        try {
            driver.findElement(By.xpath("//td[text()='manchester']"));
            manchesterCellAbsent = false; // If found, set to false
        } catch (NoSuchElementException e) {
            manchesterCellAbsent = true; // If not found, set to true
        }

        // Assert that <td>car</td> is absent
        assertTrue(manchesterCellAbsent, "<td>manchester</td> is displayed on the page!");
    }
    @Test
    public void test_submitFilters_truckVehicleIncompatibleWorkshop() {

        driver.get("http://localhost:8080/interface.html"); // Replace with your actual URL

        // Locate the elements and input the values
        WebElement startDateInput = driver.findElement(By.id("free_timeslot_start"));
        startDateInput.sendKeys("01-01-2006");

        WebElement endDateInput = driver.findElement(By.id("free_timeslot_end"));
        endDateInput.sendKeys("01-01-2030");

        WebElement carTypeSelect = driver.findElement(By.id("car_type_filter_pick"));
        carTypeSelect.sendKeys("truck");

        WebElement workshopSelect = driver.findElement(By.id("workshop_filter_pick"));
        workshopSelect.sendKeys("london");

        // Click the submit button
        WebElement submitButton = driver.findElement(By.id("submit_filters"));
        submitButton.click();

        WebElement responseTextElement = driver.findElement(By.id("get_response_text"));
        String expectedText = "No workshops can meet these conditions";
        assertTrue(responseTextElement.getText().contains(expectedText),
                "The message 'No workshops can meet these conditions' is not displayed!");
    }
    @Test
    public void test_submitFilters_faultyDates() {

        driver.get("http://localhost:8080/interface.html"); // Replace with your actual URL

        // Locate the elements and input the values
        WebElement startDateInput = driver.findElement(By.id("free_timeslot_start"));
        startDateInput.sendKeys("01-01-2030");

        WebElement endDateInput = driver.findElement(By.id("free_timeslot_end"));
        endDateInput.sendKeys("01-01-2006");

        WebElement carTypeSelect = driver.findElement(By.id("car_type_filter_pick"));
        carTypeSelect.sendKeys("any");

        WebElement workshopSelect = driver.findElement(By.id("workshop_filter_pick"));
        workshopSelect.sendKeys("any");

        // Click the submit button
        WebElement submitButton = driver.findElement(By.id("submit_filters"));
        submitButton.click();

        WebElement responseTextElement = driver.findElement(By.id("get_response_text"));
        String expectedText = "No workshops can meet these conditions";
        assertTrue(responseTextElement.getText().contains(expectedText),
                "The message 'No workshops can meet these conditions' is not displayed!");
    }


}
