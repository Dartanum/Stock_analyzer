package ru.dartanum.stock_analyzer.app.impl.parser.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.dartanum.stock_analyzer.app.api.repository.ChannelRepository;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.SessionStorage;
import ru.dartanum.stock_analyzer.domain.news.Channel;
import ru.dartanum.stock_analyzer.domain.news.Post;
import ru.dartanum.stock_analyzer.framework.spring.ValuesProperties;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramParser {
    private final WebDriver webDriver;
    private final ChannelRepository channelRepository;
    private final ValuesProperties valuesProperties;

    private static final String SCROLL_UP_SCRIPT = "let scrollContainer = document.querySelector('#column-center').querySelector('div.has-sticky-dates div.scrollable-y');" +
            "scrollContainer.scroll(0, scrollContainer.scrollTop - 4000)";

    @Value("${telegram.admin.phone-number}")
    private String adminPhoneNumber;

    public boolean signIn() {
        try {
            webDriver.get("https://web.telegram.org/k/");
            WebElement btnSignInByPhone = webDriver.findElement(By.xpath("//*[@id=\"auth-pages\"]/div/div[2]/div[3]/div/div[2]/button[1]"));
            btnSignInByPhone.click();
            WebElement phoneNumberInput = webDriver.findElement(By.xpath("//div[@inputmode='decimal']"));
            phoneNumberInput.sendKeys(adminPhoneNumber + Keys.ENTER);
            return true;
        } catch (WebDriverException e) {
            return false;
        }
    }

    public boolean enterCode(String code) {
        try {
            webDriver.findElement(By.xpath("//input[@type='tel']")).sendKeys(code);
            return true;
        } catch (WebDriverException e) {
            return false;
        }
    }

    public synchronized Set<Post> parse(Long userId) {
        ValuesProperties.Parser parserValues = valuesProperties.getParser();
        JavascriptExecutor jse = (JavascriptExecutor) webDriver;
        LocalDateTime limitDate = LocalDateTime.now().minusDays(SessionStorage.getByUserIdOrSave(userId).getNumberOfDaysForNewParsing());
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
                WebElement buttonGoDown;
                do {
                    try {
                        buttonGoDown = new WebDriverWait(webDriver, Duration.ofSeconds(3))
                                .until(ExpectedConditions.elementToBeClickable(By.className("bubbles-go-down")));
                    } catch (TimeoutException e) {
                        break;
                    }
                    buttonGoDown.click();
                    log.info("Go down button clicked");
                } while (buttonGoDown != null);
            } catch (WebDriverException e) {
                log.info("Go down button is not present");
            }

            List<WebElement> messagePortion;
            LocalDateTime firstMessageDate;

            do {
                messagePortion = webDriver.findElements(By.xpath("//div[@class='message spoilers-container']"));
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
                        log.warn("Error during parse message", e);
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
        List<String> lowerCaseKeyWords = keyWords.stream().map(String::toLowerCase).toList();
        return Arrays.stream(content.toLowerCase().split(" ")).anyMatch(lowerCaseKeyWords::contains);
    }
}
