package smoma.controller.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    /** English translation of {@link #name}, shown instead of it when the UI language is English. */
    private String nameEn;

    private String acronym;
    private String headName;

    /** English translation of {@link #headName}, shown instead of it when the UI language is English. */
    private String headNameEn;

    public Department() {
    }

    public Department(String name, String acronym, String headName) {
        this.name = name;
        this.acronym = acronym;
        this.headName = headName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public String getHeadName() {
        return headName;
    }

    public void setHeadName(String headName) {
        this.headName = headName;
    }

    public String getHeadNameEn() {
        return headNameEn;
    }

    public void setHeadNameEn(String headNameEn) {
        this.headNameEn = headNameEn;
    }
}
