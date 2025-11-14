package features.recommendation;

import domain.Book;
import java.util.*;

public class Graph {

    public final Map<String, Map<String, Double>> adjList = new HashMap<>();
    public final Map<String, Integer> titleToId = new HashMap<>();

    // Optional synonym map for languages/tags
    private static final Map<String, String> SYNONYM_MAP = Map.of(
            "js", "javascript",
            "c#", "csharp",
            "cpp", "c++",
            "ai", "artificial intelligence",
            "dsa", "data structure",
            "ml", "machine learning",
            "py", "python"
    );

    // Normalize string: trim, lower-case, and apply synonyms
    private String normalize(String s) {
        if (s == null) return "";
        s = s.trim().toLowerCase();
        return SYNONYM_MAP.getOrDefault(s, s);
    }

    // Normalize tags: split by common separators and apply normalization
    private Set<String> normalizeTags(String[] tags) {
        if (tags == null) return Collections.emptySet();
        Set<String> result = new HashSet<>();
        for (String t : tags) {
            if (t == null) continue;
            String[] parts = t.split("[,;/]");
            for (String p : parts) {
                String clean = normalize(p);
                if (!clean.isBlank()) result.add(clean);
            }
        }
        return result;
    }

    // Build graph using similarity scores
    public void buildGraph(List<Book> books) {
        if (books == null) return;

        List<String> normalizedTitles = new ArrayList<>();
        for (Book b : books) {
            if (b == null || b.getTitle() == null) {
                normalizedTitles.add(null);
            } else {
                String norm = normalize(b.getTitle());
                normalizedTitles.add(norm);
                adjList.putIfAbsent(norm, new HashMap<>());
                titleToId.put(norm, b.getBookId());
            }
        }

        for (int i = 0; i < books.size(); i++) {
            Book b1 = books.get(i);
            String t1 = normalizedTitles.get(i);
            if (t1 == null || t1.isBlank()) continue;

            for (int j = i + 1; j < books.size(); j++) {
                Book b2 = books.get(j);
                String t2 = normalizedTitles.get(j);
                if (t2 == null || t2.isBlank()) continue;

                double score = calculateSimilarityScore(b1, b2);
                if (score <= 0.0) continue;

                // Add edges both ways; keep the highest score
                adjList.get(t1).merge(t2, score, Math::max);
                adjList.get(t2).merge(t1, score, Math::max);
            }
        }
    }

    // Compute similarity score between two books
    public double calculateSimilarityScore(Book b1, Book b2) {
        if (b1 == null || b2 == null) return 0.0;

        double score = 0.0;

        // Author match (strong)
        if (equalsIgnoreCase(b1.getAuthor(), b2.getAuthor())) score += 1.0;

        // Programming language match (high)
        if (equalsIgnoreCase(b1.getProgLang(), b2.getProgLang())) score += 0.9;

        // Tags overlap (moderate)
        Set<String> tags1 = normalizeTags(b1.getTag());
        Set<String> tags2 = normalizeTags(b2.getTag());
        if (!tags1.isEmpty() || !tags2.isEmpty()) {
            Set<String> intersection = new HashSet<>(tags1);
            intersection.retainAll(tags2);
            Set<String> union = new HashSet<>(tags1);
            union.addAll(tags2);
            if (!union.isEmpty()) score += (intersection.size() / (double) union.size()) * 0.5;
        }

        // Category match (weak)
        if (equalsIgnoreCase(b1.getCategory(), b2.getCategory()) && score < 0.9) {
            score += 0.2;
        }

        // Minimum threshold to filter weak links
        return score >= 0.3 ? score : 0.0;
    }

    private boolean equalsIgnoreCase(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }

    // Popularity-aware recommendations
    public List<String> recommendPopularBooks(String bookTitle, int limit, Map<Integer, Double> popularityMap) {
        String key = normalize(bookTitle);
        Map<String, Double> relatedBooks = adjList.getOrDefault(key, Collections.emptyMap());
        List<String> result = new ArrayList<>(relatedBooks.keySet());

        final double ALPHA = 0.7; // 70% relevance, 30% popularity

        result.sort((a, b) -> {
            double relA = relatedBooks.getOrDefault(a, 0.0);
            double relB = relatedBooks.getOrDefault(b, 0.0);

            double popA = popularityMap != null
                    ? popularityMap.getOrDefault(titleToId.getOrDefault(a, -1), 0.0)
                    : 0.0;

            double popB = popularityMap != null
                    ? popularityMap.getOrDefault(titleToId.getOrDefault(b, -1), 0.0)
                    : 0.0;

            double scoreA = ALPHA * relA + (1 - ALPHA) * popA;
            double scoreB = ALPHA * relB + (1 - ALPHA) * popB;

            return Double.compare(scoreB, scoreA);
        });

        return result.subList(0, Math.min(limit, result.size()));
    }
}