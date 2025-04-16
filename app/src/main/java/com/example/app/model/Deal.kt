package com.example.app.model

import com.google.gson.annotations.SerializedName



data class Deal(

    @SerializedName("ID")
    val id: String,

    @SerializedName("TITLE")
    val title: String,

    @SerializedName("TYPE_ID")
    val typeId: String,

    @SerializedName("STAGE_ID")
    val stageId: String,

    @SerializedName("PROBABILITY")
    val probability: Double?,

    @SerializedName("CURRENCY_ID")
    val currencyId: String,

    @SerializedName("OPPORTUNITY")
    val opportunity: String,

    @SerializedName("IS_MANUAL_OPPORTUNITY")
    val isManualOpportunity: String,

    @SerializedName("TAX_VALUE")
    val taxValue: Double?,

    @SerializedName("LEAD_ID")
    val leadId: String?,

    @SerializedName("COMPANY_ID")
    val companyId: String,

    @SerializedName("CONTACT_ID")
    val contactId: String?,

    @SerializedName("QUOTE_ID")
    val quoteId: String?,

    @SerializedName("BEGINDATE")
    val beginDate: String,

    @SerializedName("CLOSEDATE")
    val closeDate: String,

    @SerializedName("ASSIGNED_BY_ID")
    val assignedById: String,

    @SerializedName("CREATED_BY_ID")
    val createdById: String,

    @SerializedName("MODIFY_BY_ID")
    val modifyById: String,

    @SerializedName("DATE_CREATE")
    val dateCreate: String,

    @SerializedName("DATE_MODIFY")
    val dateModify: String,

    @SerializedName("OPENED")
    val opened: String,

    @SerializedName("CLOSED")
    val closed: String,

    @SerializedName("COMMENTS")
    val comments: String?,

    @SerializedName("ADDITIONAL_INFO")
    val additionalInfo: String?,

    @SerializedName("LOCATION_ID")
    val locationId: String?,

    @SerializedName("CATEGORY_ID")
    val categoryId: String,

    @SerializedName("STAGE_SEMANTIC_ID")
    val stageSemanticId: String,

    @SerializedName("IS_NEW")
    val isNew: String,

    @SerializedName("IS_RECURRING")
    val isRecurring: String,

    @SerializedName("IS_RETURN_CUSTOMER")
    val isReturnCustomer: String,

    @SerializedName("IS_REPEATED_APPROACH")
    val isRepeatedApproach: String,

    @SerializedName("SOURCE_ID")
    val sourceId: String,

    @SerializedName("SOURCE_DESCRIPTION")
    val sourceDescription: String?,

    @SerializedName("ORIGINATOR_ID")
    val originatorId: String?,

    @SerializedName("ORIGIN_ID")
    val originId: String?,

    @SerializedName("MOVED_BY_ID")
    val movedById: String,

    @SerializedName("MOVED_TIME")
    val movedTime: String,

    @SerializedName("LAST_ACTIVITY_TIME")
    val lastActivityTime: String?,

    @SerializedName("UTM_SOURCE")
    val utmSource: String?,

    @SerializedName("UTM_MEDIUM")
    val utmMedium: String?,

    @SerializedName("UTM_CAMPAIGN")
    val utmCampaign: String?,

    @SerializedName("UTM_CONTENT")
    val utmContent: String?,

    @SerializedName("UTM_TERM")
    val utmTerm: String?,

    @SerializedName("PARENT_ID_1036")
    val parentId1036: String?,

    @SerializedName("PARENT_ID_1058")
    val parentId1058: String?,

    @SerializedName("PARENT_ID_1074")
    val parentId1074: String?,

    @SerializedName("PARENT_ID_1102")
    val parentId1102: String?,

    @SerializedName("LAST_ACTIVITY_BY")
    val lastActivityBy: String?
)