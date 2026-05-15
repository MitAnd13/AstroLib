package ru.msu.cmc.cipher.astrolib.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

final class TestBrowserFactory {
    private TestBrowserFactory() {
    }

    static WebDriver create() {
        WebDriverException lastException = null;

        try {
            EdgeOptions options = new EdgeOptions();
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1600,1200");
            return new EdgeDriver(options);
        } catch (WebDriverException exception) {
            lastException = exception;
        }

        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1600,1200");
            options.addArguments("--remote-allow-origins=*");
            return new ChromeDriver(options);
        } catch (WebDriverException exception) {
            lastException = exception;
        }

        throw new IllegalStateException("No compatible browser was found for Selenium UI tests", lastException);
    }
}
