package com.stepup.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.stepup.model.Product;
import com.stepup.model.ChatMessage;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private com.stepup.service.ChatRuleService chatRuleService;

    @Autowired
    private com.stepup.service.OrderService orderService;

    @Autowired
    private com.stepup.service.ProductService productService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> getResponse(@RequestBody Map<String, String> payload,
            jakarta.servlet.http.HttpSession session) {
        String userMessage = payload.getOrDefault("message", "").toLowerCase();
        Map<String, Object> responseMap = new HashMap<>();
        com.stepup.model.User user = (com.stepup.model.User) session.getAttribute("user");

        List<com.stepup.model.ChatRule> rules = chatRuleService.getAllRules();
        com.stepup.model.ChatRule matchedRule = null;

        for (com.stepup.model.ChatRule rule : rules) {
            String[] keywords = rule.getKeywords().toLowerCase().split(",");
            for (String keyword : keywords) {
                String kw = keyword.trim();
                if (kw.isEmpty()) continue;
                
                // Exact match first (using word boundaries to prevent 'hi' matching 'shipping')
                if (userMessage.matches("(?i).*\\b" + Pattern.quote(kw) + "\\b.*")) {
                    matchedRule = rule;
                    break;
                }
                
                // Fuzzy match (tolerant of minor typos)
                // Split user message into words to compare against keyword
                String[] userWords = userMessage.split("\\s+");
                for (String word : userWords) {
                    if (word.length() >= 4 && kw.length() >= 4) {
                        int distance = calculateLevenshteinDistance(word, kw);
                        // Allow 1 typo for ~4-5 letter words, 2 typos for longer words
                        int maxTypos = kw.length() <= 5 ? 1 : 2;
                        if (distance <= maxTypos) {
                            matchedRule = rule;
                            break;
                        }
                    }
                }
                if (matchedRule != null) break;
            }
            if (matchedRule != null)
                break;
        }

        String botMessage = null;
        List<String> suggestionList = new ArrayList<>();

        // 1. Detect Intent: Order ID Search
        Long extractedOrderId = extractOrderId(userMessage);
        if (extractedOrderId != null) {
            com.stepup.model.Order order = orderService.getOrderById(extractedOrderId);
            if (order != null && user != null && order.getUser().getId().equals(user.getId())) {
                botMessage = "I found Order #" + order.getId() + ". Current status: " + order.getStatus() +
                        ". Expected Delivery: "
                        + (order.getExpectedDeliveryDate() != null
                                ? order.getExpectedDeliveryDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                                : "Updating...");
                suggestionList.add("Track Order #" + order.getId());
                suggestionList.add("Refund Status");
            } else if (order != null) {
                botMessage = "I found that order, but you need to be logged in as the correct user to see details.";
            } else {
                botMessage = "I couldn't find an order with ID #" + extractedOrderId
                        + ". Please double check the number.";
            }
        }

        // 2. Detect Intent: Refund Status
        if (botMessage == null && userMessage.contains("refund")) {
            if (user == null) {
                botMessage = "Please log in to check your refund status.";
                suggestionList.add("Login");
            } else {
                List<com.stepup.model.Order> orders = orderService.getOrdersByUser(user);
                if (orders.isEmpty()) {
                    botMessage = "You haven't placed any orders yet, so there are no refunds to track.";
                } else {
                    com.stepup.model.Order lastOrder = orders.get(0);
                    if (lastOrder.getRefundStatus() != null && !lastOrder.getRefundStatus().equals("NOT_APPLICABLE")) {
                        botMessage = "Refund for Order #" + lastOrder.getId() + " is currently: **"
                                + lastOrder.getRefundStatus() + "**. Typically takes 5-7 business days.";
                    } else {
                        botMessage = "I don't see any active refunds for your latest orders. If you just cancelled, it might take a few minutes to update.";
                    }
                }
            }
        }

        // 3. Detect Intent: Cancel/Return (Guide them to the portal)
        if (botMessage == null && (userMessage.contains("cancel") || userMessage.contains("return"))) {
            botMessage = "You can manage your orders (Cancel or Return) directly from your 'My Orders' section. Would you like me to take you there?";
            suggestionList.add("Go to My Orders");
            suggestionList.add("Return Policy");
        }

        // 4. Detect Intent: Recommendations & Search
        boolean isShoppingIntent = false;
        if (botMessage == null) {
            String[] shopKeywords = {"recommend", "show", "similar", "shoe", "shoes", "under", "below", "size", "browse", "products", "everything"};
            String[] userWords = userMessage.split("\\s+");
            
            for (String userWord : userWords) {
                String word = userWord.replaceAll("[^a-zA-Z]", "");
                if (word.length() < 4 || word.equals("how")) continue; 
                for (String kw : shopKeywords) {
                    if (word.contains(kw)) {
                        isShoppingIntent = true; break;
                    }
                    if (kw.length() >= 4 && calculateLevenshteinDistance(word, kw) <= 1) {
                        isShoppingIntent = true; break;
                    }
                }
                if (isShoppingIntent) break;
            }
        }

        if (botMessage == null && (isShoppingIntent || userMessage.contains("all products"))) {

            // Context extraction
            BigDecimal priceLimit = extractPriceLimit(userMessage);
            String sizeRequested = extractSize(userMessage);

            // Interaction Context Memory
            Map<String, String> chatContext = (Map<String, String>) session.getAttribute("chatContext");
            if (chatContext == null)
                chatContext = new HashMap<>();

            // Detect Language and store in session
            String language = (String) chatContext.get("language");
            if (userMessage.contains("வணக்கம்") || userMessage.contains("எப்படி") || userMessage.contains("செருப்பு"))
                language = "tamil";
            else if (userMessage.contains("नमस्ते") || userMessage.contains("कैसे") || userMessage.contains("जूते"))
                language = "hindi";
            else if (language == null)
                language = "english";
            chatContext.put("language", language);

            if (userMessage.contains("women") || userMessage.contains("பெண்கள்") || userMessage.contains("பெண்களுக்கு")
                    || userMessage.contains("महिला"))
                chatContext.put("category", "Womens");
            else if (userMessage.contains("men") || userMessage.contains("ஆண்கள்") || userMessage.contains("ஆண்களுக்கு")
                    || userMessage.contains("पुरूष"))
                chatContext.put("category", "Mens");
            else if (userMessage.contains("kid") || userMessage.contains("குழந்தை") || userMessage.contains("बच्चா"))
                chatContext.put("category", "Kids");

            if (priceLimit != null)
                chatContext.put("maxPrice", priceLimit.toString());
            if (sizeRequested != null)
                chatContext.put("lastSize", sizeRequested);

            session.setAttribute("chatContext", chatContext);

            String category = chatContext.get("category");
            // Detection Logic for Product Search
            BigDecimal maxPrice = null;
            Pattern pricePattern = Pattern.compile("(\\d+)|(₹\\d+)|(\\d+\\srupees)");
            Matcher priceMatcher = pricePattern.matcher(userMessage);
            while (priceMatcher.find()) {
                String match = priceMatcher.group().replaceAll("[^0-9]", "");
                if (!match.isEmpty())
                    maxPrice = new BigDecimal(match);
            }

            List<Product> results;
            // Regional Keyword Detection for Products
            if (userMessage.contains("செருப்பு") || userMessage.contains("காலணி") || userMessage.contains("ஜூத்தே")
                    || userMessage.contains("जूते")) {
                if (category == null)
                    category = (String) chatContext.get("category");
                results = productService.getRecommendations(userMessage, category);
                botMessage = "நிச்சயமாக, உங்களுக்காக சில சிறந்த தயாரிப்புகள் இதோ:\n";
                if (userMessage.contains("जूते"))
                    botMessage = "जी हाँ, आपके लिए कुछ बेहतरीन विकल्प यहाँ हैं:\n";
            } else if (userMessage.contains("browse") || userMessage.contains("all products")
                    || userMessage.contains("show everything")) {
                if (category != null) {
                    results = productService.searchProducts(null, category, null, null,
                            org.springframework.data.domain.Sort.by("rating").descending());
                    botMessage = "I've brought our collection for " + category + " for you to browse! 👟\n";
                } else {
                    results = productService.getAllProducts();
                    botMessage = "I've brought our entire collection for you to browse! 👟\n";
                }
            } else if (maxPrice != null) {
                results = productService.searchProducts(null, category, null, maxPrice,
                        org.springframework.data.domain.Sort.by("price").ascending());
                botMessage = "I found these options " + (category != null ? "for " + category : "") + " under ₹"
                        + maxPrice + ":\n";
            } else {
                results = productService.getRecommendations(userMessage, category);
                botMessage = "Here are some top-rated picks " + (category != null ? "for " + category : "")
                        + " you might like:\n";
            }

            if (!results.isEmpty()) {
                botMessage += "<div class='chat-products-carousel'>";
                for (int i = 0; i < Math.min(8, results.size()); i++) {
                    Product p = results.get(i);
                    botMessage += "<div class='chat-product-card'>";
                    botMessage += "  <img src='" + (p.getImage() != null ? p.getImage() : "/images/placeholder.jpg")
                            + "' class='chat-product-img'>";
                    botMessage += "  <div class='chat-product-info'>";
                    botMessage += "    <div class='chat-product-name'>" + p.getName() + "</div>";
                    botMessage += "    <div class='chat-product-meta'>";
                    botMessage += "      <span class='chat-product-price'>₹" + p.getPrice() + "</span>";
                    botMessage += "      <span class='chat-product-rating'>" + p.getRating() + " ⭐</span>";
                    botMessage += "    </div>";
                    botMessage += "    <div class='chat-product-actions'>";
                    botMessage += "      <button onclick=\"addToCartFromChat(" + p.getId() + ", '"
                            + p.getName().replace("'", "\\'")
                            + "')\" class='chat-btn-cart'><i class='bi bi-cart-plus'></i> Add</button>";
                    botMessage += "      <a href='/product/" + p.getId() + "' class='chat-btn-details'>Details</a>";
                    botMessage += "    </div>";
                    botMessage += "  </div>";
                    botMessage += "</div>";
                }
                botMessage += "</div>";

                String currentSize = chatContext.get("lastSize");
                if (currentSize != null) {
                    botMessage += "\n<p class='mt-2 mb-0 small text-muted'>I've noted you're looking for <b>Size "
                            + currentSize + "</b>. These are popular picks in that size!</p>";
                }
            } else {
                botMessage = "I couldn't find exactly what you're looking for right now. Why not try browsing our latest arrivals?";
            }
            suggestionList.add("Men's Shoes");
            suggestionList.add("Women's Shoes");
            suggestionList.add("Browse All");
        }

        // 4.5 Detect Intent: My Recent Orders (Last 5)
        if (botMessage == null && (userMessage.contains("my orders") || userMessage.contains("recent orders") 
                || userMessage.contains("last orders") || userMessage.contains("order history"))) {
            if (user == null) {
                botMessage = "Please log in to view your recent orders.";
                suggestionList.add("Login");
            } else {
                List<com.stepup.model.Order> userOrders = orderService.getOrdersByUser(user);
                if (userOrders.isEmpty()) {
                    botMessage = "You haven't placed any orders yet.";
                    suggestionList.add("Browse All");
                } else {
                    botMessage = "Here are your most recent orders:\n<ul class='chat-order-list' style='list-style: none; padding-left: 0;'>";
                    int count = 0;
                    for (com.stepup.model.Order o : userOrders) {
                        if (count >= 5) break;
                        String formattedDate = o.getOrderDate() != null 
                             ? o.getOrderDate().format(DateTimeFormatter.ofPattern("MMM dd")) 
                             : "Recent";
                        botMessage += "<li style='margin-bottom: 8px; padding: 8px; border: 1px solid #eee; border-radius: 4px;'>" +
                                      "<strong>Order #" + o.getId() + "</strong> (" + formattedDate + ")<br>" +
                                      "Status: " + o.getStatus() + " | Total: ₹" + o.getTotalAmount() +
                                      "</li>";
                        count++;
                    }
                    botMessage += "</ul>";
                    suggestionList.add("Track Order");
                    suggestionList.add("Go to My Orders");
                }
            }
        }

        // 5. Detect Intent: Support Tickets
        if (botMessage == null && (userMessage.contains("ticket") || userMessage.contains("raise")
                || userMessage.contains("complain") || userMessage.contains("issue"))) {
            if (user == null) {
                botMessage = "Please log in to raise a support ticket. I can also help you with general queries here!";
                suggestionList.add("Login");
            } else {
                botMessage = "I've started a support ticket for you. Our team will look into your recent activity. You can also describe your issue here and I'll add it to the ticket.";
                orderService.createSupportTicket(user, "Chatbot Inquiry", userMessage);
                suggestionList.add("My Tickets");
                suggestionList.add("Talk to Agent");
            }
        }

        // 6. Detect Intent: Login Assistance / OTP
        if (botMessage == null && (userMessage.contains("login") || userMessage.contains("password")
                || userMessage.contains("otp") || userMessage.contains("account"))) {
            botMessage = "Having trouble with your account? You can reset your password using the 'Forgot Password' link on the Login page. I can also help you track orders if you're already logged in!";
            suggestionList.add("Go to Login");
            suggestionList.add("Track Order");
        }

        // 7. Detect Intent: Update Address
        if (botMessage == null && (userMessage.contains("address") || userMessage.contains("profile")
                || userMessage.contains("change my details"))) {
            if (user == null) {
                botMessage = "Please log in to update your shipping address.";
                suggestionList.add("Login");
            } else {
                botMessage = "You can update your delivery address directly from your profile. Just click on your name in the header and select 'Update Profile'.";
                suggestionList.add("My Orders");
            }
        }

        // Detect Intent: Multilingual Greeting (Basic)
        if (botMessage == null && (userMessage.contains("namaste") || userMessage.contains("kaise ho")
                || userMessage.contains("hindi"))) {
            botMessage = "Namaste! 🙏 Main aapki kaise madad kar sakta hoon? (How can I help you today?)";
            suggestionList.add("Order Status");
            suggestionList.add("Refund kab aayega?");
        } else if (botMessage == null && (userMessage.contains("vanakkam") || userMessage.contains("eppadi")
                || userMessage.contains("tamil") || userMessage.contains("வணக்கம்")
                || userMessage.contains("எப்படி"))) {
            botMessage = "Vanakkam! 🙏 Ungalukku naan eppadi uthavalaam? (How can I help you?)";
            suggestionList.add("ஆர்டர் நிலை (Order Track)");
            suggestionList.add("பணம் எப்போது வரும்? (Refund)");
        }

        // 8. Detect Intent: Live Agent Handover
        if (botMessage == null && (userMessage.contains("agent") || userMessage.contains("human")
                || userMessage.contains("talk to someone"))) {
            botMessage = "Connecting you to a live agent... Please hold on. In the meantime, you can reach us at support@stepup.in or Call/WhatsApp: +91 98765 43210.";
            suggestionList.add("Contact Support");
        }

        // 6. Fallback to Rules or Default
        if (botMessage == null) {
            if (matchedRule != null) {
                botMessage = matchedRule.getResponse();
                // Special handling for latest order if not already covered
                if ((matchedRule.getKeywords().contains("order") || matchedRule.getKeywords().contains("status"))
                        && user != null) {
                    List<com.stepup.model.Order> userOrders = orderService.getOrdersByUser(user);
                    if (!userOrders.isEmpty()) {
                        com.stepup.model.Order lastOrder = userOrders.get(0);
                        botMessage = "Your latest order #" + lastOrder.getId() + " is currently: "
                                + lastOrder.getStatus() + ". Tracking: " + lastOrder.getTrackingNumber();
                        suggestionList.add("Track Order #" + lastOrder.getId());
                    }
                }
                if (matchedRule.getSuggestions() != null && !matchedRule.getSuggestions().isEmpty()) {
                    String[] sugs = matchedRule.getSuggestions().split(",");
                    for (String s : sugs)
                        suggestionList.add(s.trim());
                }
            } else {
                botMessage = "I'm not sure I understand. I can help you track orders, browse shoes under ₹2000, or connect you with an agent. Try asking 'Shoes under 1500'!";
                suggestionList.add("Track Order");
                suggestionList.add("Shoes under 2000");
                suggestionList.add("Talk to Agent");
            }
        }

        responseMap.put("message", botMessage);
        responseMap.put("suggestions", suggestionList);
        responseMap.put("timestamp", System.currentTimeMillis());

        // Record history for authenticated users
        if (user != null) {
            List<ChatMessage> chatHistory = (List<ChatMessage>) session.getAttribute("chatHistory");
            if (chatHistory == null) {
                chatHistory = new ArrayList<>();
            }
            chatHistory.add(new ChatMessage(user, "User", payload.get("message")));
            chatHistory.add(new ChatMessage(user, "Bot", botMessage));
            session.setAttribute("chatHistory", chatHistory);
        }

        return ResponseEntity.ok(responseMap);
    }

    private Long extractOrderId(String msg) {
        Pattern pattern = Pattern.compile("(?:order|id|#)\\s*#?(\\d+)");
        Matcher matcher = pattern.matcher(msg);
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private BigDecimal extractPriceLimit(String msg) {
        Pattern pattern = Pattern.compile("(?:under|below|less than|within|₹)\\s*(\\d+)");
        Matcher matcher = pattern.matcher(msg);
        if (matcher.find()) {
            try {
                return new BigDecimal(matcher.group(1));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private String extractSize(String msg) {
        Pattern pattern = Pattern.compile("(?:size|fit)\\s*(\\d+)");
        Matcher matcher = pattern.matcher(msg);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    // Helper method for Fuzzy Matching (Typo tolerance)
    private int calculateLevenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] 
                     + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1), 
                     Math.min(dp[i - 1][j] + 1, 
                     dp[i][j - 1] + 1));
                }
            }
        }
        return dp[a.length()][b.length()];
    }
}
