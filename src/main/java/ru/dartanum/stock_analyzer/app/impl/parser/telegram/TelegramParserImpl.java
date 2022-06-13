package ru.dartanum.stock_analyzer.app.impl.parser.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.dartanum.stock_analyzer.app.api.parser.telegram.TelegramParser;
import ru.dartanum.stock_analyzer.app.api.repository.ChannelRepository;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.SessionStorage;
import ru.dartanum.stock_analyzer.domain.Channel;
import ru.dartanum.stock_analyzer.domain.Post;
import ru.dartanum.stock_analyzer.framework.spring.ValuesProperties;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramParserImpl implements TelegramParser {
    private final WebDriver webDriver;
    private final ChannelRepository channelRepository;
    private final ValuesProperties valuesProperties;
    private final SessionStorage sessionStorage;

    private static final String SCROLL_UP_SCRIPT = "let scrollContainer = document.querySelector('#column-center').querySelector('div.has-sticky-dates div.scrollable-y');" +
            "scrollContainer.scroll(0, scrollContainer.scrollTop - 4000)";

    @Value("${telegram.user.phone-number}")
    private String userPhoneNumber;

    @Override
    public void signIn() {
        webDriver.get("https://web.telegram.org/k/");
        WebElement btnSignInByPhone = webDriver.findElement(By.className("btn-primary-transparent"));
        btnSignInByPhone.click();
        WebElement phoneNumberInput = webDriver.findElement(By.xpath("//div[@inputmode='decimal']"));
        phoneNumberInput.sendKeys(userPhoneNumber + Keys.ENTER);
    }

    @Override
    public boolean enterCode(String code) {
        try {
            webDriver.findElement(By.xpath("//input[@type='tel']")).sendKeys(code);
            return true;
        } catch (WebDriverException e) {
            return false;
        }
    }

    @Override
    public synchronized Set<Post> parse() {
        ValuesProperties.Parser parserValues = valuesProperties.getParser();
        JavascriptExecutor jse = (JavascriptExecutor) webDriver;
        LocalDateTime limitDate = LocalDateTime.now().minusDays(sessionStorage.getNumberOfDaysForCollectData());
        List<Channel> channels = channelRepository.findAll();
        List<String> channelNames = channels.stream().map(Channel::getName).collect(toList());
        Set<Post> collectedPosts = new LinkedHashSet<>();
        Set<Post> result = new LinkedHashSet<>();

        //webDriver.get("C:\\Users\\Dartanum\\Desktop\\Telegram Web.html");
        Map<String, WebElement> channelsLiElementByName = getChannelElementsByName(channelNames);

        channelsLiElementByName.forEach((channelName, entry) -> {
            Channel channel = channelRepository.findByName(channelName);
            collectedPosts.clear();

            entry.click();
            try {
                wait(1000);
                WebElement buttonGoDown = new WebDriverWait(webDriver, 1)
                        .until(ExpectedConditions.elementToBeClickable(By.className("bubbles-go-down")));
                buttonGoDown.click();
                log.info("Go down button clicked");
            } catch (WebDriverException e) {
                log.info("Go down button is not present");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            List<WebElement> messagePortion;
            LocalDateTime firstMessageDate;

            do {
                messagePortion = webDriver.findElements(By.className("message"));
                if (messagePortion.size() == 0) {
                    break;
                }
                firstMessageDate = getDate(messagePortion.get(0));

                for (WebElement msg : messagePortion) {
                    Post post = new Post();
                    try {
                        post.setCreationDate(getDate(msg));
                        post.setChannel(channel);

                        String content = getContent(msg.getText());
                        if (!isContentRelevant(content, parserValues.getKeyWords())) {
                            continue;
                        }
                        boolean isContentValid = content.split(" ").length > parserValues.getMinWordsInMessage();
                        if (isContentValid) {
                            post.setContent(content);
                        }
                        if (collectedPosts.contains(post)) {
                            break;
                        } else if (isContentValid) {
                            collectedPosts.add(post);
                        }
                    } catch (Exception e) {
                        log.warn("Error during parse message");
                    }
                }
                try {
                    wait(1000);
                    jse.executeScript(SCROLL_UP_SCRIPT);
                } catch (JavascriptException | InterruptedException e) {
                    e.printStackTrace();
                }
            } while (firstMessageDate != null && firstMessageDate.isAfter(limitDate));
            result.addAll(collectedPosts);
        });

        return result;

    }

//-----------------------------------implementation-----------------------------------

    private Map<String, WebElement> getChannelElementsByName(List<String> channelNames) {
        return webDriver.findElements(By.className("chatlist-chat"))
                .stream()
                .filter(li -> channelNames.contains(getChannelName(li)))
                .collect(toMap(this::getChannelName, webElement -> webElement));
    }

    private String getChannelName(WebElement channelElement) {
        return channelElement.findElement(By.className("user-title")).getText();
    }

    private String getContent(String message) {
        return Arrays.stream(message.split("[\r\n]+")).reduce("", (s, s2) -> {
            if (!(s2.matches("\\d\\d:\\d\\d") || s2.matches("\\d*.?\\d*K") || s2.equals("edited"))) {
                return s + s2;
            }
            return s;
        });
    }

    private LocalDateTime getDate(WebElement message) {
        try {
            String dateTimeString = message.findElement(By.xpath(".//div[@class='inner tgico']")).getAttribute("title").split("[\r\n]+")[0];
            return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm:ss", Locale.ENGLISH));
        } catch (Exception e) {
            log.warn("Cannot parse date for post");
            return null;
        }
    }

    private boolean isContentRelevant(String content, List<String> keyWords) {
        List<String> lowerCaseKeyWords = keyWords.stream().map(String::toLowerCase).collect(toList());
        return Arrays.stream(content.toLowerCase().split(" ")).anyMatch(lowerCaseKeyWords::contains);
    }
}
