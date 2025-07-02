// model/CaseMaster.java
package com.ppi.utility.importer.model;

import jakarta.persistence.*; // Use jakarta.persistence for Spring Boot 3+
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a row in the CASE_MASTER_TBL.
 * This class holds the data extracted from the Excel file and default values
 * before insertion into the database.
 */
@Entity
@Table(name = "CASE_MASTER_TBL")
public class CaseMaster {

    // CASE_ID is populated by a sequence
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "case_id_seq_generator")
    @SequenceGenerator(name = "case_id_seq_generator", sequenceName = "CASE_ID_SEQ", allocationSize = 1)
    @Column(name = "CASE_ID", length = 20)
    private String caseId; // Changed to String as per DB type VARCHAR2(20)

    @Column(name = "CHANNEL_ID", length = 3)
    private String channelId = "10"; // Changed to String as per DB type VARCHAR2(3)

    @Column(name = "USER_ID", nullable = false, length = 50)
    private String userId = "SYS";

    @Column(name = "SUBMITTED_TS") // Maps LocalDateTime to TIMESTAMP(6)
    private LocalDateTime submittedTs;

    @Column(name = "CASE_TYPE", length = 20)
    private String caseType = "QRY";

    @Column(name = "CASE_STATUS_ID")
    private Integer caseStatusId = 8;

    @Column(name = "IS_CURRENT_UK_RESIDENT", length = 1)
    private String isCurrentUkResident = "Y";

    @Column(name = "TITLE_CODE", length = 35)
    private String titleCode = null; // Explicitly null as per requirements

    @Column(name = "FIRST_NAME", length = 35)
    private String firstName;

    @Column(name = "MIDDLE_NAME", length = 35)
    private String middleName = null; // Explicitly null as per requirements

    @Column(name = "LAST_NAME", length = 35)
    private String lastName;

    @Column(name = "DATE_OF_BIRTH") // Maps LocalDate to DATE
    private LocalDate dateOfBirth;

    @Column(name = "POST_CODE", length = 37)
    private String postCode;

    @Column(name = "THIRD_PARTY_REFERENCE_1", length = 50)
    private String thirdPartyReference1;

    @Column(name = "THIRD_PARTY_REFERENCE_2", length = 50)
    private String thirdPartyReference2;

    // Constructors (default and potentially one for convenience)
    public CaseMaster() {
    }

    // Getters and Setters

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getSubmittedTs() {
        return submittedTs;
    }

    public void setSubmittedTs(LocalDateTime submittedTs) {
        this.submittedTs = submittedTs;
    }

    public String getCaseType() {
        return caseType;
    }

    public void setCaseType(String caseType) {
        this.caseType = caseType;
    }

    public Integer getCaseStatusId() {
        return caseStatusId;
    }

    public void setCaseStatusId(Integer caseStatusId) {
        this.caseStatusId = caseStatusId;
    }

    public String getIsCurrentUkResident() {
        return isCurrentUkResident;
    }

    public void setIsCurrentUkResident(String isCurrentUkResident) {
        this.isCurrentUkResident = isCurrentUkResident;
    }

    public String getTitleCode() {
        return titleCode;
    }

    public void setTitleCode(String titleCode) {
        this.titleCode = titleCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getThirdPartyReference1() {
        return thirdPartyReference1;
    }

    public void setThirdPartyReference1(String thirdPartyReference1) {
        this.thirdPartyReference1 = thirdPartyReference1;
    }

    public String getThirdPartyReference2() {
        return thirdPartyReference2;
    }

    public void setThirdPartyReference2(String thirdPartyReference2) {
        this.thirdPartyReference2 = thirdPartyReference2;
    }
}
