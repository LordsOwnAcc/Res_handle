package com.resumeintel.service;

import com.resumeintel.dto.ParsedResumeDraft;
import com.resumeintel.dto.ResumeDtos.ProjectInput;
import com.resumeintel.dto.ResumeDtos.SkillInput;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns an uploaded PDF/DOCX/TXT into a draft the user confirms. Heuristic, not AI: it finds
 * contact details with regexes, skills from a dictionary, and splits sections by headings.
 * Resume text is processed in memory only — never stored or logged.
 */
@Service
public class ResumeFileParser {

    private static final Map<String, String> SKILLS = new LinkedHashMap<>();
    static {
        for (String s : List.of("Java","Kotlin","Python","C++","C#","JavaScript","TypeScript","Go","Rust","Swift","Dart","PHP","Ruby","Scala","SQL","R"))
            SKILLS.put(s, "language");
        for (String s : List.of("Spring Boot","Spring","Hibernate","JPA","Django","Flask","FastAPI","React","Angular","Vue","Node.js","Express","Jetpack Compose","Android","Flutter","Next.js","Tailwind","Retrofit","Room"))
            SKILLS.put(s, "framework");
        for (String s : List.of("PostgreSQL","MySQL","MongoDB","Redis","SQLite","Oracle","Firebase","Elasticsearch"))
            SKILLS.put(s, "database");
        for (String s : List.of("AWS","GCP","Azure","Render","Vercel","Heroku"))
            SKILLS.put(s, "cloud");
        for (String s : List.of("Docker","Kubernetes","Jenkins","GitHub Actions","CI/CD","Terraform","Nginx","Linux"))
            SKILLS.put(s, "devops");
        for (String s : List.of("TensorFlow","PyTorch","scikit-learn","Machine Learning","Deep Learning","NLP","OpenCV","Pandas","NumPy","RAG","LLM"))
            SKILLS.put(s, "ml");
        for (String s : List.of("Git","REST API","GraphQL","Kafka","RabbitMQ","Maven","Gradle","JUnit","Postman","Microservices","Ktor","Coroutines"))
            SKILLS.put(s, "tool");
    }

    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern PHONE = Pattern.compile("(\\+?\\d{1,3}[\\s-]?)?(\\d[\\s-]?){9,11}\\d");
    private static final Pattern GITHUB = Pattern.compile("(?i)(https?://)?(www\\.)?github\\.com/[A-Za-z0-9-_.]+");
    private static final Pattern LINKEDIN = Pattern.compile("(?i)(https?://)?(www\\.)?linkedin\\.com/in/[A-Za-z0-9-_%]+");
    private static final Pattern SECTION = Pattern.compile(
        "(?i)^\\s*(education|projects?|personal projects?|academic projects?|experience|work experience|internships?|" +
        "skills|technical skills|certifications?|achievements?|awards|summary|objective|profile|positions? of responsibility|extracurricular.*)\\s*:?\\s*$");
    private static final Pattern DATE_RANGE = Pattern.compile(
        "(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\\.?\\s*'?(\\d{2,4})\\s*(?:-|–|—|to)\\s*" +
        "(?:(present|current|now)|(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\\.?\\s*'?(\\d{2,4}))");

    public String extractText(MultipartFile file) throws IOException {
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();
        byte[] bytes = file.getBytes();
        if (name.endsWith(".pdf")) {
            try (PDDocument doc = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setSortByPosition(true);
                return stripper.getText(doc);
            }
        }
        if (name.endsWith(".docx")) {
            try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(bytes));
                 XWPFWordExtractor ex = new XWPFWordExtractor(doc)) {
                return ex.getText();
            }
        }
        if (name.endsWith(".txt")) return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        throw new IllegalArgumentException("Unsupported file type. Upload a .pdf, .docx or .txt (old .doc isn't supported).");
    }

    public ParsedResumeDraft parse(String text) {
        List<String> warnings = new ArrayList<>();
        String[] lines = text.replace("\r", "").split("\n");
        List<String> nonEmpty = Arrays.stream(lines).map(String::trim).filter(l -> !l.isEmpty()).toList();

        if (nonEmpty.size() < 5) {
            warnings.add("Very little text was found. If this is a scanned/image PDF it can't be read — fill the form manually.");
        }

        String name = nonEmpty.stream().filter(l -> l.length() <= 50 && !l.contains("@") && !l.matches(".*\\d{5,}.*"))
            .findFirst().orElse("");
        String email = find(EMAIL, text);
        String phone = find(PHONE, text);
        String github = find(GITHUB, text);
        String linkedin = find(LINKEDIN, text);

        Map<String, List<String>> sections = splitSections(lines);

        // Skills: dictionary match over the whole text (word-boundary aware).
        List<SkillInput> skills = new ArrayList<>();
        for (var e : SKILLS.entrySet()) if (containsSkill(text, e.getKey())) skills.add(new SkillInput(e.getKey(), e.getValue()));

        List<ProjectInput> projects = parseProjects(sections.getOrDefault("projects", List.of()));
        if (projects.isEmpty()) warnings.add("No Projects section detected — add projects manually; they drive the project-relevance score.");

        int months = estimateMonths(String.join("\n", sections.getOrDefault("experience", List.of())));

        String edu = "NONE";
        String lower = text.toLowerCase();
        if (lower.matches("(?s).*\\b(ph\\.?d|doctorate)\\b.*")) edu = "PHD";
        else if (lower.matches("(?s).*\\b(m\\.?tech|m\\.?sc|m\\.?e\\b|master|mba|mca)\\b.*")) edu = "MASTERS";
        else if (lower.matches("(?s).*\\b(b\\.?tech|b\\.?e\\b|b\\.?sc|bca|bachelor)\\b.*")) edu = "BACHELORS";

        String summary = String.join(" ", sections.getOrDefault("summary", List.of())).trim();
        if (skills.isEmpty()) warnings.add("No known skills were detected — add them manually.");
        warnings.add("Extracted automatically — review everything before saving.");

        return new ParsedResumeDraft(name, email, phone, github, linkedin, summary, List.of(), edu, months, skills, projects, warnings);
    }

    private Map<String, List<String>> splitSections(String[] lines) {
        Map<String, List<String>> out = new HashMap<>();
        String current = "header";
        for (String raw : lines) {
            String line = raw.trim();
            Matcher m = SECTION.matcher(line);
            if (m.matches()) {
                String h = m.group(1).toLowerCase();
                if (h.contains("project")) current = "projects";
                else if (h.contains("experience") || h.contains("internship")) current = "experience";
                else if (h.contains("summary") || h.contains("objective") || h.contains("profile")) current = "summary";
                else current = h;
                continue;
            }
            if (!line.isEmpty()) out.computeIfAbsent(current, k -> new ArrayList<>()).add(line);
        }
        return out;
    }

    /** A project title = a short non-bullet line; following lines (bullets/sentences) are its description. */
    private List<ProjectInput> parseProjects(List<String> lines) {
        List<ProjectInput> projects = new ArrayList<>();
        String title = null;
        StringBuilder desc = new StringBuilder();
        for (String l : lines) {
            boolean bullet = l.matches("^[•\\-*▪●◦·].*");
            boolean titleLike = !bullet && l.length() <= 90 && !l.endsWith(".");
            if (titleLike && (title == null || desc.length() > 0)) {
                if (title != null) projects.add(build(title, desc.toString()));
                title = l.replaceAll("\\s*[|–—-]\\s*.*$", "").trim(); // drop "| Tech, Tech" suffix from the title
                if (title.isEmpty()) title = l;
                desc = new StringBuilder(l.contains("|") ? l.substring(l.indexOf('|') + 1) + " " : "");
            } else if (title != null) {
                desc.append(l.replaceFirst("^[•\\-*▪●◦·]\\s*", "")).append(' ');
            }
        }
        if (title != null) projects.add(build(title, desc.toString()));
        return projects.stream().limit(8).toList();
    }

    private ProjectInput build(String title, String description) {
        List<String> tech = SKILLS.keySet().stream().filter(s -> containsSkill(title + " " + description, s)).toList();
        return new ProjectInput(title, tech, description.trim(), "", "");
    }

    private int estimateMonths(String experienceText) {
        int total = 0;
        Matcher m = DATE_RANGE.matcher(experienceText);
        while (m.find()) {
            int start = monthIndex(m.group(1)) + 12 * year(m.group(2));
            int end;
            if (m.group(3) != null) { LocalDate now = LocalDate.now(); end = now.getMonthValue() - 1 + 12 * now.getYear(); }
            else end = monthIndex(m.group(4)) + 12 * year(m.group(5));
            if (end >= start) total += end - start + 1;
        }
        return total;
    }
    private int year(String y) { int v = Integer.parseInt(y); return v < 100 ? 2000 + v : v; }
    private int monthIndex(String mon) {
        return List.of("jan","feb","mar","apr","may","jun","jul","aug","sep","oct","nov","dec").indexOf(mon.toLowerCase().substring(0, 3));
    }

    private boolean containsSkill(String text, String skill) {
        Pattern p = Pattern.compile("(?i)(?<![A-Za-z0-9+#])" + Pattern.quote(skill) + "(?![A-Za-z0-9+#])");
        return p.matcher(text).find();
    }

    private String find(Pattern p, String text) {
        Matcher m = p.matcher(text);
        return m.find() ? m.group().trim() : "";
    }
}
