package ui.gui.services;

import domain.Book;
import domain.SearchResult;
import features.search.QueryProcessor;
import features.search.ReRanker;
import features.search.Suggester;
import utils.LoggingService; // (Moved to utils? Check your imports)

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DevShelfService {

    private final Map<Integer, Book> bookMap;
    private final QueryProcessor queryProcessor;
    private final ReRanker reRanker;
    private final Suggester suggester;
    private final LoggingService loggingService;

    public DevShelfService(Map<Integer, Book> bookMap, QueryProcessor queryProcessor,
                           ReRanker reRanker, Suggester suggester, LoggingService loggingService) {
        this.bookMap = bookMap;
        this.queryProcessor = queryProcessor;
        this.reRanker = reRanker;
        this.suggester = suggester;
        this.loggingService = loggingService;
    }

    public SearchResponse search(String query) {
        System.out.println("🔍 GUI Processing Query: [" + query + "]");

        // 1. Raw Search
        List<SearchResult> results = queryProcessor.search(query);
        String usedQuery = query;
        boolean isSuggestion = false;

        // 2. Handle No Results / Typos
        if (results.isEmpty()) {
            String suggestion = suggester.suggestSimilar(query);
            if (suggestion != null) {
                System.out.println("💡 Suggestion found: " + suggestion);
                results = queryProcessor.search(suggestion);
                usedQuery = suggestion;
                isSuggestion = true;
            }
        }

        // 3. Re-Rank (The "Intelligence" Layer)
        // We explicitly re-rank to ensure popularity is accounted for
        List<SearchResult> rankedResults = reRanker.reRank(results, usedQuery);

        // --- DEBUGGING: Print top 5 results to Console to compare with CLI ---
        System.out.println("📊 Top 5 Results (DocID : Score):");
        for (int i = 0; i < Math.min(5, rankedResults.size()); i++) {
            SearchResult r = rankedResults.get(i);
            System.out.printf("   [%d] DocID: %d | Score: %.4f%n", i+1, r.getDocId(), r.getScore());
        }
        // ---------------------------------------------------------------------

        // 4. Convert to Books (Preserving Order strictly)
        List<Book> books = new ArrayList<>();
        for (SearchResult res : rankedResults) {
            Book b = bookMap.get(res.getDocId());
            if (b != null) {
                books.add(b);
            }
        }

        return new SearchResponse(books, isSuggestion, usedQuery);
    }

    public void logClick(String query, int bookId) {
        System.out.println("🖱️ Click Logged: BookID " + bookId + " for query '" + query + "'");
        loggingService.logClick(query, bookId);
    }

    // Helper class remains the same...
    public static class SearchResponse {
        public final List<Book> books;
        public final boolean isSuggestion;
        public final String successfulQuery;

        public SearchResponse(List<Book> books, boolean isSuggestion, String successfulQuery) {
            this.books = books;
            this.isSuggestion = isSuggestion;
            this.successfulQuery = successfulQuery;
        }
    }
}