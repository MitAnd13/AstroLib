package ru.msu.cmc.cipher.astrolib.selenium;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import ru.msu.cmc.cipher.astrolib.dao.AstroObjectDAO;
import ru.msu.cmc.cipher.astrolib.dao.EventDAO;
import ru.msu.cmc.cipher.astrolib.models.AstroObjects;
import ru.msu.cmc.cipher.astrolib.models.Events;
import ru.msu.cmc.cipher.astrolib.models.ObjectsToEvents;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AstrolibSeleniumTest {

    private static final String ECLIPSE = "\u0417\u0430\u0442\u043c\u0435\u043d\u0438\u0435";
    private static final String CONJUNCTION = "\u0421\u043e\u0435\u0434\u0438\u043d\u0435\u043d\u0438\u0435";
    private static final String UNIQUE = "\u0423\u043d\u0438\u043a\u0430\u043b\u044c\u043d\u043e\u0435";
    private static final String EARTH_LIKE = "\u0417\u0435\u043c\u043d\u043e\u0439 \u0433\u0440\u0443\u043f\u043f\u044b";
    private static final String EVENT_SOURCE = "\u0418\u0441\u0442\u043e\u0447\u043d\u0438\u043a \u044f\u0432\u043b\u0435\u043d\u0438\u044f";
    private static final String OBSERVED_OBJECT = "\u041d\u0430\u0431\u043b\u044e\u0434\u0430\u0435\u043c\u044b\u0439 \u043e\u0431\u044a\u0435\u043a\u0442";
    private static final String OBJECT_SUCCESS = "\u041e\u0431\u044a\u0435\u043a\u0442 \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d \u0432 \u0431\u0430\u0437\u0443 \u0434\u0430\u043d\u043d\u044b\u0445";
    private static final String EVENT_SUCCESS = "\u042f\u0432\u043b\u0435\u043d\u0438\u0435 \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u043e \u0432 \u0431\u0430\u0437\u0443 \u0434\u0430\u043d\u043d\u044b\u0445";
    private static final String OBJECT_NAME_REQUIRED = "\u0423\u043a\u0430\u0436\u0438\u0442\u0435 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u043e\u0431\u044a\u0435\u043a\u0442\u0430";
    private static final String RELATED_OBJECT_REQUIRED = "\u0414\u043e\u0431\u0430\u0432\u044c\u0442\u0435 \u0445\u043e\u0442\u044f \u0431\u044b \u043e\u0434\u0438\u043d \u0441\u0432\u044f\u0437\u0430\u043d\u043d\u044b\u0439 \u043e\u0431\u044a\u0435\u043a\u0442";
    private static final String OBJECT_RESULTS_EMPTY = "\u041e\u0431\u044a\u0435\u043a\u0442\u044b \u043f\u043e \u044d\u0442\u043e\u043c\u0443 \u0437\u0430\u043f\u0440\u043e\u0441\u0443 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u044b.";
    private static final String EVENT_RESULTS_EMPTY = "\u042f\u0432\u043b\u0435\u043d\u0438\u044f \u043f\u043e \u044d\u0442\u043e\u043c\u0443 \u0437\u0430\u043f\u0440\u043e\u0441\u0443 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u044b.";
    private static final String OBJECT_SEARCH_INITIAL = "\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0442\u0438\u043f \u043e\u0431\u044a\u0435\u043a\u0442\u0430 \u0438 \u0437\u0430\u0434\u0430\u0439\u0442\u0435 \u0444\u0438\u043b\u044c\u0442\u0440\u044b \u0434\u043b\u044f \u043f\u043e\u0438\u0441\u043a\u0430.";
    private static final String FILTER_RESULTS_EMPTY = "\u041f\u043e \u044d\u0442\u0438\u043c \u0444\u0438\u043b\u044c\u0442\u0440\u0430\u043c \u043d\u0438\u0447\u0435\u0433\u043e \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e.";
    private static final String EVENT_SEARCH_INITIAL = "\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0445\u043e\u0442\u044f \u0431\u044b \u043e\u0434\u0438\u043d \u0444\u0438\u043b\u044c\u0442\u0440 \u0434\u043b\u044f \u043f\u043e\u0438\u0441\u043a\u0430 \u044f\u0432\u043b\u0435\u043d\u0438\u0439.";
    private static final String EVENT_FILTER_EMPTY = "\u041f\u043e \u044d\u0442\u0438\u043c \u0444\u0438\u043b\u044c\u0442\u0440\u0430\u043c \u044f\u0432\u043b\u0435\u043d\u0438\u044f \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u044b.";
    private static final String PARENT_STAR_NOT_FOUND = "\u0420\u043e\u0434\u0438\u0442\u0435\u043b\u044c\u0441\u043a\u0430\u044f \u0437\u0432\u0435\u0437\u0434\u0430 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430";
    private static final String EVENT_DATE_ORDER_ERROR = "\u0414\u0430\u0442\u0430 \u043e\u043a\u043e\u043d\u0447\u0430\u043d\u0438\u044f \u043d\u0435 \u043c\u043e\u0436\u0435\u0442 \u0431\u044b\u0442\u044c \u0440\u0430\u043d\u044c\u0448\u0435 \u0434\u0430\u0442\u044b \u043d\u0430\u0447\u0430\u043b\u0430";
    private static final String LINK_ROLE_REQUIRED = "\u0423\u043a\u0430\u0436\u0438\u0442\u0435 \u0440\u043e\u043b\u044c \u0441\u0432\u044f\u0437\u0430\u043d\u043d\u043e\u0433\u043e \u043e\u0431\u044a\u0435\u043a\u0442\u0430";

    @LocalServerPort
    private int port;

    @Autowired
    private AstroObjectDAO astroObjectDAO;

    @Autowired
    private EventDAO eventDAO;

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @BeforeAll
    void setUp() {
        driver = TestBrowserFactory.create();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        baseUrl = "http://localhost:" + port;
    }

    @AfterAll
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void resetBrowserState() {
        driver.manage().deleteAllCookies();
    }

    @Test
    void nameSearchShouldOpenObjectAndEventCards() {
        String token = uniqueToken();
        AstroObjects star = insertStar("Selenium Star " + token);
        Events event = insertEvent("Selenium Event " + token, ECLIPSE, star, EVENT_SOURCE);

        open("/search/name");
        type(By.id("query"), token);
        click(By.cssSelector("button[type='submit']"));

        waitForText("Selenium Star " + token);
        waitForText("Selenium Event " + token);

        click(By.cssSelector("a[href='/objects/" + star.getId() + "']"));
        wait.until(ExpectedConditions.urlContains("/objects/" + star.getId()));
        waitForText("Selenium Star " + token);

        open("/search/name?query=" + token);
        waitForText("Selenium Star " + token);
        waitForText("Selenium Event " + token);

        click(By.cssSelector("a[href='/events/" + event.getId() + "']"));
        wait.until(ExpectedConditions.urlContains("/events/" + event.getId()));
        waitForText("Selenium Event " + token);
        waitForText(EVENT_SOURCE);
        waitForText("Selenium Star " + token);
    }

    @Test
    void nameSearchShouldShowEmptyStateForUnknownQuery() {
        open("/search/name");
        type(By.id("query"), "missing-" + uniqueToken());
        click(By.cssSelector("button[type='submit']"));

        waitForText(OBJECT_RESULTS_EMPTY);
        waitForText(EVENT_RESULTS_EMPTY);
    }

    @Test
    void objectFilterSearchShouldShowRelevantFiltersAndOpenDetailPage() {
        String token = uniqueToken();
        AstroObjects star = insertStar("Filter Star " + token);

        open("/search/objects");
        new Select(find(By.id("objectKind"))).selectByValue("star");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("starSpectre")));
        assertTrue(find(By.id("starSpectre")).isDisplayed());
        assertFalse(find(By.id("cometClass")).isDisplayed());

        click(By.cssSelector("button[type='submit']"));

        waitForText("Filter Star " + token);
        click(By.cssSelector("a[href='/objects/" + star.getId() + "']"));
        wait.until(ExpectedConditions.urlContains("/objects/" + star.getId()));
        waitForText("Filter Star " + token);
    }

    @Test
    void objectFilterSearchShouldShowInitialPromptWithoutType() {
        open("/search/objects");
        waitForText(OBJECT_SEARCH_INITIAL);
    }

    @Test
    void objectFilterSearchShouldShowEmptyStateForUnmatchedFilters() {
        open("/search/objects");
        new Select(find(By.id("objectKind"))).selectByValue("planet");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("planetParentStar")));
        type(By.id("planetParentStar"), "Missing Parent " + uniqueToken());
        click(By.cssSelector("button[type='submit']"));

        waitForText(FILTER_RESULTS_EMPTY);
    }

    @Test
    void eventFilterSearchShouldFindEventByLinkedObject() {
        String token = uniqueToken();
        AstroObjects star = insertStar("Event Filter Star " + token);
        Events event = insertEvent("Event Filter " + token, CONJUNCTION, star, OBSERVED_OBJECT);

        open("/search/events");
        new Select(find(By.id("eventType"))).selectByValue(CONJUNCTION);
        type(By.id("linkedObjectName"), star.getName());
        click(By.cssSelector("button[type='submit']"));

        waitForText("Event Filter " + token);
        click(By.cssSelector("a[href='/events/" + event.getId() + "']"));
        wait.until(ExpectedConditions.urlContains("/events/" + event.getId()));
        waitForText(OBSERVED_OBJECT);
        waitForText(star.getName());
    }

    @Test
    void eventFilterSearchShouldShowInitialPromptWithoutFilters() {
        open("/search/events");
        waitForText(EVENT_SEARCH_INITIAL);
    }

    @Test
    void eventFilterSearchShouldShowEmptyStateForUnknownLinkedObject() {
        open("/search/events");
        type(By.id("linkedObjectName"), "Missing Event Object " + uniqueToken());
        click(By.cssSelector("button[type='submit']"));

        waitForText(EVENT_FILTER_EMPTY);
    }

    @Test
    void discoveryFormShouldCreateObject() {
        String token = uniqueToken();
        String name = "Created Object " + token;

        open("/discoveries/new");
        type(By.id("name"), name);
        type(By.id("catalogId"), "CAT-" + token);
        setDate(By.id("foundDate"), "2026-05-14");
        type(By.id("discoverer"), "Selenium");
        type(By.id("massMantissa"), "1.25");
        type(By.id("massExponent"), "10");
        click(By.cssSelector("input[name='objectKind'][value='star'] + span"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("starSpectre")));
        new Select(find(By.id("starSpectre"))).selectByValue("G");
        new Select(find(By.id("starLight"))).selectByValue("V");
        type(By.id("starCount"), "1");
        type(By.id("rightAscension"), "12.5");
        type(By.id("declension"), "10.5");
        type(By.id("sunDistance"), "150");
        type(By.id("constellation"), "Orion");
        type(By.id("notes"), "Created from selenium");
        click(By.cssSelector("button[type='submit']"));

        waitForText(OBJECT_SUCCESS);
        assertNotNull(astroObjectDAO.getByName(name));
    }

    @Test
    void discoveryFormShouldShowObjectValidationError() {
        open("/discoveries/new");
        click(By.cssSelector("button[type='submit']"));

        waitForText(OBJECT_NAME_REQUIRED);
    }

    @Test
    void discoveryFormShouldRejectPlanetWithUnknownParentStar() {
        String token = uniqueToken();

        open("/discoveries/new");
        type(By.id("name"), "Broken Planet " + token);
        type(By.id("catalogId"), "CAT-" + token);
        setDate(By.id("foundDate"), "2026-05-14");
        type(By.id("discoverer"), "Selenium");
        type(By.id("massMantissa"), "5.1");
        type(By.id("massExponent"), "23");
        click(By.cssSelector("input[name='objectKind'][value='planet'] + span"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("planetType")));
        new Select(find(By.id("planetType"))).selectByValue(EARTH_LIKE);
        type(By.id("planetParentStar"), "Unknown Star " + token);
        type(By.id("semiaxis"), "1");
        type(By.id("eccentricity"), "0.1");
        type(By.id("inclination"), "5");
        type(By.id("longitudeOfAscAngle"), "12");
        type(By.id("minVelocity"), "10");
        type(By.id("maxVelocity"), "20");
        type(By.id("minLight"), "1");
        type(By.id("maxLight"), "2");
        type(By.id("notes"), "Invalid parent star");
        click(By.cssSelector("button[type='submit']"));

        waitForText(PARENT_STAR_NOT_FOUND);
    }

    @Test
    void discoveryFormShouldCreateEvent() {
        String token = uniqueToken();
        AstroObjects star = insertStar("Linked Star " + token);
        String eventName = "Created Event " + token;

        open("/discoveries/new");
        click(By.cssSelector("input[name='discoveryKind'][value='event'] + span"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("eventType")));
        type(By.id("name"), eventName);
        new Select(find(By.id("eventType"))).selectByValue(ECLIPSE);
        new Select(find(By.id("periodicity"))).selectByValue(UNIQUE);
        setDate(By.id("eventStart"), "2026-05-14");
        setDate(By.id("eventEnd"), "2026-05-15");
        type(By.id("linkedObjectNames0"), star.getName());
        new Select(find(By.id("linkedObjectRoles0"))).selectByValue(EVENT_SOURCE);
        type(By.id("notes"), "Created event from selenium");
        click(By.cssSelector("button[type='submit']"));

        waitForText(EVENT_SUCCESS);
        assertNotNull(eventDAO.getByName(eventName));
    }

    @Test
    void discoveryFormShouldShowEventValidationError() {
        open("/discoveries/new");
        click(By.cssSelector("input[name='discoveryKind'][value='event'] + span"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("eventType")));
        type(By.id("name"), "Invalid Event " + uniqueToken());
        new Select(find(By.id("eventType"))).selectByValue(ECLIPSE);
        new Select(find(By.id("periodicity"))).selectByValue(UNIQUE);
        setDate(By.id("eventStart"), "2026-05-14");
        setDate(By.id("eventEnd"), "2026-05-15");
        type(By.id("notes"), "Missing linked object");
        click(By.cssSelector("button[type='submit']"));

        waitForText(RELATED_OBJECT_REQUIRED);
    }

    @Test
    void discoveryFormShouldRejectEventWithEndBeforeStart() {
        open("/discoveries/new");
        click(By.cssSelector("input[name='discoveryKind'][value='event'] + span"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("eventType")));
        type(By.id("name"), "Invalid Dates " + uniqueToken());
        new Select(find(By.id("eventType"))).selectByValue(ECLIPSE);
        new Select(find(By.id("periodicity"))).selectByValue(UNIQUE);
        setDate(By.id("eventStart"), "2026-05-15");
        setDate(By.id("eventEnd"), "2026-05-14");
        type(By.id("notes"), "Broken dates");
        click(By.cssSelector("button[type='submit']"));

        waitForText(EVENT_DATE_ORDER_ERROR);
    }

    @Test
    void discoveryFormShouldRejectEventWithMissingLinkedObjectRole() {
        String token = uniqueToken();
        AstroObjects star = insertStar("Roleless Star " + token);

        open("/discoveries/new");
        click(By.cssSelector("input[name='discoveryKind'][value='event'] + span"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("eventType")));
        type(By.id("name"), "Missing Role " + token);
        new Select(find(By.id("eventType"))).selectByValue(ECLIPSE);
        new Select(find(By.id("periodicity"))).selectByValue(UNIQUE);
        setDate(By.id("eventStart"), "2026-05-14");
        setDate(By.id("eventEnd"), "2026-05-15");
        type(By.id("linkedObjectNames0"), star.getName());
        type(By.id("notes"), "Missing role");
        click(By.cssSelector("button[type='submit']"));

        waitForText(LINK_ROLE_REQUIRED);
    }

    AstroObjects insertStar(String name) {
        AstroObjects star = new AstroObjects(name, AstroObjects.ObjType.STAR);
        star.setStar_spectre('G');
        star.setStar_light("V");
        star.setStar_count(1);
        star.setCatalog_id("CAT-" + uniqueToken());
        star.setFound_date(LocalDate.of(2026, 5, 14));
        star.setFound_name("Seeder");
        star.setNotes("Seeded star");
        astroObjectDAO.insertTyped(star);
        return astroObjectDAO.getByName(name);
    }

    Events insertEvent(String name, String type, AstroObjects object, String role) {
        Events event = new Events(name, type);
        event.setCatalog_id(UNIQUE);
        event.setStart_date(LocalDate.of(2026, 5, 14));
        event.setEnd_date(LocalDate.of(2026, 5, 15));
        event.setNotes("Seeded event");
        ObjectsToEvents link = new ObjectsToEvents(object, event, role);
        eventDAO.insert(event, List.of(link));
        return eventDAO.getByName(name);
    }

    private void open(String path) {
        driver.get(baseUrl + path);
    }

    private WebElement find(By by) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }

    private void click(By by) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(by));
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});",
            element
        );

        try {
            wait.until(ExpectedConditions.elementToBeClickable(by)).click();
        } catch (ElementClickInterceptedException exception) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    private void type(By by, String value) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        element.clear();
        element.sendKeys(value);
    }

    private void setDate(By by, String value) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(by));
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input', {bubbles: true})); arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
            element,
            value
        );
    }

    private void waitForText(String text) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), text));
    }

    private String uniqueToken() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
