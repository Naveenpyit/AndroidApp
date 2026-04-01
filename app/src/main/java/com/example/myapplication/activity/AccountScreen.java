package com.example.myapplication.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.myapplication.R;
import com.example.myapplication.utils.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AccountScreen extends AppCompatActivity {

    private TextView tvUserName, tvUserPhone;
    private ImageView ivAvatar, btnEditProfile;
    private TextView btnLogout;
    private BottomNavigationView bottomNav;
    private TokenManager tokenManager;

    // ── Menu row root views ───────────────────────────────────────────────────
    private View rowSetup, rowOrders, rowBuyAgain, rowFavourites, rowReturn;
    private View rowNotifications, rowWallet, rowCoupons;
    private View rowVerification, rowAddress, rowLanguage, rowSettings;
    private View rowShareApp, rowComplaint, rowAbout, rowTerms;

    /*1.add whislist
url : https://www.tomhiddleb2b.com/api/add-wishlist
request
{
    "n_category":"1",
    "n_product":"6",
    "n_pack":"5",
    "n_user":"4"
}
response
{
    "n_status": 1,
    "c_message": "Wishlist Added",
    "n_wishlist_count": "3",
    "n_cart_count": "1",
    "j_data": []
}

2.list whislist
url : https://www.tomhiddleb2b.com/api/list-wishlist
request
{
    "n_user":"10"
}
response
{
    "n_status": 1,
    "c_message": "Wishlist Data Found",
    "j_data": [
        {
            "n_id": "11",
            "n_customer": "10",
            "n_category": "1",
            "n_product": "8",
            "n_status": "1",
            "d_created": "2025-12-11 18:02:06",
            "n_gst": "5.00",
            "c_pack_name": "M-2XL",
            "n_mrp": "2796.00",
            "n_selling_price": "822.00",
            "category_name": "MEN",
            "c_item_code": "A4-BP-0039-M-2XL",
            "c_fabric": "SINGLE JERSEY 190GSM",
            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0039/1.webp",
            "n_gst_value": "41.10"
        },
        {
            "n_id": "12",
            "n_customer": "10",
            "n_category": "1",
            "n_product": "10",
            "n_status": "1",
            "d_created": "2025-12-11 18:02:08",
            "n_gst": "5.00",
            "c_pack_name": "M-2XL",
            "n_mrp": "2796.00",
            "n_selling_price": "822.00",
            "category_name": "MEN",
            "c_item_code": "A4-BP-0044-M-2XL",
            "c_fabric": "SINGLE JERSEY 190GSM",
            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0044/1.webp",
            "n_gst_value": "41.10"
        },
        {
            "n_id": "21",
            "n_customer": "10",
            "n_category": "1",
            "n_product": "259",
            "n_status": "1",
            "d_created": "2025-12-13 12:46:06",
            "n_gst": "5.00",
            "c_pack_name": "S-L",
            "n_mrp": "4497.00",
            "n_selling_price": "649.50",
            "category_name": "MEN",
            "c_item_code": "A3-OS-345-S-L",
            "c_fabric": "SINGLE JERSEY 190GSM",
            "c_image": "https://qritechpark.com/tomhiddle/images/OS-345/1.webp",
            "n_gst_value": "32.48"
        },
        {
            "n_id": "27",
            "n_customer": "10",
            "n_category": "1",
            "n_product": "256",
            "n_status": "1",
            "d_created": "2025-12-13 13:46:52",
            "n_gst": "5.00",
            "c_pack_name": "S-XL",
            "n_mrp": "5996.00",
            "n_selling_price": "866.00",
            "category_name": "MEN",
            "c_item_code": "A4-OS-0363-S-XL",
            "c_fabric": "SINGLE JERSEY 190GSM",
            "c_image": "https://qritechpark.com/tomhiddle/images/OS-0363/1.webp",
            "n_gst_value": "43.30"
        },
        {
            "n_id": "35",
            "n_customer": "10",
            "n_category": "1",
            "n_product": "9",
            "n_status": "1",
            "d_created": "2025-12-17 19:34:39",
            "n_gst": "5.00",
            "c_pack_name": "M-2XL",
            "n_mrp": "2796.00",
            "n_selling_price": "822.00",
            "category_name": "MEN",
            "c_item_code": "A4-BP-0042-M-2XL",
            "c_fabric": "SINGLE JERSEY 190GSM",
            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0042/1.webp",
            "n_gst_value": "41.10"
        },
        {
            "n_id": "39",
            "n_customer": "10",
            "n_category": "1",
            "n_product": "17",
            "n_status": "1",
            "d_created": "2025-12-19 21:22:27",
            "n_gst": "5.00",
            "c_pack_name": "M-2XL",
            "n_mrp": "2796.00",
            "n_selling_price": "822.00",
            "category_name": "MEN",
            "c_item_code": "A4-BP-0082-M-2XL",
            "c_fabric": "SINGLE JERSEY 190GSM",
            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0082/1.webp",
            "n_gst_value": "41.10"
        },
        {
            "n_id": "40",
            "n_customer": "10",
            "n_category": "1",
            "n_product": "18",
            "n_status": "1",
            "d_created": "2025-12-19 21:22:29",
            "n_gst": "5.00",
            "c_pack_name": "M-2XL",
            "n_mrp": "2796.00",
            "n_selling_price": "822.00",
            "category_name": "MEN",
            "c_item_code": "A4-BP-0083-M-2XL",
            "c_fabric": "SINGLE JERSEY 190GSM",
            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0083/1.webp",
            "n_gst_value": "41.10"
        },
        {
            "n_id": "43",
            "n_customer": "10",
            "n_category": "1",
            "n_product": "6",
            "n_status": "1",
            "d_created": "2025-12-19 21:28:03",
            "n_gst": "5.00",
            "c_pack_name": "M-2XL",
            "n_mrp": "2796.00",
            "n_selling_price": "822.00",
            "category_name": "MEN",
            "c_item_code": "A4-BP-0036-M-2XL",
            "c_fabric": "SINGLE JERSEY 190GSM",
            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0036/1.webp",
            "n_gst_value": "41.10"
        },
        {
            "n_id": "51",
            "n_customer": "10",
            "n_category": "1",
            "n_product": "248",
            "n_status": "1",
            "d_created": "2025-12-20 19:45:02",
            "n_gst": "5.00",
            "c_pack_name": "S-XL",
            "n_mrp": "5996.00",
            "n_selling_price": "866.00",
            "category_name": "MEN",
            "c_item_code": "A4-OS-0382-S-XL",
            "c_fabric": "SINGLE JERSEY 190GSM",
            "c_image": "https://qritechpark.com/tomhiddle/images/OS-0382/1.webp",
            "n_gst_value": "43.30"
        },
        {
            "n_id": "52",
            "n_customer": "10",
            "n_category": "1",
            "n_product": "247",
            "n_status": "1",
            "d_created": "2025-12-20 19:45:23",
            "n_gst": "5.00",
            "c_pack_name": "S-XL",
            "n_mrp": "5996.00",
            "n_selling_price": "866.00",
            "category_name": "MEN",
            "c_item_code": "A4-OS-0383-S-XL",
            "c_fabric": "SINGLE JERSEY 190GSM",
            "c_image": "https://qritechpark.com/tomhiddle/images/OS-0383/1.webp",
            "n_gst_value": "43.30"
        },
        {
            "n_id": "53",
            "n_customer": "10",
            "n_category": "1",
            "n_product": "246",
            "n_status": "1",
            "d_created": "2025-12-20 19:47:52",
            "n_gst": "5.00",
            "c_pack_name": "S-XL",
            "n_mrp": "5996.00",
            "n_selling_price": "866.00",
            "category_name": "MEN",
            "c_item_code": "A4-OS-0386-S-XL",
            "c_fabric": "SINGLE JERSEY 190GSM",
            "c_image": "https://qritechpark.com/tomhiddle/images/OS-0386/1.webp",
            "n_gst_value": "43.30"
        },
        {
            "n_id": "55",
            "n_customer": "10",
            "n_category": "1",
            "n_product": "255",
            "n_status": "1",
            "d_created": "2025-12-20 19:54:52",
            "n_gst": "5.00",
            "c_pack_name": "S-L",
            "n_mrp": "4497.00",
            "n_selling_price": "649.50",
            "category_name": "MEN",
            "c_item_code": "A3-OS-0364-S-L",
            "c_fabric": "SINGLE JERSEY 190GSM",
            "c_image": "https://qritechpark.com/tomhiddle/images/OS-0364/1.webp",
            "n_gst_value": "32.48"
        },
        {
            "n_id": "56",
            "n_customer": "10",
            "n_category": "1",
            "n_product": "254",
            "n_status": "1",
            "d_created": "2025-12-20 19:55:14",
            "n_gst": "5.00",
            "c_pack_name": "S-XL",
            "n_mrp": "5996.00",
            "n_selling_price": "866.00",
            "category_name": "MEN",
            "c_item_code": "A4-OS-0365-S-XL",
            "c_fabric": "SINGLE JERSEY 190GSM",
            "c_image": "https://qritechpark.com/tomhiddle/images/OS-0365/1.webp",
            "n_gst_value": "43.30"
        },
        {
            "n_id": "64",
            "n_customer": "10",
            "n_category": "1",
            "n_product": "2",
            "n_status": "1",
            "d_created": "2025-12-24 21:08:59",
            "n_gst": "5.00",
            "c_pack_name": "M-2XL",
            "n_mrp": "2796.00",
            "n_selling_price": "822.00",
            "category_name": "MEN",
            "c_item_code": "A4-BP-0020-M-2XL",
            "c_fabric": "SINGLE JERSEY 190GSM",
            "c_image": "https://qritechpark.com/tomhiddle/images/M-2XL/1.webp",
            "n_gst_value": "41.10"
        },
        {
            "n_id": "66",
            "n_customer": "10",
            "n_category": "1",
            "n_product": "56",
            "n_status": "1",
            "d_created": "2026-01-11 04:09:59",
            "n_gst": "5.00",
            "c_pack_name": "M-2XL",
            "n_mrp": "2796.00",
            "n_selling_price": "822.00",
            "category_name": "MEN",
            "c_item_code": "A4-BP-0140-M-2XL",
            "c_fabric": "SINGLE JERSEY 190GSM",
            "c_image": "https://qritechpark.com/tomhiddle/images/M-2XL/1.webp",
            "n_gst_value": "41.10"
        },
        {
            "n_id": "67",
            "n_customer": "10",
            "n_category": "1",
            "n_product": "57",
            "n_status": "1",
            "d_created": "2026-01-11 04:10:01",
            "n_gst": "5.00",
            "c_pack_name": "M-2XL",
            "n_mrp": "2796.00",
            "n_selling_price": "822.00",
            "category_name": "MEN",
            "c_item_code": "A4-BP-0141-M-2XL",
            "c_fabric": "SINGLE JERSEY 190GSM",
            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0141/1.webp",
            "n_gst_value": "41.10"
        },
        {
            "n_id": "82",
            "n_customer": "10",
            "n_category": "1",
            "n_product": "7",
            "n_status": "1",
            "d_created": "2026-03-06 18:37:59",
            "n_gst": "5.00",
            "c_pack_name": "M-2XL",
            "n_mrp": "2796.00",
            "n_selling_price": "822.00",
            "category_name": "MEN",
            "c_item_code": "A4-BP-0038-M-2XL",
            "c_fabric": "SINGLE JERSEY 190GSM",
            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0038/1.webp",
            "n_gst_value": "41.10"
        },
        {
            "n_id": "86",
            "n_customer": "10",
            "n_category": "1",
            "n_product": "4",
            "n_status": "1",
            "d_created": "2026-03-06 18:52:28",
            "n_gst": "5.00",
            "c_pack_name": "M-2XL",
            "n_mrp": "2796.00",
            "n_selling_price": "822.00",
            "category_name": "MEN",
            "c_item_code": "A4-BP-0028-M-2XL",
            "c_fabric": "SINGLE JERSEY 190GSM",
            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0028/1.webp",
            "n_gst_value": "41.10"
        },
        {
            "n_id": "88",
            "n_customer": "10",
            "n_category": "1",
            "n_product": "11",
            "n_status": "1",
            "d_created": "2026-03-06 18:54:42",
            "n_gst": "5.00",
            "c_pack_name": "M-2XL",
            "n_mrp": "2796.00",
            "n_selling_price": "822.00",
            "category_name": "MEN",
            "c_item_code": "A4-BP-0052-M-2XL",
            "c_fabric": "SINGLE JERSEY 190GSM",
            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0052/1.webp",
            "n_gst_value": "41.10"
        },
        {
            "n_id": "89",
            "n_customer": "10",
            "n_category": "1",
            "n_product": "12",
            "n_status": "1",
            "d_created": "2026-03-06 18:55:10",
            "n_gst": "5.00",
            "c_pack_name": "M-2XL",
            "n_mrp": "2796.00",
            "n_selling_price": "822.00",
            "category_name": "MEN",
            "c_item_code": "A4-BP-0055-M-2XL",
            "c_fabric": "SINGLE JERSEY 190GSM",
            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0055/1.webp",
            "n_gst_value": "41.10"
        },
        {
            "n_id": "90",
            "n_customer": "10",
            "n_category": "1",
            "n_product": "13",
            "n_status": "1",
            "d_created": "2026-03-06 18:55:32",
            "n_gst": "5.00",
            "c_pack_name": "M-2XL",
            "n_mrp": "2796.00",
            "n_selling_price": "822.00",
            "category_name": "MEN",
            "c_item_code": "A4-BP-0056-M-2XL",
            "c_fabric": "SINGLE JERSEY 190GSM",
            "c_image": "https://qritechpark.com/tomhiddle/images/BP-0056/1.webp",
            "n_gst_value": "41.10"
        }
    ]
}

3.delete wishlist
url : https://www.tomhiddleb2b.com/api/delete-wishlist
request
{
    "n_user":"4",
    "n_wishlist":"7"
}

====================================================


eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0b2tlbiI6IkJaR244dmNkTzFjMlRqNXJJbHBXOEdzZXRRRDFFbXNrIiwidGltZSI6MTc3NDc1NDUxMX0.RzuVWTzdSIXCj6w9qPJp8Txp1C5URGBXgPsGgZut6IM

1.reg list api
url : https://www.tomhiddleb2b.com/api/register-details
request
{"n_mobile":"8608079111"}
resposne
{
    "n_status": 1,
    "c_message": "User Registered details fetched successfully",
    "n_step": "3",
    "j_data": {
        "owner_details": {
            "c_name": "INDIRA",
            "n_mobile": "8608079111",
            "c_email": "devicenl2025@gmail.com"
        },
        "business_details": {
            "n_type": "3",
            "n_verify_type": "1",
            "c_pan": "AFZPK7190K",
            "c_gst": "",
            "c_image": "https://www.tomhiddleb2b.com/public/uploads/customer/"
        },
        "address_details": {
            "n_address_type": "2",
            "n_pincode": "600042",
            "c_address": "14, 2nd Main Rd, Venkateswara Nagar, Velachery, Chennai, Tamil Nadu 600042, India",
            "n_state": "6",
            "n_city": "36",
            "c_longitude": "80.2166202",
            "c_latitude": "12.9811756"
        },
        "is_informations": {
            "n_owner": 1,
            "n_business": 1,
            "n_address": 1
        }
    }
}

2.reg create api
url : https://www.tomhiddleb2b.com/api/register-insert
n_step
c_pincode
c_address
n_state
n_city
c_longitude
c_latitude
n_address_type
c_name
n_mobile
c_email
n_type
n_verify_type
c_pan
c_gst
c_image

sample request
{"c_name":"sridfharan","n_step":"1","n_mobile":"8608079669","c_email": "nareshkumar@gmail.com"}
response
{
    "n_status": 1,
    "c_message": "Saved successfully.",
    "j_data": [
        {
            "n_screen_type": 3
        }
    ]
}

3.state list api
url : https://www.tomhiddleb2b.com/api/state-list (GET)

4.state wise city
url : https://www.tomhiddleb2b.com/api/city-list
{
    "n_state":"1"
}
repseonse
{
    "n_status": 1,
    "c_message": "City list fetched successfully",
    "j_data": [
        {
            "n_id": "334",
            "c_city_name": "Port Blair"
        }
    ]
}

*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        setupStatusBar();
        tokenManager = new TokenManager(this);
        initViews();
        bindUserInfo();
        setupMenuRows();
        setupBottomNav();
    }

    // ── Init ─────────────────────────────────────────────────────────────────

    private void initViews() {
        tvUserName    = findViewById(R.id.tv_user_name);
        tvUserPhone   = findViewById(R.id.tv_user_phone);
        ivAvatar      = findViewById(R.id.iv_avatar);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnLogout     = findViewById(R.id.btn_logout);
        bottomNav     = findViewById(R.id.bottom_nav);

        // Menu rows
        rowSetup         = findViewById(R.id.row_setup);
        rowOrders        = findViewById(R.id.row_orders);
        rowBuyAgain      = findViewById(R.id.row_buy_again);
        rowFavourites    = findViewById(R.id.row_favourites);
        rowReturn        = findViewById(R.id.row_return);
        rowNotifications = findViewById(R.id.row_notifications);
        rowWallet        = findViewById(R.id.row_wallet);
        rowCoupons       = findViewById(R.id.row_coupons);
        rowVerification  = findViewById(R.id.row_verification);
        rowAddress       = findViewById(R.id.row_address);
        rowLanguage      = findViewById(R.id.row_language);
        rowSettings      = findViewById(R.id.row_settings);
        rowShareApp      = findViewById(R.id.row_share_app);
        rowComplaint     = findViewById(R.id.row_complaint);
        rowAbout         = findViewById(R.id.row_about);
        rowTerms         = findViewById(R.id.row_terms);
    }

    // ── Bind user info from TokenManager ─────────────────────────────────────

    private void bindUserInfo() {
//        String name  = tokenManager.getUserName();
//        String phone = tokenManager.getUserPhone();
                String name  = "Nivetha";
        String phone = "4867984699";

        tvUserName.setText((name  != null && !name.isEmpty())  ? name  : "User");
        tvUserPhone.setText((phone != null && !phone.isEmpty()) ? phone : "");

        btnEditProfile.setOnClickListener(v ->
                Toast.makeText(this, "Edit Profile", Toast.LENGTH_SHORT).show());
    }

    // ── Configure each menu row: icon, title, subtitle, click ────────────────

    private void setupMenuRows() {
        configRow(rowSetup,         R.drawable.ic_person,          "Setup your account",
                "0 items pending orders", true,
                v -> Toast.makeText(this, "Setup Account", Toast.LENGTH_SHORT).show());

        configRow(rowOrders,        R.drawable.ic_orders,          "Orders",         null, false,
                v -> startActivity(new Intent(this, ProductListingScreen.class)));

        configRow(rowBuyAgain,      R.drawable.ic_replay,          "Buy Again",      null, false,
                v -> Toast.makeText(this, "Buy Again", Toast.LENGTH_SHORT).show());

        configRow(rowFavourites,    R.drawable.ic_favorite_border, "Favourites",     null, false,
                v -> startActivity(new Intent(this, WishlistScreen.class)));

        configRow(rowReturn,        R.drawable.ic_return,          "Return",         null, false,
                v -> Toast.makeText(this, "Return", Toast.LENGTH_SHORT).show());

        configRow(rowNotifications, R.drawable.notification,       "Notifications",  null, false,
                v -> Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show());

        configRow(rowWallet,        R.drawable.ic_wallet,          "Wallet",         null, false,
                v -> Toast.makeText(this, "Wallet", Toast.LENGTH_SHORT).show());

        configRow(rowCoupons,       R.drawable.ic_coupon,          "Coupons",        null, false,
                v -> Toast.makeText(this, "Coupons", Toast.LENGTH_SHORT).show());

        configRow(rowVerification,  R.drawable.ic_verified,        "Verification Details", null, false,
                v -> Toast.makeText(this, "Verification Details", Toast.LENGTH_SHORT).show());

        configRow(rowAddress,       R.drawable.ic_location,        "Address",        null, false,
                v -> Toast.makeText(this, "Address", Toast.LENGTH_SHORT).show());

        configRow(rowLanguage,      R.drawable.ic_language,        "Language",       null, false,
                v -> Toast.makeText(this, "Language", Toast.LENGTH_SHORT).show());

        configRow(rowSettings,      R.drawable.ic_settings,        "Settings",       null, false,
                v -> Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show());

        configRow(rowShareApp,      R.drawable.ic_share,           "Share App with Your Friends", null, false,
                v -> shareApp());

        configRow(rowComplaint,     R.drawable.ic_complaint,       "Raise a complaint", null, false,
                v -> Toast.makeText(this, "Raise a complaint", Toast.LENGTH_SHORT).show());

        configRow(rowAbout,         R.drawable.ic_info,            "About Us",       null, false,
                v -> Toast.makeText(this, "About Us", Toast.LENGTH_SHORT).show());

        configRow(rowTerms,         R.drawable.ic_document,        "Terms & Conditions", null, false,
                v -> Toast.makeText(this, "Terms & Conditions", Toast.LENGTH_SHORT).show());

        // Logout
        btnLogout.setOnClickListener(v -> logout());
    }

    /**
     * Helper to configure an included menu row.
     *
     * @param row         The root View of the included item_account_menu_row layout
     * @param iconRes     Drawable resource for the left icon
     * @param title       Primary label text
     * @param subtitle    Secondary label (null = hide)
     * @param showSubtitle Whether to show the subtitle TextView
     * @param clickListener Click action for the whole row
     */
    private void configRow(View row, int iconRes, String title,
                           String subtitle, boolean showSubtitle,
                           View.OnClickListener clickListener) {
        if (row == null) return;

        ImageView icon     = row.findViewById(R.id.iv_menu_icon);
        TextView  tvTitle  = row.findViewById(R.id.tv_menu_title);
        TextView  tvSub    = row.findViewById(R.id.tv_menu_subtitle);

        icon.setImageResource(iconRes);
        tvTitle.setText(title);

        if (showSubtitle && subtitle != null && !subtitle.isEmpty()) {
            tvSub.setText(subtitle);
            tvSub.setVisibility(View.VISIBLE);
        } else {
            tvSub.setVisibility(View.GONE);
        }

        row.setOnClickListener(clickListener);
    }

    // ── Share App ─────────────────────────────────────────────────────────────

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "🛍️ Shop wholesale fashion at amazing prices!\n\nDownload our app now.");
        startActivity(Intent.createChooser(shareIntent, "Share app via"));
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    private void logout() {
        tokenManager.clear();
        Intent intent = new Intent(this, LoginPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ── Bottom Nav ────────────────────────────────────────────────────────────

    private void setupBottomNav() {
        // Mark Account tab as selected
        bottomNav.setSelectedItemId(R.id.nav_account);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, ProductListingScreen.class));
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartScreen.class));
                return true;
            } else if (id == R.id.nav_account) {
                return true; // already here
            }
            return false;
        });
    }

    // ── Status Bar ────────────────────────────────────────────────────────────

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.red_primary, getTheme()));
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                    .setAppearanceLightStatusBars(false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.red_primary));
        }
    }
}