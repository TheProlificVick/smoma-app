package smoma.dto;

public class CreateDepartmentRequest {
    private String name;
    private String acronym;
    private String headName;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAcronym() { return acronym; }
    public void setAcronym(String acronym) { this.acronym = acronym; }
    public String getHeadName() { return headName; }
    public void setHeadName(String headName) { this.headName = headName; }
}
