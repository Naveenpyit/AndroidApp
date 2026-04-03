package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

/**
 * Flexible request model for register-insert API.
 * All fields are optional — only populate what is needed for each step.
 *
 * Step 1 (Owner): n_step, c_name, n_mobile, c_email
 * Step 2 (Business): n_step, n_mobile, n_type, n_verify_type, c_pan / c_gst, c_image
 * Step 3 (Address): n_step, n_mobile, n_address_type, n_pincode, c_address,
 *                   n_state, n_city, c_longitude, c_latitude
 */
public class RegisterInsertRequest {

    @SerializedName("n_step")
    private String nStep;

    @SerializedName("n_mobile")
    private String nMobile;

    // ── Owner fields ──────────────────────────────────────────────────────────
    @SerializedName("c_name")
    private String cName;

    @SerializedName("c_email")
    private String cEmail;

    // ── Business fields ───────────────────────────────────────────────────────
    @SerializedName("n_type")
    private String nType;

    @SerializedName("n_verify_type")
    private String nVerifyType;

    @SerializedName("c_pan")
    private String cPan;

    @SerializedName("c_gst")
    private String cGst;

    @SerializedName("c_image")
    private String cImage;

    // ── Address fields ────────────────────────────────────────────────────────
    @SerializedName("n_address_type")
    private String nAddressType;

    @SerializedName("n_pincode")
    private String nPincode;

    @SerializedName("c_pincode")
    private String cPincode;

    @SerializedName("c_address")
    private String cAddress;

    @SerializedName("n_state")
    private String nState;

    @SerializedName("n_city")
    private String nCity;

    @SerializedName("c_longitude")
    private String cLongitude;

    @SerializedName("c_latitude")
    private String cLatitude;

    // ─────────────────────────────────────────────────────────────────────────
    // Static builders for each step
    // ─────────────────────────────────────────────────────────────────────────

    /** Step 1 – Owner details */
    public static RegisterInsertRequest forOwner(String name, String mobile, String email) {
        RegisterInsertRequest r = new RegisterInsertRequest();
        r.nStep   = "1";
        r.cName   = name;
        r.nMobile = mobile;
        r.cEmail  = email;
        return r;
    }

    /** Step 2 – Business details */
    public static RegisterInsertRequest forBusiness(String mobile,
                                                    String businessType,
                                                    String verifyType,
                                                    String pan,
                                                    String gst,
                                                    String imageBase64OrUrl) {
        RegisterInsertRequest r = new RegisterInsertRequest();
        r.nStep       = "2";
        r.nMobile     = mobile;
        r.nType       = businessType;
        r.nVerifyType = verifyType;
        r.cPan        = pan;
        r.cGst        = gst;
        r.cImage      = imageBase64OrUrl;
        return r;
    }

    /** Step 3 – Address details */
    public static RegisterInsertRequest forAddress(String mobile,
                                                   String addressType,
                                                   String pincode,
                                                   String address,
                                                   String stateId,
                                                   String cityId,
                                                   String latitude,
                                                   String longitude) {
        RegisterInsertRequest r = new RegisterInsertRequest();
        r.nStep        = "3";
        r.nMobile      = mobile;
        r.nAddressType = addressType;
        r.nPincode     = pincode;   // n_pincode
        r.cPincode     = pincode;   // c_pincode (both sent - API whichever accepts)
        r.cAddress     = address;
        r.nState       = stateId;
        r.nCity        = cityId;
        r.cLatitude    = latitude;
        r.cLongitude   = longitude;
        return r;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String getNStep()   { return nStep; }
    public String getNMobile() { return nMobile; }
    public String getCName()   { return cName; }
}