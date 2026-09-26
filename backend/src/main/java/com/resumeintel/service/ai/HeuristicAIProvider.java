package com.resumeintel.service.ai;

import com.resumeintel.service.matching.ProjectFact;
import com.resumeintel.service.matching.ResumeProfile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Fully offline provider — the default bean, so the app's core loop never depends on a
 * network call or API key (mirrors the spec's "don't hardcode the AI provider" requirement).
 * A real cloud-backed provider (OpenAiProvider / GeminiProvider) implements the same
 * interface and gets swapped in via a Spring profile/@Primary bean — see README.
 */
@Service
public class HeuristicAIProvider implements AIProvider {

    private static final List<String> KNOWN_SKILLS = List.of(
        "java", "kotlin", "python", "spring boot", "django", "flask", "react", "android",
        "jetpack compose", "sql", "postgresql", "mysql", "mongodb", "aws", "gcp", "azure",
        "docker", "kubernetes", "kafka", "rest api", "graphql", "git", "linux", "ci/cd",
        "tensorflow", "pytorch", "machine learning", "nlp", "typescript", "javascript", "node.js"
    );

    @Override
    public String name() { return "Local (offline heuristic)"; }

    @Override
    public boolean requiresNetwork() { return false; }

    @Override
    public ResumeAnalysis summarizeResume(ResumeProfile resume) {
        List<String> topSkills = resume.skills().stream().limit(6).toList();
        String category = inferCategory(resume.skills());
        String projectMention = resume.projects().isEmpty() ? "" :
            " Strongest evidence comes from " + resume.projects().get(0).name() + ".";
        String summary = category + " resume emphasizing " +
            String.join(", ", topSkills.stream().limit(4).toList()) + "." + projectMention;

        return new ResumeAnalysis(
            summary,
            inferRoles(category),
            inferCategories(category),
            topSkills,
            List.of(), // populated once compared against a specific JD
            resume.atsKeywords().isEmpty() ? resume.skills() : resume.atsKeywords(),
            resume.skills().size() >= 4 ? 0.8f : 0.5f
        );
    }

    @Override
    public ParsedJd parseJobDescription(String rawText) {
        String lower = rawText.toLowerCase();
        List<String> lines = Arrays.stream(rawText.split("\n")).map(String::trim).filter(l -> !l.isBlank()).toList();

        Float years = null;
        Matcher yearsMatcher = Pattern.compile("(\\d+)\\+?\\s*year").matcher(lower);
        if (yearsMatcher.find()) years = Float.parseFloat(yearsMatcher.group(1));

        List<String> foundSkills = KNOWN_SKILLS.stream().filter(lower::contains).toList();

        String requiredBlockSrc = lower;
        Matcher reqMatcher = Pattern.compile("(required|must have)([\\s\\S]{0,300})").matcher(lower);
        String requiredBlock = reqMatcher.find() ? reqMatcher.group() : lower;

        List<String> required = foundSkills.stream().filter(requiredBlock::contains).toList();
        if (required.isEmpty()) {
            int take = Math.max(1, (int) (foundSkills.size() * 0.7));
            required = foundSkills.stream().limit(take).toList();
        }
        List<String> requiredFinal = required;
        List<String> preferred = foundSkills.stream().filter(s -> !requiredFinal.contains(s)).toList();

        String education = lower.contains("master") ? "MASTERS"
            : (lower.contains("bachelor") || lower.contains("b.tech")) ? "BACHELORS" : "NONE";

        String location = firstGroupOrNull(Pattern.compile("(?i)location:\\s*(.+)"), rawText);
        String employmentType = firstMatchOrNull(Pattern.compile("(?i)(full[- ]time|intern(ship)?|contract|part[- ]time)"), rawText);
        String salary = firstGroupOrNull(Pattern.compile("(?i)(salary|ctc)[:\\s]*([\\d,.$\u2013-]+\\s*(lpa|k|per annum)?)"), rawText, 2);

        List<String> responsibilities = lines.stream().filter(l -> l.startsWith("-") || l.startsWith("\u2022")).toList();

        String title = lines.size() > 1 ? lines.get(1) : "Untitled role";
        String company = lines.stream().filter(l -> l.length() < 40 && !l.isBlank() && Character.isUpperCase(l.charAt(0)))
            .findFirst().orElse(null);

        String summary = title + " requiring " +
            (required.isEmpty() ? "unspecified core skills" : String.join(", ", required.stream().limit(4).toList())) + ".";

        return new ParsedJd(company, title, location, employmentType, years, required, preferred, education,
            responsibilities, foundSkills, salary, summary);
    }

    private String inferCategory(List<String> skills) {
        List<String> s = skills.stream().map(String::toLowerCase).toList();
        boolean hasAndroid = s.stream().anyMatch(x -> x.contains("compose") || x.contains("android") || x.contains("kotlin"));
        boolean hasBackend = s.stream().anyMatch(x -> x.contains("spring") || x.contains("java"));
        boolean hasMl = s.stream().anyMatch(x -> x.contains("tensorflow") || x.contains("pytorch") || x.contains("ml"));
        if (hasAndroid && hasBackend) return "Android + Backend";
        if (hasAndroid) return "Android";
        if (hasMl) return "AI/ML";
        if (hasBackend) return "Java Backend";
        return "General SDE";
    }

    private List<String> inferRoles(String category) {
        return switch (category) {
            case "Java Backend" -> List.of("Java Developer", "Backend Developer", "SDE");
            case "Android" -> List.of("Android Developer", "Mobile Engineer");
            case "Android + Backend" -> List.of("Android Developer", "Backend Developer", "Full-stack SDE");
            case "AI/ML" -> List.of("ML Engineer", "AI Engineer", "Data Scientist");
            default -> List.of("Software Development Engineer", "SDE Fresher");
        };
    }

    private List<String> inferCategories(String category) {
        return switch (category) {
            case "Java Backend" -> List.of("Java/Spring Boot", "Backend", "REST API", "General SDE");
            case "Android" -> List.of("Android", "Mobile", "Jetpack Compose");
            case "AI/ML" -> List.of("Machine Learning", "AI Engineer", "Data-heavy roles");
            default -> List.of("General SDE");
        };
    }

    private String firstMatchOrNull(Pattern p, String text) {
        Matcher m = p.matcher(text);
        return m.find() ? m.group() : null;
    }
    private String firstGroupOrNull(Pattern p, String text) { return firstGroupOrNull(p, text, 1); }
    private String firstGroupOrNull(Pattern p, String text, int group) {
        Matcher m = p.matcher(text);
        return m.find() ? m.group(group) : null;
    }
}
