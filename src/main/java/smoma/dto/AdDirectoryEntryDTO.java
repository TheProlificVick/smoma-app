package smoma.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive DTO representing a complete entry from the Active Directory.
 * Captures the full set of attributes necessary for fortified role-based access.
 */
public class AdDirectoryEntryDTO {

    private String distinguishedName;
    private String cn;
    private String name;
    private String objectClass;
    private String objectCategory;
    private String sAMAccountName;
    private String userPrincipalName;
    private String givenName;
    private String sn;
    private String displayName;
    private String mail;
    private String title;
    private String department;
    private String company;
    private String office;
    private String telephoneNumber;
    private String mobile;
    private String employeeID;
    private String employeeNumber;
    private String description;
    private String streetAddress;
    private String postalCode;
    private String physicalDeliveryOfficeName;
    private String manager;
    private List<String> memberOf = new ArrayList<>();
    private List<String> directReports = new ArrayList<>();
    private String userAccountControl;
    private String userAccountStatus;
    private boolean accountEnabled;
    private String whenCreated;
    private String whenChanged;
    private String lastLogon;
    private String lastLogoff;
    private String accountExpires;
    private String primaryGroupId;
    private String homeDirectory;
    private String homeDrive;
    private String scriptPath;
    private String profilePath;
    private String logonCount;
    private String badPasswordTime;
    private String badPwdCount;
    private String entryType;

    // Getters and Setters
    public String getDistinguishedName() { return distinguishedName; }
    public void setDistinguishedName(String distinguishedName) { this.distinguishedName = distinguishedName; }

    public String getCn() { return cn; }
    public void setCn(String cn) { this.cn = cn; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getObjectClass() { return objectClass; }
    public void setObjectClass(String objectClass) { this.objectClass = objectClass; }

    public String getObjectCategory() { return objectCategory; }
    public void setObjectCategory(String objectCategory) { this.objectCategory = objectCategory; }

    public String getsAMAccountName() { return sAMAccountName; }
    public void setsAMAccountName(String sAMAccountName) { this.sAMAccountName = sAMAccountName; }

    public String getUserPrincipalName() { return userPrincipalName; }
    public void setUserPrincipalName(String userPrincipalName) { this.userPrincipalName = userPrincipalName; }

    public String getGivenName() { return givenName; }
    public void setGivenName(String givenName) { this.givenName = givenName; }

    public String getSn() { return sn; }
    public void setSn(String sn) { this.sn = sn; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getOffice() { return office; }
    public void setOffice(String office) { this.office = office; }

    public String getTelephoneNumber() { return telephoneNumber; }
    public void setTelephoneNumber(String telephoneNumber) { this.telephoneNumber = telephoneNumber; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getEmployeeID() { return employeeID; }
    public void setEmployeeID(String employeeID) { this.employeeID = employeeID; }

    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getPhysicalDeliveryOfficeName() { return physicalDeliveryOfficeName; }
    public void setPhysicalDeliveryOfficeName(String physicalDeliveryOfficeName) { this.physicalDeliveryOfficeName = physicalDeliveryOfficeName; }

    public String getManager() { return manager; }
    public void setManager(String manager) { this.manager = manager; }

    public List<String> getMemberOf() { return memberOf; }
    public void setMemberOf(List<String> memberOf) { this.memberOf = memberOf; }

    public List<String> getDirectReports() { return directReports; }
    public void setDirectReports(List<String> directReports) { this.directReports = directReports; }

    public String getUserAccountControl() { return userAccountControl; }
    public void setUserAccountControl(String userAccountControl) { this.userAccountControl = userAccountControl; }

    public String getUserAccountStatus() { return userAccountStatus; }
    public void setUserAccountStatus(String userAccountStatus) { this.userAccountStatus = userAccountStatus; }

    public boolean isAccountEnabled() { return accountEnabled; }
    public void setAccountEnabled(boolean accountEnabled) { this.accountEnabled = accountEnabled; }

    public String getWhenCreated() { return whenCreated; }
    public void setWhenCreated(String whenCreated) { this.whenCreated = whenCreated; }

    public String getWhenChanged() { return whenChanged; }
    public void setWhenChanged(String whenChanged) { this.whenChanged = whenChanged; }

    public String getLastLogon() { return lastLogon; }
    public void setLastLogon(String lastLogon) { this.lastLogon = lastLogon; }

    public String getLastLogoff() { return lastLogoff; }
    public void setLastLogoff(String lastLogoff) { this.lastLogoff = lastLogoff; }

    public String getAccountExpires() { return accountExpires; }
    public void setAccountExpires(String accountExpires) { this.accountExpires = accountExpires; }

    public String getPrimaryGroupId() { return primaryGroupId; }
    public void setPrimaryGroupId(String primaryGroupId) { this.primaryGroupId = primaryGroupId; }

    public String getHomeDirectory() { return homeDirectory; }
    public void setHomeDirectory(String homeDirectory) { this.homeDirectory = homeDirectory; }

    public String getHomeDrive() { return homeDrive; }
    public void setHomeDrive(String homeDrive) { this.homeDrive = homeDrive; }

    public String getScriptPath() { return scriptPath; }
    public void setScriptPath(String scriptPath) { this.scriptPath = scriptPath; }

    public String getProfilePath() { return profilePath; }
    public void setProfilePath(String profilePath) { this.profilePath = profilePath; }

    public String getLogonCount() { return logonCount; }
    public void setLogonCount(String logonCount) { this.logonCount = logonCount; }

    public String getBadPasswordTime() { return badPasswordTime; }
    public void setBadPasswordTime(String badPasswordTime) { this.badPasswordTime = badPasswordTime; }

    public String getBadPwdCount() { return badPwdCount; }
    public void setBadPwdCount(String badPwdCount) { this.badPwdCount = badPwdCount; }

    public String getEntryType() { return entryType; }
    public void setEntryType(String entryType) { this.entryType = entryType; }
}