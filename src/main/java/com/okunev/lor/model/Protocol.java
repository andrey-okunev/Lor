package com.okunev.lor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Protocol {

    @JsonProperty("num")
    private String num;

    @JsonProperty("name")
    private String name;

    @JsonProperty("population")
    private String population;

    @JsonProperty("section")
    private String section;

    @JsonProperty("diag_required")
    private String diagRequired;

    @JsonProperty("diag_extra")
    private String diagExtra;

    @JsonProperty("treatment")
    private String treatment;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("duration")
    private String duration;

    @JsonProperty("file")
    private String file;

    @JsonProperty("page")
    private Integer page;

    @JsonProperty("level")
    private String level;

    /** Текст, по которому имеет смысл искать — всё, кроме служебных полей. */
    public String searchableText() {
        return String.join(" ",
                safe(name), safe(diagRequired), safe(diagExtra),
                safe(treatment), safe(notes), safe(duration));
    }

    private static String safe(String s) { return s == null ? "" : s; }

    public String getNum() { return num; }
    public String getName() { return name; }
    public String getPopulation() { return population; }
    public String getSection() { return section; }
    public String getDiagRequired() { return diagRequired; }
    public String getDiagExtra() { return diagExtra; }
    public String getTreatment() { return treatment; }
    public String getNotes() { return notes; }
    public String getDuration() { return duration; }
    public String getFile() { return file; }
    public Integer getPage() { return page; }
    public String getLevel() { return level; }

    public void setNum(String num) { this.num = num; }
    public void setName(String name) { this.name = name; }
    public void setPopulation(String population) { this.population = population; }
    public void setSection(String section) { this.section = section; }
    public void setDiagRequired(String diagRequired) { this.diagRequired = diagRequired; }
    public void setDiagExtra(String diagExtra) { this.diagExtra = diagExtra; }
    public void setTreatment(String treatment) { this.treatment = treatment; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setFile(String file) { this.file = file; }
    public void setPage(Integer page) { this.page = page; }
    public void setLevel(String level) { this.level = level; }

    @Override
    public String toString() {
        return "№" + num + " — " + name;
    }
}