package ru.dartanum.stock_analyzer.parser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.dartanum.stock_analyzer.domain.Channel;
import ru.dartanum.stock_analyzer.domain.Post;
import ru.dartanum.stock_analyzer.repository.ChannelRepository;
import ru.dartanum.stock_analyzer.repository.PostRepository;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
@Setter
@Getter
@RequiredArgsConstructor
public class TelegramParser implements Runnable {
    private final WebDriver webDriver;
    private final PostRepository postRepository;
    private final ChannelRepository channelRepository;

    private final String scrollUpScript = "let scrollContainer = document.querySelector('#column-center').querySelector('div.has-sticky-dates div.scrollable-y');" +
            "scrollContainer.scroll(0, scrollContainer.scrollTop - 4000)";

    @Value("${telegram.user.phone-number}")
    private String userPhoneNumber;
    @Value("${values.parser.number-of-days-for-collect-data}")
    private int dayRange;
    @Value("${values.parser.min-words-in-message}")
    private int minWordsInMessage;
    @Value("#{'${values.parser.key-words}'.toLowerCase().split(',')}")
    private List<String> keyWords;
    private Consumer<String> botMessageSend;

    public void signIn() {
        webDriver.get("https://web.telegram.org/k/");
        WebElement btnSignInByPhone = webDriver.findElement(By.className("btn-primary-transparent"));
        btnSignInByPhone.click();
        WebElement phoneNumberInput = webDriver.findElement(By.xpath("//div[@inputmode='decimal']"));
        phoneNumberInput.sendKeys(userPhoneNumber + Keys.ENTER);
    }

    public void enterCode(String code) {
        webDriver.findElement(By.xpath("//input[@type='tel']")).sendKeys(code);
    }

    @Override
    public void run() {
        collectData();
    }

    private void collectData() {
        JavascriptExecutor jse = (JavascriptExecutor) webDriver;
        LocalDateTime limitDate = LocalDateTime.now().minusDays(dayRange);
        List<Channel> channels = channelRepository.findAll();
        List<String> channelNames = channels.stream().map(Channel::getName).collect(toList());
        Set<Post> collectedPosts = new LinkedHashSet<>();
        Set<Post> savedPosts = new LinkedHashSet<>();

        try {
            //webDriver.get("C:\\Users\\Dartanum\\Desktop\\Telegram Web2.html");
            Map<String, WebElement> channelsLiElementByName = getChannelElementsByName(channelNames);

            channelsLiElementByName.forEach((channelName, entry) -> {
                Channel channel = channelRepository.findByName(channelName);
                savedPosts.clear();
                savedPosts.addAll(postRepository.findAllByChannel(channel));
                entry.click();
                try {
                    WebElement buttonScrollDown = webDriver.findElement(By.className("bubbles-go-down"));
                    buttonScrollDown.click();
                } catch (Exception e) {}

                List<WebElement> messagePortion;
                LocalDateTime firstMessageDate = null;

                do {
                    messagePortion = webDriver.findElements(By.className("message"));
                    if (messagePortion.size() == 0) {
                        continue;
                    }
                    firstMessageDate = getDate(messagePortion.get(0));
                    for (WebElement msg : messagePortion) {
                        Post post = new Post();
                        try {
                            post.setCreationDate(getDate(msg));
                            post.setChannel(channel);

                            String content = getContent(msg.getText());
                            if (!isContentRelevant(content)) {
                                continue;
                            }
                            boolean isContentValid = content.split(" ").length > minWordsInMessage;
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
                        jse.executeScript(scrollUpScript);
                    } catch (Exception e) {
                        jse.executeScript(scrollUpScript); //TODO replace
                    }
                } while (firstMessageDate != null && firstMessageDate.isAfter(limitDate));
                try {
                    postRepository.saveAll(diffSet(collectedPosts, savedPosts));
                } catch (ConstraintViolationException e) {
                    e.printStackTrace();
                }
                collectedPosts.clear();
            });
            botMessageSend.accept("Сбор данных завершен");
        } catch (Exception e) {
            e.printStackTrace();
            botMessageSend.accept("Произошла ошибка во время сбора данных");
        }
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

    //set of elements contains elements from left set and not from right set
    private Set<Post> diffSet(Set<Post> left, Set<Post> right) {
        Set<Post> update = new HashSet<>(left);
        left.retainAll(right);
        update.removeAll(left);

        return update;
    }

    private boolean isContentRelevant(String content) {
        return Arrays.stream(content.toLowerCase().split(" ")).anyMatch(word -> keyWords.contains(word));
    }
}
