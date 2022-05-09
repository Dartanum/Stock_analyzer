package ru.dartanum.stock_analyzer.parser;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.dartanum.stock_analyzer.domain.Channel;
import ru.dartanum.stock_analyzer.domain.Post;
import ru.dartanum.stock_analyzer.repository.ChannelRepository;
import ru.dartanum.stock_analyzer.repository.PostRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class TelegramParser {
    private final WebDriver webDriver;
    private final PostRepository postRepository;
    private final ChannelRepository channelRepository;

    @Value("${telegram.user.phone-number}")
    private String userPhoneNumber;
    @Value("${values.number-of-days-for-collect-data}")
    private int dayRange;
    @Value("${values.min-words-in-message}")
    private int minWordsInMessage;

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

    public void collectData() {
        JavascriptExecutor jse = (JavascriptExecutor) webDriver;
        LocalDateTime limitDate = LocalDateTime.now().minusDays(dayRange);
        List<Channel> channels = channelRepository.findAll();
        List<String> channelNames = channels.stream().map(Channel::getName).collect(toList());
        Set<Post> savedPosts = postRepository.getInDateRange(limitDate, LocalDateTime.now());
        Set<Post> collectedPosts = new HashSet<>();

        webDriver.get("C:\\Users\\Dartanum\\Desktop\\Telegram Web.html");
        Map<String, WebElement> channelsLiElementByName = getChannelElementsByName(channelNames);

        channelsLiElementByName.forEach((channelName, entry) -> {
            entry.click();
            List<WebElement> messagePortion;
            boolean finish = false;
            do {
                messagePortion = webDriver.findElements(By.className("message"));
                for (WebElement msg : messagePortion) {
                    Post post = new Post();
                    try {
                        post.setCreationDate(getDate(msg));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    post.setChannel(channelRepository.findByName(channelName));
                    String content = getContent(msg.getText());
                    boolean isContentValid = content.split(" ").length > minWordsInMessage;
                    if (isContentValid) {
                        post.setContent(content);
                    }
                    if (collectedPosts.contains(post)) {
                        finish = true;
                        break;
                    } else if (isContentValid) {
                        collectedPosts.add(post);
                    }
                }
                jse.executeScript("window.scrollBy(0, 1000)");
            } while (getDate(messagePortion.get(0)).isAfter(limitDate) && messagePortion.size() < 10 && !finish);
        });

        postRepository.saveAll(diffSet(collectedPosts, savedPosts));
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
        String dateTimeString = message.findElement(By.xpath(".//div[@class='inner tgico']")).getAttribute("title").split("[\r\n]+")[0];
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm:ss", Locale.ENGLISH));
    }

    //set of elements contains elements from left set and not from right set
    private Set<Post> diffSet(Set<Post> left, Set<Post> right) {
        Set<Post> update = new HashSet<>(left);
        left.retainAll(right);
        update.removeAll(left);

        return update;
    }

    private boolean postsEquals(Post lhs, Post rhs) {
        return lhs.getChannel().equals(rhs.getChannel()) && lhs.getCreationDate().equals(rhs.getCreationDate());
    }
}
