package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class RegisterDetailsResponse {

    @SerializedName("n_status")  private int    nStatus;
    @SerializedName("c_message") private String cMessage;
    @SerializedName("n_step")    private String nStepStr; // API String return பண்றது
    @SerializedName("j_data")    private JData  jData;

    public int    getNStatus()  { return nStatus; }
    public String getCMessage() { return cMessage; }
    public JData  getJData()    { return jData; }

    // ✅ String → int safe convert
    public int getNStep() {
        try { return Integer.parseInt(nStepStr); }
        catch (Exception e) { return 0; }
    }


    public static class JData {

        @SerializedName("owner_details")    private OwnerDetails    ownerDetails;
        @SerializedName("business_details") private BusinessDetails businessDetails;
        @SerializedName("address_details")  private AddressDetails  addressDetails;
        @SerializedName("is_informations")  private IsInformations  isInformations;

        public OwnerDetails    getOwnerDetails()    { return ownerDetails; }
        public BusinessDetails getBusinessDetails() { return businessDetails; }
        public AddressDetails  getAddressDetails()  { return addressDetails; }
        public IsInformations  getIsInformations()  { return isInformations; }
    }

    // ── OwnerDetails ───────────────────────────────────────────────
    public static class OwnerDetails {
        @SerializedName("c_name")   private String cName;
        @SerializedName("n_mobile") private String nMobile;
        @SerializedName("c_email")  private String cEmail;

        public String getCName()   { return cName; }
        public String getNMobile() { return nMobile; }
        public String getCEmail()  { return cEmail; }
    }


    public static class BusinessDetails {
        @SerializedName("n_type")        private String nType;
        @SerializedName("n_verify_type") private String nVerifyType;
        @SerializedName("c_pan")         private String cPan;
        @SerializedName("c_gst")         private String cGst;
        @SerializedName("c_image")       private String cImage;

        public String getNType()       { return nType; }
        public String getNVerifyType() { return nVerifyType; }
        public String getCPan()        { return cPan; }
        public String getCGst()        { return cGst; }
        public String getCImage()      { return cImage; }
    }


    public static class AddressDetails {
        @SerializedName("n_address_type") private String nAddressType;
        @SerializedName("n_pincode")      private String nPincode;
        @SerializedName("c_address")      private String cAddress;
        @SerializedName("n_state")        private String nState;
        @SerializedName("n_city")         private String nCity;
        @SerializedName("c_longitude")    private String cLongitude;
        @SerializedName("c_latitude")     private String cLatitude;

        public String getNPincode()   { return nPincode; }
        public String getCAddress()   { return cAddress; }
        public String getNState()     { return nState; }
        public String getNCity()      { return nCity; }
        public String getCLongitude() { return cLongitude; }
        public String getCLatitude()  { return cLatitude; }
    }


    public static class IsInformations {
        @SerializedName("n_owner")    private int nOwner;
        @SerializedName("n_business") private int nBusiness;
        @SerializedName("n_address")  private int nAddress;

        public int getNOwner()    { return nOwner; }
        public int getNBusiness() { return nBusiness; }
        public int getNAddress()  { return nAddress; }
    }
}