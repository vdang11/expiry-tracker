package com.expiry.service;

import com.expiry.entity.Item;
import com.expiry.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Fills in category / ingredientName for items that do not have them yet.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemClassificationService {

    private final ItemRepository itemRepository;
    private final ItemClassifier itemClassifier;

    @Value("${item-classification.backfill-on-startup:true}")
    private boolean backfillOnStartup;

    /**
     * Classify one user's unclassified items (used before generating recipes,
     * so items saved while the AI was down still get handled).
     */
    public void classifyPending(Long userId) {
        List<Item> pending = itemRepository.findByUser_IdAndCategoryIsNull(userId);

        for (int from = 0; from < pending.size(); from += ItemClassifier.MAX_BATCH) {
            int to = Math.min(from + ItemClassifier.MAX_BATCH, pending.size());
            classifyAndSave(pending.subList(from, to));
        }
    }

    /**
     * One-time re-classification of every existing item without a category.
     * Runs in the background after startup; items classified once are never
     * touched again, so restarting the app does not repeat the work.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void backfillOnStartup() {
        if (!backfillOnStartup) {
            return;
        }

        Thread worker = new Thread(this::backfillAll, "item-classification-backfill");
        worker.setDaemon(true);
        worker.start();
    }

    void backfillAll() {
        int total = 0;

        while (true) {
            List<Item> batch = itemRepository.findTop50ByCategoryIsNullOrderByIdAsc();
            if (batch.isEmpty()) break;

            int done = classifyAndSave(batch);
            total += done;

            // AI down or answering badly: stop instead of looping forever
            if (done == 0) {
                log.warn("Backfill stopped: {} item(s) could not be classified, will retry on next start",
                        batch.size());
                break;
            }
        }

        if (total > 0) {
            log.info("Backfill classified {} item(s)", total);
        }
    }

    private int classifyAndSave(List<Item> items) {
        List<ItemClassifier.Classification> results =
                itemClassifier.classifyAll(items.stream().map(Item::getProductName).toList());

        int done = 0;

        for (int i = 0; i < items.size(); i++) {
            ItemClassifier.Classification c = results.get(i);
            if (c == null) continue;

            items.get(i).setCategory(c.category());
            items.get(i).setIngredientName(c.ingredientName());
            done++;
        }

        if (done > 0) {
            itemRepository.saveAll(items);
        }

        return done;
    }
}
