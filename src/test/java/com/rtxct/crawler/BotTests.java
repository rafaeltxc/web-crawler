package com.rtxct.crawler;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.test.context.TestComponent;

import com.rtxct.crawler.bot.Bot;
import com.rtxct.crawler.dto.PageDTO;
import com.rtxct.crawler.utils.NginxTestContainer;

@TestComponent
@TestInstance(Lifecycle.PER_CLASS)
public class BotTests {

  /** Properties */
  private String url;

  private String simpleCrawling;

  private List<String> fullCrawling;

  /** Dependencies */
  private Bot bot;

  private NginxTestContainer nginx;

  /**
   * Before all the tests, starts the local testContainer nginx server, and
   * initialize the properties that rely on the server URL.
   */
  @BeforeAll
  public void setup() {
    try {
      CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
        this.nginx = new NginxTestContainer();
      });

      future.get();

      this.url = nginx.getServerPort();

      this.simpleCrawling = String.format(
          "[{\"title\":\"Index\",\"desc\":\"Index description\",\"url\":\"%s\"}]",
          url);

      this.fullCrawling = Arrays.asList(
          String.format(
              "{\"title\":\"Index\",\"desc\":\"Index description\",\"url\":\"%s\"}",
              url),
          String.format(
              "{\"title\":\"Link\",\"desc\":\"Link description\",\"url\":\"%s/link.html\"}",
              url));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * After all the tests, stop the nginx testContainer server.
   */
  @AfterAll
  public void cleanup() {
    try {
      this.nginx.stopContainer();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Crawl the local server and assert that the returned page is equal to the
   * mocked response (Crawls a single page).
   */
  @Test
  public void testCrawlSyncSimpleCrawling() {
    this.bot = new Bot(Arrays.asList(url), 0);
    List<PageDTO> result = bot.crawlSync();

    result.forEach(str -> {
      Assert.assertEquals(simpleCrawling, str);
    });
  }

  /**
   * Crawl the local server and assert that the returned page is equal to the
   * mocked response (Crawls multiple pages).
   */
  @Test
  public void testCrawlSyncFullCrawling() {
    this.bot = new Bot(Arrays.asList(url), 1);
    List<PageDTO> result = bot.crawlSync();

    for (int i = 0; i < result.size(); i++) {
      Assert.assertEquals(fullCrawling.get(i), result.get(i));
    }
  }

  /**
   * Crawl asynchronously the local server and assert that the returned page is
   * equal to the
   * mocked response (Crawls a single pages).
   */
  @Test
  public void testCrawlAsyncSimpleCrawling() {
    this.bot = new Bot(Arrays.asList(url), 0);

    CompletableFuture<List<PageDTO>> future = CompletableFuture.supplyAsync(() -> {
      return bot.crawlAsync(2);
    });

    try {
      List<PageDTO> result = future.get();

      result.forEach(str -> {
        Assert.assertEquals(simpleCrawling, str);
      });
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
  }

  /**
   * Crawl asynchronously the local server and assert that the returned page is
   * equal to the mocked response (Crawls multiple pages).
   */
  @Test
  public void testCrawlAsyncFullCrawling() {
    this.bot = new Bot(Arrays.asList(url), 1);

    CompletableFuture<List<PageDTO>> future = CompletableFuture.supplyAsync(() -> {
      return bot.crawlAsync(2);
    });

    try {
      List<PageDTO> result = future.get();

      for (int i = 0; i < result.size(); i++) {
        Assert.assertEquals(fullCrawling.get(i), result.get(i));
      }
      ;
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
  }

}
