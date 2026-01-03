package com.ansh.listing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for searching images from external APIs based on item descriptions.
 * Uses item name and optionally store name to find relevant product images.
 * Supports Pexels API for high-quality, relevant product images.
 */
@Service
public class ImageSearchService {

    private static final Logger logger = LoggerFactory.getLogger(ImageSearchService.class);

    @Value("${unsplash.access-key:}")
    private String unsplashAccessKey;

    @Value("${pexels.api-key:}")
    private String pexelsApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Curated fallback images by product category (real product photos)
    private static final java.util.Map<String, String> CATEGORY_IMAGES = java.util.Map.ofEntries(
        // Food & Grocery
        java.util.Map.entry("paper towels", "https://images.pexels.com/photos/4239013/pexels-photo-4239013.jpeg?w=400"),
        java.util.Map.entry("toilet paper", "https://images.pexels.com/photos/3962285/pexels-photo-3962285.jpeg?w=400"),
        java.util.Map.entry("nuts", "https://images.pexels.com/photos/1295572/pexels-photo-1295572.jpeg?w=400"),
        java.util.Map.entry("mixed nuts", "https://images.pexels.com/photos/1295572/pexels-photo-1295572.jpeg?w=400"),
        java.util.Map.entry("coffee", "https://images.pexels.com/photos/894695/pexels-photo-894695.jpeg?w=400"),
        java.util.Map.entry("coffee beans", "https://images.pexels.com/photos/894695/pexels-photo-894695.jpeg?w=400"),
        java.util.Map.entry("olive oil", "https://images.pexels.com/photos/1022385/pexels-photo-1022385.jpeg?w=400"),
        java.util.Map.entry("pasta", "https://images.pexels.com/photos/1279330/pexels-photo-1279330.jpeg?w=400"),
        java.util.Map.entry("salmon", "https://images.pexels.com/photos/3296279/pexels-photo-3296279.jpeg?w=400"),
        java.util.Map.entry("fish", "https://images.pexels.com/photos/3296279/pexels-photo-3296279.jpeg?w=400"),
        java.util.Map.entry("chicken", "https://images.pexels.com/photos/2338407/pexels-photo-2338407.jpeg?w=400"),
        java.util.Map.entry("honey", "https://images.pexels.com/photos/1872898/pexels-photo-1872898.jpeg?w=400"),
        java.util.Map.entry("trail mix", "https://images.pexels.com/photos/1295572/pexels-photo-1295572.jpeg?w=400"),
        java.util.Map.entry("detergent", "https://images.pexels.com/photos/4239035/pexels-photo-4239035.jpeg?w=400"),
        java.util.Map.entry("laundry", "https://images.pexels.com/photos/4239035/pexels-photo-4239035.jpeg?w=400"),
        java.util.Map.entry("dishwasher", "https://images.pexels.com/photos/4239091/pexels-photo-4239091.jpeg?w=400"),
        java.util.Map.entry("soap", "https://images.pexels.com/photos/4239091/pexels-photo-4239091.jpeg?w=400"),
        java.util.Map.entry("water bottle", "https://images.pexels.com/photos/1342529/pexels-photo-1342529.jpeg?w=400"),
        java.util.Map.entry("backpack", "https://images.pexels.com/photos/2905238/pexels-photo-2905238.jpeg?w=400"),
        java.util.Map.entry("butter", "https://images.pexels.com/photos/4110256/pexels-photo-4110256.jpeg?w=400"),
        java.util.Map.entry("almond", "https://images.pexels.com/photos/1013420/pexels-photo-1013420.jpeg?w=400"),
        java.util.Map.entry("cereal", "https://images.pexels.com/photos/135525/pexels-photo-135525.jpeg?w=400"),
        java.util.Map.entry("milk", "https://images.pexels.com/photos/248412/pexels-photo-248412.jpeg?w=400"),
        java.util.Map.entry("bread", "https://images.pexels.com/photos/1775043/pexels-photo-1775043.jpeg?w=400"),
        java.util.Map.entry("cheese", "https://images.pexels.com/photos/821365/pexels-photo-821365.jpeg?w=400"),
        java.util.Map.entry("eggs", "https://images.pexels.com/photos/162712/egg-white-food-protein-162712.jpeg?w=400"),
        java.util.Map.entry("fruit", "https://images.pexels.com/photos/1132047/pexels-photo-1132047.jpeg?w=400"),
        java.util.Map.entry("vegetables", "https://images.pexels.com/photos/1656666/pexels-photo-1656666.jpeg?w=400"),
        java.util.Map.entry("snack", "https://images.pexels.com/photos/1618898/pexels-photo-1618898.jpeg?w=400"),
        java.util.Map.entry("chips", "https://images.pexels.com/photos/1618898/pexels-photo-1618898.jpeg?w=400"),
        java.util.Map.entry("soda", "https://images.pexels.com/photos/50593/coca-cola-cold-drink-soft-drink-coke-50593.jpeg?w=400"),
        java.util.Map.entry("juice", "https://images.pexels.com/photos/96974/pexels-photo-96974.jpeg?w=400"),
        java.util.Map.entry("wine", "https://images.pexels.com/photos/1123260/pexels-photo-1123260.jpeg?w=400"),
        java.util.Map.entry("beer", "https://images.pexels.com/photos/1552630/pexels-photo-1552630.jpeg?w=400")
    );

    // Generic category fallbacks
    private static final String DEFAULT_GROCERY_IMAGE = "https://images.pexels.com/photos/264636/pexels-photo-264636.jpeg?w=400";
    private static final String DEFAULT_PRODUCT_IMAGE = "https://images.pexels.com/photos/5632399/pexels-photo-5632399.jpeg?w=400";
    private static final String DEFAULT_FOOD_IMAGE = "https://images.pexels.com/photos/1640777/pexels-photo-1640777.jpeg?w=400";
    private static final String DEFAULT_ELECTRONICS_IMAGE = "https://images.pexels.com/photos/356056/pexels-photo-356056.jpeg?w=400";
    private static final String DEFAULT_CLOTHING_IMAGE = "https://images.pexels.com/photos/996329/pexels-photo-996329.jpeg?w=400";
    private static final String DEFAULT_HOME_IMAGE = "https://images.pexels.com/photos/1643383/pexels-photo-1643383.jpeg?w=400";

    // Store name to category mapping for better image search
    private static final java.util.Map<String, String> STORE_CATEGORIES = java.util.Map.ofEntries(
        java.util.Map.entry("costco", "grocery"),
        java.util.Map.entry("walmart", "retail"),
        java.util.Map.entry("target", "retail"),
        java.util.Map.entry("safeway", "grocery"),
        java.util.Map.entry("whole foods", "organic food"),
        java.util.Map.entry("trader joe", "grocery"),
        java.util.Map.entry("kroger", "grocery"),
        java.util.Map.entry("albertsons", "grocery"),
        java.util.Map.entry("best buy", "electronics"),
        java.util.Map.entry("home depot", "home improvement"),
        java.util.Map.entry("lowes", "home improvement"),
        java.util.Map.entry("ikea", "furniture"),
        java.util.Map.entry("amazon", "retail"),
        java.util.Map.entry("cvs", "pharmacy"),
        java.util.Map.entry("walgreens", "pharmacy")
    );

    public ImageSearchService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Search for an image based on item name only.
     * Returns the first matching image URL or a category-based placeholder.
     *
     * @param itemName The name of the item to search for
     * @param category Optional category for better fallback selection
     * @return URL of the found image or a placeholder
     */
    public String searchImageForItem(String itemName, String category) {
        return searchImageForItem(itemName, null, category);
    }

    /**
     * Search for an image based on item name and store.
     * Builds a search query combining item description with store context.
     *
     * @param itemName The name of the item to search for
     * @param storeName Optional store name for context (e.g., "Costco Sunnyvale")
     * @param category Optional category for better fallback selection
     * @return URL of the found image or a placeholder
     */
    public String searchImageForItem(String itemName, String storeName, String category) {
        if (itemName == null || itemName.trim().isEmpty()) {
            return getDefaultImageForCategory(category);
        }

        // Clean up item name for matching
        String cleanedName = cleanItemName(itemName);
        logger.debug("Searching image for: {} (cleaned: {})", itemName, cleanedName);

        // First, try to find a curated image match from our category map
        String curatedImage = findCuratedImage(cleanedName);
        if (curatedImage != null) {
            logger.debug("Found curated image for: {}", cleanedName);
            return curatedImage;
        }

        // Try Pexels API if configured (better search results)
        if (pexelsApiKey != null && !pexelsApiKey.isEmpty()) {
            String imageUrl = searchPexels(cleanedName);
            if (imageUrl != null) {
                return imageUrl;
            }
        }

        // Try Unsplash API if configured
        if (unsplashAccessKey != null && !unsplashAccessKey.isEmpty()) {
            String imageUrl = searchUnsplash(cleanedName);
            if (imageUrl != null) {
                return imageUrl;
            }
        }

        // Fallback to store-based category image
        String storeCategory = getStoreCategoryContext(storeName);
        return getDefaultImageForCategory(storeCategory != null ? storeCategory : category);
    }

    /**
     * Find a curated image from our predefined category map.
     * Matches keywords in the cleaned item name.
     */
    private String findCuratedImage(String cleanedName) {
        if (cleanedName == null || cleanedName.isEmpty()) {
            return null;
        }

        // Try exact match first
        if (CATEGORY_IMAGES.containsKey(cleanedName)) {
            return CATEGORY_IMAGES.get(cleanedName);
        }

        // Try partial match - check if any keyword is contained in the name
        for (java.util.Map.Entry<String, String> entry : CATEGORY_IMAGES.entrySet()) {
            if (cleanedName.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Try reverse - check if name is contained in any keyword
        for (java.util.Map.Entry<String, String> entry : CATEGORY_IMAGES.entrySet()) {
            if (entry.getKey().contains(cleanedName) && cleanedName.length() >= 3) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Search Pexels API for images matching the query.
     */
    private String searchPexels(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format(
                "https://api.pexels.com/v1/search?query=%s&per_page=1&orientation=square",
                encodedQuery
            );

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", pexelsApiKey);
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                entity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode photos = root.get("photos");
                if (photos != null && photos.isArray() && photos.size() > 0) {
                    JsonNode firstPhoto = photos.get(0);
                    JsonNode src = firstPhoto.get("src");
                    if (src != null && src.has("medium")) {
                        return src.get("medium").asText();
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to search Pexels for '{}': {}", query, e.getMessage());
        }
        return null;
    }

    /**
     * Clean up item name by removing quantities, pack sizes, and other noise.
     * Examples:
     *   "Kirkland Paper Towels (12-pack)" -> "paper towels"
     *   "Organic Mixed Nuts (2.5 lbs)" -> "organic mixed nuts"
     *   "Coffee Beans (3 lbs)" -> "coffee beans"
     */
    private String cleanItemName(String itemName) {
        if (itemName == null) return "";

        String cleaned = itemName.toLowerCase()
            // Remove parenthetical content (pack sizes, weights, etc.)
            .replaceAll("\\([^)]*\\)", "")
            // Remove common quantity patterns
            .replaceAll("\\d+\\s*(pack|count|oz|lb|lbs|kg|g|ml|l|ct)s?\\b", "")
            .replaceAll("\\d+[-]?(pack|count|roll|ct)s?\\b", "")
            // Remove brand names that might confuse image search
            .replaceAll("\\b(kirkland|great value|signature|member's mark|o organics)\\b", "")
            // Remove extra whitespace
            .replaceAll("\\s+", " ")
            .trim();

        // If we stripped too much, return original without parentheses
        if (cleaned.length() < 3) {
            return itemName.toLowerCase().replaceAll("\\([^)]*\\)", "").trim();
        }

        return cleaned;
    }

    /**
     * Get category context from store name to improve search relevance.
     */
    private String getStoreCategoryContext(String storeName) {
        if (storeName == null) return null;

        String lowerStore = storeName.toLowerCase();

        // Check each known store
        for (java.util.Map.Entry<String, String> entry : STORE_CATEGORIES.entrySet()) {
            if (lowerStore.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Search Unsplash API for images matching the query.
     */
    private String searchUnsplash(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format(
                "https://api.unsplash.com/search/photos?query=%s&per_page=1&orientation=squarish",
                encodedQuery
            );

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Client-ID " + unsplashAccessKey);
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                entity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode results = root.get("results");
                if (results != null && results.isArray() && results.size() > 0) {
                    JsonNode firstResult = results.get(0);
                    JsonNode urls = firstResult.get("urls");
                    if (urls != null && urls.has("small")) {
                        return urls.get("small").asText();
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to search Unsplash for '{}': {}", query, e.getMessage());
        }
        return null;
    }

    /**
     * Get a default image based on category.
     * Returns curated, relevant product images.
     */
    private String getDefaultImageForCategory(String category) {
        if (category == null) {
            return DEFAULT_GROCERY_IMAGE;
        }

        String lowerCategory = category.toLowerCase();
        if (lowerCategory.contains("food") || lowerCategory.contains("grocery") ||
            lowerCategory.contains("organic")) {
            return DEFAULT_GROCERY_IMAGE;
        } else if (lowerCategory.contains("electronic") || lowerCategory.contains("tech") ||
                   lowerCategory.contains("phone") || lowerCategory.contains("computer")) {
            return DEFAULT_ELECTRONICS_IMAGE;
        } else if (lowerCategory.contains("cloth") || lowerCategory.contains("fashion") ||
                   lowerCategory.contains("wear") || lowerCategory.contains("apparel")) {
            return DEFAULT_CLOTHING_IMAGE;
        } else if (lowerCategory.contains("home") || lowerCategory.contains("furniture") ||
                   lowerCategory.contains("kitchen") || lowerCategory.contains("decor") ||
                   lowerCategory.contains("improvement")) {
            return DEFAULT_HOME_IMAGE;
        } else if (lowerCategory.contains("pharmacy") || lowerCategory.contains("health")) {
            return DEFAULT_PRODUCT_IMAGE;
        } else if (lowerCategory.contains("retail")) {
            return DEFAULT_GROCERY_IMAGE;
        }

        return DEFAULT_GROCERY_IMAGE;
    }

    /**
     * Get multiple image suggestions for an item.
     * Returns curated images based on item keywords.
     */
    public List<String> getImageSuggestions(String itemName, int count) {
        return getImageSuggestions(itemName, null, count);
    }

    /**
     * Get multiple image suggestions for an item with store context.
     * Returns relevant curated images from our category map.
     *
     * @param itemName The item name
     * @param storeName Optional store name for context
     * @param count Number of suggestions to return
     * @return List of image URLs
     */
    public List<String> getImageSuggestions(String itemName, String storeName, int count) {
        List<String> suggestions = new ArrayList<>();
        String cleanedName = cleanItemName(itemName);

        // First add the best matching curated image
        String curatedImage = findCuratedImage(cleanedName);
        if (curatedImage != null) {
            suggestions.add(curatedImage);
        }

        // Add category-based alternatives
        String storeCategory = getStoreCategoryContext(storeName);
        suggestions.add(getDefaultImageForCategory(storeCategory));

        // Add more varied curated images
        for (java.util.Map.Entry<String, String> entry : CATEGORY_IMAGES.entrySet()) {
            if (suggestions.size() >= count) break;
            if (!suggestions.contains(entry.getValue())) {
                // Add images that might be related
                if (isRelatedCategory(cleanedName, entry.getKey())) {
                    suggestions.add(entry.getValue());
                }
            }
        }

        // Fill remaining with default images
        while (suggestions.size() < count) {
            if (!suggestions.contains(DEFAULT_GROCERY_IMAGE)) {
                suggestions.add(DEFAULT_GROCERY_IMAGE);
            } else if (!suggestions.contains(DEFAULT_FOOD_IMAGE)) {
                suggestions.add(DEFAULT_FOOD_IMAGE);
            } else if (!suggestions.contains(DEFAULT_PRODUCT_IMAGE)) {
                suggestions.add(DEFAULT_PRODUCT_IMAGE);
            } else {
                break;
            }
        }

        return suggestions.subList(0, Math.min(count, suggestions.size()));
    }

    /**
     * Check if two product names are in related categories.
     */
    private boolean isRelatedCategory(String itemName, String categoryKey) {
        // Food-related items
        if (containsAny(itemName, "food", "snack", "drink", "eat")) {
            return containsAny(categoryKey, "food", "snack", "drink", "fruit", "vegetable", "cereal", "milk", "bread", "cheese");
        }
        // Cleaning products
        if (containsAny(itemName, "clean", "wash", "soap", "detergent")) {
            return containsAny(categoryKey, "detergent", "soap", "laundry", "dishwasher");
        }
        // Paper products
        if (containsAny(itemName, "paper", "towel", "tissue")) {
            return containsAny(categoryKey, "paper", "towel", "toilet");
        }
        return false;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
