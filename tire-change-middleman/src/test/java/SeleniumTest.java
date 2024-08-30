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

public class SeleniumTest {

    private WebDriver driver;

    @BeforeEach
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();

        // Set the implicit wait time for the server to respond.
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void test_submitBooking_success() throws InterruptedException {
        // Arrange
        driver.get("http://localhost:8080/interface.html");

        WebElement startDateInput = driver.findElement(By.id("free_timeslot_start"));
        startDateInput.sendKeys("01-01-2006");

        WebElement endDateInput = driver.findElement(By.id("free_timeslot_end"));
        endDateInput.sendKeys("01-01-2030");

        WebElement carTypeSelect = driver.findElement(By.id("car_type_filter_pick"));
        carTypeSelect.sendKeys("any");

        WebElement workshopSelect = driver.findElement(By.id("workshop_filter_pick"));
        workshopSelect.sendKeys("any");

        WebElement submitButton = driver.findElement(By.id("submit_filters"));
        submitButton.click();

        WebElement resultsTable = driver.findElement(By.id("results_table"));
        WebElement targetElement = resultsTable.findElement(By.xpath("./tbody/tr[1]"));
        targetElement.click();

        // Act
        WebElement bookingButton = driver.findElement(By.id("submit_booking"));
        bookingButton.click();

        Thread.sleep(3000); // Waiting for the server to respond.

        WebElement postRequestResponse = driver.findElement(By.id("post_response_text"));

        //Assert
        assertEquals(postRequestResponse.getText(),"Time booked successfully");
    }

    @Test
    public void test_submitBooking_unavailableTime() throws InterruptedException {
        // Arrange
        driver.get("http://localhost:8080/interface.html");

        WebElement startDateInput = driver.findElement(By.id("free_timeslot_start"));
        startDateInput.sendKeys("01-01-2006");

        WebElement endDateInput = driver.findElement(By.id("free_timeslot_end"));
        endDateInput.sendKeys("01-01-2030");

        WebElement carTypeSelect = driver.findElement(By.id("car_type_filter_pick"));
        carTypeSelect.sendKeys("any");

        WebElement workshopSelect = driver.findElement(By.id("workshop_filter_pick"));
        workshopSelect.sendKeys("any");

        WebElement submitButton = driver.findElement(By.id("submit_filters"));
        submitButton.click();

        WebElement resultsTable = driver.findElement(By.id("results_table"));
        WebElement targetElement = resultsTable.findElement(By.xpath("./tbody/tr[1]"));
        targetElement.click();

        WebElement bookingButton = driver.findElement(By.id("submit_booking"));
        bookingButton.click(); // Book a time.

        // Act
        bookingButton.click(); // Rebook that same time.

        Thread.sleep(3000); // Waiting for the server to respond.

        WebElement postRequestResponse = driver.findElement(By.id("post_response_text"));

        // Assert
        assertEquals(postRequestResponse.getText(),"No such available timeslot exists");

    }

    @Test
    public void test_submitBooking_wrongVehicle() throws InterruptedException {
        // Arrange
        driver.get("http://localhost:8080/interface.html");

        String vehicleType = "truck";

        WebElement startDateInput = driver.findElement(By.id("free_timeslot_start"));
        startDateInput.sendKeys("01-01-2006");

        WebElement endDateInput = driver.findElement(By.id("free_timeslot_end"));
        endDateInput.sendKeys("01-01-2030");

        WebElement carTypeSelect = driver.findElement(By.id("car_type_filter_pick"));
        carTypeSelect.sendKeys("car");

        WebElement workshopSelect = driver.findElement(By.id("workshop_filter_pick"));
        workshopSelect.sendKeys("london");

        WebElement submitButton = driver.findElement(By.id("submit_filters"));
        submitButton.click();

        WebElement resultsTable = driver.findElement(By.id("results_table"));
        WebElement targetElement = resultsTable.findElement(By.xpath("./tbody/tr[1]"));
        targetElement.click();

        WebElement bookingCarTypeSelect = driver.findElement(By.id("car_type_pick"));
        bookingCarTypeSelect.sendKeys("truck"); // Picking a car type that is not supported by that workshop.

        WebElement bookingButton = driver.findElement(By.id("submit_booking"));

        // Act
        bookingButton.click();

        Thread.sleep(3000); // Waiting for the server to respond.

        WebElement postRequestResponse = driver.findElement(By.id("post_response_text"));

        // Assert
        assertEquals(postRequestResponse.getText(),"This workshop does not service the vehicle type of " + vehicleType);

    }

    @Test
    public void test_submitFilters_anyVehicleAnyWorkshop() {
        // Arrange
        driver.get("http://localhost:8080/interface.html");

        WebElement startDateInput = driver.findElement(By.id("free_timeslot_start"));
        startDateInput.sendKeys("01-01-2006");

        WebElement endDateInput = driver.findElement(By.id("free_timeslot_end"));
        endDateInput.sendKeys("01-01-2030");

        WebElement carTypeSelect = driver.findElement(By.id("car_type_filter_pick"));
        carTypeSelect.sendKeys("any");

        WebElement workshopSelect = driver.findElement(By.id("workshop_filter_pick"));
        workshopSelect.sendKeys("any");

        WebElement submitButton = driver.findElement(By.id("submit_filters"));

        // Act
        submitButton.click();

        WebElement londonCell = driver.findElement(By.xpath("//td[text()='London']"));
        WebElement manchesterCell = driver.findElement(By.xpath("//td[text()='Manchester']"));

        // Assert
        assertTrue(londonCell.isDisplayed(), "<td>london</td> is not displayed on the page!");
        assertTrue(manchesterCell.isDisplayed(), "<td>manchester</td> is not displayed on the page!");
    }

    @Test
    public void test_submitFilters_truckVehicleAnyWorkshop() {
        // Arrange
        driver.get("http://localhost:8080/interface.html");

        WebElement startDateInput = driver.findElement(By.id("free_timeslot_start"));
        startDateInput.sendKeys("01-01-2006");

        WebElement endDateInput = driver.findElement(By.id("free_timeslot_end"));
        endDateInput.sendKeys("01-01-2030");

        WebElement carTypeSelect = driver.findElement(By.id("car_type_filter_pick"));
        carTypeSelect.sendKeys("truck");

        WebElement workshopSelect = driver.findElement(By.id("workshop_filter_pick"));
        workshopSelect.sendKeys("any");

        WebElement submitButton = driver.findElement(By.id("submit_filters"));

        // Act
        submitButton.click();

;

        // Check for the car,truck cell
        WebElement carAndTruckCell = driver.findElement(By.xpath("//td[text()='car,truck']"));
        assertTrue(carAndTruckCell.isDisplayed(), "<td>car,truck</td> is not displayed on the page!");

        // Check, that <td>car</td> does not exist.
        boolean carCellAbsent;
        try {
            driver.findElement(By.xpath("//td[text()='car']"));
            carCellAbsent = false; // If found, set to false.
        } catch (NoSuchElementException e) {
            carCellAbsent = true; // If not found, set to true.
        }

        // Assert that <td>car</td> is absent.
        assertTrue(carCellAbsent, "<td>car</td> is displayed on the page!");
    }

    @Test
    public void test_submitFilters_carVehicleAnyWorkshop() {
        // Arrange
        driver.get("http://localhost:8080/interface.html");

        WebElement startDateInput = driver.findElement(By.id("free_timeslot_start"));
        startDateInput.sendKeys("01-01-2006");

        WebElement endDateInput = driver.findElement(By.id("free_timeslot_end"));
        endDateInput.sendKeys("01-01-2030");

        WebElement carTypeSelect = driver.findElement(By.id("car_type_filter_pick"));
        carTypeSelect.sendKeys("any");

        WebElement workshopSelect = driver.findElement(By.id("workshop_filter_pick"));
        workshopSelect.sendKeys("any");

        WebElement submitButton = driver.findElement(By.id("submit_filters"));

        // Act
        submitButton.click();

        WebElement carCell = driver.findElement(By.xpath("//td[text()='car']"));
        WebElement carAndTruckCell = driver.findElement(By.xpath("//td[text()='car,truck']"));

        // Assert
        assertTrue(carCell.isDisplayed(), "<td>car</td> is not displayed on the page!");
        assertTrue(carAndTruckCell.isDisplayed(), "<td>car,truck</td> is not displayed on the page!");
    }

    @Test
    public void test_submitFilters_carVehicleLondonWorkshop() {
        // Arrange
        driver.get("http://localhost:8080/interface.html");

        WebElement startDateInput = driver.findElement(By.id("free_timeslot_start"));
        startDateInput.sendKeys("01-01-2006");

        WebElement endDateInput = driver.findElement(By.id("free_timeslot_end"));
        endDateInput.sendKeys("01-01-2030");

        WebElement carTypeSelect = driver.findElement(By.id("car_type_filter_pick"));
        carTypeSelect.sendKeys("car");

        WebElement workshopSelect = driver.findElement(By.id("workshop_filter_pick"));
        workshopSelect.sendKeys("london");

        WebElement submitButton = driver.findElement(By.id("submit_filters"));

        // Act
        submitButton.click();

        // Check for the car,truck cell
        WebElement carCell = driver.findElement(By.xpath("//td[text()='car']"));
        WebElement londonCell = driver.findElement(By.xpath("//td[text()='London']"));
        assertTrue(londonCell.isDisplayed(), "<td>london</td> is not displayed on the page!");
        assertTrue(carCell.isDisplayed(), "<td>car,truck</td> is not displayed on the page!");

        boolean manchesterCellAbsent;
        try {
            driver.findElement(By.xpath("//td[text()='manchester']"));
            manchesterCellAbsent = false; // If found, set to false.
        } catch (NoSuchElementException e) {
            manchesterCellAbsent = true; // If not found, set to true.
        }

        assertTrue(manchesterCellAbsent, "<td>manchester</td> is displayed on the page!");
    }

    @Test
    public void test_submitFilters_truckVehicleIncompatibleWorkshop() {
        // Arrange
        driver.get("http://localhost:8080/interface.html");

        WebElement startDateInput = driver.findElement(By.id("free_timeslot_start"));
        startDateInput.sendKeys("01-01-2006");

        WebElement endDateInput = driver.findElement(By.id("free_timeslot_end"));
        endDateInput.sendKeys("01-01-2030");

        WebElement carTypeSelect = driver.findElement(By.id("car_type_filter_pick"));
        carTypeSelect.sendKeys("truck");

        WebElement workshopSelect = driver.findElement(By.id("workshop_filter_pick"));
        workshopSelect.sendKeys("london");

        WebElement submitButton = driver.findElement(By.id("submit_filters"));

        // Act
        submitButton.click();

        WebElement responseTextElement = driver.findElement(By.id("get_response_text"));
        String expectedText = "No workshops can meet these conditions";

        // Assert
        assertTrue(responseTextElement.getText().contains(expectedText),
                "The message 'No workshops can meet these conditions' is not displayed!");
    }

    @Test
    public void test_submitFilters_faultyDates() {
        // Arrange
        driver.get("http://localhost:8080/interface.html");

        WebElement startDateInput = driver.findElement(By.id("free_timeslot_start"));
        startDateInput.sendKeys("01-01-2030");

        WebElement endDateInput = driver.findElement(By.id("free_timeslot_end"));
        endDateInput.sendKeys("01-01-2006");

        WebElement carTypeSelect = driver.findElement(By.id("car_type_filter_pick"));
        carTypeSelect.sendKeys("any");

        WebElement workshopSelect = driver.findElement(By.id("workshop_filter_pick"));
        workshopSelect.sendKeys("any");

        WebElement submitButton = driver.findElement(By.id("submit_filters"));

        // Act
        submitButton.click();

        WebElement responseTextElement = driver.findElement(By.id("get_response_text"));
        String expectedText = "No workshops can meet these conditions";

        // Assert
        assertTrue(responseTextElement.getText().contains(expectedText),
                "The message 'No workshops can meet these conditions' is not displayed!");
    }
}
